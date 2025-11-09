package com.marketplace.catalog.repository;

import com.marketplace.catalog.model.AuditRecord;

import java.util.List;

/**
 * Репозиторий для хранения записей аудита.
 */
public interface AuditRepository {

    /**
     * Сохраняет запись аудита.
     *
     * @param record запись аудита
     */
    void save(AuditRecord record);

    /**
     * Возвращает все записи аудита.
     *
     * @return список записей
     */
    List<AuditRecord> findAll();
}
