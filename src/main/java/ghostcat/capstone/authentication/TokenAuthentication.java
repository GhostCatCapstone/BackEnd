package ghostcat.capstone.authentication;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import ghostcat.capstone.holders.Factory;

import java.util.ArrayList;
import java.util.Iterator;

public class TokenAuthentication {

    //authToken timeout value is one week
    static long TIMEOUT_VAL = Long.valueOf("604800000");

    /**
     * Determines the user's authentication status. Returns true if the given authToken is associated with the
     * given user, and if the authToken was created within a valid timeframe (currently within one week).
     *
     * @param authToken String passed from the front end that represents user authentication
     * @param userID User's ID
     * @return True if user is authenticated, false if user is not authenticated.
     */
    public static boolean authenticateToken(String authToken, String userID) {
        TokenAuthenticationDAO dao = Factory.tokenAuthenticationDAO;
        Item i = dao.queryAuthTable(authToken);
        if (i == null) return false;

        if (!i.getString("userID").equals(userID)) {
            return false;
        }
        Long currentTime = System.currentTimeMillis();
        Long tokenTime = Long.valueOf(i.getString("time"));
        if (currentTime - tokenTime > TIMEOUT_VAL) {
            return dao.removeAuthToken(authToken);
        }
        return true;
    }
}
