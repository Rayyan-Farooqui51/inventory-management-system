package model;

import java.time.LocalDateTime;

public class StockMovement {
    private final String movementId;
    private final Product product;
    private final StockMovementType type;
    private final int quantity;
    private final LocalDateTime timestamp;

    public StockMovement(String movementId, Product product, StockMovementType type, int quantity, LocalDateTime timestamp){
        if (movementId == null || movementId.isBlank()){
            throw new IllegalArgumentException("Movement Id cannot be null or blank");
        }

        if (product == null){
            throw new IllegalArgumentException("Product cannot be null");
        }

        if (type == null){
            throw new IllegalArgumentException("Stock Movement Type Id cannot be null");
        }

        if (quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (timestamp == null){
            throw new IllegalArgumentException("Time stamp cannot be null");
        }

        this.movementId = movementId;
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public String getMovementId() {
        return movementId;
    }

    public Product getProduct() {
        return product;
    }

    public StockMovementType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "movementId='" + movementId + '\'' +
                ", productId='" + product.getProductId() + '\'' +
                ", type=" + type +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                '}';
    }
}
