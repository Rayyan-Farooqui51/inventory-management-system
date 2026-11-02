package repository.jdbc;

import database.DatabaseConnectionProvider;
import exception.ProductNotFoundException;
import model.Category;
import model.Product;
import model.Supplier;
import repository.ProductRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductRepository implements ProductRepository {
    private final DatabaseConnectionProvider databaseConnectionProvider;

    public JdbcProductRepository(DatabaseConnectionProvider databaseConnectionProvider){
        if (databaseConnectionProvider == null){
            throw new IllegalArgumentException("Database Connection Provider cannot be null");
        }

        this.databaseConnectionProvider = databaseConnectionProvider;
    }

    private Product mapRow(ResultSet resultSet) throws SQLException {
        Category category = new Category(resultSet.getString("category_id"), resultSet.getString("category_name"), resultSet.getString("category_description"));

        return new Product(resultSet.getString("product_id"), resultSet.getString("sku"), resultSet.getString("name"), category, resultSet.getBigDecimal("price"), resultSet.getInt("quantity"), resultSet.getInt("reorder_level"));
    }

    @Override
    public void save(Product product){
        if (product == null){
            throw new IllegalArgumentException("Product cannot be null");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();

                PreparedStatement productStatement = connection.prepareStatement("INSERT INTO products(product_id, sku, name, category_id,price, quantity, reorder_level) VALUES (?, ?, ?, ?, ?, ?, ?)");
                PreparedStatement productSupplierStatement = connection.prepareStatement("INSERT INTO product_suppliers (product_id, supplier_id) VALUES (?, ?)")
        ){
            try {
                connection.setAutoCommit(false);
                productStatement.setString(1,product.getProductId());
                productStatement.setString(2,product.getSku());
                productStatement.setString(3,product.getName());
                productStatement.setString(4,product.getCategory().getCategoryId());
                productStatement.setBigDecimal(5,product.getPrice());
                productStatement.setInt(6,product.getQuantity());
                productStatement.setInt(7,product.getReorderLevel());

                int rowsAffected1 = productStatement.executeUpdate();

                if (rowsAffected1 != 1){
                    throw new IllegalStateException( "Expected to insert one product, but inserted " + rowsAffected1);
                }

                int rowsAffected2;

                for(Supplier supplier : product.getSuppliers()){
                    productSupplierStatement.setString(1, product.getProductId());
                    productSupplierStatement.setString(2,supplier.getSupplierId());
                    rowsAffected2 = productSupplierStatement.executeUpdate();

                    if (rowsAffected2 != 1){
                        throw new IllegalStateException("Expected to insert one product-supplier association, but inserted " + rowsAffected2);
                    }
                }
                connection.commit();
            }
            catch (SQLException | RuntimeException e){
                try {
                    connection.rollback();
                }
                catch (SQLException rollbackException){
                    e.addSuppressed(rollbackException);
                }
                throw e;
            }
        }
        catch (SQLException e){
            throw new RuntimeException("Failed to save product",e);
        }
    }

    @Override
    public void update(Product product){
        if (product == null){
            throw new IllegalArgumentException("Product cannot be null");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE products SET name = ?, category_id = ?, price = ?,quantity = ?, reorder_level = ? WHERE product_id = ?");
                PreparedStatement productSupplierStatement = connection.prepareStatement("INSERT INTO product_suppliers (product_id, supplier_id) VALUES (?, ?)");
                PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM product_suppliers WHERE product_id = ?")
        ){
            try{

                connection.setAutoCommit(false);

                statement.setString(1,product.getName());
                statement.setString(2, product.getCategory().getCategoryId());
                statement.setBigDecimal(3, product.getPrice());
                statement.setInt(4, product.getQuantity());
                statement.setInt(5, product.getReorderLevel());
                statement.setString(6, product.getProductId());

                int rowsAffected1 = statement.executeUpdate();

                if (rowsAffected1 == 0){
                    if (!existsById(product.getProductId())){
                        throw new ProductNotFoundException("Product with Id " + product.getProductId() + " does not exits");
                    }
                }

                deleteStatement.setString(1, product.getProductId());
                deleteStatement.executeUpdate();

                int rowsAffected2;

                for(Supplier supplier : product.getSuppliers()){
                    productSupplierStatement.setString(1, product.getProductId());
                    productSupplierStatement.setString(2, supplier.getSupplierId());
                    rowsAffected2 = productSupplierStatement.executeUpdate();

                    if (rowsAffected2 != 1){
                        throw new IllegalStateException("Expected to insert one product-supplier association, but inserted " + rowsAffected2);
                    }
                }
                connection.commit();
            }
            catch (SQLException | RuntimeException e){
                try{
                    connection.rollback();
                }
                catch (SQLException rollBackException){
                    e.addSuppressed(rollBackException);
                }
                throw e;
            }
        }
        catch (SQLException e){
            throw new RuntimeException("Failed to update product", e);
        }
    }

    @Override
    public Optional<Product> findById(String productId){
        if (productId == null || productId.isBlank()){
            throw new IllegalArgumentException("Product Id cannot be null or blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();

                PreparedStatement statement = connection.prepareStatement("SELECT p.product_id, p.sku, p.name, p.price,p.quantity,p.reorder_level, c.category_id, c.name AS category_name, c.description AS category_description FROM products p JOIN categories c ON p.category_id = c.category_id WHERE p.product_id = ?");

                PreparedStatement supplierStatement = connection.prepareStatement("SELECT s.supplier_id, s.name, s.email, s.phone FROM product_suppliers ps JOIN suppliers s ON ps.supplier_id = s.supplier_id WHERE ps.product_id = ?")
        ) {
            statement.setString(1, productId);

            supplierStatement.setString(1, productId);

            Product product;

            try(ResultSet resultSet = statement.executeQuery()){
                if (!resultSet.next()){
                    return Optional.empty();
                }
                product = mapRow(resultSet);

            }

            try(ResultSet resultSet = supplierStatement.executeQuery()) {
                while (resultSet.next()){
                    Supplier supplier = new Supplier(resultSet.getString("supplier_id"), resultSet.getString("name"), resultSet.getString("email"), resultSet.getString("phone"));

                    product.addSupplier(supplier);
                }
            }
            return Optional.of(product);
        }
        catch (SQLException e){
            throw new RuntimeException("Failed to find product by ID", e);
        }
    }

    @Override
    public Optional<Product> findBySku(String sku){
        if (sku == null || sku.isBlank()){
            throw new IllegalArgumentException("Sku cannot be null or blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();

                PreparedStatement statement = connection.prepareStatement("SELECT p.product_id, p.sku, p.name, p.price, p.quantity, p.reorder_level, c.category_id, c.name AS category_name, c.description AS category_description FROM products p JOIN categories c ON p.category_id = c.category_id WHERE p.sku = ?");

                PreparedStatement supplierStatement = connection.prepareStatement("SELECT s.supplier_id, s.name, s.email, s.phone FROM product_suppliers ps JOIN suppliers s ON ps.supplier_id = s.supplier_id WHERE ps.product_id = ?")
        ) {
            statement.setString(1, sku);

            Product product;

            try(ResultSet resultSet = statement.executeQuery()){
                if (!resultSet.next()){
                    return Optional.empty();
                }
                product = mapRow(resultSet);

            }

            supplierStatement.setString(1, product.getProductId());

            try(ResultSet resultSet = supplierStatement.executeQuery()) {
                while (resultSet.next()){
                    Supplier supplier = new Supplier(resultSet.getString("supplier_id"), resultSet.getString("name"), resultSet.getString("email"), resultSet.getString("phone"));

                    product.addSupplier(supplier);
                }
            }
            return Optional.of(product);
        }
        catch (SQLException e){
            throw new RuntimeException("Failed to find product by SKU", e);
        }
    }

    @Override
    public List<Product> findAll(){
        List<Product> products = new ArrayList<>();

        try(
                Connection connection  = databaseConnectionProvider.getConnection();

                PreparedStatement statement = connection.prepareStatement("SELECT p.product_id, p.sku, p.name, p.price, p.quantity, p.reorder_level, c.category_id, c.name AS category_name, c.description AS category_description FROM products p JOIN categories c ON p.category_id = c.category_id");

                PreparedStatement supplierStatement = connection.prepareStatement("SELECT s.supplier_id, s.name, s.email, s.phone FROM product_suppliers ps JOIN suppliers s ON ps.supplier_id = s.supplier_id WHERE ps.product_id = ?")
        ){

            try(ResultSet resultSet = statement.executeQuery()){
                while (resultSet.next()){
                    products.add(mapRow(resultSet));
                }
            }


            for (Product product : products){
                supplierStatement.setString(1, product.getProductId());

                try(ResultSet resultSet = supplierStatement.executeQuery()) {
                    while (resultSet.next()){
                        Supplier supplier = new Supplier(resultSet.getString("supplier_id"), resultSet.getString("name"), resultSet.getString("email"), resultSet.getString("phone"));

                        product.addSupplier(supplier);
                    }
                }
            }

            return products;

        }
        catch (SQLException e){
            throw new RuntimeException("Failed to find products" ,e);
        }

    }

    @Override
    public boolean existsById(String productId){
        if (productId == null || productId.isBlank()){
            throw new IllegalArgumentException("Product Id cannot be null or blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM products WHERE product_id = ?")
        ) {
            statement.setString(1,productId);

            try(ResultSet resultSet = statement.executeQuery()){
                return resultSet.next();
            }

        }
        catch (SQLException e){
            throw new RuntimeException("Failed to check existence by ID", e);
        }

    }

    @Override
    public boolean existsBySku(String sku){
        if (sku == null || sku.isBlank()){
            throw new IllegalArgumentException("Sku cannot be null or blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM products WHERE sku = ?")
        ) {
            statement.setString(1,sku);

            try(ResultSet resultSet = statement.executeQuery()){
                return resultSet.next();
            }

        }
        catch (SQLException e){
            throw new RuntimeException("Failed to check existence by SKU", e);
        }

    }

}
