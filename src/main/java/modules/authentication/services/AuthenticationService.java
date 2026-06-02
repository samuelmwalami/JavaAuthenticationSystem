package modules.authentication.services;

import com.fasterxml.uuid.Generators;
import modules.authentication.DTO.requestDTO.*;
import modules.authentication.DTO.responseDTO.*;
import modules.authentication.DTO.commonDTO.*;
import modules.authentication.Domain.AuthenticationToken;
import modules.authentication.Domain.User;
import modules.authentication.infrastructure.TokenDAO;
import modules.authentication.infrastructure.UserDAO;
import modules.authentication.repository.TokenRepository;
import modules.authentication.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class AuthenticationService{
    UserRepository userRepository = new UserDAO(); // Storage access Object for User
    TokenRepository tokenRepository = new TokenDAO(); // Storage access object for AuthenticationToken

    public Response<SignupResponse> registerUser(SignupRequest request) {
        Response<SignupResponse> response = new Response<>();
        response.data = new SignupResponse();
        User user = new User();

        ArrayList<String> errors = handleInvalidRegistrationInputs(request);

        // return if there is any error
        if (!errors.isEmpty()){
            response.errors.addAll(errors);
            return response;
        }

        // Map request to UserDTO
        user.setUserId(Generators.timeBasedEpochGenerator().generate());
        user.setFirstName(user.getFirstName().toLowerCase());
        user.setLastName(user.getLastName().toLowerCase());
        user.setUserName(user.getUserName().toLowerCase());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setConfirmPassword(request.getConfirmPassword());

        UserDTO userDTO = user.userToUserDTOMapper();

        // Save user to storage
        int rowsAffected = userRepository.saveUser(userDTO);
        if (rowsAffected != 1){
            response.errors.add("Account creation not successful");
            return response;
        }

        // Response
        SignupResponse signupResponse = new SignupResponse();
        signupResponse.setMessage("Account created Successfully");
        signupResponse.setUserId(user.getUserId().toString());
        response.data = signupResponse;

        return response;
    }

    public Response<UserDetailsResponse> getUserDetailsByEmail(UserDetailsRequest request){
        User user = new User();

        Response<UserDetailsResponse> response = new Response<>();
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse();
        response.data = userDetailsResponse;

        //Validate input
        if(!user.isEmailValid(request.getEmail())){
            response.errors.add("Invalid email");
            return response;
        }

        UserRepository userRepository = new UserDAO();
        UserDTO userDTO = userRepository.getUserByEmail(request.getEmail());

        // Handle token not found in storage
        if(userDTO.getUserId() == null){
            response.errors.add("User does not exist");
            return response;
        }

        // map user details to UserDetailsResponse
        userDetailsResponse.setUserId(userDTO.getUserId());
        userDetailsResponse.setFirstName(userDTO.getFirstName());
        userDetailsResponse.setLastName(userDTO.getLastName());
        userDetailsResponse.setUserName(userDTO.getUserName());
        userDetailsResponse.setEmail(userDTO.getEmail());
        userDetailsResponse.setCreatedAt(userDTO.getCreatedAt().toString());

        response.data = userDetailsResponse;

        return response;
    }

    public Response<DeleteUserResponse> deleteUserAccount(UserDetailsRequest request){
        User user = new User();

        Response<DeleteUserResponse> response = new Response<>();
        DeleteUserResponse deleteUserResponse = new DeleteUserResponse();


        //Validate input
        if(!user.isEmailValid(request.getEmail())){
            response.data = deleteUserResponse;
            response.errors.add("Invalid email");
            return response;
        }

        UserRepository userRepository = new UserDAO();
        int rowsAffected = userRepository.deleteUser(request.getEmail());

        // Handle token not found in storage
        if(rowsAffected != 1){
            response.data = deleteUserResponse;
            response.errors.add("Error deleting account");
            return response;
        }

        deleteUserResponse.setMessage("Account deleted successfully");

        response.data = deleteUserResponse;

        return response;
    }

    public Response<LoginResponse> loginUser(LoginRequest request){
        User userDomain = new User();
        AuthenticationToken authenticationToken = new AuthenticationToken();

        Response<LoginResponse> response = new Response<>();
        LoginResponse loginResponse = new LoginResponse();

        // Validate input
        if(!userDomain.isEmailValid(request.getEmail())){
            response.data = loginResponse;
            response.errors.add("Use a valid email structure");
            return response;
        }
        if(request.getPassword().isEmpty()){
            response.data = loginResponse;
            response.errors.add("Invalid credentials");
            return response;
        }

        // Get user from Storage
        UserDTO user = userRepository.getUserByEmailAndPassword(request.getEmail(), request.getPassword());
        if(user.getUserId() == null || user.getUserId().toString().isEmpty()){
            response.data = loginResponse;
            response.errors.add("Invalid credentials");
            return response;
        }

        // get access and refresh token
        String accessToken = authenticationToken.getAccessToken(user.getUserId().toString(),new HashMap<>());
        String refreshToken = authenticationToken.getRefreshToken(user.getUserId().toString(), new HashMap<>());

        //save refresh token to storage
        AccessTokenDTO token = new AccessTokenDTO();
        token.setTokenId(Generators.timeBasedEpochGenerator().generate());
        token.setRefreshToken(refreshToken);
        token.setUserId(user.getUserId());
        int rowsAffected = tokenRepository.saveRefreshToken(token);

        if (rowsAffected == 1){
            IO.println("Token saved to Storage");
        }
        else{
            IO.println("Token not saved to database");
        }


        // Response
        loginResponse.setMessage("Logged in Successfully");
        loginResponse.setAccessToken(accessToken);
        loginResponse.setAccessTokenExpirationDuration(authenticationToken.getAccessTokenExpiryDuration());
        loginResponse.setRefreshToken(refreshToken);
        loginResponse.setRefreshTokenExpirationDuration(authenticationToken.getRefreshTokenExpiryDuration());

        return response;
    }

    public Response<LogoutResponse> logoutUser(LogoutRequest request){
        Response<LogoutResponse> response = new Response<>();
        LogoutResponse logoutResponse = new LogoutResponse();

        // Handle empty input
        if(request.getRefreshToken().isEmpty()){
            response.data = logoutResponse;
            response.errors.add("Please input the refresh token");
            return response;
        }

        AccessTokenDTO token = tokenRepository.fetchRefreshToken(request.getRefreshToken());

        // Handle token not found in storage
        if(token.getTokenId().toString().isEmpty()){
            response.data = logoutResponse;
            response.errors.add("Invalid refresh token");
            return response;
        }

        // Response
        logoutResponse.setMessage("Logged out successfully");
        logoutResponse.setUserId(token.getUserId().toString());
        response.data = logoutResponse;
        return response;


    }

    public Response<RenewAccessTokenResponse> renewAccessToken(RenewAccessTokenRequest request){
        AuthenticationToken authenticationToken = new AuthenticationToken();

        Response<RenewAccessTokenResponse> response = new Response<>();
        RenewAccessTokenResponse accessTokenResponse  = new RenewAccessTokenResponse();

        // Handle empty input
        if(request.getRefreshToken().isEmpty()){
            response.data = accessTokenResponse;
            response.errors.add("Please input the refresh token");
            return response;
        }

        AccessTokenDTO token = tokenRepository.fetchRefreshToken(request.getRefreshToken());

        // Handle token not found
        if(token.getTokenId().toString().isEmpty()){
            response.data = accessTokenResponse;
            response.errors.add("Invalid refresh token");
            return response;
        }

        // Handle Expired Token
        if(authenticationToken.isTokenExpired(token.getRefreshToken())){
            response.data = accessTokenResponse;
            response.errors.add("Refresh token has Expired");
            return response;
        }

        String tokenSub = authenticationToken.getSub(token.getRefreshToken());
        String newAccessToken = authenticationToken.getAccessToken(tokenSub,new HashMap<>());

        // Response
        accessTokenResponse.setAccessToken(newAccessToken);
        accessTokenResponse.setAccessTokenExpirationDuration(authenticationToken.getAccessTokenExpiryDuration());
        response.data = accessTokenResponse;

        return response;

    }

    // Helper Functions
    public  ArrayList<String> handleInvalidRegistrationInputs(SignupRequest request){
        User user = new User();
        ArrayList<String> errors = new ArrayList<>();

        // validate name fields
        if (!user.isNameValid(request.getFirstName()) ||
                !user.isNameValid(request.getLastName()) ||
                !user.isNameValid(request.getUserName())) {
            errors.add("All name fields must be filled");
        }

        // validate password
        if (!user.doPasswordsMatch(request.getPassword(), request.getConfirmPassword())) {
            errors.add("The passwords provided do not match");
        } else {
            if (user.isPasswordStrong(request.getPassword())) {
                errors.add("The password provided is not Strong");
            }
        }

        // validate email
        if (!user.isEmailValid(request.getEmail())) {
            errors.add("Your Email is invalid");
        }
        return errors;
    }
}
