package modules.authentication.DTO.responseDTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    String message;
    String accessToken;
    int accessTokenExpirationDuration;
    String refreshToken;
    int refreshTokenExpirationDuration;
}
