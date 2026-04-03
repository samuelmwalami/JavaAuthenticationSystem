package services.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


/**
 * Class to get and verify JWT token using HS256
 * @author Sam
 * @ version 1.O
 *
 */
public class JWT {
    /**
     * Method to get JWT string
     * @param secret secret for the HMAC algorithm
     * @param payload the payload Object of the JWT token
     * @return the jwt string
     */

    public static String getJWT(String secret, JWTPayload payload) {
        String jwtToken = "";
        try {
            JWTHeader jwtHeaderObject = new JWTHeader("HS256", "JWT");
            ObjectMapper mapper = new ObjectMapper();
            // convert header and payload to Json String
            String jwtHeaderJSON = mapper.writeValueAsString(jwtHeaderObject);
            String jwtPayloadJSON = mapper.writeValueAsString(payload);

            //Convert header to base64
            String base64Header = Base64.getUrlEncoder().withoutPadding().encodeToString(jwtHeaderJSON.getBytes());
            String base64payload = Base64.getUrlEncoder().withoutPadding().encodeToString(jwtPayloadJSON.getBytes());

            //message for signing
            String message = String.format("%s.%s",base64Header,base64payload);

            // get hash from header and payload
            String base64Hash = Base64.getUrlEncoder().withoutPadding().encodeToString(getHMAC(secret,message));
            jwtToken = String.format("%s.%s",message,base64Hash);
        }
        catch (JsonProcessingException e){
            System.out.println(e);
        }

        return jwtToken;
    }

    /**
     * Verifies the integrity of the JWT
     * @param secret secret for HMAC algorithm
     * @param JWTToken jwt token to be checked
     * @return boolean of whether jwt is valid
     */
    public static boolean verifyJWT(String secret, String JWTToken){

        String[] jwtParts = JWTToken.split("\\.");
        String jwtMessage = String.format("%s.%s",jwtParts[0],jwtParts[1]);
        String jwtHash = jwtParts[2];
        // Encode the Jwt
        String computedHash = Base64.getUrlEncoder().withoutPadding().encodeToString(getHMAC(secret,jwtMessage));

        // compare received hash with computed hash to check for compromise in integrity
        return MessageDigest.isEqual(computedHash.getBytes(),jwtHash.getBytes());

    }

    /**
     * generates Hash-based MAC from message using HMAC256 algorithm
     * @param secret The secret to be used in the HMAC Algorithm
     * @param message The content to be encrypted
     *
     */
    public static byte[] getHMAC(String secret, String message){
        String algorithm = "HmacSHA256";
        byte[] hmac = new byte[0];
        try {
            if(!secret.isEmpty() && !message.isEmpty()) {
                // Get the Key
                SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), algorithm);
                Mac mac = Mac.getInstance(algorithm);
                mac.init(keySpec);
                mac.update(message.getBytes());
                hmac = mac.doFinal();
            }
        }
        catch (NoSuchAlgorithmException | InvalidKeyException e){
            System.out.println(e);
        }

        return hmac;

    }


    public static void main(String[] args) {
        String secret = "notsecure";
        JWTPayload jwtPayload = new JWTPayload("john","auth","1212","1414");
        String jwt = getJWT(secret,jwtPayload);
        System.out.println(jwt);
        System.out.println(String.format("JWT Integrity OK: %s",verifyJWT(secret,jwt)));

    }
}
