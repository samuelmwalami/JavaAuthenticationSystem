package modules.authentication.API;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.authentication.DTO.responseDTO.ApiResponse;
import modules.authentication.services.AuthenticationService;
import modules.authentication.DTO.requestDTO.SignupRequest;


import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/auth/signup")
public class SignUp extends HttpServlet {
    ObjectMapper mapper;
    AuthenticationService authenticationService;

    @Override
    public void init(){
        authenticationService = AuthenticationService.getInstance();
        mapper = new ObjectMapper();
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        try {
            StringBuilder requestBuffer= new StringBuilder();
            BufferedReader requestReader = request.getReader();
            String requestLine;

            while ((requestLine = requestReader.readLine()) != null){
                requestBuffer.append(requestLine);
            }

            String requestJSON = requestBuffer.toString();
            SignupRequest signupRequest = mapper.readValue(requestJSON, SignupRequest.class);

            ApiResponse apiResponse = authenticationService.registerUser(signupRequest);
            String responseJSON = mapper.writeValueAsString(apiResponse.getContent());

            response.getWriter().write(responseJSON);

            response.setStatus(apiResponse.getStatusCode());

        }
        catch (IOException e){
            System.out.println(e);
        }
    }

}
