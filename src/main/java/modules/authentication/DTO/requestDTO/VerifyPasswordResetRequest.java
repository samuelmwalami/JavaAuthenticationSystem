package modules.authentication.DTO.requestDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPasswordResetRequest {
    String email;
    String otp;
    String password;
    String confirmPassword;
}
