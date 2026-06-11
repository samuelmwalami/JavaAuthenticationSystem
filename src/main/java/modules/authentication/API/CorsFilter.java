package modules.authentication.API;


import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        res.setHeader("Access-Control-Allow-Origin","*");
        res.setHeader("Access-Control-Allow-Methods","GET, POST, PUT, GET, DELETE");
        res.setHeader("Access-Control-Allow-Headers", "*");

        if("OPTIONS".equalsIgnoreCase(req.getMethod())){
            res.setStatus(HttpServletResponse.SC_OK);
        }

        filterChain.doFilter(request,response);
    }
}
