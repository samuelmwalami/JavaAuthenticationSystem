package services.security;

public class AuthenticationToken {
    int accessTokenExpirationDuration ;
    int refreshTokenExpirationDuration;

    public String getAccessToken(){
        return "";
    }
    public String getRefreshToken(){
        return "";
    }
    public boolean isTokenExpired(){
        return false;
    }
    public boolean isTokenIssuerValid(){
        return false;
    }
}
