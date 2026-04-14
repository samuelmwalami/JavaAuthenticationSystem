package services.security;

public class InvalidEpochException extends Exception {
    InvalidEpochException(){
        super("Epoch cannot be less than Zero");
    }
}
