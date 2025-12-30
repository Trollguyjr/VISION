import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;

public class ObjectDetection extends JFrame {
    private JLabel cameraFeed;
    private VideoCapture cam;
    private JPanel panel;
    private Mat camImage,blurImage,grayImage,edgeImage;

    public ObjectDetection(){
        panel = new JPanel();
        panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        panel.setLayout(new GridLayout(2,2,5,5));
        panel.setVisible(true);

        cameraFeed = new JLabel();
        cameraFeed.setSize(300,300);
        cameraFeed.setVisible(true);

        this.add(cameraFeed);

        this.add(panel);
        this.setSize(500,500);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
    }

    private void CameraStart(){
        System.out.println("Check");
        cam = new VideoCapture(0);
        camImage = new Mat();
        blurImage = new Mat();



        if(!cam.isOpened()){
            System.out.println("Fail");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                MatOfByte camInfo = new MatOfByte();
                while(cam.isOpened()){
                    if(cam.read(camImage)){
                        Core.flip(camImage,camImage,+1); // flips camera

                        Imgcodecs.imencode(".jpg",camImage,camInfo); // encodes into info that is readable
                        ImageIcon icon = new ImageIcon(camInfo.toArray());// turns into image icon so that label can update

                        SwingUtilities.invokeLater(() ->{
                            cameraFeed.setIcon(icon);
                        });
                    }
                }
            }
        }).start();
    }

    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ObjectDetection obj = new ObjectDetection();
        obj.CameraStart();
    }
}
