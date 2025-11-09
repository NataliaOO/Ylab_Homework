package com.marketplace.catalog.repository;

import com.marketplace.catalog.model.AuditRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileAuditRepositoryTest {

    private final String fileName = "test-audit.dat";

    @AfterEach
    void tearDown() {
        File f = new File(fileName);
        if (f.exists()) {
            assertTrue(f.delete(), "Не удалось удалить файл тестового аудита");
        }
    }

    @Test
    void saveAndReload_shouldPersistAuditRecords() {
        FileAuditRepository repo1 = new FileAuditRepository(fileName);

        repo1.save(new AuditRecord(LocalDateTime.now(), "admin", "LOGIN", "User logged in"));
        repo1.save(new AuditRecord(LocalDateTime.now(), "admin", "CREATE_PRODUCT", "id=1"));

        assertEquals(2, repo1.findAll().size());

        FileAuditRepository repo2 = new FileAuditRepository(fileName);
        List<AuditRecord> loaded = repo2.findAll();

        assertEquals(2, loaded.size());
        assertTrue(loaded.stream().anyMatch(r -> r.toString().contains("LOGIN")));
        assertTrue(loaded.stream().anyMatch(r -> r.toString().contains("CREATE_PRODUCT")));
    }
}
