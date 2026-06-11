package modules.authentication.repository.storage;

import modules.authentication.DTO.commonDTO.UserDTO;

import java.util.UUID;

public interface UserRepository {
    public int saveUser(UserDTO userDTO);
    public UserDTO getUserByEmailAndUserId(String email, UUID userId);
    public UserDTO getUserWithPasswordByEmail(String email);
    public int deleteUserByEmailAndUserId(String email, UUID userID);
    public UserDTO getUserByUserName(String userName);
    public UserDTO getUserByEmail(String email);
}
