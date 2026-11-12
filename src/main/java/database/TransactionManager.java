package database;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {
    private final DatabaseConnectionProvider databaseConnectionProvider;

    public TransactionManager(DatabaseConnectionProvider databaseConnectionProvider){
        if (databaseConnectionProvider == null){
            throw new IllegalArgumentException("Database Connection Provider cannot be null");
        }

        this.databaseConnectionProvider = databaseConnectionProvider;
    }

    public <T> T executeInTransaction(TransactionCallback<T> callback){
        if (callback == null){
            throw new IllegalArgumentException("callback cannot be null");
        }

        try(Connection connection = databaseConnectionProvider.getConnection();){

            connection.setAutoCommit(false);

            T obj;

            try{
                obj = callback.execute(connection);
                connection.commit();
            }
            catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;
            }
            return obj;
        }
        catch (SQLException e){
            throw new RuntimeException("Transaction Failed", e);
        }
    }
}
