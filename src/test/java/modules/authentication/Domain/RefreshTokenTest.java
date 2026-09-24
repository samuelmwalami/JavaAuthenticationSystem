package modules.authentication.Domain;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenTest {

    @Test
    void getRefreshTokenProducesThreeDotSeparatedToken() {
        RefreshToken refreshToken = new RefreshToken();
        String token = refreshToken.getRefreshToken("user-123", new HashMap<>());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void isRefreshTokenCompromisedReturnsFalseForUntamperedToken() {
        RefreshToken refreshToken = new RefreshToken();
        String token = refreshToken.getRefreshToken("user-123", new HashMap<>());

        assertFalse(RefreshToken.isRefreshTokenCompromised(token));
    }

    @Test
    void isRefreshTokenCompromisedReturnsTrueForTamperedToken() {
        RefreshToken refreshToken = new RefreshToken();
        String token = refreshToken.getRefreshToken("user-123", new HashMap<>());
        String[] parts = token.split("\\.");

        String forgedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"attacker\"}".getBytes());
        String tamperedToken = parts[0] + "." + forgedPayload + "." + parts[2];

        assertTrue(RefreshToken.isRefreshTokenCompromised(tamperedToken));
    }

    @Test
    void isRefreshTokenProvidedReturnsFalseWhenBlank() {
        RefreshToken refreshToken = new RefreshToken("");
        assertFalse(refreshToken.isRefreshTokenProvided());
    }

    @Test
    void isRefreshTokenProvidedReturnsTrueWhenPresent() {
        RefreshToken refreshToken = new RefreshToken("some.jwt.token");
        assertTrue(refreshToken.isRefreshTokenProvided());
    }
}