package com.marketplace.catalog.service;

import com.marketplace.catalog.model.AuditRecord;
import com.marketplace.catalog.repository.AuditRepository;

import java.util.List;

/**
 * Сервис для работы с аудитом.
 */
public class AuditService {

    private final AuditRepository repository;

    public AuditService(AuditRepository repository) {
        this.repository = repository;
    }

    /**
     * Возвращает все записи аудита.
     */
    public List<AuditRecord> findAll() {
        return repository.findAll();
    }
}
