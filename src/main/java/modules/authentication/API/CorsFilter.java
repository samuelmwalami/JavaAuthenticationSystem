package modules.authentication.API;


import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class CorsFilter implements Filter {
    // 50mb max request size
    private static final long MAX_REQUEST_SIZE = 1024 *1024 * 50;

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

        // set max request size
        if(req.getContentLengthLong() > MAX_REQUEST_SIZE){
            res.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            res.setContentType("application/json");
            res.getWriter().write("{message: \"ERRROR\",\ncontent : \"Content size too large\"}");
        }



        filterChain.doFilter(request,response);
    }
}
