package service;

import database.TransactionManager;
import exception.ProductNotFoundException;
import model.Product;
import model.StockMovement;
import model.StockMovementType;
import repository.ProductRepository;
import repository.StockMovementRepository;

import java.time.LocalDateTime;

public class InventoryService {
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final TransactionManager transactionManager;

    public InventoryService(ProductRepository productRepository, StockMovementRepository stockMovementRepository, TransactionManager transactionManager){
        if (productRepository == null){
            throw new IllegalArgumentException("Product Repository cannot be null");
        }

        if (stockMovementRepository == null){
            throw new IllegalArgumentException("Stock Movement Repository cannot be null");
        }

        if (transactionManager == null){
            throw new IllegalArgumentException("Transaction Manager cannot be null");
        }

        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.transactionManager = transactionManager;
    }

    public StockMovement stockIn(String movementId, String productId, int amount){
        return transactionManager.executeInTransaction(connection -> {
            Product product = productRepository.findByIdForUpdate(connection, productId)
                    .orElseThrow(()-> new ProductNotFoundException("Product with ID " + productId + " does not exist"));

            product.increaseStock(amount);

            productRepository.updateStockQuantity(connection,product);

            StockMovement stockMovement = new StockMovement(movementId, product, StockMovementType.STOCK_IN, amount, LocalDateTime.now());

            stockMovementRepository.save(connection,stockMovement);

            return stockMovement;
        });

    }

    public StockMovement stockOut(String movementId, String productId, int amount){
        return transactionManager.executeInTransaction(connection -> {
            Product product = productRepository.findByIdForUpdate(connection, productId)
                    .orElseThrow(()-> new ProductNotFoundException("Product with ID " + productId + " does not exist"));

            product.decreaseStock(amount);

            productRepository.updateStockQuantity(connection, product);

            StockMovement stockMovement = new StockMovement(movementId, product, StockMovementType.STOCK_OUT, amount, LocalDateTime.now());

            stockMovementRepository.save(connection, stockMovement);

            return stockMovement;
        });

    }
}