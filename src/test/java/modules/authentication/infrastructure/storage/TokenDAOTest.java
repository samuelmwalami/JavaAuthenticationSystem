package modules.authentication.infrastructure.storage;

import modules.authentication.DTO.commonDTO.AccessTokenDTO;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenDAOTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private MockedStatic<DatabaseConnector> databaseConnectorMock;

    private final TokenDAO tokenDAO = new TokenDAO();

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
    void saveRefreshTokenExecutesUpdateWithAllFieldsBound() throws Exception {
        AccessTokenDTO tokenDTO = new AccessTokenDTO();
        tokenDTO.setTokenId(UUID.randomUUID());
        tokenDTO.setRefreshToken("some.jwt.token");
        tokenDTO.setUserId(UUID.randomUUID());

        when(preparedStatement.executeUpdate()).thenReturn(1);

        int result = tokenDAO.saveRefreshToken(tokenDTO);

        assertEquals(1, result);
        verify(preparedStatement).setObject(1, tokenDTO.getTokenId());
        verify(preparedStatement).setString(2, "some.jwt.token");
        verify(preparedStatement).setObject(3, tokenDTO.getUserId());
    }

    @Test
    void deleteRefreshTokenByUserIdExecutesUpdateWithUserIdBound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(preparedStatement.executeUpdate()).thenReturn(1);

        tokenDAO.deleteRefreshTokenByUserId(userId);

        verify(preparedStatement).setObject(1, userId);
    }

    @Test
    void updateRefreshTokenByUserIdDoesNotUseOnConflictClause() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        tokenDAO.updateRefreshTokenByUserId(UUID.randomUUID(), "some.jwt.token");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();

        assertFalse(sql.contains("ON CONFLICT"));
    }

    @Test
    void fetchRefreshTokenMapsResultSetWhenRowFound() throws Exception {
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id", UUID.class)).thenReturn(tokenId);
        when(resultSet.getString("refresh_token")).thenReturn("some.jwt.token");
        when(resultSet.getObject("user_id", UUID.class)).thenReturn(userId);

        AccessTokenDTO result = tokenDAO.fetchRefreshToken("some.jwt.token");

        assertEquals(tokenId, result.getTokenId());
        assertEquals("some.jwt.token", result.getRefreshToken());
        assertEquals(userId, result.getUserId());
    }
}