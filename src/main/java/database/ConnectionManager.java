package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    public Connection getConnection() throws ClassNotFoundException {
        String db_url = System.getenv("DB_URL");
        String db_username = System.getenv("DB_USERNAME");
        String db_password = System.getenv("DB_PASSWORD");

        if (db_url == null || db_url.isBlank() || db_username == null || db_username.isBlank() || db_password == null || db_password.isBlank() ){
            throw new IllegalStateException("Database environment variables are missing or blank");
        }

        try {
                return DriverManager.getConnection(db_url, db_username, db_password);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to MySQL", e);
        }

    }
}

