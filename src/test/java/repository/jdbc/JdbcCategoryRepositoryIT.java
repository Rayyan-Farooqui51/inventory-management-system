package repository.jdbc;

import exception.CategoryNotFoundException;
import model.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import database.TestConnectionManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcCategoryRepositoryIT {

    private TestConnectionManager testConnectionManager;
    private JdbcCategoryRepository jdbcCategoryRepository;

    private final String testCategoryId = "IT_CAT_001";

    @BeforeEach
    void setUp(){
        testConnectionManager = new TestConnectionManager();
        jdbcCategoryRepository = new JdbcCategoryRepository(testConnectionManager);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        try (Connection connection = testConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM categories WHERE category_id IN (?, ?)")) {

            statement.setString(1, testCategoryId);
            statement.setString(2, "IT_CAT_002");

            statement.executeUpdate();
        }
    }

    @Test
    void shouldSaveCategory(){
        Category category = new Category(
                testCategoryId,
                "Integration Test Category",
                "Test description"
        );

        jdbcCategoryRepository.save(category);

        Optional<Category> retrieveCategory = jdbcCategoryRepository.findById(testCategoryId);

        assertTrue(retrieveCategory.isPresent());

        Category savedCategory = retrieveCategory.orElseThrow();

        assertEquals(testCategoryId, savedCategory.getCategoryId());
        assertEquals("Integration Test Category", savedCategory.getName());
        assertEquals("Test description", savedCategory.getDescription().orElse(null));

    }

    @Test
    void shouldRejectDuplicateCategoryName() {
        Category first = new Category(
                testCategoryId,
                "Duplicate Test Category",
                "First category"
        );

        jdbcCategoryRepository.save(first);

        Category duplicate = new Category(
                "IT_CAT_002",
                "Duplicate Test Category",
                "Duplicate category"
        );

        assertThrows(
                RuntimeException.class,
                () -> jdbcCategoryRepository.save(duplicate)
        );
    }

    @Test
    void shouldRejectDuplicateCategoryId() {
        Category first = new Category(
                testCategoryId,
                "First Test Category",
                "First description"
        );

        jdbcCategoryRepository.save(first);

        Category duplicate = new Category(
                testCategoryId,
                "Second Test Category",
                "Second description"
        );

        assertThrows(
                RuntimeException.class,
                () -> jdbcCategoryRepository.save(duplicate)
        );
    }

    @Test
    void shouldReturnEmptyWhenCategoryDoesNotExist() {
        Optional<Category> result =
                jdbcCategoryRepository.findById("IT_CAT_MISSING");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnAllCategories() {
        Category category = new Category(
                testCategoryId,
                "Integration Test Category",
                "Test description"
        );

        jdbcCategoryRepository.save(category);

        List<Category> categories = jdbcCategoryRepository.findAll();

        assertTrue(categories.stream()
                .anyMatch(c -> c.getCategoryId().equals(testCategoryId)));
    }

    @Test
    void shouldReturnTrueWhenCategoryIdExists() {
        Category category = new Category(
                testCategoryId,
                "Integration Test Category",
                "Test description"
        );

        jdbcCategoryRepository.save(category);

        assertTrue(jdbcCategoryRepository.existsById(testCategoryId));
    }

    @Test
    void shouldReturnFalseWhenCategoryIdDoesNotExist() {
        assertFalse(jdbcCategoryRepository.existsById("IT_CAT_MISSING"));
    }

    @Test
    void shouldFindCategoryByName() {
        Category category = new Category(
                testCategoryId,
                "Integration Test Category",
                "Test description"
        );

        jdbcCategoryRepository.save(category);

        Optional<Category> result =
                jdbcCategoryRepository.findByName("Integration Test Category");

        assertTrue(result.isPresent());

        Category savedCategory = result.orElseThrow();
        assertEquals(testCategoryId, savedCategory.getCategoryId());
        assertEquals("Integration Test Category", savedCategory.getName());
    }

    @Test
    void shouldReturnEmptyWhenCategoryNameDoesNotExist() {
        Optional<Category> result =
                jdbcCategoryRepository.findByName("Missing Test Category");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenCategoryNameExists() {
        Category category = new Category(
                testCategoryId,
                "Integration Test Category",
                "Test description"
        );

        jdbcCategoryRepository.save(category);

        assertTrue(jdbcCategoryRepository.existsByName("Integration Test Category"));
    }

    @Test
    void shouldReturnFalseWhenCategoryNameDoesNotExist() {
        assertFalse(jdbcCategoryRepository.existsByName("Missing Test Category"));
    }

    @Test
    void shouldUpdateExistingCategory() {
        Category category = new Category(
                testCategoryId,
                "Original Category",
                "Original description"
        );

        jdbcCategoryRepository.save(category);

        Category updatedCategory = new Category(
                testCategoryId,
                "Updated Category",
                "Updated description"
        );

        jdbcCategoryRepository.update(updatedCategory);

        Category savedCategory = jdbcCategoryRepository
                .findById(testCategoryId)
                .orElseThrow();

        assertEquals("Updated Category", savedCategory.getName());
        assertEquals("Updated description",
                savedCategory.getDescription().orElse(null));
    }

    @Test
    void shouldThrowWhenUpdatingNonExistingCategory() {
        Category category = new Category(
                "IT_CAT_MISSING",
                "Missing Category",
                "Test description"
        );

        assertThrows(
                CategoryNotFoundException.class,
                () -> jdbcCategoryRepository.update(category)
        );
    }

}
