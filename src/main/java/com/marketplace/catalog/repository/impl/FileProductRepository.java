package com.marketplace.catalog.repository.impl;

import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.repository.impl.DataSnapshot;
import com.marketplace.catalog.repository.ProductRepository;

import java.io.*;
import java.util.*;

/**
 * Реализация ProductRepository, сохраняющая товары в файл с помощью сериализации.
 */
public class FileProductRepository implements ProductRepository {

    private final File file;
    private final Map<Long, Product> storage = new HashMap<>();
    private long sequence = 0L;

    /**
     * Создаёт файловый репозиторий товаров.
     *
     * @param fileName имя файла для хранения данных
     */
    public FileProductRepository(String fileName) {
        this.file = new File(fileName);
        loadFromFile();
    }

    @Override
    public synchronized Product save(Product product) {
        if (product.getId() == null) {
            product.setId(++sequence);
        } else if (product.getId() > sequence) {
            sequence = product.getId();
        }
        storage.put(product.getId(), product);
        saveToFile();
        return product;
    }

    @Override
    public synchronized Optional<Product> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public synchronized List<Product> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public synchronized void deleteById(Long id) {
        storage.remove(id);
        saveToFile();
    }

    @Override
    public synchronized long count() {
        return storage.size();
    }

    private void loadFromFile() {
        if (!file.exists()) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            DataSnapshot snapshot = (DataSnapshot) ois.readObject();
            storage.clear();
            for (Product p : snapshot.products()) {   // <-- было snapshot.products
                storage.put(p.getId(), p);
                if (p.getId() != null && p.getId() > sequence) {
                    sequence = p.getId();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Не удалось загрузить товары из файла: " + e.getMessage());
        }
    }

    private void saveToFile() {
        DataSnapshot snapshot = new DataSnapshot(new ArrayList<>(storage.values())); // <-- конструктор
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(snapshot);
        } catch (IOException e) {
            System.err.println("Не удалось сохранить товары в файл: " + e.getMessage());
        }
    }
}
