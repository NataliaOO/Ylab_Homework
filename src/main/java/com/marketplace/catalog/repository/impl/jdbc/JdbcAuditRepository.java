package com.marketplace.catalog.repository.impl.jdbc;

import com.marketplace.catalog.config.AppConfig;
import com.marketplace.catalog.model.AuditRecord;
import com.marketplace.catalog.repository.AuditRepository;
import com.marketplace.catalog.db.ConnectionFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-репозиторий для записей аудита.
 */
public class JdbcAuditRepository implements AuditRepository {
    // ---- Константы схемы и таблицы -----------------------------------------

    private static final String PROP_SCHEMA = "db.schema";
    private static final String SCHEMA      = AppConfig.get(PROP_SCHEMA);

    private static final String TABLE_NAME  = "audit";
    private static final String TBL_AUDIT   = SCHEMA + "." + TABLE_NAME;

    // ---- Колонки -----------------------------------------------------------

    private static final String COL_CREATEDAT= "created_at";
    private static final String COL_USERNAME = "username";
    private static final String COL_ACTION   = "action";
    private static final String COL_DETAILS  = "details";

    // ---- SQL ---------------------------------------------------------------

    private static final String SQL_INSERT = """
            INSERT INTO %s (%s, %s, %s, %s)
            VALUES (?, ?, ?, ?)
            """.formatted(
            TBL_AUDIT,
            COL_CREATEDAT, COL_USERNAME, COL_ACTION, COL_DETAILS
    );

    private static final String SQL_FIND_ALL = """
            SELECT %s, %s, %s, %s
            FROM %s
            ORDER BY %s ASC
            """.formatted(
            COL_CREATEDAT, COL_USERNAME, COL_ACTION, COL_DETAILS,
            TBL_AUDIT,
            COL_CREATEDAT
    );

    private static final String ERR_INSERT = "Insert audit failed";
    private static final String ERR_QUERY  = "Query audit failed";


    @Override
    public void save(AuditRecord r) {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT)) {

            // Порядок столбцов: ts, username, action, details
            LocalDateTime ts = r.timestamp();
            ps.setTimestamp(1, ts != null ? Timestamp.valueOf(ts) : null);
            ps.setString(2, r.username());
            ps.setString(3, r.action());
            ps.setString(4, r.details());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ERR_INSERT, e);
        }
    }

    @Override
    public List<AuditRecord> findAll() {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            List<AuditRecord> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(ERR_QUERY, e);
        }
    }

    private AuditRecord mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp(COL_CREATEDAT);
        LocalDateTime time = ts != null ? ts.toLocalDateTime() : null;
        String username = rs.getString(COL_USERNAME);
        String action   = rs.getString(COL_ACTION);
        String details  = rs.getString(COL_DETAILS);

        return new AuditRecord(time, username, action, details);
    }
}
