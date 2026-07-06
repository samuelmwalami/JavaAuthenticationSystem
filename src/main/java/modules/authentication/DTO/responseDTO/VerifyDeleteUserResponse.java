package modules.authentication.DTO.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyDeleteUserResponse extends ResponseBody{
    String message;
    String userID;
    String email;

    public VerifyDeleteUserResponse(){}

    public VerifyDeleteUserResponse(String message, String userID, String email){
        this.message = message;
        this.userID = userID;
        this.email = email;
    }
}
