package modules.authentication.services.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class UserDetailsResponse {
    UUID id;
    String firstName;
    String lastName;
    String userName;
    String email;
    String createdAt;
}
