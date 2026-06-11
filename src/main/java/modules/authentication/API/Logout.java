package modules.authentication.API;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.authentication.DTO.requestDTO.LogoutRequest;
import modules.authentication.DTO.responseDTO.ApiResponse;
import modules.authentication.services.AuthenticationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/auth/logout")
public class Logout extends HttpServlet {
    AuthenticationService authenticationService;
    ObjectMapper mapper;
    @Override
    public void init(){
        authenticationService = new AuthenticationService();
        mapper = new ObjectMapper();
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

            String requestJSON = requestBuffer.toString();
            LogoutRequest logoutRequest = mapper.readValue(requestJSON,LogoutRequest.class);

            ApiResponse logoutResponse= authenticationService.logoutUser(logoutRequest,accessToken);
            String responseJSON = mapper.writeValueAsString(logoutResponse);
            response.setStatus(logoutResponse.getStatusCode());

            PrintWriter out  = response.getWriter();
            out.write(responseJSON);

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
