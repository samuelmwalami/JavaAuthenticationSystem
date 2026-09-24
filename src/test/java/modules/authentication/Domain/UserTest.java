package modules.authentication.Domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void isPasswordStrongAcceptsCompliantPassword() {
        assertTrue(User.isPasswordStrong("Str0ng!Pass"));
    }

    @Test
    void isPasswordStrongRejectsShortPassword() {
        assertFalse(User.isPasswordStrong("Str0!P"));
    }

    @Test
    void isPasswordStrongRejectsMissingSpecialCharacter() {
        assertFalse(User.isPasswordStrong("Strong123"));
    }

    @Test
    void isPasswordStrongRejectsMissingUppercase() {
        assertFalse(User.isPasswordStrong("str0ng!pass"));
    }

    @Test
    void isPasswordStrongRejectsNull() {
        assertFalse(User.isPasswordStrong(null));
    }

    @Test
    void doPasswordsMatchReturnsTrueForIdenticalPasswords() {
        assertTrue(User.doPasswordsMatch("Str0ng!Pass", "Str0ng!Pass"));
    }

    @Test
    void doPasswordsMatchIgnoresSurroundingWhitespace() {
        assertTrue(User.doPasswordsMatch(" Str0ng!Pass ", "Str0ng!Pass"));
    }

    @Test
    void doPasswordsMatchReturnsFalseForDifferentPasswords() {
        assertFalse(User.doPasswordsMatch("Str0ng!Pass", "Different!1"));
    }

    @Test
    void doPasswordsMatchReturnsFalseWhenEitherIsNull() {
        assertFalse(User.doPasswordsMatch(null, "Str0ng!Pass"));
        assertFalse(User.doPasswordsMatch("Str0ng!Pass", null));
    }

    @Test
    void isFirstNameValidAcceptsLowercaseAlphanumeric() {
        User user = new User();
        user.setFirstName("john_99");
        assertTrue(user.isFirstNameValid());
    }

    @Test
    void isFirstNameValidRejectsUppercase() {
        User user = new User();
        user.setFirstName("John");
        assertFalse(user.isFirstNameValid());
    }

    @Test
    void isFirstNameValidRejectsTooShort() {
        User user = new User();
        user.setFirstName("jo");
        assertFalse(user.isFirstNameValid());
    }

    @Test
    void isUserNameValidRejectsNull() {
        User user = new User();
        assertFalse(user.isUserNameValid());
    }

    @Test
    void isEmailValidAcceptsWellFormedEmail() {
        User user = new User();
        user.setEmail("john.doe@example.com");
        assertTrue(user.isEmailValid());
    }

    @Test
    void isEmailValidRejectsMissingAtSymbol() {
        User user = new User();
        user.setEmail("john.doe.example.com");
        assertFalse(user.isEmailValid());
    }

    @Test
    void isEmailValidRejectsNull() {
        User user = new User();
        assertFalse(user.isEmailValid());
    }

    @Test
    void userToUserDTOMapperLowercasesAllFields() {
        User user = new User("John", "Doe", "JohnDoe", "John.Doe@Example.COM");
        user.setPassword("hashedValue");

        var dto = user.userToUserDTOMapper();

        assertEquals("john", dto.getFirstName());
        assertEquals("doe", dto.getLastName());
        assertEquals("johndoe", dto.getUserName());
        assertEquals("john.doe@example.com", dto.getEmail());
        assertEquals("hashedValue", dto.getPassword());
    }
}