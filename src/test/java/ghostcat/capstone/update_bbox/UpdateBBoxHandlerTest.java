package ghostcat.capstone.update_bbox;

import com.amazonaws.services.dynamodbv2.document.Item;
import ghostcat.capstone.holders.ClassNameValue;
import ghostcat.capstone.holders.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpdateBBoxHandlerTest {
    UpdateBBoxHandler handler;
    UpdateBBoxRequest request;

    @BeforeEach
    void setUp() {
        handler = new UpdateBBoxHandler();
        request = new UpdateBBoxRequest();
        request.userID = "researcherID";
        request.projectID = "projectID";
        request.bboxID = "021a821f-67b6-360d-813b-ed02e6a73f1d";
        request.correctClassName = "Cow";
        Factory.updateBBoxDAO = mock(UpdateBBoxDAO.class);
        when(Factory.updateBBoxDAO.queryProjectDataOnUserIDAndProjectID(request)).thenReturn(new ArrayList<>(
                Arrays.asList(
                        new Item().withString("UserID", "researcherID")
                                .withString("class_1", "Cow")
                                .withString("class_2", "Mule Deer")
                                .withString("class_3", "Sheep")
                                .withString("class_4", "Other")
                                .withInt("num_classes", 4)
                )
        ));
    }

    @Test
    @DisplayName("Should return error on null/invalid userID")
    public void shouldReturnErrorOnNullInvalidUserID() {

        UpdateBBoxResponse response = null;

        request.userID = null;
        response = handler.handleRequest(request, null);
        assertFalse(response.success);

        request.userID = "InvalidUserID";
        when(Factory.updateBBoxDAO.queryProjectDataOnUserIDAndProjectID(request)).thenReturn(new ArrayList<>());
        response = handler.handleRequest(request, null);
        assertFalse(response.success);
    }

    @Test
    @DisplayName("Should return error on null/invalid projectID")
    public void shouldReturnErrorOnNullInvalidDeployment() {
        UpdateBBoxResponse response = null;

        request.projectID = null;
        response = handler.handleRequest(request, null);
        assertFalse(response.success);

        request.projectID = "InvalidProjectID";

        when(Factory.updateBBoxDAO.queryProjectDataOnUserIDAndProjectID(request)).thenReturn(new ArrayList<>());
        when(Factory.updateBBoxDAO.queryProjectDataOnUserID(request)).thenReturn(new ArrayList<>(
                Arrays.asList(
                        new Item().withString("UserID", "researcherID")
                )
        ));
        response = handler.handleRequest(request, null);
        assertFalse(response.success);
    }

    @Test
    @DisplayName("Should return error on null/invalid bboxID")
    public void shouldReturnErrorOnNullInvalidBboxID() {
        UpdateBBoxResponse response = null;

        request.bboxID = null;
        response = handler.handleRequest(request, null);
        assertFalse(response.success);

        request.bboxID = "InvalidBBoxID";
        when(Factory.updateBBoxDAO.queryBBoxOnBBoxID("userID", "InvalidBBoxID"))
                .thenReturn(new ArrayList<>());
        response = handler.handleRequest(request, null);
        assertFalse(response.success);
    }

    @Test
    @DisplayName("Should update database when given valid request")
    public void shouldUpdateDatabaseWhenGivenValidRequest() {
        UpdateBBoxResponse response = null;

        HashMap<String, String> classNames = new HashMap<>();
        classNames.put("Cow", "class_1");
        classNames.put("Mule Deer", "class_2");
        classNames.put("Sheep", "class_3");
        classNames.put("Other", "class_4");

        request.bboxID = "ValidBBoxID";
        request.userID = "userID";
        when(Factory.updateBBoxDAO.queryBBoxOnBBoxID("userID", "ValidBBoxID")).thenReturn(
                new ArrayList<>(Arrays.asList(new Item()))
        );
        when(Factory.updateBBoxDAO.setCorrectValueForBBox(request, classNames)).thenReturn(true);
        response = handler.handleRequest(request, null);
        assertTrue(response.success);
    }

}
