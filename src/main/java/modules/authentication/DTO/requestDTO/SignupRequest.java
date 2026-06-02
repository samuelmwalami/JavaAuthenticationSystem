package modules.authentication.DTO.requestDTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
        String firstName;
        String lastName;
        String userName;
        String email;
        String password;
        String confirmPassword;
}
