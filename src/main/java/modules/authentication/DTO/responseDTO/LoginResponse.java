package modules.authentication.DTO.responseDTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse extends ResponseBody {
    String message;
    String refreshToken;
    String accessToken;
    int refreshTokenExpirationDuration;
    int accessTokenExpirationDuration;


    public LoginResponse(){}

    public LoginResponse(String message,
    String accessToken,
    String refreshToken,
    int refreshTokenExpirationDuration,
    int accessTokenExpirationDuration
    ){
        this.message = message;
        this.accessToken =  accessToken;
        this.refreshToken =  refreshToken;
        this.refreshTokenExpirationDuration = refreshTokenExpirationDuration;
        this.accessTokenExpirationDuration = accessTokenExpirationDuration;
    }

}
