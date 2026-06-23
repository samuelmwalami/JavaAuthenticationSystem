package modules.mailing.Services;

import java.util.Properties;

import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import utils.ConfigReaders.MailingConfigReader;

public class MailingService {
    MailingConfigReader mailingConfigReader = new MailingConfigReader();


    public void sendMail(String to, String subject, String messageBody){
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.host", mailingConfigReader.getMailHost());
        properties.setProperty("mail.smtp.port", "587");

        String userName = mailingConfigReader.getMailUserName();
        String password = mailingConfigReader.getMailPassword();

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(userName, password);
            }
        };

        Session session = Session.getInstance(properties,authenticator);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailingConfigReader.getMailFrom()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);


            message.setContent(messageBody,"text/html");

            Transport.send(message);
        }
        catch(AddressException e){
            e.printStackTrace();
        }
        catch (MessagingException e){
            e.printStackTrace();
        }
    }

}
