package modules.authentication.infrastructure.storage;

import modules.authentication.DTO.commonDTO.UserDTO;
import modules.authentication.repository.storage.UserRepository;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserDAO implements UserRepository {

    @Override
    public int saveUser(UserDTO userDTO) {
        int rowsAffected = 0;
        String QUERY = "INSERT INTO person(id, first_name, last_name, user_name, email, user_password) VALUES(?,?,?,?,?,?)";

        try(Connection connection = DatabaseConnector.getDatabaseConnection();
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
    public UserDTO getUserByEmailAndUserId(String email, UUID userId){
        UserDTO user = new UserDTO();
        final String QUERY = "SELECT id, first_name, last_name, user_name, email, created_at FROM person WHERE email = ? and id = ?";

        try(Connection conn = DatabaseConnector.getDatabaseConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY)){

            statement.setString(1, email);
            statement.setObject(2,userId);

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
    public UserDTO getUserWithPasswordByEmail(String email){
        UserDTO user = new UserDTO();
        final String QUERY = "SELECT id, first_name, last_name, user_name, email, created_at, user_password FROM person WHERE email = ?";


        try(Connection conn = DatabaseConnector.getDatabaseConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY)){

            statement.setString(1,email);
            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                user.setUserId(rs.getObject("id",UUID.class));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setUserName(rs.getString("user_name"));
                user.setEmail(rs.getString("email"));
                user.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                user.setPassword(rs.getString("user_password"));
            }

        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public int deleteUserByEmailAndUserId(String email, UUID userId){
        int affectedRows = 0;
        String QUERY = "DELETE from user WHERE email = ? and id = ?";

        try(Connection conn = DatabaseConnector.getDatabaseConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY)){

            statement.setString(1,email);
            statement.setObject(2, userId);
            affectedRows = statement.executeUpdate();
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return affectedRows;
    }

    @Override
    public UserDTO getUserByUserName(String userName){
        UserDTO user  = new UserDTO();
        final String QUERY = "SELECT id, first_name, last_name, user_name, email, created_at, user_password FROM person WHERE user_name = ?";

        try (Connection conn = DatabaseConnector.getDatabaseConnection();
        PreparedStatement statement = conn.prepareStatement(QUERY)){

            statement.setString(1, userName);

            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                user.setUserId(rs.getObject("id",UUID.class));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setUserName(rs.getString("user_name"));
                user.setEmail(rs.getString("email"));
                user.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                user.setPassword(rs.getString("user_password"));
            }


        }
        catch (SQLException e){
            e.printStackTrace();
        }

        return user;
    }
    public UserDTO getUserByEmail(String email){
        UserDTO user  = new UserDTO();
        final String QUERY = "SELECT id, first_name, last_name, user_name, email, created_at FROM person WHERE email = ?";

        try (Connection conn = DatabaseConnector.getDatabaseConnection();
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
}
