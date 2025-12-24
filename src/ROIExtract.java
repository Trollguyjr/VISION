import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
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
            while(reader.hasNextLine()){
                String currentROI = reader.nextLine();
                System.out.println("in " + currentROI);
                if(currentROI.isEmpty()){
                    continue;
                }
                //Simplifies storing information into objects
                String[] currROI = currentROI.split(",");
                // This is easier to store information compared to Storing an ArrayList inside an ArrayList
                ROI roi1 = new ROI(currROI[0],Integer.parseInt(currROI[1]),
                        Integer.parseInt(currROI[2]),Integer.parseInt(currROI[3]));

                ROIs.add(roi1);
                //Will only show memory location, use ROIs.get(#).(objectsIdentifier) to get specifics
                System.out.println(ROIs);
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    //@TODO ADD FEATURE THAT CROPS IMAGES BASED ON THE DATA
    private static void cropSubImages(){
        for(ROI r : ROIs){

        }
    }


    // Object class that defines an ROI information
    private static class ROI{
        private String shape;
        private int length;
        private int x;
        private int y;

        public ROI(String shape, int length, int x, int y){
            this.shape = shape;
            this.length = length;
            this.x = x;
            this.y = y;
        }
    }


    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ROIExtract.runImage();
    }
}
