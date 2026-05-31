package lib.security.Exceptions;

/**
 * Exception thrown when Issued at Time exceeds the expiration time
 */
public class IatGreaterThanExpException extends Exception{
    public IatGreaterThanExpException(){
        super("iat cannot be greater or equal to exp");
    }
}
