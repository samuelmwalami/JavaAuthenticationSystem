package modules.authentication.repository;

import modules.authentication.Entities.UserEntity;

public interface UserRepository {
    public int saveUser(UserEntity entity);
    public UserEntity getUserByEmail(String email);
}
