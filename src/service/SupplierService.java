package service;

import exception.DuplicateSupplierException;
import exception.SupplierNotFoundException;
import model.Supplier;
import repository.SupplierRepository;

import java.util.List;

public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository){
        if (supplierRepository == null){
            throw new IllegalArgumentException("Supplier Repository cannot be null");
        }

        this.supplierRepository = supplierRepository;
    }

    public Supplier createSupplier(String supplierId, String name, String email, String phone){
        if (supplierRepository.existsById(supplierId)){
            throw new DuplicateSupplierException("Supplier with ID " + supplierId + " already exists");
        }

        if (supplierRepository.existsByEmail(email)){
            throw new DuplicateSupplierException("Supplier with email " + email + " already exists");
        }

        Supplier supplier = new Supplier(supplierId, name, email, phone);

        supplierRepository.save(supplier);

        return supplier;
    }

    public Supplier createSupplier(String supplierId, String name, String email){
        return createSupplier(supplierId, name, email, null);
    }

    public Supplier findById(String supplierId){
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier with ID " + supplierId + " does not exist"));
    }

    public Supplier findByEmail(String email){
        return supplierRepository.findByEmail(email)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier with email " + email + " does not exist"));
    }

    public List<Supplier> findAll(){
        return supplierRepository.findAll();
    }

}
