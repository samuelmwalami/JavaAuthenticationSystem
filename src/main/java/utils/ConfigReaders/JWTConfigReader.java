package utils.ConfigReaders;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JWTConfigReader{
    // JWT Configuration
    @Getter
    private String JWT_REFRESH_TOKEN_SECRET;
    @Getter
    private String JWT_ACCESS_TOKEN_SECRET;
    @Getter
    private long REFRESH_TOKEN_EXPIRATION_DURATION;
    @Getter
    private long ACCESS_TOKEN_EXPIRATION_DURATION;

    private final String CONFIG_FILE_PATH = "configs/jwtConfig.properties";


    public JWTConfigReader(){
        readPropertyFile();
    }

    private void readPropertyFile(){
        Properties property = new Properties();
        try(InputStream fis = DatabaseConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE_PATH)){
            property.load(fis);

            this.JWT_REFRESH_TOKEN_SECRET = property.getProperty("jwt.refreshTokenSecret");
            this.JWT_ACCESS_TOKEN_SECRET = property.getProperty("jwt.accessTokenSecret");
            this.REFRESH_TOKEN_EXPIRATION_DURATION = Long.getLong(property.getProperty("jwt.refreshTokenExpiryDuration"));
            this.ACCESS_TOKEN_EXPIRATION_DURATION = Long.getLong(property.getProperty("jwt.accessTokenExpiryDuration"));
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
