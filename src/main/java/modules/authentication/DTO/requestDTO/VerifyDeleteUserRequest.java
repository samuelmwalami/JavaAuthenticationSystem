package modules.authentication.DTO.requestDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyDeleteUserRequest {
    String email;
    String otp;
    String refreshToken;
}
