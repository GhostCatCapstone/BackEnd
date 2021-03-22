package ghostcat.capstone.holders;

import java.util.ArrayList;

public class Image implements Comparable<Image>{
    public String id;
    public int imgWidth;
    public int imgHeight;
    public boolean flash;
    public String make;
    public String model;
    public long date;
    public String cameraTrap;
    public String deployment;
    public boolean night_im;
    public String imgLink;
    public ArrayList<BoundingBox> boundingBoxes = new ArrayList<>();

    public Image(
            String id,
            int imgWidth,
            int imgHeight,
            boolean flash,
            String make,
            String model,
            long date,
            String cameraTrapName,
            String deployment,
            boolean night_im,
            String imgLink) {

        this.id = id;
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
        this.flash = flash;
        this.make = make;
        this.model = model;
        this.date = date;
        this.cameraTrap = cameraTrapName;
        this.deployment = deployment;
        this.night_im = night_im;
        this.imgLink = imgLink;
    }

    @Override
    public int compareTo(Image o) {
        if (o.date > date) return 1;
        if (o.date < date) return -1;
        return 0;
    }
}

