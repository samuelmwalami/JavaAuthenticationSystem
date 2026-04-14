package services.security;

public class IatGreaterThanExpException extends Exception{
    IatGreaterThanExpException(){
        super("iat cannot be greater or equal to exp");
    }
}
