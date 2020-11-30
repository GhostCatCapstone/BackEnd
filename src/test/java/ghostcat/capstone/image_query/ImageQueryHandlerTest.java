package ghostcat.capstone.image_query;

import ghostcat.capstone.holders.BoundingBox;
import ghostcat.capstone.holders.ClassValue;
import ghostcat.capstone.holders.Factory;
import ghostcat.capstone.holders.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ImageQueryHandlerTest {
    ImageQueryHandler handler;
    ImageQueryRequest request;

    @BeforeEach
    void setUp() {
        handler = new ImageQueryHandler();
        request = new ImageQueryRequest();
        request.userID = "researcherID";
        request.deployment = "photos_spring2019";
        request.projectID = "projectID";
        request.authToken = "helloWorld";
        request.cameraTrap = "site002";
        request.minDate = Long.valueOf("1556228937000");
        request.maxDate = Long.valueOf("1556228942000");
        request.classes.add(new ClassValue("Cow", .99));
        Factory.imageQueryDAO = mock(ImageQueryDAO.class);
    }

    @AfterEach
    void tearDown() {
        handler = null;
        request = null;
    }

    @Test
    @DisplayName("Should return error on null/invalid userID")
    public void shouldReturnErrorOnNullInvalidUserID() {
        ImageQueryResponse response = null;

        request.userID = null;
        response = handler.handleRequest(request, null);
        assertFalse(response.success);

        request.userID = "InvalidUserID";
        response = handler.handleRequest(request, null);
        //assertFalse(response.success);
        // TODO: fix the error handling so that we can test this with a mock
    }

    @Test
    @DisplayName("Should return error on null/invalid authToken")
    public void shouldReturnErrorOnNullInvalidAuthToken() {
        ImageQueryResponse response = null;

        request.authToken = null;
        response = handler.handleRequest(request, null);
        assertFalse(response.success);

        //TODO: uncomment when authToken validation is ready
//        request.authToken = "InvalidAuthtoken";
//        response = handler.handleRequest(request, null);
//        assertFalse(response.success);
    }

    @Test
    @DisplayName("Should return error on null/invalid projectID")
    public void shouldReturnErrorOnNullInvalidDeployment() {
        ImageQueryResponse response = null;

        request.projectID = null;
        response = handler.handleRequest(request, null);
        assertFalse(response.success);

        request.projectID = "InvalidProjectID";
        response = handler.handleRequest(request, null);
        //assertFalse(response.success);
        // TODO: fix the error handling so that we can test this with a mock
    }

    @Test
    @DisplayName("Should return error on invalid date range")
    public void shouldReturnErrorOnInvalidDateRange() {
        ImageQueryResponse response = null;

        request.minDate = Long.valueOf("1000");
        request.maxDate = Long.valueOf("1");
        response = handler.handleRequest(request, null);
        assertFalse(response.success);
    }

    @Test
    @DisplayName("Should return error on invalid camera trap")
    public void shouldReturnErrorOnInvalidCameraTrap() {

    }

    @Test
    @DisplayName("Should return error on invalid class name")
    public void shouldReturnErrorOnInvalidLabel() {
        ImageQueryResponse response = null;

        request.classes.add(new ClassValue("Octopus", .9));
        response = handler.handleRequest(request, null);
        //assertFalse(response.success);
        // TODO: fix the error handling so that we can test this with a mock
    }

    @Test
    @DisplayName("Should return error on invalid confidence value")
    public void shouldReturnErrorOnInvalidConfidenceValue() {
        ImageQueryResponse response = null;

        request.classes.add(new ClassValue("Cow", 1.1));
        response = handler.handleRequest(request, null);
        assertFalse(response.success);

        request.classes = new ArrayList<>();
        request.classes.add(new ClassValue("Cow", -.1));
        response = handler.handleRequest(request, null);
        assertFalse(response.success);
    }

    @Test
    @DisplayName("Query on camera trap")
    public void queryOnCameraTrap() {
        ImageQueryResponse response = null;

        request.cameraTrap = "site004";
        request.minDate = null;
        request.maxDate = null;
        request.classes = new ArrayList<>();
        response = handler.handleRequest(request, null);
        assertTrue(response.success);
        boolean correctTrapName = true;
        for (Image i : response.images) {
            assertEquals(request.cameraTrap, i.cameraTrap);
        }
    }

    @Test
    @DisplayName("Query on date range")
    public void queryOnDateRange() {
        ImageQueryResponse response = null;

        request.cameraTrap = null;
        request.minDate = Long.valueOf("1553285042000");
        request.maxDate = Long.valueOf("1553586356000");
        request.classes = new ArrayList<>();
        response = handler.handleRequest(request, null);
        assertTrue(response.success);
        for (Image i : response.images) {
            assertTrue(i.date <= request.maxDate && i.date >= request.minDate);
        }
    }

    @Test
    @DisplayName("Query on labels")
    public void queryOnLabels() {
        ImageQueryResponse response = null;

        request.cameraTrap = null;
        request.minDate = null;
        request.maxDate = null;
        request.classes = new ArrayList<>();
        request.classes.add(new ClassValue("Cow", .9));
        response = handler.handleRequest(request, null);
        assertTrue(response.success);
        for (Image i : response.images) {
            for (BoundingBox b : i.boundingBoxes) {
                assertTrue(b.classes.get("Cow") >= .9);
            }
        }
    }

    @Test
    @DisplayName("Query on deployment")
    public void queryOnDeployment() {
        ImageQueryResponse response = null;

        request.cameraTrap = null;
        request.minDate = null;
        request.maxDate = null;
        request.classes = new ArrayList<>();
        request.deployment = "photos_spring2019";
        response = handler.handleRequest(request, null);
        assertTrue(response.success);
        for (Image i : response.images) {
            assertEquals(i.deployment, "photos_spring2019");
        }
    }
}