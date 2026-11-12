package service;

import database.TransactionCallback;
import database.TransactionManager;
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
import java.sql.Connection;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Connection connection;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp(){
        inventoryService = new InventoryService(productRepository, stockMovementRepository, transactionManager);
    }

    @Test
    void shouldRejectNullProductRepository(){
        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> new InventoryService(null,stockMovementRepository, transactionManager)
        );
    }

    @Test
    void shouldRejectNullStockMovementRepository(){
        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> new InventoryService(productRepository,null, transactionManager)
        );
    }

    @Test
    void shouldRejectNullTransactionManage(){
        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> new InventoryService(productRepository,stockMovementRepository, null)
        );
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundForStockIn() {
        mockTransactionExecution();

        when(productRepository.findByIdForUpdate(connection, "P1")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                () -> inventoryService.stockIn("M1", "P1", 10)
        );

        verify(productRepository).findByIdForUpdate(connection, "P1");
        verify(productRepository, never()).updateStockQuantity(eq(connection), any(Product.class));
        verify(stockMovementRepository, never()).save(eq(connection), any(StockMovement.class));
    }

    private void mockTransactionExecution() {
        when(transactionManager.executeInTransaction(any()))
                .thenAnswer(invocation -> {
                    TransactionCallback<?> callback = invocation.getArgument(0);
                    return callback.execute(connection);
                });
    }

    @Test
    void shouldStockInSuccessfully(){
        mockTransactionExecution();

        Category category = new Category("C1", "Electronics");

        Product product = new Product("P1","SKU-1","Keyboard",category,new BigDecimal("1000.00"),20,5);

        when(productRepository.findByIdForUpdate(connection, "P1")).thenReturn(Optional.of(product));

        StockMovement result = inventoryService.stockIn("M1", "P1", 10);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(StockMovementType.STOCK_IN, result.getType());
        Assertions.assertEquals(10, result.getQuantity());
        Assertions.assertEquals(30, product.getQuantity());

        verify(productRepository).findByIdForUpdate(connection, "P1");
        verify(productRepository).updateStockQuantity(connection, product);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);

        verify(stockMovementRepository).save(eq(connection), captor.capture());

        StockMovement savedMovement = captor.getValue();

        Assertions.assertEquals("M1", savedMovement.getMovementId());
        Assertions.assertEquals(product, savedMovement.getProduct());
        Assertions.assertEquals(StockMovementType.STOCK_IN, savedMovement.getType());
        Assertions.assertEquals(10, savedMovement.getQuantity());
        Assertions.assertNotNull(savedMovement.getTimestamp());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundForStockOut(){
        mockTransactionExecution();

        when(productRepository.findByIdForUpdate(connection, "P1")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                () -> inventoryService.stockOut("M1", "P1", 10)
        );

        verify(productRepository).findByIdForUpdate(connection,"P1");
        verify(productRepository, never()).updateStockQuantity(eq(connection), any(Product.class));
        verify(stockMovementRepository, never()).save(eq(connection), any(StockMovement.class));

    }

    @Test
    void shouldStockOutSuccessfully(){
        mockTransactionExecution();

        Category category = new Category("C1", "Electronics");

        Product product = new Product("P1","SKU-1","Keyboard",category,new BigDecimal("1000.00"),20,5);

        when(productRepository.findByIdForUpdate(connection, "P1")).thenReturn(Optional.of(product));

        StockMovement result = inventoryService.stockOut("M1", "P1", 10);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(StockMovementType.STOCK_OUT, result.getType());
        Assertions.assertEquals(10, result.getQuantity());
        Assertions.assertEquals(10, product.getQuantity());

        verify(productRepository).findByIdForUpdate(connection, "P1");
        verify(productRepository).updateStockQuantity(connection, product);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);

        verify(stockMovementRepository).save(eq(connection), captor.capture());

        StockMovement savedMovement = captor.getValue();

        Assertions.assertEquals("M1", savedMovement.getMovementId());
        Assertions.assertEquals(product, savedMovement.getProduct());
        Assertions.assertEquals(StockMovementType.STOCK_OUT, savedMovement.getType());
        Assertions.assertEquals(10, savedMovement.getQuantity());
        Assertions.assertNotNull(savedMovement.getTimestamp());
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient(){
        mockTransactionExecution();

        Category category = new Category("C1", "Electronics");

        Product product = new Product("P1","SKU-1","Keyboard",category,new BigDecimal("1000.00"),20,5);

        when(productRepository.findByIdForUpdate(connection, "P1")).thenReturn(Optional.of(product));

        Assertions.assertThrows(
                InsufficientStockException.class,
                ()->inventoryService.stockOut("M1","P1", 30)
        );

        verify(productRepository).findByIdForUpdate(connection, "P1");
        verify(productRepository, never()).updateStockQuantity(eq(connection), any(Product.class));
        verify(stockMovementRepository, never()).save(eq(connection), any(StockMovement.class));
    }

}
