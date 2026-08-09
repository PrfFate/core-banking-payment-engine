package com.corebanking.payment.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Map;

@Repository
public class PaymentRepository {

    private final SimpleJdbcCall transferProcedure;
    private final SimpleJdbcCall processResultProcedure;
    private final SimpleJdbcCall reconcileProcedure;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.transferProcedure = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_ACCOUNT_CORE")
                .withProcedureName("PROC_TRANSFER_FUNDS")
                .declareParameters(
                        new SqlParameter("p_reference_code", Types.VARCHAR),
                        new SqlParameter("p_sender_id", Types.NUMERIC),
                        new SqlParameter("p_receiver_id", Types.NUMERIC),
                        new SqlParameter("p_amount", Types.NUMERIC),
                        new SqlOutParameter("p_result_code", Types.VARCHAR),
                        new SqlOutParameter("p_error_message", Types.VARCHAR)
                );
                
        this.processResultProcedure = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_ACCOUNT_CORE")
                .withProcedureName("PROC_PROCESS_PAYMENT_RESULT")
                .declareParameters(
                        new SqlParameter("p_tx_id", Types.NUMERIC),
                        new SqlParameter("p_status", Types.VARCHAR),
                        new SqlOutParameter("p_out_result", Types.VARCHAR)
                );

        this.reconcileProcedure = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_ACCOUNT_CORE")
                .withProcedureName("PROC_RECONCILE_STUCK_PAYMENTS")
                .declareParameters(
                        new SqlParameter("p_minutes_threshold", Types.NUMERIC),
                        new SqlOutParameter("p_out_count", Types.NUMERIC)
                );
    }

    public TransferResult executeTransfer(String referenceCode, Long senderId, Long receiverId, BigDecimal amount) {
        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("p_reference_code", referenceCode)
                .addValue("p_sender_id", senderId)
                .addValue("p_receiver_id", receiverId)
                .addValue("p_amount", amount);

        Map<String, Object> out = transferProcedure.execute(in);
        return new TransferResult((String) out.get("p_result_code"), (String) out.get("p_error_message"));
    }

    public String processPaymentResult(Long txId, String status) {
        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("p_tx_id", txId)
                .addValue("p_status", status);

        Map<String, Object> out = processResultProcedure.execute(in);
        return (String) out.get("p_out_result");
    }

    public Integer reconcileStuckPayments(int minutesThreshold) {
        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("p_minutes_threshold", minutesThreshold);

        Map<String, Object> out = reconcileProcedure.execute(in);
        return ((Number) out.get("p_out_count")).intValue();
    }

    public record TransferResult(String resultCode, String errorMessage) {
    }
}
