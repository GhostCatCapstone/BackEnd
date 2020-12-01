package ghostcat.capstone.authentication;

import com.amazonaws.services.dynamodbv2.document.Item;
import ghostcat.capstone.holders.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

public class TokenAuthenticationTest {
    TokenAuthentication auth = new TokenAuthentication();
    @BeforeEach
    public void setup() {
        Factory.tokenAuthenticationDAO = mock(TokenAuthenticationDAO.class);
    }

    @Test
    @DisplayName("Should return false if invalid token")
    public void shouldReturnFalseIfInvalidToken() {
        when(Factory.tokenAuthenticationDAO.queryAuthTable("invalidToken")).thenReturn(null);
        boolean succcess = auth.authenticateToken("invalidToken", "validUserID");
        assertFalse(succcess);
    }

    @Test
    @DisplayName("Should return false if wrong userID")
    public void shouldReturnFalseIfWrongUser() {
        when(Factory.tokenAuthenticationDAO.queryAuthTable("validToken")).thenReturn(
                new Item().withString("authToken", "validToken")
                .withString("userID", "validUserID")
                .withString("time", "100")
        );
        boolean succcess = auth.authenticateToken("validToken", "invalidUserID");
        assertFalse(succcess);
    }

    @Test
    @DisplayName("Should return true if valid token and userID")
    public void shouldReturnTrueIfValidTokenAndUserID() {
        when(Factory.tokenAuthenticationDAO.queryAuthTable("validToken")).thenReturn(
                new Item().withString("authToken", "validToken")
                        .withString("userID", "validUserID")
                        .withString("time", "100")
        );
        boolean succcess = auth.authenticateToken("validToken", "validUserID");
        assertFalse(succcess);
    }
}
