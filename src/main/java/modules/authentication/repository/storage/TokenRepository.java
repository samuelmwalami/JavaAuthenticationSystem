package modules.authentication.repository.storage;

import modules.authentication.DTO.commonDTO.AccessTokenDTO;

import java.util.UUID;

public interface TokenRepository {
    public int saveRefreshToken(AccessTokenDTO token);
    public AccessTokenDTO fetchRefreshToken(String refreshToken);
    public int updateRefreshTokenByUserId(UUID userId, String refreshToken);
    public int deleteRefreshTokenByUserId(UUID userId);


}
