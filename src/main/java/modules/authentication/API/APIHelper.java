package modules.authentication.API;

import jakarta.servlet.http.HttpServletRequest;

public class APIHelper {
    public static String retrieveAccessTokenFromHeader(HttpServletRequest request){
        String authorizationHeader = request.getHeader("Authorization");
        if(authorizationHeader == null){
            return "";
        }
        if (!authorizationHeader.contains("Bearer")){
            return "";
        }
        String[] headerParts = authorizationHeader.split("\\s+");
        return headerParts[1];
    }
}
