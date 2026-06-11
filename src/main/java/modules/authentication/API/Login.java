package modules.authentication.API;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.authentication.DTO.requestDTO.LoginRequest;
import modules.authentication.DTO.responseDTO.ApiResponse;
import modules.authentication.services.AuthenticationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet("/auth/login")
public class Login extends HttpServlet{
    AuthenticationService authenticationService;
    ObjectMapper mapper;
    @Override
    public void init(){
        authenticationService = new AuthenticationService();
        mapper = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        try {
            StringBuilder requestBuffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;

            while ((line = reader.readLine()) != null){
                requestBuffer.append(line);
            }

            String requestJSON = requestBuffer.toString();
            LoginRequest loginRequest = mapper.readValue(requestJSON, LoginRequest.class);

            ApiResponse loginResponse = authenticationService.loginUser(loginRequest);
            String responseJSON = mapper.writeValueAsString(loginResponse.getContent());
            response.setStatus(loginResponse.getStatusCode());

            PrintWriter out  = response.getWriter();
            out.write(responseJSON);
            

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }}
