package services.storage;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "";
    private static final String USER = "";
    private static final String PASSWORD = "";
    private static Connection databaseConnection= null;
    private DatabaseConnection(){
    }

    static {
     try{
         Class.forName("");
     }
     catch(ClassNotFoundException e){
         e.printStackTrace();
     }
    }

    public static Connection getDatabaseConnection() throws SQLException {
        if(databaseConnection== null){
            databaseConnection = DriverManager.getConnection(URL, USER,PASSWORD);
    }
        return databaseConnection;
    }


}
