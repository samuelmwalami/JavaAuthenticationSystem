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
        String QUERY = "INSERT INTO otp(id, otp, expiry, email)" +
                "VALUES(?,?,?,?) " +
                "ON CONFLICT(email) DO UPDATE " +
                "SET id = EXCLUDED.id, expiry = EXCLUDED.expiry, otp = EXCLUDED.otp";
        int rowsAffected = 0;

        try(Connection conn = DatabaseConnector.getDatabaseConnection();
        PreparedStatement statement = conn.prepareStatement(QUERY)) {

            statement.setObject(1, otpDTO.getOtpID());
            statement.setString(2, otpDTO.getOtp());
            statement.setObject(3,otpDTO.getOtpExpiry());
            statement.setString(4, otpDTO.getUserEmail());


            return statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowsAffected;
    }

    @Override
    public OtpDTO retrieveOtpByOtpAndEmail(String otp, String email) {
        final String QUERY = "SELECT otp.id, otp.otp, otp.expiry, person.email FROM otp " +
                "INNER JOIN person ON otp.email = person.email " +
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
    public OtpDTO retrieveOtpByEmail(String email) {
        final String QUERY = "SELECT otp.id, otp.otp, otp.expiry person.email FROM otp " +
                "INNER JOIN person ON otp.email = person.email " +
                "WHERE person.email = ?";

        OtpDTO otpDTO = new OtpDTO();

        try(Connection conn = DatabaseConnector.getDatabaseConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY)) {

            statement.setString(1, email);

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
