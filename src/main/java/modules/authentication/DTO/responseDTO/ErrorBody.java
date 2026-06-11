package modules.authentication.DTO.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorBody extends ResponseBody{
    String errorCode;
    String errorMessage;

    public ErrorBody(String errorCode, String errorMessage){
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;

    }
}
