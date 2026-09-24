package modules.authentication.Domain;

import lib.jwt.JWT;
import utils.ConfigReaders.JWTConfigReader;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;


/**
 * Class providing the JWS authentication infrastructure for the system
 * @author SaM
 * @version 1.0
 */
public class AuthenticationToken {
    protected static final JWTConfigReader jwtConfigReader = new JWTConfigReader();

    public AuthenticationToken(){

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
            long currentEpoch = LocalDateTime.now(ZoneId.of("UTC")).toEpochSecond(ZoneOffset.UTC);
            long jwtExpirationEpoch = ((Number) jwtObject.getPayloadClaim(jwtToken,"exp")).longValue();
            return currentEpoch > jwtExpirationEpoch;
        }



        protected boolean isTokenProvided(String token){
        return (token != null) && !token.isBlank();
        }






    }

