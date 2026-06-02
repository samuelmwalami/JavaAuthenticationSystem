package modules.authentication.infrastructure;

import modules.authentication.DTO.commonDTO.UserDTO;
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
    public int saveUser(UserDTO userDTO) {
        int rowsAffected = 0;
        String QUERY = "INSERT INTO person(id, first_name, last_name, user_name, email, user_password) VALUES(?,?,?,?,?,?)";
        try(Connection connection = connectionUtil.getDatabaseConnection();
            PreparedStatement statement = connection.prepareStatement(QUERY);
            ){
            statement.setObject(1,userDTO.getUserId());
            statement.setString(2,userDTO.getFirstName());
            statement.setString(3,userDTO.getLastName());
            statement.setString(4,userDTO.getUserName());
            statement.setString(5,userDTO.getEmail());
            statement.setString(6,userDTO.getPassword());
            rowsAffected = statement.executeUpdate();

        }

        catch (SQLException e){
            e.printStackTrace();
        }
        return rowsAffected;
    }

    @Override
    public UserDTO getUserByEmail(String email){
        UserDTO user = new UserDTO();
        final String QUERY = "SELECT id, first_name, last_name, user_name, email, created_at FROM person WHERE email = ?";
        try(Connection conn = connectionUtil.getDatabaseConnection();
        PreparedStatement statement = conn.prepareStatement(QUERY)){
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            if(rs.next()){
                user.setUserId(rs.getObject("id",UUID.class));
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

    @Override
    public UserDTO getUserByEmailAndPassword(String email, String password){
        UserDTO user = new UserDTO();
        final String QUERY = "SELECT id, first_name, last_name, user_name, email, created_at FROM person WHERE email = ? AND password = ?";


        try(Connection conn = connectionUtil.getDatabaseConnection();
        PreparedStatement statement = conn.prepareStatement(QUERY)){
            statement.setString(1,email);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                user.setUserId(rs.getObject("id",UUID.class));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setUserName(rs.getString("user_name"));
                user.setEmail(rs.getString("email"));
                user.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
            }

        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public int deleteUser(String email){
        int affectedRows = 0;
        String QUERY = "DELETE from user WHERE email = ?";

        try(Connection conn = connectionUtil.getDatabaseConnection();
        PreparedStatement statement = conn.prepareStatement(QUERY)){
            statement.setString(1,email);
            affectedRows = statement.executeUpdate();
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return affectedRows;
    }
}
