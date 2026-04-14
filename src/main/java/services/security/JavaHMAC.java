package services.security;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class JavaHMAC {
    public static void getSHA256Hmac(String message, String secret){
        try{
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(),"HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] output = mac.doFinal(message.getBytes());

            StringBuilder hashBuffer = new StringBuilder();
            for(byte character : output){
                hashBuffer.append(String.format("%02x",character));
            }

            System.out.println(hashBuffer.toString());
            System.out.println(Base64.getEncoder().encodeToString(output));

        }
        catch(NoSuchAlgorithmException | InvalidKeyException e){
            System.out.println(e);

        }


    }

    public static void main(String [] args){
        getSHA256Hmac("Hello world", "password");
    }
}
