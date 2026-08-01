package modules.authentication.DTO.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpResponse extends ResponseBody {
    String message;
    String email;

    public OtpResponse(){}
    public OtpResponse(String message, String email){
        this.message = message;
        this.email = email;
    }
}
