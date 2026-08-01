package modules.authentication.services;


import EventBus.EventBus;
import EventBus.EventType;
import EventBus.EventOtpDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import modules.authentication.DTO.requestDTO.*;
import modules.authentication.DTO.responseDTO.*;
import modules.authentication.DTO.commonDTO.*;
import modules.authentication.Domain.*;
import modules.authentication.infrastructure.security.MessageDigestInfrastructure;
import modules.authentication.infrastructure.storage.*;
import modules.authentication.repository.security.MessageDigestRepository;
import modules.authentication.repository.storage.*;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.UUID;

public class AuthenticationService{
    // Storage access Object for User
    UserRepository userRepository;
    // Storage access object for refresh tokens
    TokenRepository tokenRepository;
    // MessageDigest infrastructure
    MessageDigestRepository messageDigestInfrastructure;
    // Storage access object for OTP
    OtpRepository otpRepository;
    ObjectMapper mapper = new ObjectMapper();

    private AuthenticationService(
            UserRepository userRepository,
            TokenRepository tokenRepository,
            MessageDigestInfrastructure messageDigestInfrastructure,
            OtpRepository otpRepository){
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.messageDigestInfrastructure = messageDigestInfrastructure;
        this.otpRepository = otpRepository;

    }

    public ApiResponse registerUser(SignupRequest request) {
         new ApiResponse();

        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getUserName(),
                request.getEmail()
        );

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

        //Generate otp
        String otpString = OTP.generateOTP();
        LocalDateTime otpExpiry = OTP.getOtpExpiryTime();



        OtpDTO otpDTO = new OtpDTO(
                Generators.timeBasedEpochGenerator().generate(),
                otpString,
                otpExpiry,
                user.getEmail()
        );

        // save otp
        if(otpRepository.saveOtp(otpDTO) != 1){
            SignupResponse signupResponse = new SignupResponse("Account created successfully but could not send ant otp to your email. Sign in to get an otp code.",
                    user.getUserId().toString(),
                    user.getEmail()
            );
            return new ApiResponse(401, signupResponse);
        }

