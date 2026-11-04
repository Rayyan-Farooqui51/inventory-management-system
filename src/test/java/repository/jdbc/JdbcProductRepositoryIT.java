package repository.jdbc;

import database.TestConnectionManager;
import exception.ProductNotFoundException;
import model.Category;
import model.Product;
import model.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcProductRepositoryIT {
    private TestConnectionManager testConnectionManager;
    private JdbcSupplierRepository jdbcSupplierRepository;
    private JdbcCategoryRepository jdbcCategoryRepository;
    private JdbcProductRepository jdbcProductRepository;

    private final String testProductId = "IT_P_001";
    private final String testSku = "IT_SKU_001";
    private final String testCategoryId = "IT_CAT_001";
    private final String testSupplierId = "IT_SUP_001";
    private final String testSecondSupplierId = "IT_SUP_002";
    private final String testEmail = "supplier001@example.com";
    private final String testSecondEmail = "supplier002@example.com";


    @BeforeEach
    void setUp(){
        testConnectionManager = new TestConnectionManager();
        jdbcProductRepository = new JdbcProductRepository(testConnectionManager);
        jdbcCategoryRepository = new JdbcCategoryRepository(testConnectionManager);
        jdbcSupplierRepository = new JdbcSupplierRepository(testConnectionManager);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        SQLException firstException = null;

        try (Connection connection = testConnectionManager.getConnection()) {

            String[] queries = {
                    "DELETE FROM product_suppliers WHERE product_id = ?",
                    "DELETE FROM products WHERE product_id = ?",
                    "DELETE FROM categories WHERE category_id = ?",
                    "DELETE FROM suppliers WHERE supplier_id IN (?, ?)"
            };

            String[][] parameters = {
                    {testProductId},
                    {testProductId},
                    {testCategoryId},
                    {testSupplierId, testSecondSupplierId}
            };

            for (int i = 0; i < queries.length; i++) {
                try {
                    executeDelete(connection, queries[i], parameters[i]);
                } catch (SQLException e) {
                    if (firstException == null) {
                        firstException = e;
                    } else {
                        firstException.addSuppressed(e);
                    }
                }
            }

        } catch (SQLException e) {
            if (firstException == null) {
                firstException = e;
            } else {
                firstException.addSuppressed(e);
            }
        }

        if (firstException != null) {
            throw firstException;
        }
    }

    private void executeDelete(Connection connection, String sql, String... parameters) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < parameters.length; i++) {
                statement.setString(i + 1, parameters[i]);
            }

            statement.executeUpdate();
        }
    }

    @Test
    void save_shouldPersistProduct_whenValidProductProvided() throws SQLException{
        Category category = new Category(testCategoryId,"IT Product Test Category", "Category for product integration testing");

        jdbcCategoryRepository.save(category);

        Product product = new Product(testProductId, testSku, "IT Test Product", category, new BigDecimal("499.99"), 50, 10);

        jdbcProductRepository.save(product);

        try(
                Connection connection = testConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM products WHERE product_id = ?")
        ){
            statement.setString(1, testProductId);

            try(ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "Product should be persisted in the database");
            }
        }
    }

    @Test
    void save_shouldPersistProductWithSuppliers_whenValidProductProvided() throws SQLException{
        Category category = new Category(testCategoryId,"IT Product Test Category", "Category for product integration testing");

        jdbcCategoryRepository.save(category);

        Product product = new Product(testProductId, testSku, "IT Test Product", category, new BigDecimal("499.99"), 50, 10);


        Supplier supplier1 = new Supplier(testSupplierId, "IT Test Supplier",testEmail,"9876543210");

        Supplier supplier2 = new Supplier(testSecondSupplierId, "IT Test Supplier",testSecondEmail,"9874563210");

        product.addSupplier(supplier1);
        product.addSupplier(supplier2);

        jdbcSupplierRepository.save(supplier1);
        jdbcSupplierRepository.save(supplier2);

        jdbcProductRepository.save(product);

        try(
                Connection connection = testConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM product_suppliers WHERE product_id = ?")
        ){
            statement.setString(1, testProductId);

            try(ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals(2,resultSet.getInt(1));
            }
        }
    }

    @Test
    void findById_shouldReturnProductWithCategoryAndSuppliers_whenProductExists() throws SQLException {

        Category category = new Category(testCategoryId,"IT Product Test Category", "Category for product integration testing");

        jdbcCategoryRepository.save(category);

        Product product = new Product(testProductId, testSku, "IT Test Product", category, new BigDecimal("499.99"), 50, 10);

        Supplier supplier1 = new Supplier(testSupplierId, "IT Test Supplier",testEmail,"9876543210");

        Supplier supplier2 = new Supplier(testSecondSupplierId, "IT Test Supplier",testSecondEmail,"9874563210");

        jdbcSupplierRepository.save(supplier1);
        jdbcSupplierRepository.save(supplier2);

        product.addSupplier(supplier1);
        product.addSupplier(supplier2);

        jdbcProductRepository.save(product);

        Optional<Product> result = jdbcProductRepository.findById(testProductId);

        assertTrue(result.isPresent());

        Product retrievedProduct = result.get();

        assertEquals(testProductId, retrievedProduct.getProductId());
        assertEquals(testSku, retrievedProduct.getSku());
        assertEquals("IT Test Product", retrievedProduct.getName());

        assertEquals(testCategoryId, retrievedProduct.getCategory().getCategoryId());

        assertEquals(2, retrievedProduct.getSuppliers().size());
        assertTrue(retrievedProduct.getSuppliers().contains(supplier1));
        assertTrue(retrievedProduct.getSuppliers().contains(supplier2));
    }

    @Test
    void findById_shouldReturnEmpty_whenProductDoesNotExist() {

        Optional<Product> result = jdbcProductRepository.findById("IT_NON_EXISTENT");

        assertTrue(result.isEmpty());
    }

    @Test
    void findBySku_shouldReturnProduct_whenSkuExists() throws SQLException {
        Category category = new Category(testCategoryId,"IT Product Test Category", "Category for product integration testing");

        jdbcCategoryRepository.save(category);

        Product product = new Product(testProductId, testSku, "IT Test Product", category, new BigDecimal("499.99"), 50, 10);

        jdbcProductRepository.save(product);

        Optional<Product> result = jdbcProductRepository.findBySku(testSku);

        assertTrue(result.isPresent());

        Product retrievedProduct = result.get();

        assertEquals(testProductId, retrievedProduct.getProductId());
        assertEquals(testSku, retrievedProduct.getSku());
        assertEquals("IT Test Product", retrievedProduct.getName());
    }

    @Test
    void findBySku_shouldReturnEmpty_whenSkuDoesNotExist() {

        Optional<Product> result = jdbcProductRepository.findBySku("IT_NON_EXISTENT_SKU");

        assertTrue(result.isEmpty());
    }

    @Test
    void update_shouldModifyProduct_whenProductExists() throws SQLException {

        Category category = new Category(testCategoryId,"IT Product Test Category", "Category for product integration testing");

        jdbcCategoryRepository.save(category);

        Product product = new Product(testProductId, testSku, "IT Test Product", category, new BigDecimal("499.99"), 50, 10);

        jdbcProductRepository.save(product);

        product.rename("Updated IT Test Product");
        product.changePrice(new BigDecimal("599.99"));
        product.increaseStock(25);
        product.changeReorderLevel(15);

        // Act
        jdbcProductRepository.update(product);

        // Assert
        Optional<Product> result =
                jdbcProductRepository.findById(testProductId);

        assertTrue(result.isPresent());

        Product updatedProduct = result.get();

        assertEquals("Updated IT Test Product", updatedProduct.getName());
        assertEquals(new BigDecimal("599.99"), updatedProduct.getPrice());
        assertEquals(75, updatedProduct.getQuantity());
        assertEquals(15, updatedProduct.getReorderLevel());
    }

    @Test
    void update_shouldThrowException_whenProductDoesNotExist() {

        Category category = new Category(testCategoryId,"IT Product Test Category", "Category for product integration testing");

        jdbcCategoryRepository.save(category);

        Product product = new Product(testProductId, testSku, "IT Test Product", category, new BigDecimal("499.99"), 50, 10);

        assertThrows(
                ProductNotFoundException.class,
                () -> jdbcProductRepository.update(product)
        );
    }

    @Test
    void update_shouldModifySupplierAssociations_whenProductExists() throws SQLException {

        Category category = new Category(testCategoryId, "IT Product Test Category","Category for product integration testing");

        jdbcCategoryRepository.save(category);

        Supplier supplier1 = new Supplier(testSupplierId,"IT Test Supplier 1",testEmail,"9876543210");

        Supplier supplier2 = new Supplier(testSecondSupplierId,"IT Test Supplier 2",testSecondEmail,"9874563210");

        jdbcSupplierRepository.save(supplier1);
        jdbcSupplierRepository.save(supplier2);

        Product product = new Product(testProductId,testSku,"IT Test Product",category,new BigDecimal("499.99"),50,10);

        product.addSupplier(supplier1);

        jdbcProductRepository.save(product);

        product.removeSupplier(supplier1);
        product.addSupplier(supplier2);

        jdbcProductRepository.update(product);

        Optional<Product> result = jdbcProductRepository.findById(testProductId);

        assertTrue(result.isPresent());

        Product updatedProduct = result.get();

        assertEquals(1, updatedProduct.getSuppliers().size());
        assertFalse(updatedProduct.getSuppliers().contains(supplier1));
        assertTrue(updatedProduct.getSuppliers().contains(supplier2));
    }

    @Test
    void findAll_shouldReturnAllProducts_whenProductsExist() throws SQLException {

        // Arrange
        Category category = new Category(testCategoryId,"IT Product Test Category","Category for product integration testing");

        jdbcCategoryRepository.save(category);

        Product product1 = new Product(testProductId,testSku,"IT Test Product 1",category,new BigDecimal("499.99"),50,10);

        jdbcProductRepository.save(product1);

        List<Product> products = jdbcProductRepository.findAll();

        assertEquals(1, products.size());

        assertTrue(products.stream()
                .anyMatch(product -> product.getProductId().equals(testProductId)));

    }

    @Test
    void existsById_shouldReturnTrue_whenProductExists() throws SQLException {

        Category category = new Category(testCategoryId,"IT Product Test Category","Category for product integration testing");

        jdbcCategoryRepository.save(category);

        Product product = new Product(testProductId,testSku,"IT Test Product",category,new BigDecimal("499.99"),50,10);

        jdbcProductRepository.save(product);

        boolean exists = jdbcProductRepository.existsById(testProductId);

        assertTrue(exists);
    }

    @Test
    void existsById_shouldReturnFalse_whenProductDoesNotExist() {

        boolean exists = jdbcProductRepository.existsById("IT_NON_EXISTENT");

        assertFalse(exists);
    }

    @Test
    void existsBySku_shouldReturnTrue_whenSkuExists() throws SQLException {

        Category category = new Category(testCategoryId,"IT Product Test Category","Category for product integration testing");

        jdbcCategoryRepository.save(category);

        Product product = new Product(testProductId,testSku,"IT Test Product",category,new BigDecimal("499.99"),50,10);

        jdbcProductRepository.save(product);

        boolean exists = jdbcProductRepository.existsBySku(testSku);

        assertTrue(exists);
    }

    @Test
    void existsBySku_shouldReturnFalse_whenSkuDoesNotExist() {

        boolean exists = jdbcProductRepository.existsBySku("IT_NON_EXISTENT_SKU");

        assertFalse(exists);
    }

    @Test
    void save_shouldThrowException_whenProductIdAlreadyExists() throws SQLException {

        Category category = new Category(testCategoryId,"IT Product Test Category","Category for product integration testing");

        jdbcCategoryRepository.save(category);

        Product product1 = new Product(testProductId, testSku, "IT Product 1",category, new BigDecimal("499.99"),50, 10);

        Product product2 = new Product(testProductId, "IT_SKU_002", "IT Product 2",category, new BigDecimal("599.99"), 30, 5);

        jdbcProductRepository.save(product1);

        assertThrows(
                RuntimeException.class,
                () -> jdbcProductRepository.save(product2)
        );
    }

    @Test
    void save_shouldThrowException_whenSkuAlreadyExists() throws SQLException {

        Category category = new Category(testCategoryId,"IT Product Test Category","Category for product integration testing");

        jdbcCategoryRepository.save(category);

        Product product1 = new Product(testProductId, testSku, "IT Product 1",category, new BigDecimal("499.99"),50, 10);

        Product product2 = new Product("IT_P_002", testSku, "IT Product 2",category, new BigDecimal("599.99"),30, 5);

        jdbcProductRepository.save(product1);

        assertThrows(
                RuntimeException.class,
                () -> jdbcProductRepository.save(product2)
        );
    }

    @Test
    void save_shouldRollbackProduct_whenSupplierAssociationFails() throws SQLException {
        Category category = new Category(testCategoryId,"IT Product Test Category","Category for product integration testing");

        jdbcCategoryRepository.save(category);

        Supplier unpersistedSupplier = new Supplier(testSupplierId,"IT Supplier",testEmail,"9876543210");

        Product product = new Product(testProductId,testSku,"IT Product",category,new BigDecimal("499.99"),50,10);

        product.addSupplier(unpersistedSupplier);

        assertThrows(
                RuntimeException.class,
                () -> jdbcProductRepository.save(product)
        );

        try (Connection connection = testConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM products WHERE product_id = ?"
             )) {

            statement.setString(1, testProductId);

            try (ResultSet resultSet = statement.executeQuery()) {
                assertFalse(resultSet.next());
            }
        }
    }

    @Test
    void save_shouldThrowException_whenProductIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jdbcProductRepository.save(null)
        );
    }

    @Test
    void update_shouldThrowException_whenProductIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jdbcProductRepository.update(null)
        );
    }

    @Test
    void findById_shouldThrowException_whenProductIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jdbcProductRepository.findById(null)
        );
    }

    @Test
    void findById_shouldThrowException_whenProductIdIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jdbcProductRepository.findById("   ")
        );
    }

    @Test
    void findBySku_shouldThrowException_whenSkuIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jdbcProductRepository.findBySku(null)
        );
    }

    @Test
    void findBySku_shouldThrowException_whenSkuIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jdbcProductRepository.findBySku("   ")
        );
    }

    @Test
    void existsById_shouldThrowException_whenProductIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jdbcProductRepository.existsById(null)
        );
    }

    @Test
    void existsById_shouldThrowException_whenProductIdIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jdbcProductRepository.existsById("   ")
        );
    }

    @Test
    void existsBySku_shouldThrowException_whenSkuIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jdbcProductRepository.existsBySku(null)
        );
    }

    @Test
    void existsBySku_shouldThrowException_whenSkuIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jdbcProductRepository.existsBySku("   ")
        );
    }

    @Test
    void constructor_shouldThrowException_whenConnectionProviderIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new JdbcProductRepository(null)
        );
    }

    @Test
    void findById_shouldThrowException_whenProductIdIsEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jdbcProductRepository.findById("")
        );
    }
}
