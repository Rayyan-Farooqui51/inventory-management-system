package database;

import java.sql.Connection;

public interface DatabaseConnectionProvider {
    Connection getConnection();
}
