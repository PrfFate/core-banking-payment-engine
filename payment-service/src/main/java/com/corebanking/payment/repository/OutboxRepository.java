package com.corebanking.payment.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OutboxRepository {

    private final JdbcTemplate jdbcTemplate;

    public OutboxRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OutboxRecord> fetchUnprocessedRecords() {
        String sql = "SELECT id, aggregate_type, aggregate_id, event_type, payload " +
                     "FROM outbox WHERE processed = 0 " +
                     "FETCH FIRST 50 ROWS ONLY FOR UPDATE SKIP LOCKED";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> new OutboxRecord(
                rs.getLong("id"),
                rs.getString("aggregate_type"),
                rs.getString("aggregate_id"),
                rs.getString("event_type"),
                rs.getString("payload")
        ));
    }

    public void markAsProcessed(List<Long> ids) {
        if (ids.isEmpty()) return;

        // In a real application, you would use batchUpdate or in-clause binding safely.
        // For simplicity with JdbcTemplate and small batches:
        StringBuilder sql = new StringBuilder("UPDATE outbox SET processed = 1 WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append("?");
            if (i < ids.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");

        jdbcTemplate.update(sql.toString(), ids.toArray());
    }
}
