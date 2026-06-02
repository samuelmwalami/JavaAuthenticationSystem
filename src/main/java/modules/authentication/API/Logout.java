package modules.authentication.API;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.authentication.DTO.requestDTO.LogoutRequest;
import modules.authentication.DTO.responseDTO.LogoutResponse;
import modules.authentication.DTO.responseDTO.Response;
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
        try {
            StringBuilder requestBuffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;

            while((line = reader.readLine()) != null){
                requestBuffer.append(line);
            }

            String requestJSON = requestBuffer.toString();
            LogoutRequest logoutRequest = mapper.readValue(requestJSON,LogoutRequest.class);

            Response<LogoutResponse> logoutResponse= authenticationService.logoutUser(logoutRequest);
            String responseJSON = mapper.writeValueAsString(logoutResponse);

            PrintWriter out  = response.getWriter();
            out.write(responseJSON);

            if(!logoutResponse.errors.isEmpty()){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            else{
                response.setStatus(HttpServletResponse.SC_OK);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
