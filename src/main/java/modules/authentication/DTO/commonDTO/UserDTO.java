package modules.authentication.DTO.commonDTO;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserDTO {
    UUID userId;
    String firstName;
    String lastName;
    String userName;
    String email;
    String password;
    String confirmPassword;
    LocalDateTime createdAt;
    }
