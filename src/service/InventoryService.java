package service;

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

    public InventoryService(ProductRepository productRepository, StockMovementRepository stockMovementRepository){
        if (productRepository == null){
            throw new IllegalArgumentException("Product Repository cannot be null");
        }

        if (stockMovementRepository == null){
            throw new IllegalArgumentException("Stock Movement Repository cannot be null");
        }

        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public StockMovement stockIn(String movementId, String productId, int amount){
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ProductNotFoundException("Product with ID " + productId + " does not exist"));

        product.increaseStock(amount);

        productRepository.save(product);

        StockMovement stockMovement = new StockMovement(movementId, product, StockMovementType.STOCK_IN, amount, LocalDateTime.now());

        stockMovementRepository.save(stockMovement);

        return stockMovement;

    }

    public StockMovement stockOut(String movementId, String productId, int amount){
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ProductNotFoundException("Product with ID " + productId + " does not exist"));

        product.decreaseStock(amount);

        productRepository.save(product);

        StockMovement stockMovement = new StockMovement(movementId, product, StockMovementType.STOCK_OUT, amount, LocalDateTime.now());

        stockMovementRepository.save(stockMovement);

        return stockMovement;

    }
}