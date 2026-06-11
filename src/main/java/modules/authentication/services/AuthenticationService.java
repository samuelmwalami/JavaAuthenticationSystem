package modules.authentication.services;


import com.fasterxml.uuid.Generators;
import modules.authentication.DTO.requestDTO.*;
import modules.authentication.DTO.responseDTO.*;
import modules.authentication.DTO.commonDTO.*;
import modules.authentication.Domain.AuthenticationToken;
import modules.authentication.Domain.User;
import modules.authentication.infrastructure.security.MessageDigest;
import modules.authentication.infrastructure.storage.TokenDAO;
import modules.authentication.infrastructure.storage.UserDAO;
import modules.authentication.repository.security.MessageDigestRepository;
import modules.authentication.repository.storage.TokenRepository;
import modules.authentication.repository.storage.UserRepository;
import java.util.HashMap;
import java.util.UUID;

public class AuthenticationService{
    UserRepository userRepository = new UserDAO(); // Storage access Object for User
    TokenRepository tokenRepository = new TokenDAO(); // Storage access object for AuthenticationToken
    MessageDigestRepository MessageDigestInfrastructure = new MessageDigest(); // MessageDigest infrastructure

    public ApiResponse registerUser(SignupRequest request) {
         new ApiResponse();

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());

        IO.println(user.getUserId() + user.getUserName() + user.getLastName() + user.getFirstName() + user.getPassword());

        // handle errors if there is any error
        ApiResponse errorResponse = validateRegistrationInputs(request,user);
        if (errorResponse!= null){
            return errorResponse;
        }

        // Hash password
        String hashedPassword = MessageDigestInfrastructure.hashPassword(request.getPassword());

        user.setUserId(Generators.timeBasedEpochGenerator().generate());
        user.setPassword(hashedPassword);

        // Map request to UserDTO
        UserDTO userDTO = user.userToUserDTOMapper();
        IO.println(userDTO.getUserId() + userDTO.getUserName() + userDTO.getLastName() + userDTO.getFirstName() + userDTO.getPassword());


        // Save user to storage
        int rowsAffected = userRepository.saveUser(userDTO);
        if (rowsAffected != 1){
            ErrorBody errorBody = new ErrorBody("Error","Account creation not successful");
            return new ApiResponse(403,errorBody);
        }

        // Response
        SignupResponse signupResponse = new SignupResponse("Account created Successfully",
                user.getUserId().toString()
        );


