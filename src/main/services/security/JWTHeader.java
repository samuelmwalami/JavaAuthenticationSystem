package services.security;

/**
 * Model class for the JWT header
 * @author sam
 * @version 1.0
 */
public class JWTHeader {
    private String alg;
    private String typ;
    JWTHeader(String alg, String typ){
        this.alg = alg;
        this.typ = typ;
    }

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
