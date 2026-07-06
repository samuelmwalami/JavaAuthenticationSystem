package modules.authentication.DTO.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailResponse extends ResponseBody {
    String message;

    public VerifyEmailResponse(){
    }
    public VerifyEmailResponse(String message){
        this.message = message;
    }
}
