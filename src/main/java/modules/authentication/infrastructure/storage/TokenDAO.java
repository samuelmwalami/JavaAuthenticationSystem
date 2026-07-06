package modules.authentication.infrastructure.storage;

import modules.authentication.DTO.commonDTO.AccessTokenDTO;
import modules.authentication.repository.storage.TokenRepository;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TokenDAO implements TokenRepository {

    @Override
    public int saveRefreshToken(AccessTokenDTO token) {
        final String QUERY = "INSERT INTO jwt(id, refresh_token, user_id ) " +
                "VALUES(?, ?, ?) " +
                "ON CONFLICT(user_id) DO UPDATE " +
                "SET refresh_token = EXCLUDED.refresh_token, id = EXCLUDED.id";

        int rowsAffected = 0;

        try(Connection conn = DatabaseConnector.getDatabaseConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY)){

            statement.setObject(1,token.getTokenId());
            statement.setString(2, token.getRefreshToken());
            statement.setObject(3, token.getUserId());

            return statement.executeUpdate();


        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return rowsAffected;
    }

    @Override
    public int deleteRefreshTokenByUserId(UUID userId) {
        int rowsAffected = 0;
        String QUERY = "DELETE FROM jwt  WHERE user_id = ?";

        try(Connection conn  = DatabaseConnector.getDatabaseConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY);){

            statement.setObject(1, userId);
            return statement.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }

        return rowsAffected;
    }

    @Override
    public int updateRefreshTokenByUserId(UUID userId, String refreshToken) {
        int rowsAffected = 0;
        final String QUERY = "UPDATE jwt SET refresh_token = ? WHERE user_id = ? ON CONFLICT(user_id) DO UPDATE SET refresh_token = excluded.refreshToken ";

        try(Connection conn  = DatabaseConnector.getDatabaseConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY)){

            statement.setString(1, refreshToken);
            statement.setObject(2, userId);
            return statement.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return rowsAffected;
    }

    @Override
    public AccessTokenDTO fetchRefreshToken(String refreshToken) {
        AccessTokenDTO token = new AccessTokenDTO();
        String QUERY = "SELECT * from jwt WHERE refresh_token = ?";

        try(Connection conn = DatabaseConnector.getDatabaseConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY)){

            statement.setString(1, refreshToken);
            ResultSet rs = statement.executeQuery();
            
            if(rs.next()){
                token.setTokenId(rs.getObject("id", UUID.class));
                token.setRefreshToken(rs.getString("refresh_token"));
                token.setUserId(rs.getObject("user_id", UUID.class));
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return token;
    }
}
