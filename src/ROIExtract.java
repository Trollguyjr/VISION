import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ROIExtract {
    private static Mat imageMat;
    private static String name,fileName;

    public ROIExtract(){}

    private static void runImage(String name){
        name = "Image0";
        fileName = "Images\\"+name+".jpg";
        try {
            imageMat = Imgcodecs.imread(fileName);
            MatOfByte matI = new MatOfByte();
            Imgcodecs.imencode(".jpg",imageMat,matI);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ROIExtract.runImage(new String());
    }
}
