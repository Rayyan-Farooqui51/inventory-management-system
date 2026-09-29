package model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class CategoryTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category("CAT001","Electronics","Electronic products");
    }

    @Test
    void shouldCreateCategoryWithDescription() {
        Assertions.assertEquals("CAT001", category.getCategoryId());
        Assertions.assertEquals("Electronics", category.getName());
        Assertions.assertEquals(
                Optional.of("Electronic products"),
                category.getDescription()
        );
    }

    @Test
    void shouldCreateCategoryWithoutDescription() {
        Category category = new Category("CAT002", "Furniture");

        Assertions.assertEquals("CAT002", category.getCategoryId());
        Assertions.assertEquals("Furniture", category.getName());
        Assertions.assertTrue(category.getDescription().isEmpty());
    }

    @Test
    void shouldRejectNullCategoryId() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Category(null, "Electronics")
        );
    }

    @Test
    void shouldRejectBlankCategoryId() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Category("   ", "Electronics")
        );
    }

    @Test
    void shouldRejectNullName() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Category("CAT001", null)
        );
    }

    @Test
    void shouldRejectBlankName() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Category("CAT001", "   ")
        );
    }

    @Test
    void shouldNormalizeNullDescription() {
        Category category =
                new Category("CAT002", "Furniture", null);

        Assertions.assertTrue(category.getDescription().isEmpty());
    }

    @Test
    void shouldNormalizeBlankDescription() {
        Category category =
                new Category("CAT002", "Furniture", "   ");

        Assertions.assertTrue(category.getDescription().isEmpty());
    }

    @Test
    void shouldRenameCategory() {
        category.rename("Consumer Electronics");

        Assertions.assertEquals(
                "Consumer Electronics",
                category.getName()
        );
    }

    @Test
    void shouldRejectNullNameOnRename() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> category.rename(null)
        );

        Assertions.assertEquals("Electronics", category.getName());
    }

    @Test
    void shouldRejectBlankNameOnRename() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> category.rename("   ")
        );

        Assertions.assertEquals("Electronics", category.getName());
    }

    @Test
    void shouldChangeDescription() {
        category.changeDescription("Devices and accessories");

        Assertions.assertEquals(
                Optional.of("Devices and accessories"),
                category.getDescription()
        );
    }

    @Test
    void shouldClearDescriptionWhenNull() {
        category.changeDescription(null);

        Assertions.assertTrue(category.getDescription().isEmpty());
    }

    @Test
    void shouldClearDescriptionWhenBlank() {
        category.changeDescription("   ");

        Assertions.assertTrue(category.getDescription().isEmpty());
    }
}