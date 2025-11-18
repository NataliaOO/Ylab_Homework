package com.marketplace.catalog.service.impl;

import com.marketplace.catalog.model.AuditRecord;
import com.marketplace.catalog.repository.AuditRepository;
import com.marketplace.catalog.service.AuditService;

import java.util.List;

/**
 * Реализация сервиса аудита по умолчанию.
 */
public class AuditServiceImpl implements AuditService {

    private final AuditRepository repository;

    public AuditServiceImpl(AuditRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AuditRecord> findAll() {
        return repository.findAll();
    }
}
