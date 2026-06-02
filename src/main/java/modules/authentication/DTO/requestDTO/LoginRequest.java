package modules.authentication.DTO.requestDTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {
    String email;
    String password;
}
