package Servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/auth/signup")
public class SignUp extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        try {
            StringBuilder requestBuffer= new StringBuilder();
            BufferedReader requestReader = request.getReader();
            String requestLine;

            while ((requestLine = requestReader.readLine()) != null){
                requestBuffer.append(requestLine);
            }
            String requestJSON = requestBuffer.toString();

            response.setCharacterEncoding("UTF8");
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            out.write(requestJSON);

        }
        catch (IOException e){
            System.out.println(e);
        }
    }
}
