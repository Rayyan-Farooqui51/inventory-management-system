package repository;

import model.Product;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    void save(Product product);

    void update(Product product);

    void updateStockQuantity(Connection connection, Product product);

    Optional<Product> findByIdForUpdate(Connection connection, String productId);

    Optional<Product> findById(String productId);

    Optional<Product> findBySku(String sku);

    List<Product> findAll();

    boolean existsById(String productId);

    boolean existsBySku(String sku);

}
