package modules.authentication.API;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.authentication.services.AuthenticationService;
import modules.authentication.services.dto.requestDTO.UserDetailsRequest;
import modules.authentication.services.dto.responseDTO.Response;
import modules.authentication.services.dto.responseDTO.UserDetailsResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/auth/user")
public class UserDetails extends HttpServlet {
    AuthenticationService authenticationService = new AuthenticationService();
    ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response){

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF8");


        try {
            BufferedReader reader = request.getReader();
            StringBuilder requestBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBuffer.append(line);
            }
            String requestJSON  = requestBuffer.toString();
            UserDetailsRequest userDetailsRequest = mapper.readValue(requestJSON, UserDetailsRequest.class);

            PrintWriter out = response.getWriter();
            Response<UserDetailsResponse> responseObject = authenticationService.getUserDetailsByEmail(userDetailsRequest);
            String responseJSON = mapper.writeValueAsString(responseObject);
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
