package service;

import exception.CategoryNotFoundException;
import exception.DuplicateProductException;
import exception.ProductNotFoundException;
import exception.SupplierNotFoundException;
import model.Category;
import model.Product;
import model.Supplier;
import repository.ProductRepository;
import repository.SupplierRepository;
import repository.CategoryRepository;

import java.math.BigDecimal;
import java.util.List;

public class ProductService {
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, SupplierRepository supplierRepository, CategoryRepository categoryRepository){
        if (productRepository == null){
            throw new IllegalArgumentException("Product repository cannot be null");
        }

        if (supplierRepository == null){
            throw new IllegalArgumentException("Supplier repository cannot be null");
        }

        if (categoryRepository == null){
            throw new IllegalArgumentException("Category repository cannot be null");
        }

        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
    }

    public Product createProduct(String productId, String sku, String name, String categoryId, BigDecimal price, int quantity, int reorderLevel){
        if(productRepository.existsById(productId)){
            throw new DuplicateProductException("Product with ID " + productId + " already exists");
        }

        if (productRepository.existsBySku(sku)){
            throw new DuplicateProductException("Product with SKU " + sku + " already exists");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new CategoryNotFoundException("Category with ID " + categoryId + " does not exist"));


        Product product = new Product(productId, sku, name, category, price, quantity, reorderLevel);

        productRepository.save(product);

        return product;
    }

    public Product findById(String productId){
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID " + productId + " does not exist"));
    }

    public Product findBySku(String sku){
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product with sku " + sku + " does not exist"));
    }

    public List<Product> findAll(){
        return productRepository.findAll();
    }

    public void assignSupplier(String productId, String supplierId){
        Product product = findById(productId);

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier with ID " + supplierId + " does not exist"));

        product.addSupplier(supplier);

        productRepository.save(product);
    }

    public void removeSupplier(String productId, String supplierId){
        Product product = findById(productId);

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier with ID " + supplierId + " does not exist"));

        product.removeSupplier(supplier);

        productRepository.save(product);
    }

    public void renameProduct(String productId, String newName){
        Product product = findById(productId);

        product.rename(newName);

        productRepository.update(product);
    }

    public void changePrice(String productId, BigDecimal newPrice){
        Product product = findById(productId);

        product.changePrice(newPrice);

        productRepository.update(product);
    }

    public void changeCategory(String productId, String categoryId){
        Product product = findById(productId);

        Category newCategory = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new CategoryNotFoundException("Category with ID " + categoryId + " does not exist"));

        product.changeCategory(newCategory);

        productRepository.update(product);
    }

    public void changeReorderLevel(String productId, int newReorderLevel){
        Product product = findById(productId);

        product.changeReorderLevel(newReorderLevel);

        productRepository.update(product);
    }

}
