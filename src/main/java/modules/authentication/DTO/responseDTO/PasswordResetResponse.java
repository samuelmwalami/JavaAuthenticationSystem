package modules.authentication.DTO.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetResponse extends ResponseBody{
    String message;

    public PasswordResetResponse(){
    }

    public PasswordResetResponse(String message){
        this.message = message;
    }
}
