package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnectionManager implements DatabaseConnectionProvider {

    @Override
    public Connection getConnection() {
        String dbUrl = System.getenv("TEST_DB_URL");
        String dbUsername = System.getenv("TEST_DB_USERNAME");
        String dbPassword = System.getenv("TEST_DB_PASSWORD");

        if (dbUrl == null || dbUrl.isBlank() || dbUsername == null || dbUsername.isBlank() || dbPassword == null || dbPassword.isBlank() ){
            throw new IllegalStateException("Database environment variables are missing or blank");
        }

        if (!dbUrl.startsWith("jdbc:mysql://")) {
            throw new IllegalStateException("Test database URL must use MySQL");
        }

        String[] urlParts = dbUrl.split("\\?", 2);
        String urlWithoutParams = urlParts[0];

        int databaseSeparator = urlWithoutParams.lastIndexOf('/');

        if (databaseSeparator == -1) {
            throw new IllegalStateException("Invalid test database URL");
        }

        String databaseName = urlWithoutParams.substring(databaseSeparator + 1);

        if (!"inventory_management_test".equals(databaseName)) {
            throw new IllegalStateException(
                    "Test connection must target inventory_management_test"
            );
        }

        try {
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to MySQL", e);
        }

    }
}
