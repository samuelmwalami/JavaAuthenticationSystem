package modules.authentication.services;


import EventBus.EventBus;
import EventBus.EventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import modules.authentication.DTO.requestDTO.*;
import modules.authentication.DTO.responseDTO.*;
import modules.authentication.DTO.commonDTO.*;
import modules.authentication.Domain.*;
import modules.authentication.infrastructure.security.MessageDigest;
import modules.authentication.infrastructure.storage.*;
import modules.authentication.repository.security.MessageDigestRepository;
import modules.authentication.repository.storage.*;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

public class AuthenticationService{
    UserRepository userRepository = new UserDAO(); // Storage access Object for User
    TokenRepository tokenRepository = new TokenDAO(); // Storage access object for AuthenticationToken
    MessageDigestRepository messageDigestInfrastructure = new MessageDigest(); // MessageDigest infrastructure
    OtpRepository otpRepository = new OtpDAO(); // Storage access object for OTP
    ObjectMapper mapper = new ObjectMapper();

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
        String hashedPassword = messageDigestInfrastructure.hashPassword(request.getPassword());

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
        SignupResponse signupResponse = new SignupResponse("Account created successfully. Check your email for OTP code to verify your email",
                user.getUserId().toString()
        );
        return new ApiResponse(201, signupResponse);
    }

    public ApiResponse verifyEmail(VerifyEmailRequest request){
        //Validate email
        User user = new User();
        user.setEmail(request.getEmail());
        if(!user.isEmailValid()){
            ErrorBody errorBody =  new ErrorBody("Error", "Invalid email");
            return new ApiResponse(401, errorBody);
        }

        // Check if account exists
        UserDTO userDTO = userRepository.getUserByEmail(user.getEmail());
        if(userDTO.getUserId() == null){
            ErrorBody errorBody = new ErrorBody("Error", "Account does not exist");
            return new ApiResponse(401, errorBody);
        }
        // Check if email is verified
        if(userDTO.isEmailVerified()){
            VerifyEmailResponse verifyEmailResponse = new VerifyEmailResponse("Email has already been verified");
            return new ApiResponse(200,verifyEmailResponse);
        }

        //Generate otp
        OTP otp = new OTP();
        String otpString = otp.generateOTP();
        LocalDateTime otpExpiry = otp.getOtpExpiry();


        //Send otp
        OtpDTO otpDTO = new OtpDTO(
                Generators.timeBasedEpochGenerator().generate(),
                otpString,
                otpExpiry,
                user.getEmail()
        );

        // save otp
        if(otpRepository.saveOtp(otpDTO) != 1){
            ErrorBody errorBody = new ErrorBody("Error","Could not verify email please try again");
            return new ApiResponse(401, errorBody);
        };

        // publish verify email event
        try {
            EventOtpDTO registrationEventMessageObject = new EventOtpDTO(user.getEmail(), otpString);
            String registrationEventMessageJson = mapper.writeValueAsString(registrationEventMessageObject);
            EventBus.getInstance().publish(EventType.REGISTRATION, registrationEventMessageJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // delete OTP from storage
        int otpRowsAffected = otpRepository.deleteOtpByEmail(user.getEmail());
        IO.println(String.format("Rows affected by deleting OTP: %s", otpRowsAffected));


        // response
        VerifyEmailResponse verifyEmailResponse = new VerifyEmailResponse("Check your email for OTP to verify your email");
        return new ApiResponse(200,verifyEmailResponse);

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
        if(!messageDigestInfrastructure.verifyPassword(userDTO.getPassword(),user.getPassword())){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid credentials");
            return new ApiResponse(401, errorBody);
        }

        //Generate otp
        OTP otp = new OTP();
        String otpString = otp.generateOTP();
        LocalDateTime otpExpiry = otp.getOtpExpiry();

        //Send otp
        OtpDTO otpDTO = new OtpDTO(
                Generators.timeBasedEpochGenerator().generate(),
                otpString,
                otpExpiry,
                user.getEmail()
        );

        // save otp
        if(otpRepository.saveOtp(otpDTO) != 1){
            ErrorBody errorBody = new ErrorBody("Error","Could not login please try again");
            return new ApiResponse(401, errorBody);
        };

        // publish login event
        try{
            EventOtpDTO loginEventMessageObject = new EventOtpDTO(user.getEmail(), otpString);
            String loginEventMessageString = mapper.writeValueAsString(loginEventMessageObject);
            EventBus.getInstance().publish(EventType.LOGIN, loginEventMessageString);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Response
        LoginResponse loginResponse = new LoginResponse("Check your email for OTP code to verify login");

        return new ApiResponse(200, loginResponse);
    }

    public ApiResponse verifyLogin(VerifyLoginRequest request){

        // validate request email
        User user  = new User();
        user.setEmail(request.getEmail());
        if (!user.isEmailValid()){
            ErrorBody errorBody = new ErrorBody("Error","Use a valid email");
            return new ApiResponse(401,errorBody);
        }
        // validate request OTP
        OTP otp = new OTP(request.getOtp());
        if(!otp.isOtpValid(request.getOtp())){
            ErrorBody errorBody = new ErrorBody("Error", "Use a valid OTP");
            return  new ApiResponse(401, errorBody);
        }

        // get otp from storage
        OtpDTO otpDTO = otpRepository.retrieveOtp(request.getOtp(),request.getEmail());

        // Check if otp has been retrieved from storage
        if (otpDTO.getOtpID() == null){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid OTP code or email");
            return new ApiResponse(401, errorBody);
        }

        // Check for otp expiry
        if (otp.isOtpExpired(otpDTO.getOtpExpiry())){
            ErrorBody errorBody = new ErrorBody("Error", "Your OTP code has expired. Please get a new one");
            return new ApiResponse(401, errorBody);
        }

        // Get user from Storage
        UserDTO userDTO = userRepository.getUserByEmail(request.getEmail());

        // check if user exists;
        if(userDTO.getUserId() == null || userDTO.getUserId().toString().isEmpty()){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid email");
            return new ApiResponse(401, errorBody);

        }

        // Get access and refresh tokens
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

        // delete OTP from storage
        int otpRowsAffected = otpRepository.deleteOtpByEmail(user.getEmail());
        IO.println(String.format("Rows affected by deleting OTP: %s", otpRowsAffected));

        // Response
        VerifyLoginResponse verifyLoginResponse = new VerifyLoginResponse("Logged in successfully",
                refreshToken,
                accessToken,
                AuthenticationToken.getRefreshTokenExpiryDuration(),
                AuthenticationToken.getAccessTokenExpiryDuration()
        );

        return new ApiResponse(200,verifyLoginResponse);

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

        //Generate otp
        OTP otp = new OTP();
        String otpString = otp.generateOTP();
        LocalDateTime otpExpiry = otp.getOtpExpiry();

        //Send otp
        OtpDTO otpDTO = new OtpDTO(
                Generators.timeBasedEpochGenerator().generate(),
                otpString,
                otpExpiry,
                user.getEmail()
        );

        // save otp
        if(otpRepository.saveOtp(otpDTO) != 1){
            ErrorBody errorBody = new ErrorBody("Error","Could not login please try again");
            return new ApiResponse(401, errorBody);
        };

        // publish delete user account event
        try{
            EventOtpDTO deleteAccountEventMessageObject = new EventOtpDTO(user.getEmail(), otpString);
            String deleteAccountEventMessageJSON = mapper.writeValueAsString(deleteAccountEventMessageObject);
            EventBus.getInstance().publish(EventType.DELETE_ACCOUNT, deleteAccountEventMessageJSON);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        DeleteUserResponse deleteUserResponse = new DeleteUserResponse(
                "Check your email for OTP Code to verify account deletion");

        return new ApiResponse(200, deleteUserResponse);
    }


    public ApiResponse verifyDeleteUserAccount(VerifyDeleteUserRequest request, String accessToken){        ApiResponse errorResponse = new ApiResponse();

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

        // Verify if account exists
        UUID userID = UUID.fromString(accessTokenUserId);
        // Fetch user from storage
        UserDTO userDTO = userRepository.getUserByEmailAndUserId(request.getEmail(), userID);

        // Handle token not found in storage
        if(userDTO.getUserId() == null){
            ErrorBody apiError = new ErrorBody("Error", "User does not exist");
            return new ApiResponse(401,apiError);
        }


        User user = new User();
        user.setEmail(request.getEmail());

        //Validate email
        if(!user.isEmailValid()){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid email");
            return new ApiResponse(400, errorBody);
        }

        // validate request OTP
        OTP otp = new OTP(request.getOtp());
        if(!otp.isOtpValid(request.getOtp())){
            ErrorBody errorBody = new ErrorBody("Error", "Use a valid OTP");
            return  new ApiResponse(401, errorBody);
        }

        // get otp from storage
        OtpDTO otpDTO = otpRepository.retrieveOtp(request.getOtp(),request.getEmail());

        // Check if otp has been retrieved from storage
        if (otpDTO.getOtpID() == null){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid OTP code or email");
            return new ApiResponse(401, errorBody);
        }

        // Check for otp expiry
        if (otp.isOtpExpired(otpDTO.getOtpExpiry())){
            ErrorBody errorBody = new ErrorBody("Error", "Your OTP code has expired. Please get a new one");
            return new ApiResponse(401, errorBody);
        }

        // delete user
        int rowsAffected = userRepository.deleteUserByEmailAndUserId(user.getEmail(), UUID.fromString(refreshTokenUserId));

        // Handle token not found in storage
        if(rowsAffected != 1){
            ErrorBody errorBody = new ErrorBody("Error", "Error deleting account");
            return new ApiResponse(404, errorBody);
        }

        // delete OTP from storage
        int otpRowsAffected = otpRepository.deleteOtpByEmail(user.getEmail());
        IO.println(String.format("Rows affected by deleting OTP: %s", otpRowsAffected));

        // response
        VerifyDeleteUserResponse verifyDeleteUserResponse = new VerifyDeleteUserResponse(
                "Account deleted successfully",
                refreshTokenUserId,
                user.getEmail()
                );


        return new ApiResponse(200, verifyDeleteUserResponse);
    }


    public ApiResponse resetPassword(PasswordResetRequest request){
        User user = new User();
        user.setEmail(request.getEmail());

        //Validate email
        if(!user.isEmailValid()){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid email");
            return new ApiResponse(400, errorBody);
        }

        // Get user from Storage
        UserDTO userDTO = userRepository.getUserByEmail(request.getEmail());

        // check if user exists;
        if(userDTO.getUserId() == null || userDTO.getUserId().toString().isEmpty()){
            ErrorBody errorBody = new ErrorBody("Error", "Account with that email does not exist");
            return new ApiResponse(401, errorBody);

        }

        //Generate otp
        OTP otp = new OTP();
        String otpString = otp.generateOTP();
        LocalDateTime otpExpiry = otp.getOtpExpiry();

        //Send otp
        OtpDTO otpDTO = new OtpDTO(
                Generators.timeBasedEpochGenerator().generate(),
                otpString,
                otpExpiry,
                user.getEmail()
        );

        // save otp
        if(otpRepository.saveOtp(otpDTO) != 1){
            ErrorBody errorBody = new ErrorBody("Error","Could not login please try again");
            return new ApiResponse(401, errorBody);
        };

        // publish password reset event
        try{
            EventOtpDTO passwordResetEventMessageObject = new EventOtpDTO(user.getEmail(), otpString);
            String passwordResetEventMessageJSON = mapper.writeValueAsString(passwordResetEventMessageObject);
            EventBus.getInstance().publish(EventType.PASSWORD_RESET, passwordResetEventMessageJSON);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        PasswordResetResponse passwordResetResponse = new PasswordResetResponse("Check your email for the OTP code to reset your password");
        return new ApiResponse(200, passwordResetResponse);
    }

    public ApiResponse verifyPasswordReset(VerifyPasswordResetRequest request){
        User user = new User();
        user.setEmail(request.getEmail());

        //Validate email
        if(!user.isEmailValid()){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid email");
            return new ApiResponse(400, errorBody);
        }

        // Get user from Storage
        UserDTO userDTO = userRepository.getUserByEmail(request.getEmail());

        // check if user exists;
        if(userDTO.getUserId() == null || userDTO.getUserId().toString().isEmpty()){
            ErrorBody errorBody = new ErrorBody("Error", "Account with that email does not exist");
            return new ApiResponse(401, errorBody);

        }

        // validate request OTP
        OTP otp = new OTP(request.getOtp());
        if(!otp.isOtpValid(request.getOtp())){
            ErrorBody errorBody = new ErrorBody("Error", "Use a valid OTP");
            return  new ApiResponse(401, errorBody);
        }

        // get otp from storage
        OtpDTO otpDTO = otpRepository.retrieveOtp(request.getOtp(),request.getEmail());

        // Check if otp has been retrieved from storage
        if (otpDTO.getOtpID() == null){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid OTP code or email");
            return new ApiResponse(401, errorBody);
        }

        // Check for otp expiry
        if (otp.isOtpExpired(otpDTO.getOtpExpiry())){
            ErrorBody errorBody = new ErrorBody("Error", "Your OTP code has expired. Please get a new one");
            return new ApiResponse(401, errorBody);
        }

        // Password verification
        // check if passwords match
        if (!User.doPasswordsMatch(request.getPassword(), request.getConfirmPassword())) {
            ErrorBody errorBody = new ErrorBody("Error","The passwords provided do not match");
            return new ApiResponse(400, errorBody);

        }
        // check password strength
        if (!User.isPasswordStrong(request.getPassword())) {
            ErrorBody errorBody = new ErrorBody("Error","The password does not meet the minimum strength criteria.");
            return new ApiResponse(400, errorBody);
        }

        // Update password in storage
        String hashedPassword = messageDigestInfrastructure.hashPassword(request.getPassword());
        if(userRepository.updatePasswordByEmail(user.getEmail(), hashedPassword) != 1){
            ErrorBody errorBody  = new ErrorBody("Error", "There was a problem resetting the password. Please try again");
            return new ApiResponse(401, errorBody);
        }

        // delete OTP from storage
        int otpRowsAffected = otpRepository.deleteOtpByEmail(user.getEmail());
        IO.println(String.format("Rows affected by deleting OTP: %s", otpRowsAffected));

        // response
        VerifyPasswordResetResponse verifyPasswordResetResponse = new VerifyPasswordResetResponse("Password reset successfully");
        return new ApiResponse(200, verifyPasswordResetResponse);

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
            ErrorBody errorBody = new ErrorBody("Error","The password does not meet the required strength criteria.");
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
