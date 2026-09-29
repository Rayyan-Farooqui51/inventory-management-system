package model;

import exception.InsufficientStockException;
import exception.SupplierAlreadyAssignedException;
import exception.SupplierNotAssignedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;


public class ProductTest {

    private Category category;
    private Product product;

    @BeforeEach
    void setUp(){
        category = new Category("CAT001", "Electronics");
        product = new Product("P1", "HP-LP-14-BL","Laptop", category, new BigDecimal("70000.00"),10,5);
    }

    @Test
    void shouldCreateProductWithValidData() {

        Assertions.assertEquals("P1",product.getProductId());
        Assertions.assertEquals("HP-LP-14-BL",product.getSku());
        Assertions.assertEquals("Laptop",product.getName());
        Assertions.assertEquals(category,product.getCategory());
        Assertions.assertEquals(new BigDecimal("70000.00"),product.getPrice());
        Assertions.assertEquals(10,product.getQuantity());
        Assertions.assertEquals(5,product.getReorderLevel());
    }

    @Test
    void shouldRejectZeroPrice(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> new Product("P1", "HP-LP-14-BL","Laptop", category, BigDecimal.ZERO,10,5)
        );

    }

    @Test
    void shouldRejectNegativePrice(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> new Product("P1", "HP-LP-14-BL","Laptop", category, new BigDecimal("-1000.00"),10,5)
        );
    }

    @Test
    void shouldRejectNegativeQuantity(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> new Product("P1", "HP-LP-14-BL","Laptop", category, new BigDecimal("70000.00"),-10,5)
        );
    }

    @Test
    void shouldRejectNegativeReorderLevel(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> new Product("P1", "HP-LP-14-BL","Laptop", category, new BigDecimal("70000.00"),10,-5)
        );
    }

    @Test
    void shouldRejectNullCategory(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> new Product("P1", "HP-LP-14-BL","Laptop", null, new BigDecimal("70000.00"),10,5)
        );
    }

    @Test
    void shouldIncreaseStock(){

        product.increaseStock(5);

        Assertions.assertEquals(15, product.getQuantity());

    }

    @Test
    void shouldRejectZeroStockIncrease(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> product.increaseStock(0)
        );

        Assertions.assertEquals(10, product.getQuantity());

    }

    @Test
    void shouldRejectNegativeStockIncrease(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> product.increaseStock(-10)
        );

        Assertions.assertEquals(10, product.getQuantity());
    }

    @Test
    void shouldDecreaseStock(){

        product.decreaseStock(5);

        Assertions.assertEquals(5, product.getQuantity());
    }


    @Test
    void shouldRejectStockOutWhenInsufficient(){

        Assertions.assertThrows(
                InsufficientStockException.class,
                () -> product.decreaseStock(15)
        );

        Assertions.assertEquals(10, product.getQuantity());
    }


    @Test
    void shouldRejectZeroStockDecrease(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> product.decreaseStock(0)
        );

        Assertions.assertEquals(10, product.getQuantity());
    }

    @Test
    void shouldRejectNegativeStockDecrease(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> product.decreaseStock(-10)
        );

        Assertions.assertEquals(10, product.getQuantity());
    }

    @Test
    void shouldAllowDecreasingEntireStock(){

       product.decreaseStock(10);

        Assertions.assertEquals(0, product.getQuantity());
    }

    @Test
    void shouldRenameProduct(){

        product.rename("HP EliteBook");

        Assertions.assertEquals("HP EliteBook", product.getName());

    }

    @Test
    void shouldRejectNullProductName(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> product.rename(null)
        );

        Assertions.assertEquals("Laptop", product.getName());
    }

    @Test
    void shouldRejectBlankProductName(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> product.rename(" ")
        );

        Assertions.assertEquals("Laptop", product.getName());
    }

    @Test
    void shouldChangePrice(){

        product.changePrice(new BigDecimal("65000.00"));

        Assertions.assertEquals(new BigDecimal("65000.00"), product.getPrice());

    }

    @Test
    void shouldRejectZeroPriceChange(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> product.changePrice(BigDecimal.ZERO)
        );

        Assertions.assertEquals(new BigDecimal("70000.00"), product.getPrice());
    }

    @Test
    void shouldRejectNegativePriceChange(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> product.changePrice(new BigDecimal("-70000.00"))
        );

        Assertions.assertEquals(new BigDecimal("70000.00"), product.getPrice());
    }

    @Test
    void shouldRejectNullPriceChange(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> product.changePrice(null)
        );

        Assertions.assertEquals(new BigDecimal("70000.00"), product.getPrice());
    }

    @Test
    void shouldChangeCategory(){

        Category category1 = new Category("CAT002","Laptops");

        product.changeCategory(category1);

        Assertions.assertEquals(category1, product.getCategory());

    }

    @Test
    void shouldRejectNullCategoryChange(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> product.changeCategory(null)
        );

        Assertions.assertEquals(category,product.getCategory());

    }

    @Test
    void shouldChangeReorderLevel(){

        product.changeReorderLevel(8);

        Assertions.assertEquals(8, product.getReorderLevel());
    }

    @Test
    void shouldRejectNegativeReorderLevelChange(){

        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> product.changeReorderLevel(-8)
        );

        Assertions.assertEquals(5, product.getReorderLevel());
    }

    @Test
    void shouldAddSupplier(){
        Supplier supplier = new Supplier("SUP001", "ABC", "abc@gmail.com");

        product.addSupplier(supplier);

        Assertions.assertEquals(1, product.getSuppliers().size());
        Assertions.assertTrue(product.getSuppliers().contains(supplier));

    }

    @Test
    void shouldRejectNullSupplier(){
        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> product.addSupplier(null)
        );

        Assertions.assertEquals(0, product.getSuppliers().size());
    }

    @Test
    void shouldRejectDuplicateSupplier(){
        Supplier supplier1 = new Supplier("SUP001", "ABC", "abc@gmail.com");

        Supplier supplier2 = new Supplier("SUP001", "XYZ", "xyz@gmail.com");

        product.addSupplier(supplier1);

        Assertions.assertThrows(
                SupplierAlreadyAssignedException.class,
                () -> product.addSupplier(supplier2)
        );

        Assertions.assertEquals(1, product.getSuppliers().size());
        Assertions.assertTrue(product.getSuppliers().contains(supplier1));

    }

    @Test
    void shouldRemoveSupplier(){
        Supplier supplier = new Supplier("SUP001", "ABC", "abc@gmail.com");

        product.addSupplier(supplier);
        product.removeSupplier(supplier);

        Assertions.assertTrue(product.getSuppliers().isEmpty());

    }

    @Test
    void shouldRejectRemovingUnassignedSupplier(){
        Supplier supplier = new Supplier("SUP001", "ABC", "abc@gmail.com");

        Assertions.assertThrows(
                SupplierNotAssignedException.class,
                () -> product.removeSupplier(supplier)
        );

    }

    @Test
    void shouldReturnUnmodifiableSupplierSet(){
        Supplier supplier = new Supplier("SUP001", "ABC", "abc@gmail.com");

        Supplier anotherSupplier =
                new Supplier("SUP002", "XYZ", "xyz@gmail.com");

        product.addSupplier(supplier);

        Set<Supplier> suppliers = product.getSuppliers();

        Assertions.assertThrows(
                UnsupportedOperationException.class,
                ()->suppliers.add(anotherSupplier)
        );

        Assertions.assertEquals(1, product.getSuppliers().size());
    }

    @Test
    void shouldRemoveSupplierUsingEquivalentSupplier(){
        Supplier supplier1 = new Supplier("SUP001", "ABC", "abc@gmail.com");

        Supplier supplier2 = new Supplier("SUP001", "XYZ", "xyz@gmail.com");

        product.addSupplier(supplier1);

        product.removeSupplier(supplier2);

        Assertions.assertTrue(product.getSuppliers().isEmpty());
    }
}