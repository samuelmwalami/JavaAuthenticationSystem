package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionUtil {
    private Connection connection = null;
    static{
        try{
            Class.forName("org.postgresql.Driver");
        }
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public Connection getDatabaseConnection() throws SQLException {
        if (connection != null){
            return connection;
        }
        Properties props  = new Properties();
        try(FileInputStream fis = new FileInputStream("config.properties")){
            props.load(fis);
            connection =  DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.password"));
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return connection;
    }


}
