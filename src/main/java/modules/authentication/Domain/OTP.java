package modules.authentication.Domain;

import lombok.Getter;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class OTP {
    @Getter
    private static final int OTP_EXPIRY_DURATION = 300;
    @Getter
    private static final int GET_NEW_OTP_WINDOW_DURATION = 30;
    public static String generateOTP(){
        SecureRandom secureRandom = new SecureRandom();
        return String.valueOf(secureRandom.nextInt(100000,999999));
    }

    public static LocalDateTime getOtpExpiryTime(){
        return LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(OTP_EXPIRY_DURATION);
    }

    public static boolean isOtpValid(String otp){
        return otp.strip().length() == 6;
    }

    public static boolean isOtpExpired(LocalDateTime expiry){
        return LocalDateTime.now().isAfter(expiry);
    }


}
