package modules.authentication.Entities;


import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserEntity {
    UUID id;
    String firstName;
    String lastName;
    String userName;
    String email;
    String password;
    String confirmPassword;
    LocalDateTime createdAt;
}
