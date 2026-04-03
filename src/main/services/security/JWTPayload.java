package services.security;

/**
 *  class for storing Jwt payload data
 */
public class JWTPayload {
    String sub;
    String iss;
    String iat;
    String exp;

    JWTPayload(String sub, String iss, String iat, String exp){
        this.sub = sub;
        this.iss = iss;
        this.iat = iat;
        this.exp = exp;
    }

    // Getter
    public String getSub(){
        return this.sub;
    }
    public String getIat(){
        return this.iat;
    }

    public String getExp() {
        return this.exp;
    }

    // setters
    public void setSub(String sub){
        this.sub = sub;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public void setIat(String iat){
        this.iat = iat;
    }

    public void setExp(String exp){
        this.exp = exp;
    }


}
