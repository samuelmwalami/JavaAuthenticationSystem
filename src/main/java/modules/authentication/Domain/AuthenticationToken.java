package modules.authentication.Domain;

import lib.security.Exceptions.IatGreaterThanExpException;
import lib.security.JWT;
import lombok.Getter;
import lombok.Setter;

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
        UUID tokenId;

        @Getter
        @Setter
        String refreshToken;

        @Getter
        @Setter
        String accessToken;
        private String SECRET;
        private final long ACCESS_TOKEN_EXPIRATION_DURATION = 60*15;
        private final long REFRESH_TOKEN_EXPIRATION_DURATION = 60*60*24*7;

        /**
         * Gets an access token
         * @return a string of an access token
         * @throws IatGreaterThanExpException
         */
        public  String getAccessToken(String sub, HashMap<String,Object> claims){
            LocalDateTime expirationTime = LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(ACCESS_TOKEN_EXPIRATION_DURATION);
            JWT jwtObject = new JWT.Builder().compact();
            try{
                jwtObject = new JWT.Builder()
                        .setSub(sub)
                        .setExp(expirationTime)
                        .setClaims(claims)
                        .setSecret(SECRET)
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
            LocalDateTime expirationTime = LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(REFRESH_TOKEN_EXPIRATION_DURATION);
            JWT jwtObject = new JWT.Builder().compact();
            try{
                jwtObject = new JWT.Builder()
                        .setSub(sub)
                        .setExp(expirationTime)
                        .setClaims(claims)
                        .setSecret(SECRET)
                        .compact();
            }
            catch (IatGreaterThanExpException e){
                e.printStackTrace();
            }

            return jwtObject.getJWT();
        }

        public String getSub(String token){
           JWT jwtObject = new JWT.Builder().compact();
           return jwtObject.getPayloadClaim(token,"sub").toString();
        }

        /**
         * Shows whether JWT token has expired
         * @param jwtToken issued jwt token
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

        public int getAccessTokenExpiryDuration(){
            return (int) this.ACCESS_TOKEN_EXPIRATION_DURATION;
        }

        public int getRefreshTokenExpiryDuration(){
            return (int) this.REFRESH_TOKEN_EXPIRATION_DURATION;
        }

    }

