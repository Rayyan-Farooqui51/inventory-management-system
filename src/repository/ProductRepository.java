package repository;

import model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    void save(Product product);

    Optional<Product> findById(String productId);

    Optional<Product> findBySku(String sku);

    List<Product> findAll();

    boolean existsById(String productId);

    boolean existsBySku(String sku);

}
