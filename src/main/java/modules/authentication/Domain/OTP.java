package modules.authentication.Domain;

import lombok.Getter;
import utils.DatabaseConnector;

import javax.crypto.spec.OAEPParameterSpec;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class OTP {
    @Getter
    private static final int OTP_EXPIRY = 300;
    @Getter
    String otp;

    public OTP(){}

    public OTP(String otp){
        this.otp = otp;
    }

    public String generateOTP(){
        SecureRandom secureRandom = new SecureRandom();
        return String.valueOf(secureRandom.nextInt(100000,999999));
    }

    public LocalDateTime getOtpExpiry(){
        return LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(OTP_EXPIRY);
    }

    public boolean isOtpValid(String otp){
        return otp.strip().length() == 6;
    }

    public boolean isOtpExpired(LocalDateTime expiry){
        return LocalDateTime.now().isAfter(expiry);
    }


}
