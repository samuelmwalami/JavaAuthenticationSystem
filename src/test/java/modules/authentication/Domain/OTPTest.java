package modules.authentication.Domain;

import modules.authentication.Domain.OTP;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class OTPTest {

    @Test
    void generateOTPProducesSixDigits() {
        String otp = OTP.generateOTP();
        assertEquals(6, otp.length());

        boolean allDigits = true;
        for (int i = 0; i < otp.length(); i++) {
            if (!Character.isDigit(otp.charAt(i))) {
                allDigits = false;
                break;
            }
        }
        assertTrue(allDigits);
    }

    @Test
    void generateOTPProducesValueWithinExpectedRange() {
        for (int i = 0; i < 1000; i++) {
            int otp = Integer.parseInt(OTP.generateOTP());
            assertTrue(otp >= 100000 && otp <= 999999);
        }
    }

    @Test
    void isOtpValidAcceptsSixDigitString() {
        assertTrue(OTP.isOtpValid("123456"));
    }

    @Test
    void isOtpValidRejectsWrongLength() {
        assertFalse(OTP.isOtpValid("12345"));
        assertFalse(OTP.isOtpValid("1234567"));
    }

    @Test
    void isOtpValidIgnoresSurroundingWhitespace() {
        assertTrue(OTP.isOtpValid(" 123456 "));
    }

    @Test
    void isOtpExpiredReturnsFalseForFutureExpiry() {
        LocalDateTime future = LocalDateTime.now().plusMinutes(5);
        assertFalse(OTP.isOtpExpired(future));
    }

    @Test
    void isOtpExpiredReturnsTrueForPastExpiry() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(5);
        assertTrue(OTP.isOtpExpired(past));
    }

    @Test
    void getOtpExpiryTimeIsApproximatelyExpiryDurationFromNow() {
        LocalDateTime expiry = OTP.getOtpExpiryTime();
        LocalDateTime expected = LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(OTP.getOTP_EXPIRY_DURATION());

        long diffSeconds = Math.abs(Duration.between(expiry, expected).getSeconds());
        assertTrue(diffSeconds < 2);
    }
}