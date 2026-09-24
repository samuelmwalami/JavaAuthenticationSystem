package modules.authentication.infrastructure.storage;

import modules.authentication.DTO.commonDTO.OtpDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpDAOTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private MockedStatic<DatabaseConnector> databaseConnectorMock;

    private final OtpDAO otpDAO = new OtpDAO();

    @BeforeEach
    void setUp() throws Exception {
        databaseConnectorMock = mockStatic(DatabaseConnector.class);
        databaseConnectorMock.when(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                DatabaseConnector.getDatabaseConnection();
            }
        }).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @AfterEach
    void tearDown() {
        databaseConnectorMock.close();
    }

    @Test
    void saveOtpExecutesUpdateWithAllFieldsBound() throws Exception {
        OtpDTO otpDTO = new OtpDTO(UUID.randomUUID(), "123456", LocalDateTime.now().plusMinutes(5), "john.doe@example.com");

        when(preparedStatement.executeUpdate()).thenReturn(1);

        int result = otpDAO.saveOtp(otpDTO);

        assertEquals(1, result);
        verify(preparedStatement).setObject(1, otpDTO.getOtpID());
        verify(preparedStatement).setString(2, "123456");
        verify(preparedStatement).setObject(3, otpDTO.getOtpExpiry());
        verify(preparedStatement).setString(4, "john.doe@example.com");
    }

    @Test
    void retrieveOtpByOtpAndEmailBuildsWellFormedSql() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        otpDAO.retrieveOtpByOtpAndEmail("123456", "john.doe@example.com");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();

        assertFalse(sql.contains("otpINNER"));
        assertFalse(sql.contains("emailWHERE"));
        assertTrue(sql.contains("otp.expiry, person.email"));
    }


    @Test
    void deleteOtpByEmailExecutesUpdateWithEmailBound() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        otpDAO.deleteOtpByEmail("john.doe@example.com");

        verify(preparedStatement).setString(1, "john.doe@example.com");
    }
}