package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final ConfigFileReader configFileReader = new ConfigFileReader();

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
                    configFileReader.getDATABASE_URL(),
                    configFileReader.getDATABASE_USER(),
                    configFileReader.getDB_PASSWORD());

    }


}
