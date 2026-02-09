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

public class MainVideo extends JFrame{
    public static int liveCounter = 0; // Counter only works while live. Change name of important saves

    private static final int VERT_INT = 1;
    private static final int HORIZ_INT = -1;
    private static final int VERT_ANGLE_1 = 90;
    private static final int VERT_ANGLE_2 = 80;

    private static final int HORIZ_ANGLE_1 = 105;
    private static final int HORIZ_ANGLE_2 = 10;

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
    private static Vector lineToVector(Lines l){
        double a = l.getPt2().y - l.getPt1().y;
        double b = l.getPt2().x - l.getPt1().x;
        double c = l.getPt2().x * l.getPt1().y - l.getPt1().x * l.getPt2().y;
        Vector v = new Vector(a,b,c);
        v.magnitude = Math.sqrt((a*a) + (b*b));

        return v;
    }

    private static int lineChecker(Lines l){

        // Bad magic number will change later
        if(Math.abs(l.angleD()) > VERT_ANGLE_2 && l.length > 30){
            return VERT_INT;
        }
        else if(Math.abs(l.angleD()) < 10 && l.length > 30){
            return HORIZ_INT;
        }
        else{
            return 0;
        }
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
                    // Imgproc.HoughLines(edgeScale,lineScale,0.5,Math.PI/180,5);
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
                     */

                    Imgproc.HoughLinesP(edgeScale,lineScale,1,Math.PI/180,50,20);

                    ArrayList<Lines> vertLine = new ArrayList<>();
                    ArrayList<Lines> vertLineSorted = new ArrayList<>();

                    ArrayList<Lines> horizLine = new ArrayList<>();
                    ArrayList<Lines> horizLineSorted = new ArrayList<>();

                    ArrayList<Lines> vertLineV = new ArrayList<>();
                    ArrayList<Lines> horizLineV = new ArrayList<>();
                    if(!lineScale.empty()) {
                        for(int i = 0; i < lineScale.rows(); i++){
                            double[] values = lineScale.get(i,0); // checks which angle matches: {rho, theta}
                            System.out.println("We are doing somethings");

                            Point pt1 = new Point(values[0],values[1]);
                            Point pt2 = new Point(values[2],values[3]);

                            Lines l = new Lines(pt1,pt2);

                            // Hypotenuse formula using point
                            double theta = Math.atan2(pt2.y - pt1.y,pt2.x - pt1.x);
                            System.out.println(Math.toDegrees(theta));
                            // distance formula
                            double length = Math.sqrt(((pt2.x - pt1.x)*(pt2.x - pt1.x))+((pt2.y - pt1.y)*(pt2.y - pt1.y)));
                            l.setLength(length);
                            l.angleR(theta);

//                            Vector v = lineToVector(l);

                            switch(lineChecker(l)){
                                case VERT_INT:
                                    // Minimum of 2
                                    vertLine.add(l);
                                    break;
                                case HORIZ_INT:
                                    horizLine.add(l);
                                    break;
                                default:
                                    break;
                            };
                        }
                    }
                    while(vertLine.size() > 1){
                        Lines base = vertLine.get(0);
                        double baseMidX = (base.pt1.x + base.pt2.x)/2;
                        boolean merged = false;

                        for(int i = 1; i < vertLine.size(); i++) {
                            Lines otherLine = vertLine.get(i);
                            double otherMidX = (otherLine.pt1.x + otherLine.pt2.x) / 2;

                            boolean perpDistance = Math.abs((baseMidX) - (otherMidX)) < 10;

                            if (perpDistance) {
                                Lines confirmedLine = new Lines(base.pt1, otherLine.pt2);
                                vertLineSorted.add(confirmedLine);
                                System.out.println("Sorted LINE-----------");

                                merged = true;
                                vertLine.remove(i);
                                vertLine.remove(0);
                                break;
                            }
                        }
                        if(!merged){
                            vertLine.remove(0);
                        }
                    }

                    while(horizLine.size() > 1){
                        Lines base = horizLine.get(0);
                        double baseMidY = (base.pt1.y + base.pt2.y)/2;
                        boolean merged = false;

                        for(int j = 1; j < horizLine.size(); j++){

                            Lines otherLine = horizLine.get(j);
                            double otherMidY = (otherLine.pt1.y + otherLine.pt2.y)/2;

                            boolean perpDistance = Math.abs((baseMidY) - (otherMidY)) < 10;

                            if(perpDistance){
                                Lines confirmedLine = new Lines(base.pt1, otherLine.pt2);
                                horizLineSorted.add(confirmedLine);
                                System.out.println("Sorted LINE-----------");

                                merged = true;
                                horizLine.remove(j);
                                horizLine.remove(0);
                                break;
                            }
                        }
                        if(!merged){
                            horizLine.remove(0);
                        }
                    }
                    for(Lines l : horizLineSorted){
                        Imgproc.line(mats,l.pt1,l.pt2,new Scalar(255,0,0),10);
                    }
                    for(Lines l : vertLineSorted){
                        Imgproc.line(mats,l.pt1,l.pt2,new Scalar(0,0,255),10);
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
        public double length;

        public Lines(double rho, double theta){
            this.rho = rho;
            this.theta  = theta;
        }

        public Lines(Point pt1, Point pt2){
            this.pt1 = pt1;
            this.pt2 = pt2;
        }

        public void setLength(double length){
            this.length = length;
        }

        public double getLength(){
            return length;
        }

        public double angleD(){
            return theta;
        }

        public double angleR(double theta){
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
    public static class Vector{
        public double a; // represents Direction in y direction
        public double b; // represents x dir
        public double c; // dot product direction
        public double magnitude; //  ||v||

        public Vector(){
            a = 0;
            b = 0;
            c = 0;
            magnitude = 0;
        }

        public Vector(double a, double b, double c){
            this.a = a;
            this.b = b;
            this.c = c;
            magnitude = Math.sqrt((a*a) + (b*b) + (c*c));
        }
    }
}
