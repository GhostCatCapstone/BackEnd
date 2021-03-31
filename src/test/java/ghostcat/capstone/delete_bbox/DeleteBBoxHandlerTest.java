package ghostcat.capstone.delete_bbox;

import com.amazonaws.services.dynamodbv2.document.Item;
import ghostcat.capstone.holders.Factory;
import ghostcat.capstone.delete_bbox.DeleteBBoxDAO;
import ghostcat.capstone.delete_bbox.DeleteBBoxHandler;
import ghostcat.capstone.delete_bbox.DeleteBBoxRequest;
import ghostcat.capstone.delete_bbox.DeleteBBoxResponse;
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

public class DeleteBBoxHandlerTest {
    DeleteBBoxHandler handler;
    DeleteBBoxRequest request;

    @BeforeEach
    void setUp() {
        handler = new DeleteBBoxHandler();
        request = new DeleteBBoxRequest();
        request.userID = "researcherID";
        request.projectID = "projectID";
        request.bboxID = "021a821f-67b6-360d-813b-ed02e6a73f1d";
        Factory.deleteBBoxDAO = mock(DeleteBBoxDAO.class);
    }

    @Test
    @DisplayName("Should return error on null/invalid bboxID")
    public void shouldReturnErrorOnNullInvalidBboxID() {
        DeleteBBoxResponse response = null;

        request.bboxID = null;
        response = handler.handleRequest(request, null);
        assertFalse(response.success);

        request.bboxID = "InvalidBBoxID";
        when(Factory.deleteBBoxDAO.queryBBoxOnBBoxID("userID", "InvalidBBoxID"))
                .thenReturn(new ArrayList<>());
        response = handler.handleRequest(request, null);
        assertFalse(response.success);
    }

    @Test
    @DisplayName("Should delete bbox when given valid request")
    public void shouldUpdateDatabaseWhenGivenValidRequest() {
        DeleteBBoxResponse response = null;

        request.bboxID = "ValidBBoxID";
        request.userID = "userID";
        when(Factory.deleteBBoxDAO.queryBBoxOnBBoxID("userID", "ValidBBoxID")).thenReturn(
                new ArrayList<>(Arrays.asList(new Item()))
        );
        when(Factory.deleteBBoxDAO.deleteBBox(request)).thenReturn(true);
        response = handler.handleRequest(request, null);
        assertTrue(response.success);
    }
}
