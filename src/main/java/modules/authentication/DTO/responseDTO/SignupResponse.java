package modules.authentication.DTO.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupResponse extends ResponseBody {
    String message;
    String userId;

    public SignupResponse(){}
    public SignupResponse(String message, String userId){
        this.message = message;
        this.userId = userId;
            }
}
