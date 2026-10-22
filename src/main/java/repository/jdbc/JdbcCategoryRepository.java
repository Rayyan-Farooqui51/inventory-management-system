package repository.jdbc;

import database.DatabaseConnectionProvider;
import exception.CategoryNotFoundException;
import model.Category;
import repository.CategoryRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCategoryRepository implements CategoryRepository {

    private final DatabaseConnectionProvider databaseConnectionProvider;

    public JdbcCategoryRepository(DatabaseConnectionProvider databaseConnectionProvider){
        if (databaseConnectionProvider == null){
            throw new IllegalArgumentException("Database connection provider cannot be null");
        }

        this.databaseConnectionProvider = databaseConnectionProvider;
    }

    @Override
    public void save(Category category) {
        if (category == null){
            throw new IllegalArgumentException("Category cannot be null");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("Insert into categories (category_id, name, description) values (?, ?, ?)");
        ) {

            statement.setString(1,category.getCategoryId());
            statement.setString(2, category.getName());
            statement.setString(3, category.getDescription().orElse(null));

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected != 1){
                throw new IllegalStateException(
                        "Expected to insert one category, but inserted " + rowsAffected
                );
            }
        }
        catch (SQLException e){
            throw new RuntimeException("Failed to save category", e);
        }

    }

    @Override
    public Optional<Category> findById(String categoryId){
        if (categoryId == null){
            throw new IllegalArgumentException("Category Id cannot be null");
        }

        if (categoryId.isBlank()){
            throw new IllegalArgumentException("Category Id cannot be blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT category_id, name, description FROM categories WHERE category_id = ?")
        ) {
            statement.setString(1,categoryId);

            try(ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()){
                    return Optional.of(mapRow(resultSet));
                }

                return Optional.empty();
            }
        }
        catch (SQLException e){
            throw new RuntimeException("Failed to find category", e);
        }
    }

    private Category mapRow(ResultSet resultSet) throws SQLException{
        return new Category(resultSet.getString("category_id"), resultSet.getString("name"), resultSet.getString("description"));
    }

    @Override
    public void update(Category category){
        if (category == null){
            throw new IllegalArgumentException("Category cannot be null");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE categories SET name = ?, description = ? WHERE category_id = ?")
        ){

            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription().orElse(null));
            statement.setString(3, category.getCategoryId());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0){
                if (!existsById(category.getCategoryId())){
                    throw new CategoryNotFoundException("Category with ID " + category.getCategoryId() + " does not exist");
                }
            }
        }
        catch (SQLException e){
            throw new RuntimeException("Failed to update category ", e);
        }
    }

    @Override
    public Optional<Category> findByName(String name){
        if (name == null){
            throw new IllegalArgumentException("Name cannot be null");
        }

        if (name.isBlank()){
            throw new IllegalArgumentException("Name cannot be blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT category_id, name, description FROM categories WHERE name = ?")
        ){

            statement.setString(1, name);

            try(ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()){
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }

        }
        catch (SQLException e){
            throw new RuntimeException("Failed to find category", e);
        }
    }

    @Override
    public List<Category> findAll(){
        List<Category> categories = new ArrayList<>();
        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT category_id, name, description FROM categories")
        ) {
            try(ResultSet resultSet = statement.executeQuery()){
                while (resultSet.next()){
                    categories.add(mapRow(resultSet));
                }
                return categories;
            }

        }
        catch (SQLException e){
            throw new RuntimeException("Failed to get all categories", e);
        }
    }

    @Override
    public boolean existsById(String categoryId){
        if (categoryId == null){
            throw new IllegalArgumentException("Category Id cannot be null");
        }

        if (categoryId.isBlank()){
            throw new IllegalArgumentException("Category Id cannot be blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM categories WHERE category_id = ?)")
        ) {
            statement.setString(1, categoryId);

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
    public boolean existsByName(String name){
        if (name == null){
            throw new IllegalArgumentException("name cannot be null");
        }

        if (name.isBlank()){
            throw new IllegalArgumentException("name cannot be blank");
        }

        try(
                Connection connection = databaseConnectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM categories WHERE name = ?)")
        ) {
            statement.setString(1, name);

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