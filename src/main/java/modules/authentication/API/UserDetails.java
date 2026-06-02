package modules.authentication.API;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.authentication.DTO.requestDTO.SignupRequest;
import modules.authentication.services.AuthenticationService;
import modules.authentication.DTO.requestDTO.UserDetailsRequest;
import modules.authentication.DTO.responseDTO.Response;
import modules.authentication.DTO.responseDTO.UserDetailsResponse;

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


        try {
            BufferedReader reader = request.getReader();
            StringBuilder requestBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBuffer.append(line);
            }

            String requestJSON  = requestBuffer.toString();
            UserDetailsRequest userDetailsRequest = mapper.readValue(requestJSON, UserDetailsRequest.class);


            Response<UserDetailsResponse> responseObject = authenticationService.getUserDetailsByEmail(userDetailsRequest);
            String responseJSON = mapper.writeValueAsString(responseObject);

            PrintWriter out = response.getWriter();
            out.write(responseJSON);

            if(!responseObject.errors.isEmpty()){
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

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response){
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        try {
            StringBuilder requestBuffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;

            while((line = reader.readLine()) != null){
                requestBuffer.append(line);
            }

            String requestJSON  = requestBuffer.toString();
            UserDetailsRequest userDetailsRequest = mapper.readValue(requestJSON, UserDetailsRequest.class);

            Response<UserDetailsResponse> responseObject = authenticationService.getUserDetailsByEmail(userDetailsRequest);
            String responseJSON = mapper.writeValueAsString(responseObject);

            PrintWriter out = response.getWriter();
            out.write(responseJSON);

            if(!responseObject.errors.isEmpty()){
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
