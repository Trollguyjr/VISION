import javax.swing.*;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ImageManipulation extends JFrame {
    private Point originalPt;
    private Point currentPt,optimizedPt;
    private Mat imageMat,tempM;
    private JLabel image;
    private int slope = 0;
    private boolean drawing = false;
    public ImageManipulation(){
        this.setLayout(null);
        imageMat = Imgcodecs.imread("Images\\Image0.jpg");

        this.setSize(imageMat.width(),imageMat.height());
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        loadImage();
        this.setVisible(true);
    }

    public void loadImage(){
        image = new JLabel();
        MatOfByte matB = new MatOfByte();
        tempM = imageMat.clone();
        Imgcodecs.imencode(".jpg",imageMat,matB);
        image.setIcon(new ImageIcon(matB.toArray()));
        image.setBounds(0,0,imageMat.width(),imageMat.height());
        image.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                originalPt = new Point(e.getX(),e.getY());
                drawing = true;
                System.out.println(originalPt);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                drawing = false;
                currentPt = new Point(e.getX(),e.getY());
            }
        });

        image.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                System.out.println("dragging");
                optimizedPt = new Point(e.getX(), e.getY());
                slope = (int) Math.sqrt(Math.pow(optimizedPt.x - originalPt.x, 2) + Math.pow(optimizedPt.y - originalPt.y, 2));
                if(drawing) {
                    Imgproc.circle(tempM, originalPt, slope, new Scalar(255, 0, 0), 10);
                    Imgcodecs.imencode(".jpg", tempM, matB);
                    image.setIcon(new ImageIcon(matB.toArray()));
                }
                tempM = imageMat.clone();
                System.out.println(slope);
            }
        });
        add(image);
    }

    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ImageManipulation e = new ImageManipulation();
    }
}
