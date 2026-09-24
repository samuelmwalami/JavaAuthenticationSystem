package modules.authentication.Domain;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class AccessTokenTest {

    @Test
    void getAccessTokenProducesThreeDotSeparatedToken() {
        AccessToken accessToken = new AccessToken();
        String token = accessToken.getAccessToken("user-123", new HashMap<>());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void isAccessTokenCompromisedReturnsFalseForUntamperedToken() {
        AccessToken accessToken = new AccessToken();
        String token = accessToken.getAccessToken("user-123", new HashMap<>());

        assertFalse(AccessToken.isAccessTokenCompromised(token));
    }

    @Test
    void isAccessTokenCompromisedReturnsTrueForTamperedToken() {
        AccessToken accessToken = new AccessToken();
        String token = accessToken.getAccessToken("user-123", new HashMap<>());
        String[] parts = token.split("\\.");

        String forgedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"attacker\"}".getBytes());
        String tamperedToken = parts[0] + "." + forgedPayload + "." + parts[2];

        assertTrue(AccessToken.isAccessTokenCompromised(tamperedToken));
    }

    @Test
    void isAccessTokenProvidedReturnsFalseWhenBlank() {
        AccessToken accessToken = new AccessToken("");
        assertFalse(accessToken.isAccessTokenProvided());
    }

    @Test
    void isAccessTokenProvidedReturnsTrueWhenPresent() {
        AccessToken accessToken = new AccessToken("some.jwt.token");
        assertTrue(accessToken.isAccessTokenProvided());
    }
}