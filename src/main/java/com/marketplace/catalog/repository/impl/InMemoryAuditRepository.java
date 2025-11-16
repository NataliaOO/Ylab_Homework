package com.marketplace.catalog.repository.impl;

import com.marketplace.catalog.model.AuditRecord;
import com.marketplace.catalog.repository.AuditRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Простая in-memory реализация AuditRepository.
 */
public class InMemoryAuditRepository implements AuditRepository {

    private final List<AuditRecord> records = new ArrayList<>();

    @Override
    public synchronized void save(AuditRecord record) {
        records.add(record);
    }

    @Override
    public synchronized List<AuditRecord> findAll() {
        return new ArrayList<>(records);
    }
}
