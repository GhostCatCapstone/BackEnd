package ghostcat.capstone.get_camera_traps;

import ghostcat.capstone.holders.CameraTrap;

import java.util.ArrayList;

public class GetCameraTrapsResponse {
    public ArrayList<CameraTrap> cameraTraps = new ArrayList<>();
    public String errorMsg = "";
    public Boolean success = true;
}
