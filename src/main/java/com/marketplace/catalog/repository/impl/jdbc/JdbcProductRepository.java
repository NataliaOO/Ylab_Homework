package com.marketplace.catalog.repository.impl.jdbc;

import com.marketplace.catalog.config.AppConfig;
import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.repository.ProductRepository;
import com.marketplace.catalog.db.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class JdbcProductRepository implements ProductRepository {

    private final String schema = AppConfig.get("db.schema");

    @Override
    public Product save(Product p) {
        if (p.getId() == null) return insert(p);
        update(p); return p;
    }

    private Product insert(Product p) {
        final String sql = """
            INSERT INTO %s.products (id, name, brand, category, price, description, active)
            VALUES (nextval('%s.product_seq'), ?, ?, ?, ?, ?, ?)
            RETURNING id
            """.formatted(schema, schema);
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getBrand());
            ps.setString(3, p.getCategory() != null ? p.getCategory().name() : null);
            ps.setBigDecimal(4, p.getPrice());
            ps.setString(5, p.getDescription());
            ps.setBoolean(6, p.isActive());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) p.setId(rs.getLong(1));
            }
            return p;
        } catch (SQLException e) {
            throw new RuntimeException("Insert product failed", e);
        }
    }

    private void update(Product p) {
        final String sql = """
            UPDATE %s.products
               SET name=?, brand=?, category=?, price=?, description=?, active=?
             WHERE id=?
            """.formatted(schema);
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getBrand());
            ps.setString(3, p.getCategory() != null ? p.getCategory().name() : null);
            ps.setBigDecimal(4, p.getPrice());
            ps.setString(5, p.getDescription());
            ps.setBoolean(6, p.isActive());
            ps.setLong(7, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update product failed", e);
        }
    }

    @Override
    public Optional<Product> findById(Long id) {
        final String sql = """
            SELECT id, name, brand, category, price, description, active
              FROM %s.products
             WHERE id=?
            """.formatted(schema);
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Find product by id failed", e);
        }
    }

    @Override
    public List<Product> findAll() {
        final String sql = """
            SELECT id, name, brand, category, price, description, active
              FROM %s.products
             ORDER BY id
            """.formatted(schema);
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Product> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Find all products failed", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        final String sql = "DELETE FROM %s.products WHERE id=?".formatted(schema);
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete product failed", e);
        }
    }

    @Override
    public long count() {
        final String sql = "SELECT COUNT(*) FROM %s.products".formatted(schema);
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next(); return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException("Count products failed", e);
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        String brand = rs.getString("brand");
        String cat = rs.getString("category");
        BigDecimal price = rs.getBigDecimal("price");
        String desc = rs.getString("description");
        return new Product(id, name, brand, cat != null ? Category.valueOf(cat) : null, price, desc);
    }
}