package utils.ConfigReaders;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MailingConfigReader{
    @Getter
    private String mailUserName;
    @Getter
    private String mailPassword;
    @Getter
    private String mailHost;
    @Getter
    String mailFrom;

    private final String CONFIG_FILE_PATH = "configs/mailingConfig.properties";


    public MailingConfigReader()
    {
        readPropertyFile();
    }
    private void readPropertyFile(){
        Properties property = new Properties();
        try(InputStream fis = MailingConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE_PATH)){
            property.load(fis);

            this.mailUserName = property.getProperty("mailing.userName");
            this.mailPassword = property.getProperty("mailing.password");
            this.mailHost = property.getProperty("mailing.host");
            this.mailFrom = property.getProperty("mailing.from");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
