package modules.authentication.Domain;

import lib.jwt.Exceptions.IatGreaterThanExpException;
import lib.jwt.JWT;
import lombok.Getter;
import lombok.Setter;
import utils.ConfigReaders.JWTConfigReader;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Class providing the JWS authentication infrastructure for the system
 * @author SaM
 * @version 1.0
 */
public class AuthenticationToken {
        @Getter
        @Setter
        UUID refreshTokenId;

        @Getter
        @Setter
        String refreshToken;

        @Getter
        @Setter
        String accessToken;


        private static final JWTConfigReader jwtConfigReader = new JWTConfigReader();

        public  AuthenticationToken(){}

        public AuthenticationToken(
                UUID refreshTokenId,
                String refreshToken,
                String accessToken
        ){
            this.refreshTokenId = refreshTokenId;
            this.refreshToken = refreshToken;
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
         * Gets a refresh token
         * @return a  string of the refresh token
         * @throws IatGreaterThanExpException Exception thrown when issued at time exceeds the expiration time
         */
        public String getRefreshToken(String sub, Map<String, Object> claims){
            LocalDateTime expirationTime = LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(jwtConfigReader.getREFRESH_TOKEN_EXPIRATION_DURATION());
            JWT jwtObject = new JWT.Builder().compact();
            try{
                jwtObject = new JWT.Builder()
                        .setSub(sub)
                        .setExp(expirationTime)
                        .setClaims(claims)
                        .setSecret(jwtConfigReader.getJWT_REFRESH_TOKEN_SECRET())
                        .compact();
            }
            catch (IatGreaterThanExpException e){
                e.printStackTrace();
            }

            return jwtObject.getJWT();
        }

        public static String getSub(String token){
           JWT jwtObject = new JWT.Builder().compact();
           return jwtObject.getPayloadClaim(token,"sub").toString();
        }

        /**
         * Shows whether JWT token has expired
         * @param jwtToken issued jwt token
         * @return a boolean value showing whether the jwt token has expired
         */
        public static boolean isTokenExpired(String jwtToken){
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
        public static boolean isRefreshTokenCompromised(String jwtToken){
            JWT jwtObject = new JWT.Builder()
                    .setSecret(jwtConfigReader.getJWT_REFRESH_TOKEN_SECRET())
                    .compact();
            return jwtObject.verifyJWT(jwtToken);

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
        return jwtObject.verifyJWT(jwtToken);
        }


        public boolean isAccessTokenProvided(){
            return isTokenProvided(this.accessToken);
        }

        public boolean isRefreshTokenProvided(){
        return isTokenProvided(this.refreshToken);
        }

        private boolean isTokenProvided(String token){
        return (token != null) && !token.isBlank();
        }

        public static int getAccessTokenExpiryDuration(){
            return (int) jwtConfigReader.getACCESS_TOKEN_EXPIRATION_DURATION();
        }

        public static int getRefreshTokenExpiryDuration(){
            return (int) jwtConfigReader.getACCESS_TOKEN_EXPIRATION_DURATION();
        }


    }

