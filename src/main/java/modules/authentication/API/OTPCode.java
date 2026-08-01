package modules.authentication.API;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.authentication.DTO.requestDTO.OtpRequest;
import modules.authentication.DTO.responseDTO.ApiResponse;
import modules.authentication.services.AuthenticationService;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/auth/otp")
public class OTPCode extends HttpServlet {
    AuthenticationService authenticationService;
    ObjectMapper mapper;
    @Override
    public void init(){
        authenticationService = AuthenticationService.getInstance();
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
            OtpRequest otpRequest = mapper.readValue(requestJSON, OtpRequest.class);

            ApiResponse apiResponse = authenticationService.getOTPCode(otpRequest);
            String responseJSON = mapper.writeValueAsString(apiResponse.getContent());
            response.setStatus(apiResponse.getStatusCode());

            response.getWriter().write(responseJSON);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
