package ghostcat.capstone.authentication;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import java.util.ArrayList;
import java.util.Iterator;

public class TokenAuthenticationDAO {


    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String AUTH_TABLE = "Auth";

    public Item queryAuthTable(String authToken) {
        ArrayList<Item> results = new ArrayList<>();
        Table authTable = dynamoDB.getTable(AUTH_TABLE);
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("authToken = :v_authToken")
                .withValueMap(new ValueMap()
                        .withString(":v_authToken", authToken)
                );

        ItemCollection<QueryOutcome> items = authTable.query(spec);
        Iterator<Item> iter = items.iterator();
        if (iter.hasNext()) return iter.next();
        return null;
    }

    public boolean removeAuthToken(String authToken) {
        Table authTable = dynamoDB.getTable(AUTH_TABLE);

        DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey("authToken", authToken);
        DeleteItemOutcome outcome = authTable.deleteItem(deleteItemSpec);

        return true;
    }
}
