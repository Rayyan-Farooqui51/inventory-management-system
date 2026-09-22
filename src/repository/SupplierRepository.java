package repository;

import model.Supplier;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository {

    void save(Supplier supplier);

    Optional<Supplier> findById(String supplierId);

    Optional<Supplier> findByEmail(String email);

    List<Supplier> findAll();

    boolean existsById(String supplierId);

    boolean existsByEmail(String email);
}
