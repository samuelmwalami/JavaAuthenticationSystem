package modules.authentication.services;

import EventBus.EventBus;
import modules.authentication.DTO.commonDTO.OtpDTO;
import modules.authentication.DTO.commonDTO.UserDTO;
import modules.authentication.DTO.requestDTO.LoginRequest;
import modules.authentication.DTO.requestDTO.SignupRequest;
import modules.authentication.DTO.requestDTO.VerifyEmailRequest;
import modules.authentication.DTO.requestDTO.VerifyLoginRequest;
import modules.authentication.DTO.responseDTO.ApiResponse;
import modules.authentication.infrastructure.security.MessageDigestInfrastructure;
import modules.authentication.repository.storage.OtpRepository;
import modules.authentication.repository.storage.TokenRepository;
import modules.authentication.repository.storage.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private MessageDigestInfrastructure messageDigestInfrastructure;

    @Mock
    private OtpRepository otpRepository;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                userRepository, tokenRepository, messageDigestInfrastructure, otpRepository);
    }

    private SignupRequest validSignupRequest() {
        SignupRequest request = new SignupRequest();
        request.setFirstName("john");
        request.setLastName("doe");
        request.setUserName("johndoe");
        request.setEmail("john.doe@example.com");
        request.setPassword("Str0ng!Pass");
        request.setConfirmPassword("Str0ng!Pass");
        return request;
    }

    @Test
    void registerUserReturns201OnSuccess() {
        when(messageDigestInfrastructure.hashPassword("Str0ng!Pass")).thenReturn("hashedValue");
        when(userRepository.saveUser(any(UserDTO.class))).thenReturn(1);
        when(otpRepository.saveOtp(any(OtpDTO.class))).thenReturn(1);

        ApiResponse response = authenticationService.registerUser(validSignupRequest());

        assertEquals(201, response.getStatusCode());
    }

    @Test
    void registerUserReturns400WhenPasswordsDoNotMatch() {
        SignupRequest request = validSignupRequest();
        request.setConfirmPassword("Different!1");

        ApiResponse response = authenticationService.registerUser(request);

        assertEquals(400, response.getStatusCode());
        verify(userRepository, never()).saveUser(any(UserDTO.class));
    }

    @Test
    void registerUserReturns400WhenPasswordIsWeak() {
        SignupRequest request = validSignupRequest();
        request.setPassword("weak");
        request.setConfirmPassword("weak");

        ApiResponse response = authenticationService.registerUser(request);

        assertEquals(400, response.getStatusCode());
        verify(userRepository, never()).saveUser(any(UserDTO.class));
    }

    @Test
    void registerUserReturns400WhenEmailIsInvalid() {
        SignupRequest request = validSignupRequest();
        request.setEmail("not-an-email");

        ApiResponse response = authenticationService.registerUser(request);

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void registerUserReturns403WhenSaveUserFails() {
        when(messageDigestInfrastructure.hashPassword("Str0ng!Pass")).thenReturn("hashedValue");
        when(userRepository.saveUser(any(UserDTO.class))).thenReturn(0);

        ApiResponse response = authenticationService.registerUser(validSignupRequest());

        assertEquals(403, response.getStatusCode());
        verify(otpRepository, never()).saveOtp(any(OtpDTO.class));
    }

    @Test
    void registerUserReturns401WhenOtpSaveFails() {
        when(messageDigestInfrastructure.hashPassword("Str0ng!Pass")).thenReturn("hashedValue");
        when(userRepository.saveUser(any(UserDTO.class))).thenReturn(1);
        when(otpRepository.saveOtp(any(OtpDTO.class))).thenReturn(0);

        ApiResponse response = authenticationService.registerUser(validSignupRequest());

        assertEquals(401, response.getStatusCode());
    }

    @Test
    void verifyEmailReturns400ForInvalidEmail() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("not-an-email");
        request.setOtp("123456");

        ApiResponse response = authenticationService.verifyEmail(request);

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void verifyEmailReturns401WhenAccountDoesNotExist() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("john.doe@example.com");
        request.setOtp("123456");

        when(userRepository.getUserByEmail("john.doe@example.com")).thenReturn(new UserDTO());

        ApiResponse response = authenticationService.verifyEmail(request);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    void verifyEmailReturns200WhenAlreadyVerified() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("john.doe@example.com");
        request.setOtp("123456");

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(UUID.randomUUID());
        userDTO.setEmailVerified(true);
        when(userRepository.getUserByEmail("john.doe@example.com")).thenReturn(userDTO);

        ApiResponse response = authenticationService.verifyEmail(request);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void verifyEmailReturns401ForInvalidOtp() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("john.doe@example.com");
        request.setOtp("123456");

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(UUID.randomUUID());
        when(userRepository.getUserByEmail("john.doe@example.com")).thenReturn(userDTO);
        when(otpRepository.retrieveOtpByOtpAndEmail("123456", "john.doe@example.com")).thenReturn(new OtpDTO());

        ApiResponse response = authenticationService.verifyEmail(request);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    void verifyEmailReturns401ForExpiredOtp() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("john.doe@example.com");
        request.setOtp("123456");

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(UUID.randomUUID());
        when(userRepository.getUserByEmail("john.doe@example.com")).thenReturn(userDTO);

        OtpDTO otpDTO = new OtpDTO();
        otpDTO.setOtp("123456");
        otpDTO.setOtpExpiry(LocalDateTime.now().minusMinutes(1));
        when(otpRepository.retrieveOtpByOtpAndEmail("123456", "john.doe@example.com")).thenReturn(otpDTO);

        ApiResponse response = authenticationService.verifyEmail(request);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    void verifyEmailReturns200OnSuccess() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("john.doe@example.com");
        request.setOtp("123456");

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(UUID.randomUUID());
        when(userRepository.getUserByEmail("john.doe@example.com")).thenReturn(userDTO);

        OtpDTO otpDTO = new OtpDTO();
        otpDTO.setOtp("123456");
        otpDTO.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        when(otpRepository.retrieveOtpByOtpAndEmail("123456", "john.doe@example.com")).thenReturn(otpDTO);
        when(userRepository.setTrueEmailVerificationStatus("john.doe@example.com")).thenReturn(1);

        ApiResponse response = authenticationService.verifyEmail(request);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void loginUserReturns400ForInvalidEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("not-an-email");
        request.setPassword("Str0ng!Pass");

        ApiResponse response = authenticationService.loginUser(request);

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void loginUserReturns401WhenAccountDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("Str0ng!Pass");

        when(userRepository.getUserWithPasswordByEmail("john.doe@example.com")).thenReturn(new UserDTO());

        ApiResponse response = authenticationService.loginUser(request);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    void loginUserReturns401WhenPasswordIsIncorrect() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("WrongPassword1!");

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(UUID.randomUUID());
        userDTO.setPassword("hashedValue");
        when(userRepository.getUserWithPasswordByEmail("john.doe@example.com")).thenReturn(userDTO);
        when(messageDigestInfrastructure.verifyPassword("hashedValue", "WrongPassword1!")).thenReturn(false);

        ApiResponse response = authenticationService.loginUser(request);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    void loginUserReturns200OnValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("Str0ng!Pass");

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(UUID.randomUUID());
        userDTO.setPassword("hashedValue");
        when(userRepository.getUserWithPasswordByEmail("john.doe@example.com")).thenReturn(userDTO);
        when(messageDigestInfrastructure.verifyPassword("hashedValue", "Str0ng!Pass")).thenReturn(true);
        when(otpRepository.saveOtp(any(OtpDTO.class))).thenReturn(1);

        ApiResponse response = authenticationService.loginUser(request);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void loginUserReturns401WhenOtpSaveFails() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("Str0ng!Pass");

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(UUID.randomUUID());
        userDTO.setPassword("hashedValue");
        when(userRepository.getUserWithPasswordByEmail("john.doe@example.com")).thenReturn(userDTO);
        when(messageDigestInfrastructure.verifyPassword("hashedValue", "Str0ng!Pass")).thenReturn(true);
        when(otpRepository.saveOtp(any(OtpDTO.class))).thenReturn(0);

        ApiResponse response = authenticationService.loginUser(request);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    void verifyLoginReturns401ForInvalidEmail() {
        VerifyLoginRequest request = new VerifyLoginRequest();
        request.setEmail("not-an-email");
        request.setOtp("123456");

        ApiResponse response = authenticationService.verifyLogin(request);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    void verifyLoginReturns401ForInvalidOtpFormat() {
        VerifyLoginRequest request = new VerifyLoginRequest();
        request.setEmail("john.doe@example.com");
        request.setOtp("12");

        ApiResponse response = authenticationService.verifyLogin(request);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    void verifyLoginReturns401WhenOtpNotFound() {
        VerifyLoginRequest request = new VerifyLoginRequest();
        request.setEmail("john.doe@example.com");
        request.setOtp("123456");

        when(otpRepository.retrieveOtpByOtpAndEmail("123456", "john.doe@example.com")).thenReturn(new OtpDTO());

        ApiResponse response = authenticationService.verifyLogin(request);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    void verifyLoginReturns401WhenOtpExpired() {
        VerifyLoginRequest request = new VerifyLoginRequest();
        request.setEmail("john.doe@example.com");
        request.setOtp("123456");

        OtpDTO otpDTO = new OtpDTO();
        otpDTO.setOtpID(UUID.randomUUID());
        otpDTO.setOtpExpiry(LocalDateTime.now().minusMinutes(1));
        when(otpRepository.retrieveOtpByOtpAndEmail("123456", "john.doe@example.com")).thenReturn(otpDTO);

        ApiResponse response = authenticationService.verifyLogin(request);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    void verifyLoginReturns401WhenUserNotFound() {
        VerifyLoginRequest request = new VerifyLoginRequest();
        request.setEmail("john.doe@example.com");
        request.setOtp("123456");

        OtpDTO otpDTO = new OtpDTO();
        otpDTO.setOtpID(UUID.randomUUID());
        otpDTO.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        when(otpRepository.retrieveOtpByOtpAndEmail("123456", "john.doe@example.com")).thenReturn(otpDTO);
        when(userRepository.getUserByEmail("john.doe@example.com")).thenReturn(new UserDTO());

        ApiResponse response = authenticationService.verifyLogin(request);

        assertEquals(401, response.getStatusCode());
    }
}