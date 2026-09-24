package modules.authentication.Domain;

import lib.jwt.Exceptions.IatGreaterThanExpException;
import lib.jwt.JWT;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationTokenTest {

    private static final String SECRET = "test-secret-key-please-ignore";

    private String buildToken(String sub, LocalDateTime exp) throws IatGreaterThanExpException {
        return new JWT.Builder()
                .setSub(sub)
                .setExp(exp)
                .setSecret(SECRET)
                .compact()
                .getJWT();
    }

    @Test
    void getSubReturnsSubClaimFromToken() throws IatGreaterThanExpException {
        String token = buildToken("user-123", LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(15));
        assertEquals("user-123", AuthenticationToken.getSub(token));
    }

    @Test
    void isTokenExpiredReturnsFalseForFutureExpiry() throws IatGreaterThanExpException {
        String token = buildToken("user-123", LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(15));
        assertFalse(AuthenticationToken.isTokenExpired(token));
    }

    @Test
    void isTokenExpiredReturnsTrueForPastExpiry() throws IatGreaterThanExpException, InterruptedException {
        String token = buildToken("user-123", LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(1));
        Thread.sleep(2000);
        assertTrue(AuthenticationToken.isTokenExpired(token));
    }
}