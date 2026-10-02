package service;

import exception.*;
import model.Category;
import model.Product;
import model.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.CategoryRepository;
import repository.ProductRepository;
import repository.SupplierRepository;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private ProductService productService;

    @BeforeEach
    void setUp(){
        productService = new ProductService(productRepository, supplierRepository, categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenProductRepositoryIsNull() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ProductService(null,supplierRepository,categoryRepository)
        );
    }

    @Test
    void shouldThrowExceptionWhenSupplierRepositoryIsNull() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ProductService(productRepository,null,categoryRepository)
        );
    }

    @Test
    void shouldThrowExceptionWhenCategoryRepositoryIsNull() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ProductService(productRepository,supplierRepository,null)
        );
    }

    @Test
    void shouldFindProductById(){
        Category category = new Category("CAT001","Electronics");

        Product product = new Product("P1", "HP-LP-14-BL","Laptop", category, new BigDecimal("70000.00"),10,5);

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));

        Product result = productService.findById("P1");

        Assertions.assertEquals(product, result);

        verify(productRepository).findById("P1");

    }

    @Test
    void shouldFindProductBySku(){
        Category category = new Category("CAT001","Electronics");

        Product product = new Product("P1", "HP-LP-14-BL","Laptop", category, new BigDecimal("70000.00"),10,5);

        when(productRepository.findBySku("HP-LP-14-BL")).thenReturn(Optional.of(product));

        Product result = productService.findBySku("HP-LP-14-BL");

        Assertions.assertEquals(product, result);

        verify(productRepository).findBySku("HP-LP-14-BL");

    }

    @Test
    void shouldThrowExceptionWhenProductNotFound(){
        when(productRepository.findById("P999")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                () -> productService.findById("P999")
        );

        verify(productRepository).findById("P999");
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundBySku(){
        when(productRepository.findBySku("HP-LP-14-BL")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                () -> productService.findBySku("HP-LP-14-BL")
        );

        verify(productRepository).findBySku("HP-LP-14-BL");
    }

    @Test
    void shouldFindAllProducts(){
        Category category = new Category("CAT001","Electronics");

        Product product1 = new Product("P1", "HP-LP-14-BL","Laptop", category, new BigDecimal("70000.00"),10,5);

        Product product2 = new Product("P2", "HP-LP-15-BL","Laptop", category, new BigDecimal("70000.00"),10,5);

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        List<Product> result = productService.findAll();

        Assertions.assertEquals(List.of(product1, product2),result);

        verify(productRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsExist(){
        when(productRepository.findAll()).thenReturn(List.of());

        List<Product> result = productService.findAll();

        Assertions.assertTrue(result.isEmpty());

        verify(productRepository).findAll();
    }

    @Test
    void shouldCreateProduct(){
        Category category = new Category("CAT001","Electronics");

        when(productRepository.existsById("P1")).thenReturn(false);
        when(productRepository.existsBySku("HP-LP-14-BL")).thenReturn(false);
        when(categoryRepository.findById(category.getCategoryId())).thenReturn(Optional.of(category));

        Product result = productService.createProduct("P1", "HP-LP-14-BL","Laptop", category.getCategoryId(), new BigDecimal("70000.00"),10,5);

        Assertions.assertEquals("P1", result.getProductId());
        Assertions.assertEquals("HP-LP-14-BL", result.getSku());
        Assertions.assertEquals("Laptop", result.getName());
        Assertions.assertEquals(category, result.getCategory());

        verify(productRepository).existsById("P1");
        verify(productRepository).existsBySku("HP-LP-14-BL");
        verify(categoryRepository).findById("CAT001");
        verify(productRepository).save(result);

    }

    @Test
    void shouldThrowExceptionWhenDuplicateProductId(){
        Category category = new Category("CAT001","Electronics");


        when(productRepository.existsById("P1")).thenReturn(true);

        Assertions.assertThrows(
                DuplicateProductException.class,
                ()->productService.createProduct("P1", "HP-LP-14-BL","Laptop", category.getCategoryId(), new BigDecimal("70000.00"),10,5)
        );

        verify(productRepository).existsById("P1");
        verify(categoryRepository, never()).findById(category.getCategoryId());
        verify(productRepository, never()).save(any());

    }

    @Test
    void shouldThrowExceptionWhenDuplicateProductSku(){
        Category category = new Category("CAT001","Electronics");

        when(productRepository.existsById("P1")).thenReturn(false);
        when(productRepository.existsBySku("HP-LP-14-BL")).thenReturn(true);

        Assertions.assertThrows(
                DuplicateProductException.class,
                ()->productService.createProduct("P1", "HP-LP-14-BL","Laptop", category.getCategoryId(), new BigDecimal("70000.00"),10,5)
        );

        verify(productRepository).existsById("P1");
        verify(productRepository).existsBySku("HP-LP-14-BL");
        verify(categoryRepository, never()).findById(category.getCategoryId());
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound(){
        when(productRepository.existsById("P1")).thenReturn(false);
        when(productRepository.existsBySku("HP-LP-14-BL")).thenReturn(false);
        when(categoryRepository.findById("CAT001")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                CategoryNotFoundException.class,
                ()->productService.createProduct("P1", "HP-LP-14-BL","Laptop", "CAT001", new BigDecimal("70000.00"),10,5)
        );

        verify(categoryRepository).findById("CAT001");
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldAssignSupplierSuccessfully(){
        Category category = new Category("CAT001","Electronics");
        Product product = new Product("P1", "HP-LP-14-BL","Laptop", category, new BigDecimal("70000.00"),10,5);
        Supplier supplier = new Supplier("S1", "ABC Supplier", "abc@example.com");

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));

        when(supplierRepository.findById("S1")).thenReturn(Optional.of(supplier));

        productService.assignSupplier("P1", "S1");

        Assertions.assertTrue(product.getSuppliers().contains(supplier));

        verify(productRepository).findById("P1");
        verify(supplierRepository).findById("S1");
        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowExceptionWhenSupplierNotFoundDuringAssignment(){
        Category category = new Category("CAT001","Electronics");
        Product product = new Product("P1", "HP-LP-14-BL","Laptop", category, new BigDecimal("70000.00"),10,5);

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));

        when(supplierRepository.findById("S1")).thenReturn(Optional.empty());


        Assertions.assertThrows(
                SupplierNotFoundException.class,
                ()->productService.assignSupplier("P1", "S1")
        );

        verify(productRepository).findById("P1");
        verify(supplierRepository).findById("S1");
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldRemoveSupplierSuccessfully() {
        Category category = new Category("CAT001","Electronics");
        Product product = new Product("P1", "HP-LP-14-BL","Laptop", category, new BigDecimal("70000.00"),10,5);
        Supplier supplier = new Supplier("S1", "ABC Supplier", "abc@example.com");

        product.addSupplier(supplier);

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));

        when(supplierRepository.findById("S1")).thenReturn(Optional.of(supplier));

        productService.removeSupplier("P1", "S1");

        Assertions.assertFalse(product.getSuppliers().contains(supplier));

        verify(productRepository).findById("P1");
        verify(supplierRepository).findById("S1");
        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowExceptionWhenSupplierNotFoundDuringRemoval(){
        Category category = new Category("CAT001","Electronics");
        Product product = new Product("P1", "HP-LP-14-BL","Laptop", category, new BigDecimal("70000.00"),10,5);

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));

        when(supplierRepository.findById("S1")).thenReturn(Optional.empty());


        Assertions.assertThrows(
                SupplierNotFoundException.class,
                ()->productService.removeSupplier("P1", "S1")
        );

        verify(productRepository).findById("P1");
        verify(supplierRepository).findById("S1");
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenSupplierNotAssigned() {
        Category category = new Category("CAT001", "Electronics");
        Product product = new Product("P1", "HP-LP-14-BL", "Laptop", category, new BigDecimal("70000.00"), 10, 5);
        Supplier supplier = new Supplier("S1","ABC Supplier","abc@example.com");

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));
        when(supplierRepository.findById("S1")).thenReturn(Optional.of(supplier));

        Assertions.assertThrows(
                SupplierNotAssignedException.class,
                ()->productService.removeSupplier("P1","S1")
        );

        verify(productRepository).findById("P1");
        verify(supplierRepository).findById("S1");
        verify(productRepository, never()).save(any());

    }

    @Test
    void shouldRenameProduct(){
        Category category = new Category("CAT001", "Electronics");
        Product product = new Product("P1", "HP-LP-14-BL", "Laptop", category, new BigDecimal("70000.00"), 10, 5);

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));

        productService.renameProduct("P1","HP Laptop");

        Assertions.assertEquals("HP Laptop",product.getName());

        verify(productRepository).findById("P1");
        verify(productRepository).update(product);

    }


    @Test
    void shouldThrowProductNotFoundExceptionDuringProductRename(){
        when(productRepository.findById("P1")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                ()->productService.renameProduct("P1", "HP Laptop")
        );

        verify(productRepository).findById("P1");
        verify(productRepository, never()).update(any());
    }

    @Test
    void shouldChangePrice(){
        Category category = new Category("CAT001", "Electronics");
        Product product = new Product("P1", "HP-LP-14-BL", "Laptop", category, new BigDecimal("70000.00"), 10, 5);

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));

        productService.changePrice("P1",new BigDecimal("65000.00"));

        Assertions.assertEquals(new BigDecimal("65000.00"),product.getPrice());

        verify(productRepository).findById("P1");
        verify(productRepository).update(product);

    }

    @Test
    void shouldThrowProductNotFoundExceptionDuringChangePrice(){
        when(productRepository.findById("P1")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                ()->productService.changePrice("P1", new BigDecimal("65000.00"))
        );

        verify(productRepository).findById("P1");
        verify(productRepository, never()).update(any());
    }

    @Test
    void shouldChangeCategory(){
        Category category1 = new Category("CAT001", "Electronics");
        Product product = new Product("P1", "HP-LP-14-BL", "Laptop", category1, new BigDecimal("70000.00"), 10, 5);

        Category category2 = new Category("CAT002", "Laptops");

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));
        when(categoryRepository.findById("CAT002")).thenReturn(Optional.of(category2));

        productService.changeCategory("P1","CAT002");

        Assertions.assertEquals(category2, product.getCategory());

        verify(productRepository).findById("P1");
        verify(categoryRepository).findById("CAT002");
        verify(productRepository).update(product);
    }

    @Test
    void  shouldThrowCategoryNotFoundExceptionDuringCategoryChange(){
        Category category = new Category("CAT001", "Electronics");
        Product product = new Product("P1", "HP-LP-14-BL", "Laptop", category, new BigDecimal("70000.00"), 10, 5);

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));
        when(categoryRepository.findById("CAT002")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                CategoryNotFoundException.class,
                ()-> productService.changeCategory("P1", "CAT002")
        );

        Assertions.assertEquals(category, product.getCategory());

        verify(productRepository).findById("P1");
        verify(categoryRepository).findById("CAT002");
        verify(productRepository, never()).update(any());
    }

    @Test
    void shouldChangeReorderLevel(){
        Category category = new Category("CAT001", "Electronics");
        Product product = new Product("P1", "HP-LP-14-BL", "Laptop", category, new BigDecimal("70000.00"), 10, 5);

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));

        productService.changeReorderLevel("P1", 15);

        Assertions.assertEquals(15, product.getReorderLevel());

        verify(productRepository).findById("P1");
        verify(productRepository).update(product);
    }

    @Test
    void shouldThrowProductNotFoundExceptionDuringChangingReorderLevel(){
        when(productRepository.findById("P1")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                ()->productService.changeReorderLevel("P1",10)
        );

        verify(productRepository).findById("P1");
        verify(productRepository, never()).update(any());

    }

}
