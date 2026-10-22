package repository.jdbc;

import database.DatabaseConnectionProvider;
import exception.SupplierNotFoundException;
import model.Supplier;
import repository.SupplierRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSupplierRepository implements SupplierRepository {

    private final DatabaseConnectionProvider databaseConnectionProvider;

    public JdbcSupplierRepository   (DatabaseConnectionProvider databaseConnectionProvider){
        if (databaseConnectionProvider == null){
            throw new IllegalArgumentException("Database connection provider cannot be null");
        }

        this.databaseConnectionProvider = databaseConnectionProvider;
    }

    @Override
    public void save(Supplier supplier){
        if (supplier == null){
            throw new IllegalArgumentException("Supplier cannot be null");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO suppliers (supplier_id, name, email, phone) VALUES (?, ?, ?, ?)");
        ) {

            statement.setString(1,supplier.getSupplierId());
            statement.setString(2, supplier.getName());
            statement.setString(3, supplier.getEmail());
            statement.setString(4, supplier.getPhone().orElse(null));

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected != 1){
                throw new IllegalStateException(
                        "Expected to insert one supplier, but inserted " + rowsAffected
                );
            }
        }
        catch (SQLException e){
            throw new RuntimeException("Failed to save supplier", e);
        }
    }

    @Override
    public void update(Supplier supplier){
        if (supplier == null){
            throw new IllegalArgumentException("Supplier cannot be null");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE suppliers SET name = ?, email = ?, phone = ? WHERE supplier_id = ?")
        ){
            statement.setString(1, supplier.getName());
            statement.setString(2, supplier.getEmail());
            statement.setString(3, supplier.getPhone().orElse(null));
            statement.setString(4, supplier.getSupplierId());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0){
                if (!existsById(supplier.getSupplierId())){
                    throw new SupplierNotFoundException("Supplier with ID " + supplier.getSupplierId() + " does not exist");
                }
            }
        }
        catch (SQLException e){
            throw new RuntimeException("Failed to update supplier", e);
        }
    }

    @Override
    public Optional<Supplier> findById(String supplierId){
        if (supplierId == null){
            throw new IllegalArgumentException("Supplier Id cannot be null");
        }

        if (supplierId.isBlank()){
            throw new IllegalArgumentException("Supplier Id cannot be blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT supplier_id, name, email, phone FROM suppliers WHERE supplier_id = ?")
        ) {
            statement.setString(1,supplierId);

            try(ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()){
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }

        }
        catch (SQLException e){
            throw new RuntimeException("Failed to find supplier",e);
        }
    }

    private Supplier mapRow(ResultSet resultSet) throws SQLException{
        return new Supplier(resultSet.getString("supplier_id"),resultSet.getString("name"), resultSet.getString("email"), resultSet.getString("phone"));
    }

    @Override
    public Optional<Supplier> findByEmail(String email){
        if (email == null){
            throw new IllegalArgumentException("Email cannot be null");
        }

        if (email.isBlank()){
            throw new IllegalArgumentException("Email cannot be blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT supplier_id, name, email, phone FROM suppliers WHERE email = ?")
        ){
            statement.setString(1,email);

            try(ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()){
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }

        }
        catch (SQLException e){
            throw new RuntimeException("Failed to find supplier", e);
        }
    }

    @Override
    public List<Supplier> findAll(){
        List<Supplier> suppliers = new ArrayList<>();

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT supplier_id, name, email, phone FROM suppliers")
        ) {
            try(ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()){
                    suppliers.add(mapRow(resultSet));
                }
                return suppliers;
            }

        }
        catch (SQLException e){
            throw new RuntimeException("Failed to find suppliers", e);
        }
    }

    @Override
    public boolean existsById(String supplierId){
        if (supplierId == null){
            throw new IllegalArgumentException("Supplier Id cannot be null");
        }

        if (supplierId.isBlank()){
            throw new IllegalArgumentException("Supplier Id cannot be blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM suppliers WHERE supplier_id = ?)")
        ) {
            statement.setString(1,supplierId);

            try(ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()){
                    return resultSet.getBoolean(1);
                }
                throw new IllegalStateException("Existence query returned no results");
            }
        }
        catch (SQLException e){
            throw new RuntimeException("Failed to check existence", e);
        }
    }

    @Override
    public boolean existsByEmail(String email){
        if (email == null){
            throw new IllegalArgumentException("Email cannot be null");
        }

        if (email.isBlank()){
            throw new IllegalArgumentException("Email cannot be blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM suppliers WHERE email = ?)")
        ) {
            statement.setString(1,email);

            try(ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()){
                    return resultSet.getBoolean(1);
                }
                throw new IllegalStateException("Existence query returned no results");
            }
        }
        catch (SQLException e){
            throw new RuntimeException("Failed to check existence", e);
        }

    }

}
