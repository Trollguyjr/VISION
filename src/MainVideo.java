import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;

public class MainVideo extends JFrame {

    private static JLabel cam;
    private static Mat mats;
    private static VideoCapture vid;

    public MainVideo(){

        cam = new JLabel("Cap");
        cam.setSize(450,450);
        cam.setVisible(true);

        this.add(cam);
        this.setVisible(true);
        this.setSize(new Dimension(500,500));
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private static void Run(){
        vid = new VideoCapture(0);
        mats = new Mat();

        if(!vid.isOpened()){
            System.out.println("Failed");
        }

        new Thread(() -> {
            System.out.println("In thread");
            MatOfByte matB = new MatOfByte();
            while(vid.isOpened()){
                if(vid.read(mats)){
                    Core.flip(mats,mats,+1);
                    System.out.println("loading");
                    Imgcodecs.imencode(".jpg",mats,matB);

                    ImageIcon icon = new ImageIcon(matB.toArray());

                    SwingUtilities.invokeLater(() -> cam.setIcon(icon));
                }
            }
        }).start();
    }

    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        MainVideo vid = new MainVideo();
        MainVideo.Run();

        System.out.println("loaded");
    }
}
