package service;

import model.Category;
import model.Product;
import repository.ProductRepository;

import java.math.BigDecimal;

public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository){
        if (productRepository == null){
            throw new IllegalArgumentException("Product repository cannot be null");
        }
        this.productRepository = productRepository;
    }

    public Product createProduct(String productId, String sku, String name, Category category, BigDecimal price, int quantity, int reorderLevel){
        if(productRepository.existsById(productId)){
            throw new IllegalArgumentException("Duplicate product Id found");
        }

        if (productRepository.existsBySku(sku)){
            throw new IllegalArgumentException("Duplicate sku found");
        }

        Product product = new Product(productId, sku, name, category, price, quantity, reorderLevel);

        productRepository.save(product);

        return product;
    }



}
