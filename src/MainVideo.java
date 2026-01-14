import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainVideo extends JFrame{
    public static int liveCounter = 0; // Counter only works while live. Change name of important saves

    private static final int VERT_ANGLE_1 = 165;
    private static final int VERT_ANGLE_2 = 15;

    private static final int HORIZ_ANGLE_1 = 105;
    private static final int HORIZ_ANGLE_2 = 65;

    private static JPanel panel;
    private static JLabel cam;
    private static JLabel cam2;

    private static Mat mats, grayScale, blurScale, edgeScale, lineScale,circleScale; // captures images
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

                    //@TODO ADD COMMENTS AND FINISH THE CONFIGURATION FOR DETECTING
                    Core.flip(mats,mats,+1); // flips image as a reflection instead of inverted
                    Imgproc.cvtColor(mats,grayScale,Imgproc.COLOR_BGR2GRAY);
                    Imgproc.GaussianBlur(grayScale,blurScale,new Size(3,3),0);
                    Imgproc.Canny(blurScale,edgeScale,50,100);

                    // lineScale returns --> rho(Distance), and Angle(theta)
//                    Imgproc.HoughLines(edgeScale,lineScale,0.5,Math.PI/180,5);
                    System.out.println("loading");

                    System.out.println(lineScale.cols() + " " + lineScale.rows());
                    /*
                     * Math Concept -- Linear Algebra:
                     * Basically theta represents the angle/direction of the Normal line on a graph
                     * n-vector = (cos(theta),sin(theta))
                     *
                     * p = distance of line along the vector from origin
                     * Dir = ( -sin(theta) , cos(theta) ), it is -sin b/c normal vector(n-vector) is perpendicular
                     *
                     * rho is the perpendicular length through n-vector and origin
                     *
                     * Perpendicular Eq:
                     * (x,y) = ( p * cos(theta) , p * sin(theta) )
                     *
                     * Point Calculations:
                     * a = cos(theta)
                     * b = sin(theta)
                     * x0 = a * p -- Gets pt based on the direction and length of vector
                     * y0 = b * p
                     *
                     * We get points using the regular vector
                     * pt1 = (x0 + 1000 * (-b) , y0 + 1000 * a)
                     * pt2 = (x0 - 1000 * (-b) , y0 - 1000 * a)
                     */


                    if(!mats.empty()) {
                        for(int i = 0; i < lineScale.cols(); i++){
                            Imgproc.HoughLinesP(mats,lineScale,1,Math.PI/180,100,25);
                            double[] values = lineScale.get(0,i); // checks which angle matches: {rho, theta}
                            System.out.println("We are doing somethings");

                            Point pt1 = new Point(values[0],values[1]);
                            Point pt2 = new Point(values[2],values[3]);

                            Lines l = new Lines(pt1,pt2);

                            Imgproc.line(mats,pt1,pt2,new Scalar(255,0,0),3);

                            System.out.println("We are writing to mats");
//                            Imgproc.line(mats, pt1, pt2, new Scalar(255,0,0));
                            Imgcodecs.imwrite(".jpg", mats);
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

    public static class Lines{
        private double rho;
        private double theta;
        private Point pt1;
        private Point pt2;

        public Lines(double rho, double theta){
            this.rho = rho;
            this.theta  = theta;
        }

        public Lines(Point pt1, Point pt2){
            this.pt1 = pt1;
            this.pt2 = pt2;
        }

        public double angle(){
            return Math.toDegrees(theta);
        }

        public double angle(double theta){
            this.theta = theta;
            return Math.toDegrees(theta);
        }

        public double distance(){
            return rho;
        }

        public Point getPt1(){
            return pt1;
        }
        public Point getPt2(){
            return pt2;
        }
    }
}
