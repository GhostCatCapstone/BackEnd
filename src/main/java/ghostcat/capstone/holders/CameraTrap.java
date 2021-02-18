package ghostcat.capstone.holders;

public class CameraTrap {
    public String cameraTrapID;
    public String projectID;
    public Double lat = 0.0;
    public Double lng = 0.0;

    public CameraTrap(String cameraTrapID, String projectID, Double lat, Double lng) {
        this.cameraTrapID = cameraTrapID;
        this.projectID = projectID;
        this.lat = lat;
        this.lng = lng;
    }
}
