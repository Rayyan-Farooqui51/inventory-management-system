package repository;

import model.StockMovement;
import model.StockMovementType;

import java.util.List;
import java.util.Optional;

public interface StockMovementRepository {

    void save(StockMovement stockMovement);

    Optional<StockMovement> findById(String movementId);

    List<StockMovement> findAll();

    List<StockMovement> findByProductId(String productId);

    List<StockMovement> findByProductIdAndType(String productId, StockMovementType type);
}
