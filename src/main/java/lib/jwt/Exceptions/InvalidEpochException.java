package lib.jwt.Exceptions;

/**
 * Exception Thrown when
 * */
public class InvalidEpochException extends Exception {
    InvalidEpochException(){
        super("Epoch cannot be less than Zero");
    }
}
