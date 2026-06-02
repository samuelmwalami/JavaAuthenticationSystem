package modules.authentication.DTO.requestDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequest {
    String refreshToken;
}