        // publish user registration event
        try {
            EventOtpDTO registrationEventMessageObject = new EventOtpDTO(user.getEmail(), otpString);
            String registrationEventMessageJson = mapper.writeValueAsString(registrationEventMessageObject);
            EventBus.getInstance().publish(EventType.REGISTRATION, registrationEventMessageJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Response
        SignupResponse signupResponse = new SignupResponse("Account created successfully. Check your email for an OTP code and verify your email",
                user.getUserId().toString(),
                user.getEmail()
        );
        return new ApiResponse(201, signupResponse);
    }

    public ApiResponse verifyEmail(VerifyEmailRequest request){
        //Validate email
        User user = new User();
        user.setEmail(request.getEmail());
        if(!user.isEmailValid()){
            ErrorBody errorBody =  new ErrorBody("Error", "Invalid email");
            return new ApiResponse(400, errorBody);
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


        // retrieve stored otp
        OtpDTO otpDTO = otpRepository.retrieveOtpByOtpAndEmail(request.getOtp(),user.getEmail());
        if(otpDTO.getOtp() == null){
            ErrorBody errorBody = new ErrorBody("ERROR","Invalid OTP code");
            return new ApiResponse(401, errorBody);
        }

        // Check otpExpiry
        if(OTP.isOtpExpired(otpDTO.getOtpExpiry())){
            ErrorBody errorBody = new ErrorBody("ERROR","The OTP code has expired");
            return new ApiResponse(401, errorBody);
        }

        if(userRepository.setTrueEmailVerificationStatus(user.getEmail()) != 1){
            ErrorBody errorBody = new ErrorBody("ERROR","There has been a problem verifying the OTP code. Try Again");
            return new ApiResponse(500, errorBody);
        }

        VerifyEmailResponse verifyEmailResponse = new VerifyEmailResponse("Email verified successfully");

        return new ApiResponse(200,verifyEmailResponse);

    }

    public ApiResponse getOTPCode(OtpRequest request){

        //Validate email
        User user = new User();
        user.setEmail(request.getEmail());
        if(!user.isEmailValid()){
            ErrorBody errorBody =  new ErrorBody("Error", "Invalid email");
            return new ApiResponse(400, errorBody);
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
        // retrieve stored otp
        OtpDTO otpDTO = otpRepository.retrieveOtpByEmail(user.getEmail());

        LocalDateTime otpCreationTime;
        LocalDateTime otpWindowLimit = null;
        // get otp creation time
        if(otpDTO.getOtp() != null){
            otpCreationTime = otpDTO.getOtpExpiry().minusSeconds(OTP.getOTP_EXPIRY_DURATION());
            otpWindowLimit = otpCreationTime.plusSeconds(OTP.getGET_NEW_OTP_WINDOW_DURATION());
        }
        // Limit creating new otp request to after 30 seconds window
        if(otpDTO.getOtp() != null && otpWindowLimit.isAfter(LocalDateTime.now(ZoneId.of("UTC")))){
            ErrorBody errorBody = new ErrorBody("ERROR","Cannot request new OTP within 30 seconds after the first request");
            return new ApiResponse(401, errorBody);
        }


        //Generate otp
        String otpString = OTP.generateOTP();
        LocalDateTime otpExpiry = OTP.getOtpExpiryTime();


        OtpDTO newOtpDTO = new OtpDTO(
                Generators.timeBasedEpochGenerator().generate(),
                otpString,
                otpExpiry,
                user.getEmail()
        );

        // save otp in storage
        if(otpRepository.saveOtp(newOtpDTO) != 1){
            ErrorBody errorBody = new ErrorBody("Error","Could not verify email please try again");
            return new ApiResponse(401, errorBody);
        }

        // publish otp event
        try {
            EventOtpDTO otpEventMessageObject = new EventOtpDTO(user.getEmail(), otpString);
            String otpEventMessageJson = mapper.writeValueAsString(otpEventMessageObject);
            EventBus.getInstance().publish(EventType.OTP, otpEventMessageJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        // response
        OtpResponse verifyEmailResponse = new OtpResponse("Check your email for an OTP code", user.getEmail());
        return new ApiResponse(200,verifyEmailResponse);
    }

    public ApiResponse loginUser(LoginRequest request){
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        // Validate email
        if(!user.isEmailValid()){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid email");
            return new ApiResponse(400, errorBody);
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
        String otpString = OTP.generateOTP();
        LocalDateTime otpExpiry = OTP.getOtpExpiryTime();

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
        }

        // publish login event
        try{
            EventOtpDTO loginEventMessageObject = new EventOtpDTO(user.getEmail(), otpString);
            String loginEventMessageString = mapper.writeValueAsString(loginEventMessageObject);
            EventBus.getInstance().publish(EventType.LOGIN, loginEventMessageString);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Response
        LoginResponse loginResponse = new LoginResponse("Check your email for an OTP code and verify your login");

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
        if(!OTP.isOtpValid(request.getOtp())){
            ErrorBody errorBody = new ErrorBody("Error", "Use a valid OTP");
            return  new ApiResponse(401, errorBody);
        }

        // get otp from storage
        OtpDTO otpDTO = otpRepository.retrieveOtpByOtpAndEmail(request.getOtp(),request.getEmail());

        // Check if otp has been retrieved from storage
        if (otpDTO.getOtpID() == null){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid OTP code or email");
            return new ApiResponse(401, errorBody);
        }

        // Check for otp expiry
        if (OTP.isOtpExpired(otpDTO.getOtpExpiry())){
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
        // get access token
        AccessToken accessTokenObject = new AccessToken();
        String accessToken = accessTokenObject.getAccessToken(userDTO.getUserId().toString(),new HashMap<>());
        // get refreshToken
        RefreshToken refreshTokenObject = new RefreshToken();
        String refreshToken = refreshTokenObject.getRefreshToken(userDTO.getUserId().toString(), new HashMap<>());

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
                RefreshToken.getRefreshTokenExpiryDuration(),

                AccessToken.getAccessTokenExpiryDuration()
        );

        return new ApiResponse(200,verifyLoginResponse);

    }

    public ApiResponse logoutUser(LogoutRequest request, String accessToken){
        // validate access token
        AccessToken accessTokenObject = new AccessToken(accessToken);
        ApiResponse errorResponse = validateAccessToken(accessTokenObject);
        if(errorResponse != null){
            return errorResponse;
        }

        // validate refresh token
        RefreshToken refreshTokenObject = new RefreshToken();
        refreshTokenObject.setRefreshToken(request.getRefreshToken());
        errorResponse = validateRefreshToken(refreshTokenObject);
        if(errorResponse != null){
            return errorResponse;
        }

        // Handle mismatch between user ids of the tokens
        String accessTokenUserId = AccessToken.getSub(accessToken);
        String refreshTokenUserId = RefreshToken.getSub(request.getRefreshToken());
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
        // validate access token
        AccessToken accessTokenObject = new AccessToken(accessToken);
        ApiResponse errorResponse =validateAccessToken(accessTokenObject);
        if(errorResponse != null){
            return errorResponse;
        }

        User user = new User();
        user.setEmail(request.getEmail());

        //Validate input
        if(!user.isEmailValid()){
            ErrorBody apiError = new ErrorBody("Error", "Invalid email");
            return new ApiResponse(401,apiError);
        }

        // Retrieve userId from access token sub
        UUID userID = UUID.fromString(AccessToken.getSub(accessToken));
        // Fetch user from storage
        UserDTO userDTO = userRepository.getUserByEmailAndUserId(request.getEmail(), userID);

        // Handle token not found in storage
        if(userDTO.getUserId() == null){
            ErrorBody apiError = new ErrorBody("Error", "User does not exist");
            return new ApiResponse(404,apiError);
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
        // validate access token
        AccessToken accessTokenObject = new AccessToken(accessToken);
        ApiResponse errorResponse = validateAccessToken(accessTokenObject);
        if(errorResponse != null){
            return errorResponse;
        }



        // validate refresh token
        RefreshToken refreshTokenObject = new RefreshToken(request.getRefreshToken());
        errorResponse = validateRefreshToken(refreshTokenObject);
        if(errorResponse != null){
            return errorResponse;
        }

        // Retrieve user id from access and refresh tokens
        String accessTokenUserId = AccessToken.getSub(accessToken);
        String refreshTokenUserId = RefreshToken.getSub(request.getRefreshToken());
        // Handle mismatch between user ids retrieved from tokens
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
        String otpString = OTP.generateOTP();
        LocalDateTime otpExpiry = OTP.getOtpExpiryTime();

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
        }

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

    public ApiResponse verifyDeleteUserAccount(VerifyDeleteUserRequest request, String accessToken){
        // validate access token
        AccessToken accessTokenObject = new AccessToken(accessToken);
        ApiResponse errorResponse = validateAccessToken(accessTokenObject);
        if(errorResponse != null){
            return errorResponse;
        }


        // validate refresh token
        RefreshToken refreshTokenObject = new RefreshToken(request.getRefreshToken());
        errorResponse = validateRefreshToken(refreshTokenObject);
        if(errorResponse != null){
            return errorResponse;
        }

        // Retrieve user id from access and refresh tokens
        String accessTokenUserId = AccessToken.getSub(accessToken);
        String refreshTokenUserId = RefreshToken.getSub(request.getRefreshToken());
        // Handle mismatch between user ids retrieved from tokens
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
            return new ApiResponse(404,apiError);
        }


        User user = new User();
        user.setEmail(request.getEmail());

        //Validate email
        if(!user.isEmailValid()){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid email");
            return new ApiResponse(400, errorBody);
        }

        // validate request OTP
        if(!OTP.isOtpValid(request.getOtp())){
            ErrorBody errorBody = new ErrorBody("Error", "Use a valid OTP");
            return  new ApiResponse(401, errorBody);
        }

        // get otp from storage
        OtpDTO otpDTO = otpRepository.retrieveOtpByOtpAndEmail(request.getOtp(),request.getEmail());

        // Check if otp has been retrieved from storage
        if (otpDTO.getOtpID() == null){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid OTP code or email");
            return new ApiResponse(401, errorBody);
        }

        // Check for otp expiry
        if (OTP.isOtpExpired(otpDTO.getOtpExpiry())){
            ErrorBody errorBody = new ErrorBody("Error", "Your OTP code has expired. Please get a new one");
            return new ApiResponse(401, errorBody);
        }

        // delete user
        int rowsAffected = userRepository.deleteUserByEmailAndUserId(user.getEmail(), UUID.fromString(refreshTokenUserId));

        // Handle token not found in storage
        if(rowsAffected != 1){
            ErrorBody errorBody = new ErrorBody("Error", "Error deleting account");
            return new ApiResponse(400, errorBody);
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
        String otpString = OTP.generateOTP();
        LocalDateTime otpExpiry = OTP.getOtpExpiryTime();

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
        }

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
        if(!OTP.isOtpValid(request.getOtp())){
            ErrorBody errorBody = new ErrorBody("Error", "Use a valid OTP code");
            return  new ApiResponse(401, errorBody);
        }

        // get otp from storage
        OtpDTO otpDTO = otpRepository.retrieveOtpByOtpAndEmail(request.getOtp(),request.getEmail());

        // Check if otp has been retrieved from storage
        if (otpDTO.getOtpID() == null){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid OTP code or email");
            return new ApiResponse(401, errorBody);
        }

        // Check for otp expiry
        if (OTP.isOtpExpired(otpDTO.getOtpExpiry())){
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

        // validate refresh token
        RefreshToken refreshTokenObject = new RefreshToken(request.getRefreshToken());
        ApiResponse errorResponse = validateRefreshToken(refreshTokenObject);
        if(errorResponse != null){
            return errorResponse;
        }

        // Get new access token
        String refreshTokenSub = RefreshToken.getSub(refreshTokenObject.getRefreshToken());
        String newAccessToken = new AccessToken().getAccessToken(refreshTokenSub,new HashMap<>());

        // Response
        RenewAccessTokenResponse renewAccessTokenResponse  = new RenewAccessTokenResponse(
                newAccessToken,
                AccessToken.getAccessTokenExpiryDuration()
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
//        UserDTO userByName = userRepository.getUserByUserName(user.getUserName().toLowerCase());
//        if(!(userByName.getUserId() == null)){
//            ErrorBody errorBody = new ErrorBody("Error","User name already taken");
//            return new ApiResponse(400, errorBody);
//        }

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

//        // check if account already exists
//        UserDTO userByEmail = userRepository.getUserByEmail(user.getEmail().toLowerCase());
//        if(!(userByEmail.getUserId() == null)){
//            ErrorBody errorBody = new ErrorBody("Error","Account created using this email already exists");
//            return new ApiResponse(400, errorBody);
//        }


        return null;
    }

    public ApiResponse validateAccessToken(AccessToken tokenObject){

        // Handle empty token
        if(tokenObject.isAccessTokenProvided()){
            ErrorBody errorBody = new ErrorBody("Error", "No access token provided in the header provided");
            return new ApiResponse(401,errorBody);
        }

        // Handle compromised refresh token
        if(AccessToken.isAccessTokenCompromised(tokenObject.getAccessToken())){
            ErrorBody errorBody = new ErrorBody("Error", "The integrity of the access token has been compromised");
            return new ApiResponse(401,errorBody);
        }

        // Handle expired token
        if(AccessToken.isTokenExpired(tokenObject.getAccessToken())){
            ErrorBody errorBody = new ErrorBody("Error", "Access token has expired");
            return new ApiResponse(401,errorBody);
        }

        return null;
    }

    public ApiResponse validateRefreshToken(RefreshToken tokenObject){
        // Handle empty token
        if(tokenObject.isRefreshTokenProvided()){
            ErrorBody errorBody = new ErrorBody("Error", "No refresh token provided in the header provided");
            return new ApiResponse(401,errorBody);
        }

        // check for refresh token in storage
        AccessTokenDTO tokenDTO = tokenRepository.fetchRefreshToken(tokenObject.getRefreshToken());

        if(tokenDTO.getTokenId().toString().isEmpty()){
            ErrorBody errorBody = new ErrorBody("Error", "Invalid refresh token");
            return new ApiResponse(401,errorBody);
        }

        // Handle compromised refresh token
        if(RefreshToken.isRefreshTokenCompromised(tokenObject.getRefreshToken())){
            ErrorBody errorBody = new ErrorBody("Error", "The integrity of the refresh token has been compromised");
            return new ApiResponse(401,errorBody);
        }

        // Handle expired token
        if(RefreshToken.isTokenExpired(tokenObject.getRefreshToken())){
            ErrorBody errorBody = new ErrorBody("Error", "Refresh token has expired");
            return new ApiResponse(401,errorBody);
        }

        return null;
    }

    public  static AuthenticationService getInstance(){
        return new AuthenticationService(
                new UserDAO(),
                new TokenDAO(),
                new MessageDigestInfrastructure(),
                new OtpDAO());
    }


}
