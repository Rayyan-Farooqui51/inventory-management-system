package repository.jdbc;

import database.TestConnectionManager;
import model.Category;
import model.Product;
import model.StockMovement;
import model.StockMovementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcStockMovementRepositoryIT {

    private final TestConnectionManager connectionProvider =
            new TestConnectionManager();

    private JdbcStockMovementRepository stockMovementRepository;
    private JdbcProductRepository productRepository;

    private final String testCategoryId = "IT_SM_CAT_001";
    private final String testProductId = "IT_SM_PROD_001";
    private final String testMovementId = "IT_SM_001";

    private Product testProduct;

    @BeforeEach
    void setUp() {

        productRepository = new JdbcProductRepository(connectionProvider);

        stockMovementRepository = new JdbcStockMovementRepository(
                connectionProvider,
                productRepository
        );

        Category category = new Category(
                testCategoryId,
                "IT Stock Movement Category",
                "Category for stock movement testing"
        );

        JdbcCategoryRepository categoryRepository =
                new JdbcCategoryRepository(connectionProvider);

        categoryRepository.save(category);

        testProduct = new Product(
                testProductId,
                "IT_SM_SKU_001",
                "IT Stock Movement Product",
                category,
                new BigDecimal("100.00"),
                50,
                10
        );

        productRepository.save(testProduct);
    }

    @AfterEach
    void cleanUp() throws Exception {

        String[] movementIds = {
                "IT_SM_001",
                "IT_SM_002",
                "IT_SM_003",
                "IT_SM_004",
                "IT_SM_005",
                "IT_SM_006"
        };

        try (Connection connection = connectionProvider.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM stock_movements WHERE movement_id = ?")) {

                for (String movementId : movementIds) {
                    statement.setString(1, movementId);
                    statement.executeUpdate();
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM products WHERE product_id = ?")) {

                statement.setString(1, testProductId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM categories WHERE category_id = ?")) {

                statement.setString(1, testCategoryId);
                statement.executeUpdate();
            }
        }
    }

    @Test
    void save_shouldPersistStockMovement_whenValidMovementProvided()
            throws Exception {

        LocalDateTime timestamp = LocalDateTime.now().withNano(0);

        StockMovement stockMovement = new StockMovement(
                testMovementId,
                testProduct,
                StockMovementType.values()[0],
                10,
                timestamp
        );

        stockMovementRepository.save(stockMovement);

        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT movement_id, product_id, type, quantity, timestamp " +
                             "FROM stock_movements WHERE movement_id = ?")) {

            statement.setString(1, testMovementId);

            try (ResultSet resultSet = statement.executeQuery()) {

                assertTrue(resultSet.next());

                assertEquals(testMovementId,
                        resultSet.getString("movement_id"));

                assertEquals(testProductId,
                        resultSet.getString("product_id"));

                assertEquals(StockMovementType.values()[0].name(),
                        resultSet.getString("type"));

                assertEquals(10, resultSet.getInt("quantity"));

                assertEquals(
                        timestamp,
                        resultSet.getTimestamp("timestamp").toLocalDateTime()
                );
            }
        }
    }

    @Test
    void findById_shouldReturnStockMovement_whenMovementExists() {

        StockMovement stockMovement = new StockMovement(
                testMovementId,
                testProduct,
                StockMovementType.values()[0],
                15,
                LocalDateTime.now()
        );

        stockMovementRepository.save(stockMovement);

        Optional<StockMovement> result =
                stockMovementRepository.findById(testMovementId);

        assertTrue(result.isPresent());

        assertEquals(testMovementId,
                result.get().getMovementId());

        assertEquals(testProductId,
                result.get().getProduct().getProductId());

        assertEquals(15, result.get().getQuantity());
    }

    @Test
    void findById_shouldReturnEmpty_whenMovementDoesNotExist() {

        Optional<StockMovement> result =
                stockMovementRepository.findById("IT_NON_EXISTENT");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByProductId_shouldReturnAllMovements_whenMovementsExist() {

        StockMovement movement1 = new StockMovement(
                "IT_SM_002",
                testProduct,
                StockMovementType.values()[0],
                10,
                LocalDateTime.now().withNano(0)
        );

        StockMovement movement2 = new StockMovement(
                "IT_SM_003",
                testProduct,
                StockMovementType.values()[0],
                20,
                LocalDateTime.now().withNano(0)
        );

        stockMovementRepository.save(movement1);
        stockMovementRepository.save(movement2);

        var result = stockMovementRepository.findByProductId(testProductId);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(
                movement -> movement.getProduct().getProductId().equals(testProductId)
        ));
    }

    @Test
    void findByProductId_shouldReturnEmptyList_whenNoMovementsExist() {

        var result = stockMovementRepository.findByProductId(testProductId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByProductIdAndType_shouldReturnMatchingMovements_whenMatchesExist() {

        StockMovementType type = StockMovementType.values()[0];

        StockMovement movement1 = new StockMovement(
                "IT_SM_004", testProduct, type, 10,
                LocalDateTime.now().withNano(0)
        );

        stockMovementRepository.save(movement1);

        var result = stockMovementRepository.findByProductIdAndType(
                testProductId, type
        );

        assertEquals(1, result.size());
        assertEquals(type, result.get(0).getType());
    }

    @Test
    void findByProductIdAndType_shouldReturnEmptyList_whenNoMatchesExist() {

        StockMovementType type = StockMovementType.values()[0];

        var result = stockMovementRepository.findByProductIdAndType(
                testProductId, type
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllMovements_whenMovementsExist() {

        StockMovement movement1 = new StockMovement(
                "IT_SM_005", testProduct,
                StockMovementType.values()[0], 10,
                LocalDateTime.now().withNano(0)
        );

        StockMovement movement2 = new StockMovement(
                "IT_SM_006", testProduct,
                StockMovementType.values()[0], 20,
                LocalDateTime.now().withNano(0)
        );

        stockMovementRepository.save(movement1);
        stockMovementRepository.save(movement2);

        var result = stockMovementRepository.findAll();

        assertTrue(result.size() >= 2);

        assertTrue(result.stream().anyMatch(
                movement -> movement.getMovementId().equals("IT_SM_005")
        ));

        assertTrue(result.stream().anyMatch(
                movement -> movement.getMovementId().equals("IT_SM_006")
        ));
    }

    @Test
    void constructor_shouldThrowException_whenConnectionProviderIsNull() {

        assertThrows(IllegalArgumentException.class,
                () -> new JdbcStockMovementRepository(null, productRepository));
    }

    @Test
    void constructor_shouldThrowException_whenProductRepositoryIsNull() {

        assertThrows(IllegalArgumentException.class,
                () -> new JdbcStockMovementRepository(connectionProvider, null));
    }

    @Test
    void save_shouldThrowException_whenStockMovementIsNull() {

        assertThrows(IllegalArgumentException.class,
                () -> stockMovementRepository.save(null));
    }

    @Test
    void findById_shouldThrowException_whenMovementIdIsNull() {

        assertThrows(IllegalArgumentException.class,
                () -> stockMovementRepository.findById(null));
    }

    @Test
    void findById_shouldThrowException_whenMovementIdIsBlank() {

        assertThrows(IllegalArgumentException.class,
                () -> stockMovementRepository.findById(" "));
    }

    @Test
    void findByProductId_shouldThrowException_whenProductIdIsNull() {

        assertThrows(IllegalArgumentException.class,
                () -> stockMovementRepository.findByProductId(null));
    }

    @Test
    void findByProductId_shouldThrowException_whenProductIdIsBlank() {

        assertThrows(IllegalArgumentException.class,
                () -> stockMovementRepository.findByProductId(" "));
    }

    @Test
    void findByProductIdAndType_shouldThrowException_whenProductIdIsNull() {

        assertThrows(IllegalArgumentException.class,
                () -> stockMovementRepository.findByProductIdAndType(
                        null, StockMovementType.values()[0]));
    }

    @Test
    void findByProductIdAndType_shouldThrowException_whenTypeIsNull() {

        assertThrows(IllegalArgumentException.class,
                () -> stockMovementRepository.findByProductIdAndType(
                        testProductId, null));
    }
}