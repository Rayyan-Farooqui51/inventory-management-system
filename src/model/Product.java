package model;

import java.math.BigDecimal;

public class Product {
    private final String productId;
    private final String sku;

    private String name;
    private Category category;
    private BigDecimal price;
    private int quantity;
    private int reorderLevel;

    public Product(String productId, String sku, String name, Category category, BigDecimal price, int quantity, int reorderLevel){
        if (productId == null || productId.isBlank()){
            throw new IllegalArgumentException("Product Id cannot be null or blank");
        }

        if (sku == null || sku.isBlank()){
            throw new IllegalArgumentException("SKU cannot be null or blank");
        }

        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("Name cannot be null or blank");
        }

        if (category == null){
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (price == null){
            throw new IllegalArgumentException("Price cannot be null");
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Price cannot be negative or zero");
        }

        if (quantity < 0){
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        if (reorderLevel < 0){
            throw new IllegalArgumentException("Reorder Level cannot be negative");
        }

        this.productId = productId;
        this.sku =  sku;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public String getProductId() {
        return productId;
    }

    @Override
    public String toString() {

        return "Product{" +
                "productId='" + productId + '\'' +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category.getName() + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", reorderLevel=" + reorderLevel +
                '}';
    }
}