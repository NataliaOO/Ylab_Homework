package com.marketplace.catalog.repository.impl.jdbc;

import com.marketplace.catalog.config.AppConfig;
import com.marketplace.catalog.exception.RepositoryException;
import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.repository.ProductRepository;
import com.marketplace.catalog.db.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/**
 * JDBC-реализация репозитория товаров.
 */
public class JdbcProductRepository implements ProductRepository {
    // ---- Константы схемы и таблицы -----------------------------------------

    private static final String PROP_SCHEMA = "db.schema";
    private static final String SCHEMA      = AppConfig.get(PROP_SCHEMA);

    private static final String TABLE_NAME  = "product";
    private static final String TBL_PRODUCTS = SCHEMA + "." + TABLE_NAME;

    // ---- Имена колонок -----------------------------------------------------

    private static final String COL_ID          = "id";
    private static final String COL_NAME        = "name";
    private static final String COL_BRAND       = "brand";
    private static final String COL_CATEGORY    = "category";
    private static final String COL_PRICE       = "price";
    private static final String COL_DESCRIPTION = "description";

    // ---- SQL-выражения -----------------------------------------------------

    // Важно: колонку id НЕ указываем — БД (BIGSERIAL/IDENTITY) сама выдаёт значение.
    private static final String SQL_INSERT = """
            INSERT INTO %s (%s, %s, %s, %s, %s)
            VALUES (?, ?, ?, ?, ?)
            RETURNING %s
            """.formatted(
            TBL_PRODUCTS,
            COL_NAME, COL_BRAND, COL_CATEGORY, COL_PRICE, COL_DESCRIPTION,
            COL_ID
    );

    private static final String SQL_UPDATE = """
            UPDATE %s
            SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ?
            WHERE %s = ?
            """.formatted(
            TBL_PRODUCTS,
            COL_NAME, COL_BRAND, COL_CATEGORY, COL_PRICE, COL_DESCRIPTION,
            COL_ID
    );

    private static final String SQL_FIND_BY_ID = """
            SELECT %s, %s, %s, %s, %s, %s
            FROM %s
            WHERE %s = ?
            """.formatted(
            COL_ID, COL_NAME, COL_BRAND, COL_CATEGORY, COL_PRICE, COL_DESCRIPTION,
            TBL_PRODUCTS,
            COL_ID
    );

    private static final String SQL_FIND_ALL = """
            SELECT %s, %s, %s, %s, %s, %s
            FROM %s
            ORDER BY %s
            """.formatted(
            COL_ID, COL_NAME, COL_BRAND, COL_CATEGORY, COL_PRICE, COL_DESCRIPTION,
            TBL_PRODUCTS,
            COL_ID
    );

    private static final String SQL_DELETE_BY_ID =
            "DELETE FROM " + TBL_PRODUCTS + " WHERE " + COL_ID + " = ?";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM " + TBL_PRODUCTS;

    private static final String ERR_INSERT = "Insert product failed";
    private static final String ERR_UPDATE = "Update product failed";
    private static final String ERR_QUERY  = "Query product failed";
    private static final String ERR_DELETE = "Delete product failed";
    private final ConnectionFactory connectionFactory;

    public JdbcProductRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;

    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            return insert(product);
        } else {
            return update(product);
        }
    }

    private Product insert(Product p) {
        try (Connection c = connectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getBrand());
            ps.setString(3, p.getCategory() != null ? p.getCategory().name() : null);
            ps.setBigDecimal(4, p.getPrice());
            ps.setString(5, p.getDescription());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p.setId(rs.getLong(1));
                }
            }
            return p;
        } catch (SQLException e) {
            throw new RepositoryException(ERR_INSERT, e);
        }
    }

    private Product update(Product p) {
        try (Connection c = connectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getBrand());
            ps.setString(3, p.getCategory() != null ? p.getCategory().name() : null);
            ps.setBigDecimal(4, p.getPrice());
            ps.setString(5, p.getDescription());
            ps.setLong(6, p.getId());

            ps.executeUpdate();
            return p;
        } catch (SQLException e) {
            throw new RepositoryException(ERR_UPDATE, e);
        }
    }

    @Override
    public Optional<Product> findById(Long id) {
        try (Connection c = connectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException(ERR_QUERY, e);
        }
    }

    @Override
    public List<Product> findAll() {
        try (Connection c = connectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            List<Product> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RepositoryException(ERR_QUERY, e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection c = connectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE_BY_ID)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException(ERR_DELETE, e);
        }
    }

    @Override
    public long count() {
        try (Connection c = connectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_COUNT);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new RepositoryException(ERR_QUERY, e);
        }
    }
    private Product mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong(COL_ID);
        String name = rs.getString(COL_NAME);
        String brand = rs.getString(COL_BRAND);
        String categoryStr = rs.getString(COL_CATEGORY);
        BigDecimal price = rs.getBigDecimal(COL_PRICE);
        String description = rs.getString(COL_DESCRIPTION);

        Category category = null;
        if (categoryStr != null) {
            category = Category.valueOf(categoryStr);
        }

        return new Product(id, name, brand, category, price, description);
    }
}