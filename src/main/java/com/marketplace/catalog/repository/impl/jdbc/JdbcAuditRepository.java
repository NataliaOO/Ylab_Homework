package com.marketplace.catalog.repository.impl.jdbc;

import com.marketplace.catalog.config.AppConfig;
import com.marketplace.catalog.model.AuditRecord;
import com.marketplace.catalog.repository.AuditRepository;
import com.marketplace.catalog.db.ConnectionFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcAuditRepository implements AuditRepository {
    private final String schema = AppConfig.get("db.schema");

    @Override
    public void save(AuditRecord r) {
        final String sql = """
            INSERT INTO %s.audit (id, ts, username, action, details)
            VALUES (nextval('%s.audit_seq'), ?, ?, ?, ?)
            """.formatted(schema, schema);
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(r.timestamp()));
            ps.setString(2, r.username());
            ps.setString(3, r.action());
            ps.setString(4, r.details());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Insert audit failed", e);
        }
    }

    @Override
    public List<AuditRecord> findAll() {
        final String sql = """
            SELECT ts, username, action, details
              FROM %s.audit
             ORDER BY ts DESC, action
            """.formatted(schema);
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<AuditRecord> out = new ArrayList<>();
            while (rs.next()) {
                LocalDateTime ts = rs.getTimestamp("ts").toLocalDateTime();
                out.add(new AuditRecord(ts, rs.getString("username"), rs.getString("action"), rs.getString("details")));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Find all audit failed", e);
        }
    }
}
