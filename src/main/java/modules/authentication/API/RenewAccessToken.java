package modules.authentication.API;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.authentication.DTO.requestDTO.RenewAccessTokenRequest;
import modules.authentication.DTO.responseDTO.ApiResponse;
import modules.authentication.services.AuthenticationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/auth/token/access")
public class RenewAccessToken extends HttpServlet {
    AuthenticationService authenticationService;
    ObjectMapper mapper;
    @Override
    public void init(){
        authenticationService = new AuthenticationService();
        mapper = new ObjectMapper();
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        try {
            StringBuilder requestBuffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;

            while ((line = reader.readLine()) != null) {
                requestBuffer.append(line);
            }

            String requestJSON = requestBuffer.toString();
            RenewAccessTokenRequest renewAccessTokenRequest = mapper.readValue(requestJSON, RenewAccessTokenRequest.class);

            ApiResponse renewAccessTokenResponse = authenticationService.renewAccessToken(renewAccessTokenRequest);
            String responseJSON = mapper.writeValueAsString(renewAccessTokenResponse.getContent());
            response.setStatus(renewAccessTokenResponse.getStatusCode());

            PrintWriter out = response.getWriter();
            out.write(responseJSON);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
