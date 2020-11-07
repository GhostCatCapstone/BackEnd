package ghostcat.capstone.tests;

import ghostcat.capstone.holders.BoundingBox;
import ghostcat.capstone.holders.Image;
import ghostcat.capstone.image_query.ImageQueryHandler;
import ghostcat.capstone.image_query.ImageQueryRequest;
import ghostcat.capstone.image_query.ImageQueryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ImageQueryHandlerTest {
    ImageQueryHandler handler;
    ImageQueryRequest request;

    @BeforeEach
    void setUp() {
        handler = new ImageQueryHandler();
        request = new ImageQueryRequest();
        request.userID = "researcherID";
        request.deployment = "photos_spring2019";
        request.authToken = "helloWorld";
        request.cameraTrap = "site002";
        request.minDate = Long.valueOf("1556228937000");
        request.maxDate = Long.valueOf("1556228942000");
        request.classes.put("Cow", .99);
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
        assertFalse(response.success);
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
    @DisplayName("Should return error on null/invalid deployment")
    public void shouldReturnErrorOnNullInvalidDeployment() {
        ImageQueryResponse response = null;

        request.deployment = null;
        response = handler.handleRequest(request, null);
        assertFalse(response.success);

        request.deployment = "InvalidDeploymentID";
        response = handler.handleRequest(request, null);
        assertFalse(response.success);
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

        request.classes.put("Octopus", .9);
        response = handler.handleRequest(request, null);
        assertFalse(response.success);
    }

    @Test
    @DisplayName("Should return error on invalid confidence value")
    public void shouldReturnErrorOnInvalidConfidenceValue() {
        ImageQueryResponse response = null;

        request.classes.put("Cow", 1.1);
        response = handler.handleRequest(request, null);
        assertFalse(response.success);

        request.classes = new HashMap<>();
        request.classes.put("Cow", -.1);
        assertFalse(response.success);
    }

    @Test
    @DisplayName("Query on camera trap")
    public void queryOnCameraTrap() {
        ImageQueryResponse response = null;

        request.cameraTrap = "site004";
        request.minDate = null;
        request.maxDate = null;
        request.classes = new HashMap<>();
        response = handler.handleRequest(request, null);
        assertTrue(response.success);
        boolean correctTrapName = true;
        for (Image i : response.images) {
            assertTrue(i.cameraTrap.equals(request.cameraTrap));
        }
    }

    @Test
    @DisplayName("Query on date range")
    public void queryOnDateRange() {
        ImageQueryResponse response = null;

        request.cameraTrap = null;
        request.minDate = Long.valueOf("1553285042000");
        request.maxDate = Long.valueOf("1553586356000");
        request.classes = new HashMap<>();
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
        request.classes = new HashMap<>();
        request.classes.put("Cow", .9);
        response = handler.handleRequest(request, null);
        assertTrue(response.success);
        for (Image i : response.images) {
            for (BoundingBox b : i.boundingBoxes) {
                assertTrue(b.classes.get("class_1") >= .9);
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
        request.classes = new HashMap<>();
        request.deployment = "photos_spring2019";
        response = handler.handleRequest(request, null);
        assertTrue(response.success);
        for (Image i : response.images) {
            assertTrue(i.deployment.equals("photos_spring2019"));
        }
    }
}