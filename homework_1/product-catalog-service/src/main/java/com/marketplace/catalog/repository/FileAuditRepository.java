package com.marketplace.catalog.repository;

import com.marketplace.catalog.model.AuditRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация AuditRepository, сохраняющая записи аудита в файл.
 */
public class FileAuditRepository implements AuditRepository {

    private final File file;
    private final List<AuditRecord> records = new ArrayList<>();

    /**
     * Создаёт файловый репозиторий аудита.
     *
     * @param fileName имя файла для хранения записей
     */
    public FileAuditRepository(String fileName) {
        this.file = new File(fileName);
        loadFromFile();
    }

    @Override
    public synchronized void save(AuditRecord record) {
        records.add(record);
        saveToFile();
    }

    @Override
    public synchronized List<AuditRecord> findAll() {
        return new ArrayList<>(records);
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        if (!file.exists()) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<AuditRecord> loaded = (List<AuditRecord>) ois.readObject();
            records.clear();
            records.addAll(loaded);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Не удалось загрузить аудит из файла: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(records);
        } catch (IOException e) {
            System.err.println("Не удалось сохранить аудит в файл: " + e.getMessage());
        }
    }
}
