package ghostcat.capstone.image_query;

import com.amazonaws.services.dynamodbv2.document.Item;
import ghostcat.capstone.holders.BoundingBox;
import ghostcat.capstone.holders.ClassNameValue;
import ghostcat.capstone.holders.Factory;
import ghostcat.capstone.holders.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        request.cameraTraps = new ArrayList<>();
        request.cameraTraps.add("site002");
        request.minDate = Long.valueOf("1556228937000");
        request.maxDate = Long.valueOf("1556228942000");
        request.classes.add(new ClassNameValue("Cow", .99));
        Factory.imageQueryDAO = mock(ImageQueryDAO.class);
        when(Factory.imageQueryDAO.queryProjectDataOnUserIDAndProjectID(request)).thenReturn(new ArrayList<>(
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

    @AfterEach
    void tearDown() {
        handler = null;
        request = null;
    }

    private Item generateValidResult() {
        return new Item().withPrimaryKey("UserID", "researcherID")
                .withString("BBoxID", "boxID")
                .withDouble("bbox_X", .05)
                .withDouble("bbox_Y", .05)
                .withDouble("bbox_width", .1)
                .withDouble("bbox_height", .1)
                .withDouble("class_1", .9)
                .withDouble("class_2", .9)
                .withDouble("class_3",.9)
                .withDouble("class_4", .9)
                .withInt("img_height", 1)
                .withInt("img_width", 1)
                .withBoolean("flash_on", true)
                .withBoolean("night_img", true)
                .withString("camera_make", "make")
                .withString("camera_model", "model")
                .withLong("img_date", 9)
                .withString("img_id", "id")
                .withString("camera_trap", "trapName")
                .withString("img_link", "img")
                .withString("deployment", "deployment");
    }

    @Test
    @DisplayName("Should return error on null/invalid userID")
    public void shouldReturnErrorOnNullInvalidUserID() {

        ImageQueryResponse response = null;

        request.userID = null;
        response = handler.handleRequest(request, null);
        assertFalse(response.success);

        request.userID = "InvalidUserID";
        when(Factory.imageQueryDAO.queryProjectDataOnUserIDAndProjectID(request)).thenReturn(new ArrayList<>());
        response = handler.handleRequest(request, null);
        assertFalse(response.success);
    }

    @Test
    @DisplayName("Should return error on null/invalid projectID")
    public void shouldReturnErrorOnNullInvalidDeployment() {
        ImageQueryResponse response = null;

        request.projectID = null;
        response = handler.handleRequest(request, null);
        assertFalse(response.success);

        request.projectID = "InvalidProjectID";

        when(Factory.imageQueryDAO.queryProjectDataOnUserIDAndProjectID(request)).thenReturn(new ArrayList<>());
        when(Factory.imageQueryDAO.queryProjectDataOnUserID(request)).thenReturn(new ArrayList<>(
                Arrays.asList(
                        new Item().withString("UserID", "researcherID")
                )
        ));

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
        //FIXME
    }

    @Test
    @DisplayName("Should return error on invalid class name")
    public void shouldReturnErrorOnInvalidLabel() {
        ImageQueryResponse response = null;


        request.classes.add(new ClassNameValue("Octopus", .9));
        response = handler.handleRequest(request, null);
        assertFalse(response.success);
    }

    @Test
    @DisplayName("Should return error on invalid confidence value")
    public void shouldReturnErrorOnInvalidConfidenceValue() {
        ImageQueryResponse response = null;

        request.classes.add(new ClassNameValue("Cow", 101));
        response = handler.handleRequest(request, null);
        assertFalse(response.success);

        request.classes = new ArrayList<>();
        request.classes.add(new ClassNameValue("Cow", -.1));
        response = handler.handleRequest(request, null);
        assertFalse(response.success);
    }

    @Test
    @DisplayName("Query on camera trap")
    public void queryOnCameraTrap() {
        ImageQueryResponse response = null;
        request.cameraTraps = new ArrayList<>();
        request.cameraTraps.add("site004");
        request.minDate = null;
        request.maxDate = null;
        request.classes = new ArrayList<>();

        Item validResult = generateValidResult().withString("camera_trap", "site004");
        Item invalidResult = generateValidResult().withString("camera_trap", "site005");

        when(Factory.imageQueryDAO.queryImagesOnCameraTrap(request)).thenReturn(new ArrayList<>(
                Arrays.asList(validResult, invalidResult)
        ));


        response = handler.handleRequest(request, null);
        assertTrue(response.success);
        for (Image i : response.images) {
            assertTrue(request.cameraTraps.contains(i.cameraTrap));
        }
    }

    @Test
    @DisplayName("Query on date range")
    public void queryOnDateRange() {
        ImageQueryResponse response = null;

        request.cameraTraps = null;
        request.minDate = Long.valueOf("1553285042000");
        request.maxDate = Long.valueOf("1553586356000");
        request.classes = new ArrayList<>();

        Item validResult = generateValidResult().withLong("date", Long.valueOf("1553285041000"));
        Item invalidResult = generateValidResult().withLong("date", Long.valueOf("1553586357000"));

        when(Factory.imageQueryDAO.queryImagesOnCameraTrap(request)).thenReturn(new ArrayList<>(
                Arrays.asList(validResult, invalidResult)
        ));

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

        request.cameraTraps = null;
        request.minDate = null;
        request.maxDate = null;
        request.classes = new ArrayList<>();
        request.classes.add(new ClassNameValue("Cow", .9));

        Item validResult = generateValidResult().withDouble("class_1", .99);
        Item invalidResult = generateValidResult().withDouble("class_1", .5);

        when(Factory.imageQueryDAO.queryImagesOnCameraTrap(request)).thenReturn(new ArrayList<>(
                Arrays.asList(validResult, invalidResult)
        ));


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

        request.cameraTraps = null;
        request.minDate = null;
        request.maxDate = null;
        request.classes = new ArrayList<>();
        request.deployment = "photos_spring2019";

        Item validResult = generateValidResult().withString("deployment", "photos_spring2019");
        Item invalidResult = generateValidResult().withString("deployment", "photos_spring2020");

        when(Factory.imageQueryDAO.queryImagesOnCameraTrap(request)).thenReturn(new ArrayList<>(
                Arrays.asList(validResult, invalidResult)
        ));

        response = handler.handleRequest(request, null);
        assertTrue(response.success);
        for (Image i : response.images) {
            assertEquals(i.deployment, "photos_spring2019");
        }
    }
}