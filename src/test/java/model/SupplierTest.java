package model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class SupplierTest {

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = new Supplier("SUP001","ABC Supplies","abc@gmail.com","9876543210");
    }

    @Test
    void shouldCreateSupplierWithPhone() {
        Assertions.assertEquals("SUP001", supplier.getSupplierId());
        Assertions.assertEquals("ABC Supplies", supplier.getName());
        Assertions.assertEquals("abc@gmail.com", supplier.getEmail());
        Assertions.assertEquals(
                Optional.of("9876543210"),
                supplier.getPhone()
        );
    }

    @Test
    void shouldCreateSupplierWithoutPhone() {
        Supplier supplier =
                new Supplier("SUP002", "XYZ Supplies", "xyz@gmail.com");

        Assertions.assertEquals("SUP002", supplier.getSupplierId());
        Assertions.assertEquals("XYZ Supplies", supplier.getName());
        Assertions.assertEquals("xyz@gmail.com", supplier.getEmail());
        Assertions.assertTrue(supplier.getPhone().isEmpty());
    }

    @Test
    void shouldRejectNullSupplierId() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Supplier(null, "ABC", "abc@gmail.com")
        );
    }

    @Test
    void shouldRejectBlankSupplierId() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Supplier("   ", "ABC", "abc@gmail.com")
        );
    }

    @Test
    void shouldRejectNullName() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Supplier("SUP001", null, "abc@gmail.com")
        );
    }

    @Test
    void shouldRejectBlankName() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Supplier("SUP001", "   ", "abc@gmail.com")
        );
    }

    @Test
    void shouldRejectNullEmail() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Supplier("SUP001", "ABC", null)
        );
    }

    @Test
    void shouldRejectBlankEmail() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Supplier("SUP001", "ABC", "   ")
        );
    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Supplier("SUP001", "ABC", "invalid-email")
        );
    }

    @Test
    void shouldNormalizeNullPhone() {
        Supplier supplier =
                new Supplier("SUP002", "XYZ", "xyz@gmail.com", null);

        Assertions.assertTrue(supplier.getPhone().isEmpty());
    }

    @Test
    void shouldNormalizeBlankPhone() {
        Supplier supplier =
                new Supplier("SUP002", "XYZ", "xyz@gmail.com", "   ");

        Assertions.assertTrue(supplier.getPhone().isEmpty());
    }

    @Test
    void shouldRenameSupplier() {
        supplier.rename("ABC Distributors");

        Assertions.assertEquals("ABC Distributors", supplier.getName());
    }

    @Test
    void shouldRejectNullNameOnRename() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> supplier.rename(null)
        );

        Assertions.assertEquals("ABC Supplies", supplier.getName());
    }

    @Test
    void shouldRejectBlankNameOnRename() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> supplier.rename("   ")
        );

        Assertions.assertEquals("ABC Supplies", supplier.getName());
    }

    @Test
    void shouldChangeEmail() {
        supplier.changeEmail("new@gmail.com");

        Assertions.assertEquals("new@gmail.com", supplier.getEmail());
    }

    @Test
    void shouldRejectNullEmailChange() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> supplier.changeEmail(null)
        );

        Assertions.assertEquals("abc@gmail.com", supplier.getEmail());
    }

    @Test
    void shouldRejectBlankEmailChange() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> supplier.changeEmail("   ")
        );

        Assertions.assertEquals("abc@gmail.com", supplier.getEmail());
    }

    @Test
    void shouldRejectInvalidEmailChange() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> supplier.changeEmail("not-an-email")
        );

        Assertions.assertEquals("abc@gmail.com", supplier.getEmail());
    }

    @Test
    void shouldChangePhone() {
        supplier.changePhone("9999999999");

        Assertions.assertEquals(
                Optional.of("9999999999"),
                supplier.getPhone()
        );
    }

    @Test
    void shouldClearPhoneWhenNull() {
        supplier.changePhone(null);

        Assertions.assertTrue(supplier.getPhone().isEmpty());
    }

    @Test
    void shouldClearPhoneWhenBlank() {
        supplier.changePhone("   ");

        Assertions.assertTrue(supplier.getPhone().isEmpty());
    }

    @Test
    void shouldConsiderSuppliersWithSameIdEqual() {
        Supplier supplier1 =
                new Supplier("SUP001", "ABC", "abc@gmail.com");

        Supplier supplier2 =
                new Supplier("SUP001", "XYZ", "xyz@gmail.com");

        Assertions.assertEquals(supplier1, supplier2);
    }

    @Test
    void shouldGenerateSameHashCodeForSuppliersWithSameId() {
        Supplier supplier1 =
                new Supplier("SUP001", "ABC", "abc@gmail.com");

        Supplier supplier2 =
                new Supplier("SUP001", "XYZ", "xyz@gmail.com");

        Assertions.assertEquals(
                supplier1.hashCode(),
                supplier2.hashCode()
        );
    }

    @Test
    void shouldNotConsiderSuppliersWithDifferentIdsEqual() {
        Supplier supplier1 =
                new Supplier("SUP001", "ABC", "abc@gmail.com");

        Supplier supplier2 =
                new Supplier("SUP002", "ABC", "abc@gmail.com");

        Assertions.assertNotEquals(supplier1, supplier2);
    }
}