package repository.jdbc;

import database.TestConnectionManager;
import exception.SupplierNotFoundException;
import model.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcSupplierRepositoryIT {

    private TestConnectionManager testConnectionManager;
    private JdbcSupplierRepository jdbcSupplierRepository;

    private final String supplierId = "IT_SUP_001";
    private final String email = "supplier001@example.com";

    private final String secondSupplierId = "IT_SUP_002";
    private final String secondEmail = "supplier002@example.com";

    @BeforeEach
    void setUp(){
        testConnectionManager = new TestConnectionManager();
        jdbcSupplierRepository = new JdbcSupplierRepository(testConnectionManager);
    }

    @AfterEach
    void cleanUp() throws SQLException{
        try(
                Connection connection = testConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM suppliers WHERE supplier_id IN (?, ?) OR email IN (?, ?)")
        ){
            statement.setString(1,supplierId);
            statement.setString(2,secondSupplierId);

            statement.setString(3,email);
            statement.setString(4,secondEmail);

            statement.executeUpdate();
        }
    }

    @Test
    void shouldSaveSupplier(){
        Supplier supplier = new Supplier(supplierId,"Smart Source",email,"9876543210");

        jdbcSupplierRepository.save(supplier);

        Optional<Supplier> retrieveSupplier = jdbcSupplierRepository.findById(supplierId);

        assertTrue(retrieveSupplier.isPresent());

        Supplier savedSupplier = retrieveSupplier.orElseThrow();

        assertEquals(supplierId, savedSupplier.getSupplierId());
        assertEquals("Smart Source", savedSupplier.getName());
        assertEquals(email, savedSupplier.getEmail());
        assertEquals("9876543210", savedSupplier.getPhone().orElse(null));
    }

    @Test
    void shouldReturnEmptyWhenSupplierIdDoesNotExist(){
        Optional<Supplier> result = jdbcSupplierRepository.findById("IT_SUP_MISSING");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveSupplierWithNullPhone(){
        Supplier supplier = new Supplier(supplierId,"Smart Source",email);

        jdbcSupplierRepository.save(supplier);

        Optional<Supplier> retrieveSupplier = jdbcSupplierRepository.findById(supplierId);

        assertTrue(retrieveSupplier.isPresent());

        Supplier savedSupplier = retrieveSupplier.orElseThrow();

        assertEquals(supplierId, savedSupplier.getSupplierId());
        assertEquals("Smart Source", savedSupplier.getName());
        assertEquals(email, savedSupplier.getEmail());
        assertTrue(savedSupplier.getPhone().isEmpty());
    }

    @Test
    void shouldFindSupplierByEmail(){
        Supplier supplier = new Supplier(supplierId,"Smart Source",email,"9876543210");

        jdbcSupplierRepository.save(supplier);

        Optional<Supplier> retrieveSupplier = jdbcSupplierRepository.findByEmail(email);

        assertTrue(retrieveSupplier.isPresent());

        Supplier savedSupplier = retrieveSupplier.orElseThrow();

        assertEquals(supplierId, savedSupplier.getSupplierId());
        assertEquals("Smart Source", savedSupplier.getName());
        assertEquals(email, savedSupplier.getEmail());
        assertEquals("9876543210", savedSupplier.getPhone().orElse(null));
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist(){
        Optional<Supplier> result = jdbcSupplierRepository.findByEmail("supplier003@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindAllSuppliers(){
        Supplier supplier1 = new Supplier(supplierId,"Smart Source",email,"9876543210");
        Supplier supplier2 = new Supplier(secondSupplierId,"Smart Source",secondEmail,"9876543210");

        jdbcSupplierRepository.save(supplier1);
        jdbcSupplierRepository.save(supplier2);

        List<Supplier> suppliers = jdbcSupplierRepository.findAll();

        assertTrue(suppliers.stream()
                .anyMatch(supplier -> supplier.getSupplierId().equals(supplierId)));

        assertTrue(suppliers.stream()
                .anyMatch(supplier -> supplier.getSupplierId().equals(secondSupplierId)));
    }

    @Test
    void shouldReturnTrueWhenSupplierIdExists(){
        Supplier supplier = new Supplier(supplierId,"Smart Source",email,"9876543210");

        jdbcSupplierRepository.save(supplier);

        assertTrue(jdbcSupplierRepository.existsById(supplierId));
    }

    @Test
    void shouldReturnFalseWhenSupplierIdDoesNotExist(){
        assertFalse(jdbcSupplierRepository.existsById("IT_SUP_MISSING"));
    }

    @Test
    void shouldReturnTrueWhenSupplierEmailExists(){
        Supplier supplier = new Supplier(supplierId,"Smart Source",email,"9876543210");

        jdbcSupplierRepository.save(supplier);

        assertTrue(jdbcSupplierRepository.existsByEmail(email));
    }

    @Test
    void shouldReturnFalseWhenSupplierEmailDoesNotExist(){
        assertFalse(jdbcSupplierRepository.existsByEmail("supplier003@example.com"));
    }

    @Test
    void shouldUpdateExistingSupplier(){
        Supplier supplier = new Supplier(supplierId,"Smart Source",email,"9876543210");

        Supplier updateSupplier = new Supplier(supplierId,"Smart-Source",secondEmail,"9876543123");

        jdbcSupplierRepository.save(supplier);

        jdbcSupplierRepository.update(updateSupplier);

        Optional<Supplier> retreiveSuppleir = jdbcSupplierRepository.findById(supplierId);

        assertTrue(retreiveSuppleir.isPresent());

        Supplier savedSupplier = retreiveSuppleir.orElseThrow();

        assertEquals(secondEmail, savedSupplier.getEmail());
        assertEquals("Smart-Source", savedSupplier.getName());
        assertEquals("9876543123", savedSupplier.getPhone().orElse(null));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentSupplier(){
        Supplier supplier = new Supplier(supplierId,"Smart Source",email,"9876543210");

        assertThrows(
                SupplierNotFoundException.class,
                ()-> jdbcSupplierRepository.update(supplier)
        );

    }

    @Test
    void shouldReturnEmptyListWhenNoSuppliersExist(){
        List<Supplier> suppliers = jdbcSupplierRepository.findAll();

        assertTrue(suppliers.isEmpty());
    }

}
