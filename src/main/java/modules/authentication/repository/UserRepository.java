package modules.authentication.repository;

import modules.authentication.DTO.commonDTO.UserDTO;

public interface UserRepository {
    public int saveUser(UserDTO userDTO);
    public UserDTO getUserByEmail(String email);
    public UserDTO getUserByEmailAndPassword(String email, String password);
    public int deleteUser(String email);
}
