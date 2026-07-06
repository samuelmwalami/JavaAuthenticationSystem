package modules.authentication.API;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.authentication.DTO.requestDTO.VerifyEmailRequest;
import modules.authentication.DTO.responseDTO.ApiResponse;
import modules.authentication.services.AuthenticationService;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/auth/verify-email")
public class VerifyEmail extends HttpServlet {
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
            VerifyEmailRequest verifyEmailRequest = mapper.readValue(requestJSON, VerifyEmailRequest.class);

            ApiResponse apiResponse = authenticationService.verifyEmail(verifyEmailRequest);
            String responseJSON = mapper.writeValueAsString(apiResponse.getContent());

            response.getWriter().write(responseJSON);
            response.setStatus(apiResponse.getStatusCode());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
