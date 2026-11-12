package repository.jdbc;

import database.DatabaseConnectionProvider;
import model.Product;
import model.StockMovement;
import model.StockMovementType;
import repository.ProductRepository;
import repository.StockMovementRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcStockMovementRepository implements StockMovementRepository {

    private final DatabaseConnectionProvider databaseConnectionProvider;
    private final ProductRepository productRepository;

    public JdbcStockMovementRepository(DatabaseConnectionProvider databaseConnectionProvider, ProductRepository productRepository){
        if (databaseConnectionProvider == null){
            throw new IllegalArgumentException("Database Connection Provider cannot be null");
        }

        if (productRepository == null){
            throw new IllegalArgumentException("Product Repository cannot be null");
        }

        this.databaseConnectionProvider = databaseConnectionProvider;
        this.productRepository = productRepository;
    }

    @Override
    public void save(StockMovement stockMovement){

        if (stockMovement == null){
            throw new IllegalArgumentException("Stock Movement cannot be null");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO stock_movements(movement_id, product_id, type, quantity, timestamp) values(?,?,?,?,?)")
        ) {
            statement.setString(1,stockMovement.getMovementId());
            statement.setString(2,stockMovement.getProduct().getProductId());
            statement.setString(3,stockMovement.getType().name());
            statement.setInt(4,stockMovement.getQuantity());
            statement.setTimestamp(5,Timestamp.valueOf(stockMovement.getTimestamp()));

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected != 1){
                throw new IllegalStateException( "Expected to insert one stock movement, but inserted " + rowsAffected);
            }

        }
        catch (SQLException e){
            throw new RuntimeException("Failed to save stock movement", e);
        }

    }


    @Override
    public void save(Connection connection, StockMovement stockMovement){

        if (stockMovement == null){
            throw new IllegalArgumentException("Stock Movement cannot be null");
        }

        if (connection == null){
            throw new IllegalArgumentException("Connection cannot be null");
        }

        try(PreparedStatement statement = connection.prepareStatement("INSERT INTO stock_movements(movement_id, product_id, type, quantity, timestamp) values(?,?,?,?,?)")) {
            statement.setString(1,stockMovement.getMovementId());
            statement.setString(2,stockMovement.getProduct().getProductId());
            statement.setString(3,stockMovement.getType().name());
            statement.setInt(4,stockMovement.getQuantity());
            statement.setTimestamp(5,Timestamp.valueOf(stockMovement.getTimestamp()));

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected != 1){
                throw new IllegalStateException( "Expected to insert one stock movement, but inserted " + rowsAffected);
            }

        }
        catch (SQLException e){
            throw new RuntimeException("Failed to save stock movement", e);
        }

    }

    @Override
    public Optional<StockMovement> findById(String movementId){
        if (movementId == null || movementId.isBlank()){
            throw new IllegalArgumentException("Movement Id cannot be null or blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT movement_id, product_id, type, quantity, timestamp FROM stock_movements WHERE movement_id = ?")
        ){
            statement.setString(1,movementId);

            try(ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()){
                    Product product = productRepository.findById(resultSet.getString("product_id"))
                            .orElseThrow(() -> new IllegalStateException("Product associated with stock movement was not found"));

                    StockMovement stockMovement = new StockMovement(movementId, product, StockMovementType.valueOf(resultSet.getString("type")),resultSet.getInt("quantity"), resultSet.getTimestamp("timestamp").toLocalDateTime());

                    return Optional.of(stockMovement);
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find stock movement", e);
        }

    }

    @Override
    public List<StockMovement> findAll(){
        List<StockMovement> stockMovementList = new ArrayList<>();

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement  = connection.prepareStatement("SELECT movement_id, product_id, type, quantity, timestamp FROM stock_movements")
        ) {
            try(ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()){
                    Product product = productRepository.findById(resultSet.getString("product_id"))
                            .orElseThrow(() -> new IllegalStateException("Product associated with stock movement was not found"));

                    StockMovement stockMovement = new StockMovement(resultSet.getString("movement_id"), product, StockMovementType.valueOf(resultSet.getString("type")), resultSet.getInt("quantity"),resultSet.getTimestamp("timestamp").toLocalDateTime());

                    stockMovementList.add(stockMovement);
                }
                return stockMovementList;
            }
        }
        catch (SQLException e){
            throw new RuntimeException("Failed to find all stock movements", e);
        }
    }

    @Override
    public List<StockMovement> findByProductId(String productId){
        if (productId == null || productId.isBlank()){
            throw new IllegalArgumentException("product Id cannot be null or blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT movement_id, product_id, type, quantity, timestamp FROM stock_movements WHERE product_id = ?")
        ) {
            List<StockMovement> stockMovementList = new ArrayList<>();

            statement.setString(1,productId);

            try(ResultSet resultSet = statement.executeQuery()){
                if (!resultSet.next()){
                    return stockMovementList;
                }
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalStateException("Product associated with stock movement was not found"));

                do{

                    StockMovement stockMovement = new StockMovement(resultSet.getString("movement_id"), product, StockMovementType.valueOf(resultSet.getString("type")), resultSet.getInt("quantity"),resultSet.getTimestamp("timestamp").toLocalDateTime());

                    stockMovementList.add(stockMovement);
                }while (resultSet.next());
                return stockMovementList;
            }

        }
        catch (SQLException e){
            throw new RuntimeException("Failed to find stock movements", e);
        }
    }

    @Override
    public List<StockMovement> findByProductIdAndType(String productId, StockMovementType type){
        if (productId == null || productId.isBlank()){
            throw new IllegalArgumentException("product Id cannot be null or blank");
        }

        if (type == null){
            throw new IllegalArgumentException("Stock Movement Type cannot be null");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT movement_id, product_id, type, quantity, timestamp FROM stock_movements WHERE product_id = ? AND type = ?")
        ) {
            List<StockMovement> stockMovementList = new ArrayList<>();

            statement.setString(1, productId);
            statement.setString(2, type.name());

            try(ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()){
                    return stockMovementList;
                }

                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalStateException("Product associated with stock movement was not found"));

                do{
                    StockMovement stockMovement = new StockMovement(resultSet.getString("movement_id"), product, StockMovementType.valueOf(resultSet.getString("type")), resultSet.getInt("quantity"),resultSet.getTimestamp("timestamp").toLocalDateTime());

                    stockMovementList.add(stockMovement);

                }while (resultSet.next());

                return stockMovementList;
            }

        }
        catch (SQLException e){
            throw new RuntimeException("Failed to find stock movements", e);
        }
    }
}
