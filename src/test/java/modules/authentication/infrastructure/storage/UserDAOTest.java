package modules.authentication.infrastructure.storage;

import modules.authentication.DTO.commonDTO.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDAOTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private MockedStatic<DatabaseConnector> databaseConnectorMock;

    private final UserDAO userDAO = new UserDAO();

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
    void saveUserExecutesUpdateWithAllFieldsBound() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(UUID.randomUUID());
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setUserName("johndoe");
        userDTO.setEmail("john.doe@example.com");
        userDTO.setPassword("hashedValue");

        when(preparedStatement.executeUpdate()).thenReturn(1);

        int result = userDAO.saveUser(userDTO);

        assertEquals(1, result);
        verify(preparedStatement).setObject(1, userDTO.getUserId());
        verify(preparedStatement).setString(2, "John");
        verify(preparedStatement).setString(3, "Doe");
        verify(preparedStatement).setString(4, "johndoe");
        verify(preparedStatement).setString(5, "john.doe@example.com");
        verify(preparedStatement).setString(6, "hashedValue");
    }

    @Test
    void getUserByEmailAndUserIdMapsResultSetWhenRowFound() throws Exception {
        UUID userId = UUID.randomUUID();

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id", UUID.class)).thenReturn(userId);
        when(resultSet.getString("first_name")).thenReturn("John");
        when(resultSet.getString("last_name")).thenReturn("Doe");
        when(resultSet.getString("user_name")).thenReturn("johndoe");
        when(resultSet.getString("email")).thenReturn("john.doe@example.com");
        when(resultSet.getBoolean("email_verified")).thenReturn(true);

        UserDTO result = userDAO.getUserByEmailAndUserId("john.doe@example.com", userId);

        assertEquals(userId, result.getUserId());
        assertEquals("John", result.getFirstName());
        assertTrue(result.isEmailVerified());
    }

    @Test
    void getUserByEmailAndUserIdReturnsEmptyDtoWhenNoRowFound() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        UserDTO result = userDAO.getUserByEmailAndUserId("nobody@example.com", UUID.randomUUID());

        assertNull(result.getUserId());
    }

    @Test
    void deleteUserByEmailAndUserIdReferencesPersonTable() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        userDAO.deleteUserByEmailAndUserId("john.doe@example.com", UUID.randomUUID());

        verify(connection).prepareStatement(contains("FROM person"));
    }

    @Test
    void updatePasswordByEmailReferencesUserPasswordColumn() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        userDAO.updatePasswordByEmail("john.doe@example.com", "hashedValue");

        verify(connection).prepareStatement(contains("user_password"));
    }
}