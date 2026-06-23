package modules.authentication.Domain;

import java.security.SecureRandom;

public class OTP {
    public static int generateOTP(){
        SecureRandom secureRandom = new SecureRandom();
        return secureRandom.nextInt(100000,999999);
    }

    void main(){
        IO.println(OTP.generateOTP());
    }
}
