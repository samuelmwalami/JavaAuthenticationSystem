package modules.authentication.Domain;

import lib.jwt.Exceptions.IatGreaterThanExpException;
import lib.jwt.JWT;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;

public class AccessToken extends AuthenticationToken{
    @Getter
    @Setter
    String accessToken;

    public AccessToken(){
    }
    public AccessToken(String accessToken){
        this.accessToken = accessToken;
    }

    /**
     * Gets an access token
     * @return a string of an access token
     * @throws IatGreaterThanExpException
     */
    public String getAccessToken(String sub, HashMap<String,Object> claims){
        LocalDateTime expirationTime = LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(jwtConfigReader.getACCESS_TOKEN_EXPIRATION_DURATION());
        JWT jwtObject = new JWT.Builder().compact();
        try{
            jwtObject = new JWT.Builder()
                    .setSub(sub)
                    .setExp(expirationTime)
                    .setClaims(claims)
                    .setSecret(jwtConfigReader.getJWT_ACCESS_TOKEN_SECRET())
                    .compact();
        }
        catch (IatGreaterThanExpException e){
            e.printStackTrace();
        }

        return jwtObject.getJWT();
    }

    /**
     * Checks whether the integrity of the access token has been compromise
     * @param jwtToken the issued jwt token
     * @return a boolean value of  the JWT integrity status
     */
    public static boolean isAccessTokenCompromised(String jwtToken){
        JWT jwtObject = new JWT.Builder()
                .setSecret(jwtConfigReader.getJWT_ACCESS_TOKEN_SECRET())
                .compact();
        return !jwtObject.verifyJWT(jwtToken);
    }

    public boolean isAccessTokenProvided(){
        return super.isTokenProvided(this.accessToken);
    }

    public static int getAccessTokenExpiryDuration(){
        return (int) jwtConfigReader.getACCESS_TOKEN_EXPIRATION_DURATION();
    }
}
