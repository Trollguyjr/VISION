import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//Performs the actual extraction of Data
public class ROIExtract {
    private static Mat imageMat;
    private static String name,fileName;
    private static ArrayList<ROI> ROIs = new ArrayList<>();

    public ROIExtract(){}

    private static void runImage(){
        name = "Image0";
        fileName = "Images\\"+name+".jpg";
        try {
            imageMat = Imgcodecs.imread(fileName);
            loadData();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private static void loadData(){
        try(Scanner reader = new Scanner(new File("Images\\Data_Annotation_Copy_Image0.txt"))){
            System.out.println("start");
            int counter = 0;
            while(reader.hasNextLine()){
                counter++;
                String currentROI = reader.nextLine();
                System.out.println("in " + currentROI);
                if(currentROI.isEmpty()){
                    continue;
                }
                //Simplifies storing information into objects
                String[] currROI = currentROI.split(",");

                // This is easier to store information compared to Storing an ArrayList inside an ArrayList
                // The index will never go out of bounds, therefore this is acceptable
                // also splits the {X,Y} Points so combining string is necessary
                double[] xValues = new double[]{Double.parseDouble(currROI[2].replace("{","").trim())
                        , Double.parseDouble(currROI[4].replace("{","").trim())};
                double[] yValues = new double[]{Double.parseDouble(currROI[3].replace("}","").trim())
                        , Double.parseDouble(currROI[5].replace("}","").trim())};

                // Restore pts to get Rectangle size
                Point currPt = new Point(xValues[0],yValues[0]);
//                System.out.println(currPt + "PT");
                Point endPt = new Point(xValues[1],yValues[1]);
//                System.out.println(endPt + "PT");
                ROI roi1 = new ROI(currROI[0],Integer.parseInt(currROI[1]),currPt,endPt);

                ROIs.add(roi1);
                //Will only show memory location, use ROIs.get(#).(objectsIdentifier) to get specifics
                System.out.println(ROIs);
                System.out.println(counter);
            }
        }
        catch(Exception e){
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    //@TODO ADD FEATURE THAT CROPS IMAGES BASED ON THE DATA
    private static void cropSubImages(){
        int counter = 0;
        for(ROI r : ROIs){
            if(r.shape.equals("Circle")){
                String subFile = "Images\\SubImages\\Image0_subImage"+counter;
                // (Leftmost pixel)int x pt, (Topmost)int y pt, int width, int height
                // length represents the radius of the circle
                //+10 to add padding for the circle thickness
                int padding = 10;
                Mat subMats = imageMat.submat(new Rect((int)(r.centerPt.x - r.length) + padding/2,(int)(r.centerPt.y - r.length) + padding/2
                        ,2*r.length+padding,2*r.length+padding));
                Imgcodecs.imwrite(subFile+".jpg",subMats);
            }
            else if(r.shape.equals("Line")){
                double centerX = r.centerPt.x;
                double centerY = r.centerPt.y;
                double endX = r.endPt.x;
                double endY = r.endPt.y;
                int padding = 10;
                Mat subMats = imageMat.submat(new Rect((int)Math.min(centerX,endX) + padding/2
                        , (int)Math.min(centerY,endY) + padding/2, (int)(Math.max(centerX,endX) - Math.min(centerX,endX)) + padding
                        , (int)(Math.max(centerY,endY) - Math.min(centerY,endY)) + padding));
            }
            counter++;
        }
    }


    // Object class that defines an ROI information
    private static class ROI{
        private String shape;
        private int length;
        private Point centerPt;
        private Point endPt;

        public ROI(String shape, int length, Point centerPt, Point endPt){
            this.shape = shape;
            this.length = length;
            this.centerPt = centerPt;
            this.endPt = endPt;
        }
    }


    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ROIExtract.runImage();
    }
}
