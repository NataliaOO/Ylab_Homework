package com.marketplace.catalog.repository;

import com.marketplace.catalog.it.BasePgIT;
import com.marketplace.catalog.model.AuditRecord;
import com.marketplace.catalog.repository.impl.jdbc.JdbcAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcAuditRepositoryIT extends BasePgIT {

    private AuditRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        truncate(TBL_AUDIT);
        repo = new JdbcAuditRepository();
    }

    @Test
    void save_shouldPersistRecord() {
        AuditRecord record = new AuditRecord(
                LocalDateTime.now(),
                "admin",
                "LOGIN",
                "User logged in"
        );

        repo.save(record);

        List<AuditRecord> all = repo.findAll();
        assertEquals(1, all.size());

        AuditRecord saved = all.get(0);
        assertEquals("admin", saved.username());
        assertEquals("LOGIN", saved.action());
        assertEquals("User logged in", saved.details());
        assertNotNull(saved.timestamp());
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoRecords() throws Exception {
        truncate(TBL_AUDIT);
        assertTrue(repo.findAll().isEmpty());
    }
}
