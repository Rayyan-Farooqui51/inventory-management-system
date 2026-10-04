package service;

import exception.InsufficientStockException;
import exception.ProductNotFoundException;
import model.Category;
import model.Product;
import model.StockMovement;
import model.StockMovementType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.ProductRepository;
import repository.StockMovementRepository;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp(){
        inventoryService = new InventoryService(productRepository, stockMovementRepository);
    }

    @Test
    void shouldRejectNullProductRepository(){
        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> new InventoryService(null,stockMovementRepository)
        );
    }

    @Test
    void shouldRejectNullStockMovementRepository(){
        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> new InventoryService(productRepository,null)
        );
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundForStockIn() {
        when(productRepository.findById("P1")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                () -> inventoryService.stockIn("M1", "P1", 10)
        );

        verify(productRepository).findById("P1");
        verify(productRepository, never()).update(any(Product.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void shouldStockInSuccessfully(){
        Category category = new Category("C1", "Electronics");

        Product product = new Product("P1","SKU-1","Keyboard",category,new BigDecimal("1000.00"),20,5);

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));

        StockMovement result = inventoryService.stockIn("M1", "P1", 10);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(StockMovementType.STOCK_IN, result.getType());
        Assertions.assertEquals(10, result.getQuantity());
        Assertions.assertEquals(30, product.getQuantity());

        verify(productRepository).findById("P1");
        verify(productRepository).update(product);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);

        verify(stockMovementRepository).save(captor.capture());

        StockMovement savedMovement = captor.getValue();

        Assertions.assertEquals("M1", savedMovement.getMovementId());
        Assertions.assertEquals(product, savedMovement.getProduct());
        Assertions.assertEquals(StockMovementType.STOCK_IN, savedMovement.getType());
        Assertions.assertEquals(10, savedMovement.getQuantity());
        Assertions.assertNotNull(savedMovement.getTimestamp());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundForStockOut(){
        when(productRepository.findById("P1")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                () -> inventoryService.stockOut("M1", "P1", 10)
        );

        verify(productRepository).findById("P1");
        verify(productRepository, never()).update(any(Product.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));

    }

    @Test
    void shouldStockOutSuccessfully(){
        Category category = new Category("C1", "Electronics");

        Product product = new Product("P1","SKU-1","Keyboard",category,new BigDecimal("1000.00"),20,5);

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));

        StockMovement result = inventoryService.stockOut("M1", "P1", 10);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(StockMovementType.STOCK_OUT, result.getType());
        Assertions.assertEquals(10, result.getQuantity());
        Assertions.assertEquals(10, product.getQuantity());

        verify(productRepository).findById("P1");
        verify(productRepository).update(product);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);

        verify(stockMovementRepository).save(captor.capture());

        StockMovement savedMovement = captor.getValue();

        Assertions.assertEquals("M1", savedMovement.getMovementId());
        Assertions.assertEquals(product, savedMovement.getProduct());
        Assertions.assertEquals(StockMovementType.STOCK_OUT, savedMovement.getType());
        Assertions.assertEquals(10, savedMovement.getQuantity());
        Assertions.assertNotNull(savedMovement.getTimestamp());
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient(){
        Category category = new Category("C1", "Electronics");

        Product product = new Product("P1","SKU-1","Keyboard",category,new BigDecimal("1000.00"),20,5);

        when(productRepository.findById("P1")).thenReturn(Optional.of(product));

        Assertions.assertThrows(
                InsufficientStockException.class,
                ()->inventoryService.stockOut("M1","P1", 30)
        );

        verify(productRepository).findById("P1");
        verify(productRepository, never()).update(any(Product.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }


}
