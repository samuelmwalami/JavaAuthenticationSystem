package modules.authentication.infrastructure.security;

import modules.authentication.infrastructure.security.MessageDigestInfrastructure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageDigestInfrastructureTest {

    private final MessageDigestInfrastructure infrastructure = new MessageDigestInfrastructure();

    @Test
    void hashPasswordProducesValueDifferentFromPlainPassword() {
        String hash = infrastructure.hashPassword("Str0ng!Pass");
        assertNotEquals("Str0ng!Pass", hash);
    }

    @Test
    void hashPasswordProducesDifferentHashesForSamePasswordOnEachCall() {
        String firstHash = infrastructure.hashPassword("Str0ng!Pass");
        String secondHash = infrastructure.hashPassword("Str0ng!Pass");
        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void verifyPasswordReturnsTrueForCorrectPassword() {
        String hash = infrastructure.hashPassword("Str0ng!Pass");
        assertTrue(infrastructure.verifyPassword(hash, "Str0ng!Pass"));
    }

    @Test
    void verifyPasswordReturnsFalseForIncorrectPassword() {
        String hash = infrastructure.hashPassword("Str0ng!Pass");
        assertFalse(infrastructure.verifyPassword(hash, "WrongPassword1!"));
    }
}