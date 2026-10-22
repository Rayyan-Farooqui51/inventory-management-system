package repository.jdbc;

import model.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import database.TestConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcCategoryRepositoryIT {

    private TestConnectionManager testConnectionManager;
    private JdbcCategoryRepository jdbcCategoryRepository;

    private final String testCategoryId = "IT_CAT_001";

    @BeforeEach
    void setUp(){
        testConnectionManager = new TestConnectionManager();
        jdbcCategoryRepository = new JdbcCategoryRepository(testConnectionManager);
    }

    @AfterEach
    void cleanUp() throws SQLException {

        try(
                Connection connection = testConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM categories WHERE category_id = ?")
        ){

            statement.setString(1,testCategoryId);

            statement.executeUpdate();
        }

    }

}
