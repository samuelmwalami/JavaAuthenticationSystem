package modules.authentication.infrastructure.storage;

import com.fasterxml.uuid.Generators;
import modules.authentication.DTO.commonDTO.OtpDTO;
import modules.authentication.repository.storage.OtpRepository;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class OtpDAO implements OtpRepository {

    @Override
    public int saveOtp(OtpDTO otpDTO) {
        String QUERY = "INSERT INTO otp(id, otp, email, expiry)" +
                "VALUES(?,?,?,?) " +
                "ON CONFLICT(email) DO UPDATE " +
                "SET id = EXCLUDED.id, otp = EXCLUDED.otp, expiry = EXCLUDED.otp";
        int rowsAffected = 0;

        try(Connection conn = DatabaseConnector.getDatabaseConnection();
        PreparedStatement statement = conn.prepareStatement(QUERY)) {

            statement.setObject(1, otpDTO.getOtp());
            statement.setString(2, otpDTO.getOtp());
            statement.setString(3, otpDTO.getUserEmail());
            statement.setObject(4,otpDTO.getOtpExpiry());

            return statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowsAffected;
    }

    @Override
    public OtpDTO retrieveOtp(String otp, String email) {
        final String QUERY = "SELECT otp.id, otp.otp, otp.expiry person.email FROM otp" +
                "INNER JOIN person ON otp.email = person.email" +
                "WHERE otp.otp = ? AND person.email = ?";

        OtpDTO otpDTO = new OtpDTO();

        try(Connection conn = DatabaseConnector.getDatabaseConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY)) {

            statement.setString(1, otp);
            statement.setString(2, email);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()){
                otpDTO.setOtpID(resultSet.getObject("id", UUID.class));
                otpDTO.setOtp(resultSet.getString("otp"));
                otpDTO.setOtpExpiry(resultSet.getObject("expiry",LocalDateTime.class));
                otpDTO.setUserEmail(resultSet.getString("email"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return otpDTO;
    }

    @Override
    public int deleteOtpByEmail(String email) {
        final String QUERY = "DELETE FROM otp " +
                "WHERE email = ?";

        int rowsAffected = 0;

        try(Connection conn = DatabaseConnector.getDatabaseConnection();
        PreparedStatement statement = conn.prepareStatement(QUERY)){
            statement.setString(1,email);

            return statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsAffected;
    }
}
