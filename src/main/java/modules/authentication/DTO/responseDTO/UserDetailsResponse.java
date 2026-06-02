package modules.authentication.DTO.responseDTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UserDetailsResponse {
    UUID userId;
    String firstName;
    String lastName;
    String userName;
    String email;
    String createdAt;
}
