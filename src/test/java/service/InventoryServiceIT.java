package service;

import database.TestConnectionManager;
import database.TransactionManager;
import exception.InsufficientStockException;
import model.Category;
import model.Product;
import model.StockMovement;
import model.StockMovementType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.jdbc.JdbcCategoryRepository;
import repository.jdbc.JdbcProductRepository;
import repository.jdbc.JdbcStockMovementRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceIT {

    private final TestConnectionManager connectionProvider =
            new TestConnectionManager();

    private JdbcCategoryRepository categoryRepository;
    private JdbcProductRepository productRepository;
    private JdbcStockMovementRepository stockMovementRepository;

    private InventoryService inventoryService;

    private final String categoryId = "IT_INV_CAT_001";
    private final String productId = "IT_INV_PROD_001";
    private final String movementId = "IT_INV_MOV_001";

    private Product testProduct;

    @BeforeEach
    void setUp() throws Exception {

        categoryRepository =
                new JdbcCategoryRepository(connectionProvider);

        productRepository =
                new JdbcProductRepository(connectionProvider);

        stockMovementRepository =
                new JdbcStockMovementRepository(
                        connectionProvider,
                        productRepository
                );

        cleanUp();

        TransactionManager transactionManager =
                new TransactionManager(connectionProvider);

        inventoryService = new InventoryService(
                productRepository,
                stockMovementRepository,
                transactionManager
        );

        Category category = new Category(
                categoryId,
                "IT Inventory Category",
                "Category for inventory transaction testing"
        );

        categoryRepository.save(category);

        testProduct = new Product(
                productId,
                "IT_INV_SKU_001",
                "IT Inventory Product",
                category,
                new BigDecimal("100.00"),
                50,
                10
        );

        productRepository.save(testProduct);
    }

    @AfterEach
    void tearDown() throws Exception {
        cleanUp();
    }

    private void cleanUp() throws Exception {

        try (Connection connection = connectionProvider.getConnection()) {

            String[] movementIds = {
                    "IT_INV_MOV_001",
                    "IT_INV_MOV_002",
                    "IT_INV_MOV_003",
                    "IT_INV_CONC_MOV_001",
                    "IT_INV_CONC_MOV_002"
            };

            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM stock_movements WHERE movement_id = ?")) {

                for (String id : movementIds) {
                    statement.setString(1, id);
                    statement.executeUpdate();
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM products WHERE product_id = ?")) {

                statement.setString(1, productId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM categories WHERE category_id = ?")) {

                statement.setString(1, categoryId);
                statement.executeUpdate();
            }
        }
    }

    @Test
    void stockIn_shouldRollbackProductQuantity_whenMovementSaveFails() throws Exception {

        StockMovement existingMovement = new StockMovement(
                movementId,
                testProduct,
                StockMovementType.STOCK_IN,
                5,
                LocalDateTime.now().withNano(0)
        );

        stockMovementRepository.save(existingMovement);

        assertThrows(
                RuntimeException.class,
                () -> inventoryService.stockIn(
                        movementId,
                        productId,
                        10
                )
        );

        Optional<Product> updatedProduct =
                productRepository.findById(productId);

        assertTrue(updatedProduct.isPresent());
        assertEquals(50, updatedProduct.get().getQuantity());

        Optional<StockMovement> persistedMovement =
                stockMovementRepository.findById(movementId);

        assertTrue(persistedMovement.isPresent());

        assertEquals(
                5,
                persistedMovement.get().getQuantity()
        );

        assertEquals(
                StockMovementType.STOCK_IN,
                persistedMovement.get().getType()
        );
    }

    @Test
    void stockIn_shouldUpdateQuantityAndSaveMovement_whenValidRequest() {

        String movementId = "IT_INV_MOV_002";

        StockMovement result = inventoryService.stockIn(
                movementId,
                productId,
                10
        );

        // Verify returned movement
        assertNotNull(result);
        assertEquals(movementId, result.getMovementId());
        assertEquals(StockMovementType.STOCK_IN, result.getType());
        assertEquals(10, result.getQuantity());

        // Verify persisted product quantity
        Product updatedProduct = productRepository.findById(productId)
                .orElseThrow();

        assertEquals(60, updatedProduct.getQuantity());

        // Verify persisted stock movement
        StockMovement persistedMovement =
                stockMovementRepository.findById(movementId)
                        .orElseThrow();

        assertEquals(StockMovementType.STOCK_IN, persistedMovement.getType());
        assertEquals(10, persistedMovement.getQuantity());
        assertEquals(productId, persistedMovement.getProduct().getProductId());
    }


    @Test
    void stockOut_shouldUpdateQuantityAndSaveMovement_whenValidRequest() {

        String movementId = "IT_INV_MOV_003";

        StockMovement result = inventoryService.stockOut(
                movementId,
                productId,
                10
        );

        // Verify returned movement
        assertNotNull(result);
        assertEquals(movementId, result.getMovementId());
        assertEquals(StockMovementType.STOCK_OUT, result.getType());
        assertEquals(10, result.getQuantity());

        // Verify persisted product quantity
        Product updatedProduct = productRepository.findById(productId)
                .orElseThrow();

        assertEquals(40, updatedProduct.getQuantity());

        // Verify persisted stock movement
        StockMovement persistedMovement =
                stockMovementRepository.findById(movementId)
                        .orElseThrow();

        assertEquals(StockMovementType.STOCK_OUT, persistedMovement.getType());
        assertEquals(10, persistedMovement.getQuantity());
        assertEquals(productId, persistedMovement.getProduct().getProductId());
    }

    @Test
    void stockOut_shouldAllowOnlyOneConcurrentWithdrawal_whenStockIsInsufficient()
            throws Exception {

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> first = executor.submit(
                    () -> attemptStockOut("IT_INV_CONC_MOV_001", ready, start)
            );

            Future<Boolean> second = executor.submit(
                    () -> attemptStockOut("IT_INV_CONC_MOV_002", ready, start)
            );

            // Wait until both threads are ready.
            assertTrue(ready.await(5, TimeUnit.SECONDS));

            // Release both threads simultaneously.
            start.countDown();

            boolean firstSucceeded = first.get(10, TimeUnit.SECONDS);
            boolean secondSucceeded = second.get(10, TimeUnit.SECONDS);

            // Exactly one operation must succeed.
            assertNotEquals(firstSucceeded, secondSucceeded);

            // Verify final database quantity.
            Product updatedProduct = productRepository.findById(productId)
                    .orElseThrow();

            assertEquals(20, updatedProduct.getQuantity());

            // Verify exactly one movement was persisted.
            long movementCount = stockMovementRepository
                    .findByProductId(productId)
                    .stream()
                    .filter(m -> m.getMovementId().startsWith("IT_INV_CONC_MOV_"))
                    .count();

            assertEquals(1, movementCount);

        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private boolean attemptStockOut(String movementId, CountDownLatch ready, CountDownLatch start) throws InterruptedException {

        ready.countDown();

        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Concurrent test start timed out");
        }

        try {
            inventoryService.stockOut(movementId, productId, 30);
            return true;

        } catch (InsufficientStockException e) {
            return false;
        }
    }

}
