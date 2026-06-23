package utils.ConfigReaders;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfigReader{
    // Database Configuration
    @Getter
    private String DATABASE_URL;
    @Getter
    private String DATABASE_USER;
    @Getter
    private String DB_PASSWORD;

    private final String CONFIG_FILE_PATH = "configs/databaseConfig.properties";


    public DatabaseConfigReader(){
        readPropertyFile();
    }

    private void readPropertyFile(){
        Properties property = new Properties();
        try(InputStream fis = DatabaseConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE_PATH)){
            property.load(fis);
            this.DATABASE_URL  = property.getProperty("db.url");
            this.DATABASE_USER = property.getProperty("db.user");
            this.DB_PASSWORD = property.getProperty("db.password");

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
