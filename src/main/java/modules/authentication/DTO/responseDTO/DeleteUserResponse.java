package modules.authentication.DTO.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteUserResponse {
    String message;
    String userId;
}
