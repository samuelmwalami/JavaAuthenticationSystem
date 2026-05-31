package modules.authentication.infrastructure;

import modules.authentication.Entities.UserEntity;
import modules.authentication.repository.UserRepository;
import utils.DatabaseConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserDAO implements UserRepository {
    private final DatabaseConnectionUtil connectionUtil = new DatabaseConnectionUtil();

    @Override
    public int saveUser(UserEntity userEntity) {
        int rowsAffected = 0;
        String QUERY = "INSERT INTO person(id, first_name, last_name, user_name, email, user_password) VALUES(?,?,?,?,?,?)";
        try(Connection connection = connectionUtil.getDatabaseConnection();
            PreparedStatement statement = connection.prepareStatement(QUERY);
            ){
            statement.setObject(1,userEntity.getId());
            statement.setString(2,userEntity.getFirstName());
            statement.setString(3,userEntity.getLastName());
            statement.setString(4,userEntity.getUserName());
            statement.setString(5,userEntity.getEmail());
            statement.setString(6,userEntity.getPassword());
            rowsAffected = statement.executeUpdate();

        }

        catch (SQLException e){
            e.printStackTrace();
        }
        return rowsAffected;
    }

    @Override
    public UserEntity getUserByEmail(String email){
        UserEntity user = new UserEntity();
        final String QUERY = "SELECT id, first_name, last_name, user_name, email, created_at FROM person WHERE email = ?";
        try(Connection conn = connectionUtil.getDatabaseConnection();
        PreparedStatement statement = conn.prepareStatement(QUERY)){
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            if(rs.next()){
                user.setId(rs.getObject("id",UUID.class));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setUserName(rs.getString("user_name"));
                user.setEmail(rs.getString("email"));
                user.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return user;
    }
}
