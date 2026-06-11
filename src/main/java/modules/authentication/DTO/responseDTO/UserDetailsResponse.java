package modules.authentication.DTO.responseDTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UserDetailsResponse extends ResponseBody {
    UUID userId;
    String firstName;
    String lastName;
    String userName;
    String email;
    String createdAt;

    public UserDetailsResponse(){}

    public UserDetailsResponse(UUID userId,
    String firstName,
    String lastName,
    String userName,
    String email,
    String createdAt){
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.email = email;
        this.createdAt = createdAt;
    }
}
