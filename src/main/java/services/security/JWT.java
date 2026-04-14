package services.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;


/**
 * Class to get and verify JWT token using HS256
 * @author Sam
 * @version 1.O
 *
 */
public class JWT {
    private final JWTHeader jwtHeader;
    private final Map<Object,Object> jwtPayload;
    private String secret;


    JWT(Builder builder){
        this.jwtHeader = builder.jwtHeader;
        this.jwtPayload = builder.jwtPayload;
        this.secret = builder.jwtSecret;
    }


    /**
     * Method to get JWT string
     * @return the jwt string
     */
    public String getJWT() {
        String jwtToken = "";
        try {
            // Configure Header
            jwtHeader.setAlg("HS256");
            jwtHeader.setTyp("JWT");

            ObjectMapper mapper = new ObjectMapper();
            // convert header and payload to Json String
            String jwtHeaderJSON = mapper.writeValueAsString(jwtHeader);
            String jwtPayloadJSON = mapper.writeValueAsString(jwtPayload);

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
     * @param JWTToken jwt token to be checked
     * @return boolean of whether jwt is valid
     */
    public boolean verifyJWT(String JWTToken){

        String[] jwtParts = JWTToken.split("\\.");
        String jwtMessage = String.format("%s.%s",jwtParts[0],jwtParts[1]);
        String jwtHash = jwtParts[2];
        // Encode the Jwt
        String computedHash = Base64.getUrlEncoder().withoutPadding().encodeToString(getHMAC(secret,jwtMessage));

        // compare received hash with computed hash to check for compromise in integrity
        return MessageDigest.isEqual(computedHash.getBytes(),jwtHash.getBytes());

    }

    /**
     * Gets claim from payload
     * @param JWTToken Token Issued to the user
     * @param claim The claim to be searched for in the payload
     * @return The claim to found if found or an empty string
     */
    public Object getPayloadClaim(String JWTToken, String claim) {
        Object jwtClaim = "";
        try{
            // extract claim from the jwt
            String[] jwtParts  = JWTToken.split("\\.");
            String base64jwtPayload = Arrays.toString(Base64.getDecoder().decode(jwtParts[1]));
            ObjectMapper mapper = new ObjectMapper();
            TreeMap<Object,Object> claims  = mapper.readValue(base64jwtPayload, new TypeReference<TreeMap<Object, Object>>() {});
            if(!claims.containsKey(claim)){
                return jwtClaim;
            }

            jwtClaim = claims.get(claim);
        }
        catch(JsonProcessingException e){
            System.out.println(e);
        }
        return jwtClaim;

    }

    /**
     * generates Hash-based MAC from message using HMAC256 algorithm
     * @param secret The secret to be used in the HMAC Algorithm
     * @param message The content to be encrypted
     */
    public byte[] getHMAC(String secret, String message){
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

    /**
     * Model class for the JWT header
     */
    private static class JWTHeader {
        private String alg = "HS256";
        private String typ = "JWT";

        public String getAlg(){
            return this.alg;
        }
        public String getTyp(){
            return this.typ;
        }

        public void setAlg(String alg){
            this.alg = alg;
        }
        public void setTyp(String typ){
            this.typ = typ;
        }
    }


    // Builder class for JWT
    public static class Builder {
        private final JWTHeader jwtHeader = new JWTHeader();
        private final Map<Object, Object> jwtPayload = new TreeMap<>();
        private String jwtSecret;

        public Builder setSub(String sub){
            jwtPayload.put("sub",sub);
            return this;
        }
        public Builder setIat(LocalDateTime iat){
            jwtPayload.put("iat",iat.toEpochSecond(ZoneOffset.UTC));
            return this;
        }
        public Builder setExp(LocalDateTime exp) throws IatGreaterThanExpException {
            long expEpoch = exp.toEpochSecond(ZoneOffset.UTC);
            if(jwtPayload.containsKey("iat")){
                if(!isExpAfterIat((long)jwtPayload.get("iat"),expEpoch)){
                    throw new IatGreaterThanExpException();
                }

            };

            jwtPayload.put("exp",exp.toEpochSecond(ZoneOffset.UTC));
            return this;
        }

        public Builder setClaim(Object key, Object value){
            jwtPayload.put(key,value);
            return this;
        }

        public Builder setSecret(String secret){
            jwtSecret = secret;
            return this;
        }

        public Builder setHMACAlgorithm(String algorithm){
            jwtHeader.setAlg(algorithm);
            return this;
        }

        private boolean isExpAfterIat(long iatEpoch, long expEpoch){
            if(expEpoch < 0 || iatEpoch < 0){
                return false;
            }
            return expEpoch > iatEpoch;
        }

        public JWT compact() {
            return new JWT(this);
        }
    }

}
