package modules.authentication.DTO.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutResponse extends ResponseBody{
    String message;
    String userId;

    LogoutResponse(){}
    public LogoutResponse(String message, String userId){
        this.message = message;
        this.userId = userId;
    }
}