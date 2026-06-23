package utils;

import utils.ConfigReaders.DatabaseConfigReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final DatabaseConfigReader databaseConfigReader = new DatabaseConfigReader();

    static{
        try{
            Class.forName("org.postgresql.Driver");
        }
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public static Connection getDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(
                    databaseConfigReader.getDATABASE_URL(),
                    databaseConfigReader.getDATABASE_USER(),
                    databaseConfigReader.getDB_PASSWORD());

    }


}
