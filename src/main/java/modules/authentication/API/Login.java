package modules.authentication.API;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet("/auth/login")
public class Login extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            PrintWriter out  = response.getWriter();
            System.out.println("Retrieving request body");

            //read
            StringBuilder body = new StringBuilder();
            BufferedReader requestBuffer = request.getReader();
            String line;

            while ((line = requestBuffer.readLine()) != null){
                body.append(line);
            }
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            String requestJSON = body.toString();

            out.write(requestJSON);



        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }}
