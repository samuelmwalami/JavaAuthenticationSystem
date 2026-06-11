package modules.authentication.DTO.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenewAccessTokenResponse extends ResponseBody {
    String accessToken;
    int accessTokenExpirationDuration;

    public RenewAccessTokenResponse(){}
    public  RenewAccessTokenResponse(String accessToken, int accessTokenExpirationDuration){
        this.accessToken = accessToken;
        this.accessTokenExpirationDuration = accessTokenExpirationDuration;
    }
}