        return new ApiResponse(201, signupResponse);
    }

    public ApiResponse getUserDetailsByEmail(UserDetailsRequest request, String accessToken){
        ApiResponse errorResponse = new ApiResponse();

        AuthenticationToken accessTokenObject = new AuthenticationToken();
        accessTokenObject.setAccessToken(accessToken);

        // validate access token
        if(!isRefreshTokenValid(accessTokenObject, errorResponse)){
            return errorResponse;
        }

        User user = new User();
        user.setEmail(request.getEmail());

        //Validate input
        if(!user.isEmailValid()){
            ErrorBody apiError = new ErrorBody("Error", "Invalid email");
            return new ApiResponse(401,apiError);
        }

        UUID userID = UUID.fromString(AuthenticationToken.getSub(accessToken));
        // Fetch user from storage
        UserDTO userDTO = userRepository.getUserByEmailAndUserId(request.getEmail(), userID);

        // Handle token not found in storage
        if(userDTO.getUserId() == null){
            ErrorBody apiError = new ErrorBody("Error", "User does not exist");
            return new ApiResponse(401,apiError);
        }

        // map user details to UserDetailsResponse
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse(userDTO.getUserId(),
                userDTO.getFirstName(),
                userDTO.getLastName(),
                userDTO.getUserName(),
                userDTO.getEmail(),
                userDTO.getCreatedAt().toString());

        return new ApiResponse(200,userDetailsResponse);
    }

    public ApiResponse deleteUserAccount(DeleteUserRequest request,String accessToken){
        ApiResponse errorResponse = new ApiResponse();

        AuthenticationToken accessTokenObject = new AuthenticationToken();
        accessTokenObject.setAccessToken(accessToken);
        // validate access token
        if(!isAccessTokenValid(accessTokenObject, errorResponse)){
            return errorResponse;
        }


        AuthenticationToken refreshTokenObject = new AuthenticationToken();
        refreshTokenObject.setRefreshToken(request.getRefreshToken());
        // validate refresh token
        if(!isRefreshTokenValid(refreshTokenObject, errorResponse)){
            return errorResponse;
        }

        String accessTokenUserId = AuthenticationToken.getSub(accessToken);
        String refreshTokenUserId = AuthenticationToken.getSub(request.getRefreshToken());
        // Handle mismatch between user ids of the tokens
        if(!accessTokenUserId.equals(refreshTokenUserId)){
            ErrorBody errorBody = new ErrorBody("Error", "Unauthorized. Mismatch of access and refresh token ids");
            return new ApiResponse(401,errorBody);
        }


        User user = new User();
        user.setEmail(request.getEmail());

        //Validate email
        if(!user.isEmailValid()){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid email");
            return new ApiResponse(400, errorBody);
        }

        // delete user
        UserRepository userRepository = new UserDAO();
        int rowsAffected = userRepository.deleteUserByEmailAndUserId(user.getEmail(), UUID.fromString(refreshTokenUserId));

        // Handle token not found in storage
        if(rowsAffected != 1){
            ErrorBody errorBody = new ErrorBody("Error", "Error deleting account");
            return new ApiResponse(404, errorBody);
        }

        DeleteUserResponse deleteUserResponse = new DeleteUserResponse(
                "Account deleted successfully",
                refreshTokenUserId);

        return new ApiResponse(204, deleteUserResponse);
    }

    public ApiResponse loginUser(LoginRequest request){
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        // Validate email
        if(!user.isEmailValid()){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid email");
            return new ApiResponse(404, errorBody);
        }

        // Get user from Storage
        UserDTO userDTO = userRepository.getUserWithPasswordByEmail(request.getEmail());

        // check if user exists;
        if(userDTO.getUserId() == null || userDTO.getUserId().toString().isEmpty()){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid credentials");
            return new ApiResponse(401, errorBody);

        }

        // check if password match
        if(!MessageDigestInfrastructure.verifyPassword(userDTO.getPassword(),user.getPassword())){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid credentials");
            return new ApiResponse(401, errorBody);
        }

        AuthenticationToken authenticationToken = new AuthenticationToken();
        // get access
        String accessToken = authenticationToken.getAccessToken(userDTO.getUserId().toString(),new HashMap<>());
        String refreshToken = authenticationToken.getRefreshToken(userDTO.getUserId().toString(), new HashMap<>());

        //save refresh token to storage
        AccessTokenDTO token = new AccessTokenDTO();
        token.setTokenId(Generators.timeBasedEpochGenerator().generate());
        token.setRefreshToken(refreshToken);
        token.setUserId(userDTO.getUserId());
        int rowsAffected = tokenRepository.saveRefreshToken(token);

        if (rowsAffected == 1){
            IO.println("Token saved to Storage");
        }
        else{
            IO.println("Token not saved to database");
        }


        // Response
        LoginResponse loginResponse = new LoginResponse("Logged in Successfully",
                refreshToken,
                accessToken,
                AuthenticationToken.getRefreshTokenExpiryDuration(),
                AuthenticationToken.getAccessTokenExpiryDuration()
        );

        return new ApiResponse(200,loginResponse);
    }

    public ApiResponse logoutUser(LogoutRequest request, String accessToken){
        ApiResponse errorResponse = new ApiResponse();

        // validate access token
        AuthenticationToken accessTokenObject = new AuthenticationToken();
        accessTokenObject.setAccessToken(accessToken);
        if(!isAccessTokenValid(accessTokenObject, errorResponse)){
            return errorResponse;
        }

        // validate refresh token
        AuthenticationToken refreshTokenObject = new AuthenticationToken();
        refreshTokenObject.setRefreshToken(request.getRefreshToken());
        if(!isRefreshTokenValid(refreshTokenObject, errorResponse)){
            return errorResponse;
        }

        // Handle mismatch between user ids of the tokens
        String accessTokenUserId = AuthenticationToken.getSub(accessToken);
        String refreshTokenUserId = AuthenticationToken.getSub(request.getRefreshToken());
        if(!accessTokenUserId.equals(refreshTokenUserId)){
            ErrorBody errorBody = new ErrorBody("Error", "Unauthorized. Mismatch of access and refresh token ids");
            return new ApiResponse(401, errorBody);
        }


        // Handle token not found in storage
        AccessTokenDTO token = tokenRepository.fetchRefreshToken(request.getRefreshToken());
        if(token.getTokenId().toString().isEmpty()){
            ErrorBody errorBody = new ErrorBody("Error","Invalid refresh token");
            return new ApiResponse(401, errorBody);
        }

        // Response
        LogoutResponse logoutResponse = new LogoutResponse("Logged out successfully",
                token.getUserId().toString());

        return new ApiResponse(200, logoutResponse);


    }

    public ApiResponse renewAccessToken(RenewAccessTokenRequest request){
        ApiResponse errorResponse = new ApiResponse();

        // validate refresh token
        AuthenticationToken refreshTokenObject = new AuthenticationToken();
        refreshTokenObject.setRefreshToken(request.getAccessToken());
        if(!isRefreshTokenValid(refreshTokenObject, errorResponse)){
            return errorResponse;
        }

        // Get new access token
        String tokenSub = AuthenticationToken.getSub(refreshTokenObject.getRefreshToken());
        String newAccessToken = new AuthenticationToken().getAccessToken(tokenSub,new HashMap<>());

        // Response
        RenewAccessTokenResponse renewAccessTokenResponse  = new RenewAccessTokenResponse(
                newAccessToken,
                AuthenticationToken.getAccessTokenExpiryDuration()
        );


        return new ApiResponse(200, renewAccessTokenResponse);

    }

    // Helper functions
    public ApiResponse validateRegistrationInputs(SignupRequest request, User user) {

        // validate name fields
        if (!user.isFirstNameValid()) {
            ErrorBody errorBody = new ErrorBody("Error","Invalid first name");
            return new ApiResponse(400, errorBody);
        }

        if (!user.isLastNameValid()) {
            ErrorBody errorBody = new ErrorBody("Error","Invalid last name");
            return new ApiResponse(400, errorBody);
        }

        if (!user.isUserNameValid()) {
            ErrorBody errorBody = new ErrorBody("Error","Invalid user name");
            return new ApiResponse(400, errorBody);
        }

        // Check if userName exists
        UserDTO userByName = userRepository.getUserByUserName(user.getUserName().toLowerCase());
        if(!(userByName.getUserId() == null)){
            ErrorBody errorBody = new ErrorBody("Error","User name already taken");
            return new ApiResponse(400, errorBody);
        }

        // check if passwords match
        if (!User.doPasswordsMatch(request.getPassword(), request.getConfirmPassword())) {
            ErrorBody errorBody = new ErrorBody("Error","The passwords provided do not match");
            return new ApiResponse(400, errorBody);

        }
        // check password strength
        if (!User.isPasswordStrong(request.getPassword())) {
            ErrorBody errorBody = new ErrorBody("Error","The password provided is not Strong");
            return new ApiResponse(400, errorBody);
        }

        // validate email
        if (!user.isEmailValid()){
            ErrorBody errorBody = new ErrorBody("Error","Your Email is invalid");
            return  new ApiResponse(400, errorBody);
        }

        // check if account already exists
        UserDTO userByEmail = userRepository.getUserByEmail(user.getEmail().toLowerCase());
        if(!(userByEmail.getUserId() == null)){
            ErrorBody errorBody = new ErrorBody("Error","Account created using this email already exists");
            return new ApiResponse(400, errorBody);
        }


        return null;
    }

    public boolean isAccessTokenValid(AuthenticationToken tokenObject, ApiResponse apiResponse){

        // Handle empty token
        if(tokenObject.isAccessTokenProvided()){
            ErrorBody errorBody = new ErrorBody("Error", "No access token provided in the header provided");
            apiResponse = new ApiResponse(401,errorBody);
            return false;
        }

        // Handle compromised refresh token
        if(AuthenticationToken.isAccessTokenCompromised(tokenObject.getAccessToken())){
            ErrorBody errorBody = new ErrorBody("Error", "The integrity of the access token has been compromised");
            apiResponse = new ApiResponse(401,errorBody);
            return false;
        }

        // Handle expired token
        if(AuthenticationToken.isTokenExpired(tokenObject.getAccessToken())){
            ErrorBody errorBody = new ErrorBody("Error", "Access token has expired");
            apiResponse = new ApiResponse(401,errorBody);
            return false;
        }

        return true;
    }

    public boolean isRefreshTokenValid(AuthenticationToken tokenObject, ApiResponse apiResponse){
        // Handle empty token
        if(tokenObject.isRefreshTokenProvided()){
            ErrorBody errorBody = new ErrorBody("Error", "No refresh token provided in the header provided");
            apiResponse = new ApiResponse(401,errorBody);
            return false;
        }

        // check for refresh token in storage
        TokenDAO tokenDAO = new TokenDAO();
        AccessTokenDTO tokenDTO = tokenDAO.fetchRefreshToken(tokenObject.getRefreshToken());

        if(tokenDTO.getTokenId().toString().isEmpty()){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid refresh token");
            apiResponse = new ApiResponse(401,errorBody);
            return false;
        }

        // Handle compromised refresh token
        if(AuthenticationToken.isRefreshTokenCompromised(tokenObject.getRefreshToken())){
            ErrorBody errorBody = new ErrorBody("Error", "The integrity of the refresh token has been compromised");
            apiResponse = new ApiResponse(401,errorBody);
            return false;
        }

        // Handle expired token
        if(AuthenticationToken.isTokenExpired(tokenObject.getRefreshToken())){
            ErrorBody errorBody = new ErrorBody("Error", "Refresh token has expired");
            apiResponse = new ApiResponse(401,errorBody);
            return false;
        }

        return true;
    }


}
