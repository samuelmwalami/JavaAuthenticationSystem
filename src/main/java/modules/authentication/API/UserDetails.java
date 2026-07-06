package modules.authentication.API;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.authentication.DTO.requestDTO.DeleteUserRequest;
import modules.authentication.DTO.requestDTO.VerifyDeleteUserRequest;
import modules.authentication.DTO.responseDTO.ApiResponse;
import modules.authentication.services.AuthenticationService;
import modules.authentication.DTO.requestDTO.UserDetailsRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/auth/user")
public class UserDetails extends HttpServlet {
    AuthenticationService authenticationService;
    ObjectMapper mapper;

    @Override
    public void init(){
        authenticationService = new AuthenticationService();
        mapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response){
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String accessToken = APIHelper.retrieveAccessTokenFromHeader(request);

        try {
            BufferedReader reader = request.getReader();
            StringBuilder requestBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBuffer.append(line);
            }

            String requestJSON  = requestBuffer.toString();
            UserDetailsRequest userDetailsRequest = mapper.readValue(requestJSON, UserDetailsRequest.class);


            ApiResponse userDetailsResponse = authenticationService.getUserDetailsByEmail(userDetailsRequest, accessToken);
            String responseJSON = mapper.writeValueAsString(userDetailsResponse.getContent());
            response.setStatus(userDetailsResponse.getStatusCode());

            PrintWriter out = response.getWriter();
            out.write(responseJSON);




        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        String accessToken = APIHelper.retrieveAccessTokenFromHeader(request);

        try {
            StringBuilder requestBuffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;

            while((line = reader.readLine()) != null){
                requestBuffer.append(line);
            }

            String requestJSON  = requestBuffer.toString();
            DeleteUserRequest deleteUserRequest = mapper.readValue(requestJSON, DeleteUserRequest.class);

            ApiResponse apiResponse = authenticationService.deleteUserAccount(deleteUserRequest, accessToken);
            String responseJSON = mapper.writeValueAsString(apiResponse.getContent());

            PrintWriter out = response.getWriter();
            out.write(responseJSON);

            response.setStatus(apiResponse.getStatusCode());

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response){
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        String accessToken = APIHelper.retrieveAccessTokenFromHeader(request);

        try {
            StringBuilder requestBuffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;

            while((line = reader.readLine()) != null){
                requestBuffer.append(line);
            }

            String requestJSON  = requestBuffer.toString();
            VerifyDeleteUserRequest verifyDeleteUserRequest = mapper.readValue(requestJSON, VerifyDeleteUserRequest.class);

            ApiResponse apiResponse = authenticationService.verifyDeleteUserAccount(verifyDeleteUserRequest, accessToken);
            String responseJSON = mapper.writeValueAsString(apiResponse.getContent());

            PrintWriter out = response.getWriter();
            out.write(responseJSON);

            response.setStatus(apiResponse.getStatusCode());

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }


}
