import javax.swing.*;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ImageManipulation extends JFrame {
    private Point originalPt;
    private Point optimizedPt;
    private Mat imageMat,tempM;
    private JLabel image;
    private JButton saveImage;
    private int slope = 0;
    private boolean circleC,lineC = false;
    private String name, fileName;

    public ImageManipulation(){
        this.setLayout(null);

        name = "Image0";
        fileName = "Images\\" + name + ".jpg";

        imageMat = Imgcodecs.imread(fileName);

        saveImage = new JButton("Save");
        saveImage.setSize(new Dimension(75,25));
        saveImage.setLocation(0,490);
        saveImage.setVisible(true);

        //Saves image with a copy
        saveImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                name = "Copy" + name;
                fileName = "Images\\" + name + ".jpg";;
                System.out.println(fileName);
                Imgcodecs.imwrite(fileName,imageMat);
            }
        });

        this.setSize(imageMat.width(),imageMat.height()+80);
        this.add(saveImage);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        loadImage();
        this.setVisible(true);
    }

    public void loadImage(){
        image = new JLabel();
        //Turns Image into readable info
        MatOfByte matB = new MatOfByte();
        //Temp mat to demonstrate visual changes
        tempM = imageMat.clone();
        //Reads the Image
        Imgcodecs.imencode(".jpg",imageMat,matB);
        image.setIcon(new ImageIcon(matB.toArray()));
        image.setBounds(0,0,imageMat.width(),imageMat.height());
        image.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                originalPt = new Point(e.getX(),e.getY());
                //Left Click
                if(e.getButton() == 1) {
                    circleC = true;
                }
                //Right click
                else if(e.getButton() == 3){
                    lineC = true;
                }
                System.out.println(originalPt);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                //Wheel click
                if(e.getButton() == 2){
                    //saving info
                    System.out.println("Saved");
                    imageMat = tempM;
                    Imgcodecs.imencode(".jpg",imageMat,matB);
                    image.setIcon(new ImageIcon(matB.toArray()));
                }
                circleC = false;
                lineC = false;
            }
        });

        image.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                //Update everytime we drag so that the previous circle when moving doesnt save
                tempM = imageMat.clone();
                System.out.println("dragging");
                optimizedPt = new Point(e.getX(), e.getY());
                //Distance formula
                slope = (int) Math.sqrt(Math.pow(optimizedPt.x - originalPt.x, 2) + Math.pow(optimizedPt.y - originalPt.y, 2));
                if(circleC) {
                    Imgproc.circle(tempM, originalPt, slope, new Scalar(255, 0, 0), 10);
                    Imgcodecs.imencode(".jpg", tempM, matB);
                }
                else if(lineC){
                    Imgproc.line(tempM, originalPt,optimizedPt,new Scalar(255,255,255),10);
                    Imgcodecs.imencode(".jpg",tempM,matB);
                }
                image.setIcon(new ImageIcon(matB.toArray()));
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
