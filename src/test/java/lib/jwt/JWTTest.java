package lib.jwt;

import lib.jwt.Exceptions.IatGreaterThanExpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

class JWTTest {

    private static final String SECRET = "test-secret-key-please-ignore";
    private JWT.Builder builder;

    @BeforeEach
    void setUp() {
        builder = new JWT.Builder().setSecret(SECRET);
    }

    @Test
    void getJWTProducesThreeDotSeparatedParts() throws IatGreaterThanExpException {
        JWT jwt = builder
                .setSub("user-123")
                .setExp(LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(15))
                .compact();

        String token = jwt.getJWT();

        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void verifyJWTReturnsTrueForValidToken() throws IatGreaterThanExpException {
        JWT jwt = builder
                .setSub("user-123")
                .setExp(LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(15))
                .compact();

        String token = jwt.getJWT();

        assertTrue(jwt.verifyJWT(token));
    }

    @Test
    void verifyJWTReturnsFalseForTamperedPayload() throws IatGreaterThanExpException {
        JWT jwt = builder
                .setSub("user-123")
                .setExp(LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(15))
                .compact();

        String token = jwt.getJWT();
        String[] parts = token.split("\\.");

        String forgedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"attacker\"}".getBytes());
        String tamperedToken = parts[0] + "." + forgedPayload + "." + parts[2];

        assertFalse(jwt.verifyJWT(tamperedToken));
    }

    @Test
    void verifyJWTReturnsFalseForWrongSecret() throws IatGreaterThanExpException {
        JWT signedWithSecretA = builder
                .setSub("user-123")
                .setExp(LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(15))
                .compact();
        String token = signedWithSecretA.getJWT();

        JWT verifierWithSecretB = new JWT.Builder().setSecret("a-completely-different-secret").compact();

        assertFalse(verifierWithSecretB.verifyJWT(token));
    }

    @Test
    void getPayloadClaimReturnsSubValue() throws IatGreaterThanExpException {
        JWT jwt = builder
                .setSub("user-123")
                .setExp(LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(15))
                .compact();

        String token = jwt.getJWT();

        Object sub = jwt.getPayloadClaim(token, "sub");

        assertEquals("user-123", sub);
    }

    @Test
    void getPayloadClaimReturnsExpValue() throws IatGreaterThanExpException {
        LocalDateTime expTime = LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(15);
        long expectedEpoch = expTime.toEpochSecond(ZoneOffset.UTC);

        JWT jwt = builder.setSub("user-123").setExp(expTime).compact();
        String token = jwt.getJWT();

        Object exp = jwt.getPayloadClaim(token, "exp");

        assertTrue(exp instanceof Number);
        assertEquals(expectedEpoch, ((Number) exp).longValue());
    }

    @Test
    void getPayloadClaimReturnsEmptyStringForMissingClaim() throws IatGreaterThanExpException {
        JWT jwt = builder
                .setSub("user-123")
                .setExp(LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(15))
                .compact();

        String token = jwt.getJWT();

        assertEquals("", jwt.getPayloadClaim(token, "not_a_real_claim"));
    }

    @Test
    void setExpThrowsWhenBeforeIssuedAt() {
        JWT.Builder b = new JWT.Builder().setSecret(SECRET).setSub("user-123");

        assertThrows(IatGreaterThanExpException.class,
                () -> b.setExp(LocalDateTime.now(ZoneId.of("UTC")).minusMinutes(5)));
    }
}