package modules.authentication.DTO.commonDTO;

import lombok.Getter;
import lombok.Setter;
import modules.authentication.DTO.responseDTO.ResponseBody;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserDTO extends ResponseBody {
    UUID userId;
    String firstName;
    String lastName;
    String userName;
    String email;
    String password;
    LocalDateTime createdAt;
    }
