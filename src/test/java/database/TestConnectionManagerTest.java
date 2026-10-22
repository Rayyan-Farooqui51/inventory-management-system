package database;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class TestConnectionManagerTest {

    @Test
    void shouldConnectToTestDatabase() throws SQLException{
        TestConnectionManager connectionManager = new TestConnectionManager();
        try(Connection connection = connectionManager.getConnection();) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());

            try(
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT DATABASE()");
            ){
                assertTrue(resultSet.next(), "Database query should return a row");
                assertEquals("inventory_management_test", resultSet.getString(1));

            }
        }
    }

}