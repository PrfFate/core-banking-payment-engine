-- 1. Sequences
CREATE SEQUENCE seq_core_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- 2. Tables
CREATE TABLE accounts (
    id NUMBER PRIMARY KEY,
    customer_id NUMBER NOT NULL,
    balance NUMBER(19, 4) DEFAULT 0 NOT NULL,
    currency VARCHAR2(3) DEFAULT 'TRY' NOT NULL,
    status VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL
);

CREATE TABLE transactions (
    id NUMBER PRIMARY KEY,
    reference_code VARCHAR2(100) NOT NULL UNIQUE,
    sender_account_id NUMBER NOT NULL,
    receiver_account_id NUMBER NOT NULL,
    amount NUMBER(19, 4) NOT NULL,
    status VARCHAR2(20) DEFAULT 'PENDING' NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE TABLE outbox (
    id NUMBER PRIMARY KEY,
    aggregate_type VARCHAR2(50) NOT NULL,
    aggregate_id VARCHAR2(100) NOT NULL,
    event_type VARCHAR2(50) NOT NULL,
    payload CLOB NOT NULL,
    processed NUMBER(1) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE TABLE security_audit_logs (
    id NUMBER PRIMARY KEY,
    transaction_id NUMBER, -- No FK constraint intentionally
    action VARCHAR2(100) NOT NULL,
    status VARCHAR2(50) NOT NULL,
    error_message VARCHAR2(4000),
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

-- 3. Package Specification
CREATE OR REPLACE PACKAGE PKG_ACCOUNT_CORE AS
    PROCEDURE PROC_TRANSFER_FUNDS (
        p_reference_code IN VARCHAR2,
        p_sender_id IN NUMBER,
        p_receiver_id IN NUMBER,
        p_amount IN NUMBER,
        p_result_code OUT VARCHAR2,
        p_error_message OUT VARCHAR2
    );

    PROCEDURE PROC_PROCESS_PAYMENT_RESULT(
        p_tx_id IN NUMBER,
        p_status IN VARCHAR2,
        p_out_result OUT VARCHAR2
    );

    PROCEDURE PROC_RECONCILE_STUCK_PAYMENTS(
        p_minutes_threshold IN NUMBER,
        p_out_count OUT NUMBER
    );
END PKG_ACCOUNT_CORE;
/

-- 4. Package Body
CREATE OR REPLACE PACKAGE BODY PKG_ACCOUNT_CORE AS

    PROCEDURE LOG_AUDIT(
        p_tx_id IN NUMBER,
        p_action IN VARCHAR2,
        p_status IN VARCHAR2,
        p_error IN VARCHAR2
    ) IS
        PRAGMA AUTONOMOUS_TRANSACTION;
    BEGIN
        INSERT INTO security_audit_logs (id, transaction_id, action, status, error_message)
        VALUES (seq_core_id.NEXTVAL, p_tx_id, p_action, p_status, p_error);
        COMMIT;
    END;

    PROCEDURE PROC_TRANSFER_FUNDS (
        p_reference_code IN VARCHAR2,
        p_sender_id IN NUMBER,
        p_receiver_id IN NUMBER,
        p_amount IN NUMBER,
        p_result_code OUT VARCHAR2,
        p_error_message OUT VARCHAR2
    ) IS
        v_first_id NUMBER;
        v_second_id NUMBER;
        v_sender_status VARCHAR2(20);
        v_receiver_status VARCHAR2(20);
        v_sender_balance NUMBER(19,4);
        v_tx_id NUMBER;
        v_dummy_id NUMBER;
    BEGIN
        IF p_sender_id < p_receiver_id THEN
            v_first_id := p_sender_id;
            v_second_id := p_receiver_id;
        ELSE
            v_first_id := p_receiver_id;
            v_second_id := p_sender_id;
        END IF;

        SELECT id INTO v_dummy_id FROM accounts WHERE id = v_first_id FOR UPDATE;
        SELECT id INTO v_dummy_id FROM accounts WHERE id = v_second_id FOR UPDATE;

        SELECT status, balance INTO v_sender_status, v_sender_balance FROM accounts WHERE id = p_sender_id;
        SELECT status INTO v_receiver_status FROM accounts WHERE id = p_receiver_id;

        IF v_sender_status != 'ACTIVE' OR v_receiver_status != 'ACTIVE' THEN
            p_result_code := 'ERR_INVALID_ACCOUNT';
            p_error_message := 'One or both accounts are not active.';
            LOG_AUDIT(NULL, 'TRANSFER', 'FAILED', p_error_message);
            RETURN;
        END IF;

        IF v_sender_balance < p_amount THEN
            p_result_code := 'ERR_INSUFFICIENT_FUNDS';
            p_error_message := 'Sender does not have enough balance.';
            LOG_AUDIT(NULL, 'TRANSFER', 'FAILED', p_error_message);
            RETURN;
        END IF;

        v_tx_id := seq_core_id.NEXTVAL;
        INSERT INTO transactions (id, reference_code, sender_account_id, receiver_account_id, amount, status)
        VALUES (v_tx_id, p_reference_code, p_sender_id, p_receiver_id, p_amount, 'PENDING');

        UPDATE accounts SET balance = balance - p_amount WHERE id = p_sender_id;

        INSERT INTO outbox (id, aggregate_type, aggregate_id, event_type, payload)
        VALUES (
            seq_core_id.NEXTVAL,
            'TRANSFER',
            TO_CHAR(v_tx_id),
            'PAYMENT_INITIATED',
            '{"transactionId":' || v_tx_id || ',"senderId":' || p_sender_id || ',"receiverId":' || p_receiver_id || ',"amount":' || p_amount || ',"referenceCode":"' || p_reference_code || '"}'
        );

        p_result_code := 'SUCCESS';
        p_error_message := NULL;
        
        LOG_AUDIT(v_tx_id, 'TRANSFER', 'SUCCESS', NULL);
        
    EXCEPTION
        WHEN DUP_VAL_ON_INDEX THEN
            p_result_code := 'ERR_DUPLICATE_REQUEST';
            p_error_message := 'Duplicate idempotency key detected.';
            LOG_AUDIT(NULL, 'TRANSFER', 'FAILED', p_error_message);
        WHEN OTHERS THEN
            p_result_code := 'ERR_SYSTEM_ERROR';
            p_error_message := SQLERRM;
            LOG_AUDIT(NULL, 'TRANSFER', 'FAILED', p_error_message);
            RAISE;
    END PROC_TRANSFER_FUNDS;

    PROCEDURE PROC_PROCESS_PAYMENT_RESULT(
        p_tx_id IN NUMBER,
        p_status IN VARCHAR2,
        p_out_result OUT VARCHAR2
    ) IS
        v_sender_id NUMBER;
        v_receiver_id NUMBER;
        v_amount NUMBER(19,4);
    BEGIN
        -- Lock the transaction record if it's PENDING
        UPDATE transactions 
        SET status = p_status 
        WHERE id = p_tx_id AND status = 'PENDING'
        RETURNING sender_account_id, receiver_account_id, amount 
        INTO v_sender_id, v_receiver_id, v_amount;
        
        IF SQL%ROWCOUNT = 0 THEN
            p_out_result := 'IGNORED_OR_NOT_FOUND';
            RETURN;
        END IF;

        IF p_status = 'SUCCESS' THEN
            -- Add money to receiver
            UPDATE accounts SET balance = balance + v_amount WHERE id = v_receiver_id;
            p_out_result := 'PROCESSED_SUCCESS';
        ELSIF p_status = 'REJECT' OR p_status = 'RECOMPENSATED' THEN
            -- Refund money to sender
            UPDATE accounts SET balance = balance + v_amount WHERE id = v_sender_id;
            p_out_result := 'PROCESSED_REFUNDED';
        END IF;
        
        LOG_AUDIT(p_tx_id, 'PAYMENT_RESULT', p_status, NULL);
    EXCEPTION
        WHEN OTHERS THEN
            p_out_result := 'ERROR';
            LOG_AUDIT(p_tx_id, 'PAYMENT_RESULT', 'FAILED', SQLERRM);
            RAISE;
    END PROC_PROCESS_PAYMENT_RESULT;

    PROCEDURE PROC_RECONCILE_STUCK_PAYMENTS(
        p_minutes_threshold IN NUMBER,
        p_out_count OUT NUMBER
    ) IS
        v_count NUMBER := 0;
        v_dummy VARCHAR2(100);
    BEGIN
        FOR rec IN (
            SELECT id FROM transactions 
            WHERE status = 'PENDING' 
            AND created_at < SYSTIMESTAMP - NUMTODSINTERVAL(p_minutes_threshold, 'MINUTE')
            FOR UPDATE SKIP LOCKED
        ) LOOP
            PROC_PROCESS_PAYMENT_RESULT(rec.id, 'RECOMPENSATED', v_dummy);
            v_count := v_count + 1;
        END LOOP;
        
        p_out_count := v_count;
        LOG_AUDIT(NULL, 'RECONCILIATION', 'SUCCESS', 'Compensated ' || v_count || ' transactions');
    END PROC_RECONCILE_STUCK_PAYMENTS;
END PKG_ACCOUNT_CORE;
/
