package utils;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ConfigFileReader {
    // Database Configuration
    @Getter
    private String DATABASE_URL;
    @Getter
    private String DATABASE_USER;
    @Getter
    private String DB_PASSWORD;

    // JWT Configuration
    @Getter
    private String JWT_REFRESH_TOKEN_SECRET;
    @Getter
    private String JWT_ACCESS_TOKEN_SECRET;

    private final String CONFIG_FILE_PATH = "configs/config.properties";

    private final Properties property = new Properties();

    public ConfigFileReader(){
        readPropertyFile();
    }

    private void readPropertyFile(){
        try(InputStream fis = ConfigFileReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE_PATH)){
            property.load(fis);
            this.DATABASE_URL  = property.getProperty("db.url");
            this.DATABASE_USER = property.getProperty("db.user");
            this.DB_PASSWORD = property.getProperty("db.password");

            this.JWT_REFRESH_TOKEN_SECRET = property.getProperty("jwt.refreshTokenSecret");
            this.JWT_ACCESS_TOKEN_SECRET = property.getProperty("jwt.accessTokenSecret");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}
