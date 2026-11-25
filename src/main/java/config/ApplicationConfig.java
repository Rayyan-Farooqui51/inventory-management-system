package config;

import database.ConnectionManager;
import database.DatabaseConnectionProvider;
import database.TransactionManager;
import repository.CategoryRepository;
import repository.ProductRepository;
import repository.StockMovementRepository;
import repository.SupplierRepository;
import repository.jdbc.JdbcCategoryRepository;
import repository.jdbc.JdbcProductRepository;
import repository.jdbc.JdbcStockMovementRepository;
import repository.jdbc.JdbcSupplierRepository;
import service.CategoryService;
import service.InventoryService;
import service.ProductService;
import service.SupplierService;

public class ApplicationConfig {

    private final DatabaseConnectionProvider databaseConnectionProvider;
    private final TransactionManager transactionManager;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SupplierRepository supplierRepository;

    private final CategoryService categoryService;
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final SupplierService supplierService;

    public ApplicationConfig(){
        this.databaseConnectionProvider = new ConnectionManager();

        this.transactionManager = new TransactionManager(this.databaseConnectionProvider);

        this.categoryRepository = new JdbcCategoryRepository(this.databaseConnectionProvider);
        this.productRepository = new JdbcProductRepository(this.databaseConnectionProvider);
        this.stockMovementRepository = new JdbcStockMovementRepository(this.databaseConnectionProvider, this.productRepository);
        this.supplierRepository = new JdbcSupplierRepository(this.databaseConnectionProvider);

        this.categoryService = new CategoryService(this.categoryRepository);
        this.inventoryService = new InventoryService(this.productRepository, this.stockMovementRepository, this.transactionManager);
        this.productService = new ProductService(this.productRepository, this.supplierRepository, this.categoryRepository);
        this.supplierService = new SupplierService(this.supplierRepository);

    }


    public SupplierService getSupplierService() {
        return supplierService;
    }

    public ProductService getProductService() {
        return productService;
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }
}
