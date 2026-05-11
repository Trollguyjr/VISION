import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Point;

import javax.sound.sampled.Line;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainVideo extends JFrame{
    public static int liveCounter = 0; // Counter only works while live. Change name of important saves

    private static JPanel panel;
    private static JLabel cam;
    private static JLabel cam2;

    private static Mat mats, grayScale, blurScale, edgeScale, lineScale,circleScale; // captures images
    private static VideoCapture vid; // live camera feed

    private static JButton save;

    private static boolean saved = false; // One image saved instead of 100 in one press

    private static final double MIN_AREA = 100;

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
        this.setSize(900,600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
//        this.pack();
    }


    private static void Run(){
        System.out.println("Checkeck");
        vid = new VideoCapture(0); // starts camera
        mats = new Mat(); // creates empty "Image" or matrices
        grayScale = new Mat();
        blurScale = new Mat();
        edgeScale = new Mat();
        lineScale = new Mat();
        circleScale = new Mat();

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
                    Imgproc.GaussianBlur(grayScale,blurScale,new Size(5,5),0);
                    Imgproc.Canny(blurScale,edgeScale,40,120);
                    System.out.println("loading");

                    // Will store all the points that are graphed
                    List<MatOfPoint> contours = new ArrayList<>();

                    // Morph_Rect, in order to create a rectangular Kernel space and Morph_close to
                    Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5));
                    Imgproc.morphologyEx(edgeScale,edgeScale,Imgproc.MORPH_CLOSE,kernel);

                    Mat hierarchy = new Mat();

                    //CHAIN_APPROX... simplifies the mapping of items, E.g. if rectangle: only maps 4 corners
                    //when hieararchy is mapped
                    Imgproc.findContours(edgeScale,contours,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);

                    for(MatOfPoint m : contours){
                        //Contour error, function takes in Mat
                        double actualArea = Imgproc.contourArea(m);

                        //gets Matrix
                        MatOfPoint2f contourVectors = new MatOfPoint2f(m.toArray());
                        double contourPerimeter = Imgproc.arcLength(contourVectors,true);

                        MatOfPoint2f approx = new MatOfPoint2f();
                        Imgproc.approxPolyDP(contourVectors,approx,0.06 * contourPerimeter,true);

                        MatOfPoint approxInt = new MatOfPoint();
                        approx.convertTo(approxInt, CvType.CV_32S);

                        Rect rectBound = Imgproc.boundingRect(approx);
                        double rectArea = rectBound.height * rectBound.width;

                        System.out.println("APPROX TOTAL : " + approx.total());

                        //Keep within 4 to 6 vertices since approx picks up multiple vertices despite
                        if(rectArea >= MIN_AREA && 6 >= approx.total() && approx.total() >= 4){
                            System.out.println("Convex? : " + Imgproc.isContourConvex(approxInt));
                            List<MatOfPoint> validContour = new ArrayList<>();
                            validContour.add(m);

                            // Need to get the points to create a vector
                            // Using the vectors we can check if the "plane" is rectangular by its dot prod
                            Point[] points = approx.toArray();
                            int edgeCounter = 0;
                            for(int i = 0; i < points.length; i++){
                                Point pointA = points[(i-1 + 4)%4];
                                Point pointB = points[i];
                                Point pointC = points[(i+1 + 4)%4];

                                Point vectorAB = new Point(pointA.x-pointB.x,pointA.y-pointB.y);
                                Point vectorCB = new Point(pointC.x-pointB.x, pointC.y-pointB.y);

                                double lengthAB = Math.sqrt((vectorAB.x*vectorAB.x)+(vectorAB.y*vectorAB.y));
                                double lengthCB = Math.sqrt((vectorCB.x*vectorCB.x)+(vectorCB.y*vectorCB.y));

                                double dotProd = (vectorCB.x * vectorAB.x)+(vectorCB.y * vectorAB.y);
                                double discrepancy = dotProd/(lengthAB * lengthCB);
                                if(discrepancy <= .15){
                                    edgeCounter++;
                                }
                                System.out.println("Discrepancy value" + discrepancy);
                            }
                            boolean isPerp = (edgeCounter >= 2);
                            System.out.println(isPerp);

//                            double fillArea = actualArea/rectArea;

                            if(Imgproc.isContourConvex(approxInt) && isPerp) {
                                Imgproc.drawContours(mats,validContour, 0, new Scalar(0, 0, 255));
                                Imgproc.rectangle(mats, rectBound, new Scalar(255, 0, 0),5);
                            }
                        }
                    }

                    Imgcodecs.imencode(".jpg",mats,matB); // Makes image into jpg type
                    Imgcodecs.imencode(".jpg",edgeScale,matGray);

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
