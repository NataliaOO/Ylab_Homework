package com.marketplace.catalog.service;

import com.marketplace.catalog.model.AuditRecord;

import java.util.List;

/**
 * Контракт сервиса для работы с аудитом.
 */
public interface AuditService {

    /**
     * Возвращает все записи аудита.
     */
    List<AuditRecord> findAll();
}
