package modules.authentication.DTO.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupResponse extends ResponseBody {
    String message;
    String userId;
    String email;

    public SignupResponse(){}
    public SignupResponse(String message, String userId,String email){
        this.message = message;
        this.userId = userId;
        this.email = email;
            }
}
