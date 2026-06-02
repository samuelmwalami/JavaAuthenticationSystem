package modules.authentication.DTO.commonDTO;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;


@Getter
@Setter
public class AccessTokenDTO {
    UUID tokenId;
    String refreshToken;
    UUID userId;
}

