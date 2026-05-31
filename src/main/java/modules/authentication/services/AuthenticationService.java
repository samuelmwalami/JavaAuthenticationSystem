package modules.authentication.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import modules.authentication.Domain.User;
import modules.authentication.infrastructure.UserDAO;
import modules.authentication.repository.UserRepository;
import modules.authentication.Entities.UserEntity;
import modules.authentication.services.dto.requestDTO.UserDetailsRequest;
import modules.authentication.services.dto.responseDTO.UserDetailsResponse;
import modules.authentication.services.dto.responseDTO.Response;
import modules.authentication.services.dto.responseDTO.SignupResponse;

import java.util.ArrayList;

public class AuthenticationService{
    ObjectMapper mapper = new ObjectMapper();
    User userDomain = new User();
    UserRepository userRepository = new UserDAO();


    public Response<SignupResponse> registerUser(String request) {
        Response<SignupResponse> response = new Response<>();
        response.data = new SignupResponse("Error");

        try {
            UserEntity userEntity = mapper.readValue(request, UserEntity.class);
            ArrayList<String> errors = handleInvalidInputs(userEntity);

            // return if there is any error
            if (!errors.isEmpty()){
                response.errors.addAll(errors);
                return response;
            }
            userEntity.setFirstName(userEntity.getFirstName().toLowerCase());
            userEntity.setLastName(userEntity.getLastName().toLowerCase());
            userEntity.setUserName(userEntity.getUserName().toLowerCase());
            userEntity.setId(Generators.timeBasedEpochGenerator().generate());
            int rowsAffected = userRepository.saveUser(userEntity);

            if (rowsAffected != 0){
                response.errors.add("Account creation not successful");
                return response;
            }

            SignupResponse data = new SignupResponse("Account created Successfully");
            response.data = data;

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return response;
    }

    public  ArrayList<String> handleInvalidInputs(UserEntity userEntity){
        ArrayList<String> errors = new ArrayList<>();

        // validate name fields
        if (!userDomain.isNameValid(userEntity.getFirstName()) ||
                !userDomain.isNameValid(userEntity.getLastName()) ||
                !userDomain.isNameValid(userEntity.getUserName())) {
            errors.add("All name fields must be filled");
        }

        // validate password
        if (!userDomain.doPasswordsMatch(userEntity.getPassword(), userEntity.getConfirmPassword())) {
            errors.add("The passwords provided do not match");
        } else {
            if (userDomain.isPasswordStrong(userEntity.getPassword())) {
                errors.add("The password provided is not Strong");
            }
        }

        // validate email
        if (!userDomain.isEmailValid(userEntity.getEmail())) {
            errors.add("Your Email is invalid");
        }
        return errors;
    }

    public Response<UserDetailsResponse> getUserDetailsByEmail(UserDetailsRequest request){
        Response<UserDetailsResponse> response = new Response<>();
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse();
        response.data = userDetailsResponse;

        if(!userDomain.isEmailValid(request.getEmail())){
            response.errors.add("Invalid email");
        }

        UserRepository userRepository = new UserDAO();
        UserEntity user = userRepository.getUserByEmail(request.getEmail());

        if(user.getId() == null){
            response.errors.add("User does not exist");
            return response;
        }
        userDetailsResponse = mapUserToUserDetails(userDetailsResponse,user);
        response.data = userDetailsResponse;
        return response;
    }

    public UserDetailsResponse mapUserToUserDetails(UserDetailsResponse userDetailsResponse, UserEntity user){
        userDetailsResponse.setId(user.getId());
        userDetailsResponse.setFirstName(user.getFirstName());
        userDetailsResponse.setLastName(user.getLastName());
        userDetailsResponse.setUserName(user.getUserName());
        userDetailsResponse.setEmail(user.getEmail());
        userDetailsResponse.setCreatedAt(user.getCreatedAt().toString());
        return userDetailsResponse;
    }
}



