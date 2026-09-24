package modules.authentication.Domain;

import lib.jwt.Exceptions.IatGreaterThanExpException;
import lib.jwt.JWT;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

public class RefreshToken extends AuthenticationToken{
    @Getter
    @Setter
    UUID refreshTokenId;

    @Getter
    @Setter
    String refreshToken;

    public  RefreshToken(){}

    public RefreshToken(String refreshToken){
        this.refreshToken = refreshToken;
    }

    public RefreshToken(
            UUID refreshTokenId,
            String refreshToken
    ){
        this.refreshTokenId = refreshTokenId;
        this.refreshToken = refreshToken;
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

    /**
     * Checks whether the integrity of the JWT has been tampered with
     * @param jwtToken the issued jwt token
     * @return a boolean value of  the JWT integrity status
     */
    public static boolean isRefreshTokenCompromised(String jwtToken){
        JWT jwtObject = new JWT.Builder()
                .setSecret(jwtConfigReader.getJWT_REFRESH_TOKEN_SECRET())
                .compact();
        return !jwtObject.verifyJWT(jwtToken);

    }

    public boolean isRefreshTokenProvided(){
        return super.isTokenProvided(this.refreshToken);
    }

    public static int getRefreshTokenExpiryDuration(){
        return (int) jwtConfigReader.getREFRESH_TOKEN_EXPIRATION_DURATION();
    }
}
