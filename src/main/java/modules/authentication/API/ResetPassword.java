package modules.authentication.API;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.authentication.DTO.requestDTO.PasswordResetRequest;
import modules.authentication.DTO.requestDTO.VerifyLoginRequest;
import modules.authentication.DTO.requestDTO.VerifyPasswordResetRequest;
import modules.authentication.DTO.responseDTO.ApiResponse;
import modules.authentication.services.AuthenticationService;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/auth/password-reset")
public class ResetPassword  extends HttpServlet {
    AuthenticationService authenticationService;
    ObjectMapper mapper;

    @Override
    public void init(){
        authenticationService = new AuthenticationService();
        mapper  = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            BufferedReader reader = request.getReader();
            StringBuilder requestBuffer =  new StringBuilder();
            String line;

            while((line = reader.readLine()) != null){
                requestBuffer.append(line);
            }

            String requestJSON = requestBuffer.toString();
            PasswordResetRequest passwordResetRequest = mapper.readValue(requestJSON, PasswordResetRequest.class);

            ApiResponse apiResponse = authenticationService.resetPassword(passwordResetRequest);
            String responseJSON = mapper.writeValueAsString(apiResponse.getContent());

            response.getWriter().write(responseJSON);
            response.setStatus(apiResponse.getStatusCode());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response){
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            BufferedReader reader = request.getReader();
            StringBuilder requestBuffer =  new StringBuilder();
            String line;

            while((line = reader.readLine()) != null){
                requestBuffer.append(line);
            }

            String requestJSON = requestBuffer.toString();
            VerifyPasswordResetRequest verifyPasswordResetRequest = mapper.readValue(requestJSON, VerifyPasswordResetRequest.class);

            ApiResponse apiResponse = authenticationService.verifyPasswordReset(verifyPasswordResetRequest);
            String responseJSON = mapper.writeValueAsString(apiResponse.getContent());

            response.getWriter().write(responseJSON);
            response.setStatus(apiResponse.getStatusCode());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
