package modules.authentication.API;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.authentication.services.AuthenticationService;
import modules.authentication.services.dto.responseDTO.Response;
import modules.authentication.services.dto.responseDTO.SignupResponse;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/auth/signup")
public class SignUp extends HttpServlet {
    ObjectMapper mapper = new ObjectMapper();
    AuthenticationService authenticationService = new AuthenticationService();

    @Override
    public void init(){
       // AuthenticationService authenticationService = new AuthenticationService();

    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        response.setCharacterEncoding("UTF8");
        response.setContentType("application/json");
        try {
            StringBuilder requestBuffer= new StringBuilder();
            BufferedReader requestReader = request.getReader();
            String requestLine;

            while ((requestLine = requestReader.readLine()) != null){
                requestBuffer.append(requestLine);
            }
            String requestJSON = requestBuffer.toString();

            PrintWriter out = response.getWriter();

            Response<SignupResponse> responseObject = authenticationService.registerUser(requestJSON);
            String responseJSON = mapper.writeValueAsString(responseObject);
            out.write(responseJSON);
            if (!responseObject.errors.isEmpty()){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            else{
                response.setStatus(HttpServletResponse.SC_CREATED);
            }


        }
        catch (IOException e){
            System.out.println(e);
        }
    }

}
