package services.authentication;

import services.security.IatGreaterThanExpException;
import services.security.JWT;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Class providing the JWS authentication infrastructure for the system
 * @author SaM
 * @version 1.0
 */
public class AuthenticationService {
    private String SECRET;
    private final long ACCESS_TOKEN_EXPIRATION_DURATION = 60*30;
    private final long REFRESH_TOKEN_EXPIRATION_DURATION = 60*60*24*7;

    /**
     * Gets an access token
     * @return a string of an access token
     * @throws IatGreaterThanExpException
     */
    public  String getAccessToken() throws IatGreaterThanExpException {
        LocalDateTime expirationTime = LocalDateTime.now(ZoneId.of("UTC"));
        LocalDateTime issuedAtTime  = LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(ACCESS_TOKEN_EXPIRATION_DURATION);
        JWT jwtObject = new JWT.Builder()
                .setSub("JohnDoe")
                .setIat(issuedAtTime)
                .setExp(expirationTime)
                .setSecret(SECRET)
                .compact();
        return jwtObject.getJWT();
    }

    /**
     * Gets a refresh token
     * @return a  string of the refresh token
     * @throws IatGreaterThanExpException Exception thrown when issued at time exceeds the expiration time
     */
    public String getRefreshToken() throws IatGreaterThanExpException{
        LocalDateTime expirationTime = LocalDateTime.now(ZoneId.of("UTC"));
        LocalDateTime issuedAtTime = LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(REFRESH_TOKEN_EXPIRATION_DURATION);
        JWT jwtObject  = new JWT.Builder()
                    .setSub("JohnDoe")
                    .setIat(issuedAtTime)
                    .setExp(expirationTime)
                    .setSecret(SECRET)
                    .compact();

        return jwtObject.getJWT();
    }

    /**
     * Shows whether JWT token has expired
     * @param jwtToken the issued jwt token
     * @return a boolean value showing whether the jwt token has expired
     */
    public boolean isTokenExpired(String jwtToken){
        JWT jwtObject  = new JWT.Builder().compact();
        long currentEpoch = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long jwtExpirationEpoch = (long) jwtObject.getPayloadClaim(jwtToken,"exp");
        return currentEpoch > jwtExpirationEpoch;
    }

    /**
     * Checks whether the integrity of the JWT has been tampered with
     * @param jwtToken the issued jwt token
     * @return a boolean value of  the JWT integrity status
     */
    public boolean isTokenValid(String jwtToken){
        JWT jwtObject = new JWT.Builder()
                .setSecret(SECRET)
                .compact();
        return jwtObject.verifyJWT(jwtToken);

    }
}
