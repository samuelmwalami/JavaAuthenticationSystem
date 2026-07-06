package modules.authentication.DTO.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteUserResponse extends ResponseBody {
    String message;
    String userId;

    public DeleteUserResponse(){}

    public DeleteUserResponse(String message){
        this.message = message;
    }
}
