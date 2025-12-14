import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainVideo extends JFrame{
    private static int liveCounter = 0; // Counter only works while live. Change name of important saves

    private static JPanel panel;
    private static JLabel cam;
    private static JLabel cam2;

    private static Mat mats, grayScale; // captures images
    private static VideoCapture vid; // live camera feed

    private static JButton save;

    private static boolean saved = false; // One image saved instead of 100 in one press

    public MainVideo(){
        panel = new JPanel();
        panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        panel.setLayout(new GridLayout(2,2,10,10));


        cam = new JLabel("cam1",SwingConstants.CENTER);
        cam.setSize(150,150);
        cam.setVisible(true);

        cam2 = new JLabel("cam2",SwingConstants.CENTER);
        cam2.setSize(150,150);
        cam2.setVisible(true);

        save = new JButton("Save");
        save.setSize(100,100);
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saved = true;
            }
        });
        save.setVisible(true);


        panel.add(cam);
        panel.add(cam2);
        panel.add(save);

        panel.setVisible(true);
        this.add(panel);
        this.setVisible(true);
        this.setSize(900,400);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
//        this.pack();
    }

    private static void Run(){
        System.out.println("Checkeck");
        vid = new VideoCapture(0); // starts camera
        mats = new Mat(); // creates empty "Image" or matrices
        grayScale = new Mat();

        System.out.println("check2");

        if(!vid.isOpened()){
            System.out.println("Failed");
        }
        System.out.println("check1");
        new Thread(() -> {
            System.out.println("In thread");
            MatOfByte matB = new MatOfByte(); // turns Mat into bytes
            MatOfByte matGray = new MatOfByte();
            while(vid.isOpened()){
                if(vid.read(mats)){

                    Core.flip(mats,mats,+1); // flips image as a reflection instead of inverted
                    Imgproc.cvtColor(mats,grayScale,Imgproc.COLOR_BGR2GRAY);

                    System.out.println("loading");

                    Imgcodecs.imencode(".jpg",mats,matB); // Makes image into jpg type
                    Imgcodecs.imencode(".jpg",grayScale,matGray);


                    ImageIcon icon = new ImageIcon(matB.toArray()); // makes image with Bytes and updating with every loop
                    ImageIcon icon2 = new ImageIcon(matGray.toArray());

                    SwingUtilities.invokeLater(() -> {
                        cam.setIcon(icon);
                        cam2.setIcon(icon2);
                    }); // sets Camera to label

                    // Saves current GrayScale Image
                    if(saved){
                        System.out.println("SAVING IMAGE: " + liveCounter);
                        Imgcodecs.imwrite("Images\\Image"+ liveCounter+ ".jpg",grayScale);
                        liveCounter++;
                        saved = false;
                    }

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
