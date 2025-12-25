import javax.swing.*;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.lang.reflect.InaccessibleObjectException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Replication of ImageManipulation.java, more usage of this class and MetaData features
 */
public class AnnotationTools extends JFrame {
    private static Point originalPt;
    private Point optimizedPt;
    private Mat imageMat,tempM;
    private JLabel image;
    private JButton saveImage;
    private int slope = 0;
    private boolean circleC;
    private boolean lineC;
    private  String name, fileName;

    private static ArrayList<String> actions = new ArrayList<String>();


    public AnnotationTools(){
        this.setLayout(null);

        //GUI implementation and initialization
        name = "Image0";
        fileName = "Images\\" + name + ".jpg";
        //Setting up Mat by reading a file that is currently predetermined for testing
        imageMat = Imgcodecs.imread(fileName);

        saveImage = new JButton("Save");
        saveImage.setSize(new Dimension(75,25));
        saveImage.setLocation(0,490);
        saveImage.setVisible(true);

        //Saves image with a copy
        saveImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                name = "Annotation_Copy_" + name;
                fileName = "Images\\" + name + ".jpg";
                System.out.println(fileName);
                Imgcodecs.imwrite(fileName,imageMat);
                saveMetaData();
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
                System.out.println("Saved");

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if(e.getButton() == 1) {
                    circleC = false;
                    // Shape, radius, Center pt, End pt
                    actions.add("Circle,"+slope + "," + originalPt +","+ optimizedPt);
                }
                //Right click
                else if(e.getButton() == 3){
                    lineC = false;
                    // Shape, Length, Starting pt, End pt
                    actions.add("Line,"+slope + "," + originalPt +","+ optimizedPt);
                }
                imageMat = tempM;
//                Imgcodecs.imencode(".jpg",imageMat,matB);
//                image.setIcon(new ImageIcon(matB.toArray()));
//                System.out.println(originalPt);
            }
        });

        image.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                //Update everytime we drag so that the previous circle when moving doesn't save
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

    private static void saveMetaData(){
        //Using predetermined file
        File fileLocation = new File("Images\\Data_Annotation_Copy_Image0.txt");

        //Write metadata of actions
        try(FileWriter writer = new FileWriter(fileLocation,true)){
            writer.write("\n");
            for(String s : actions){
                writer.write(s);
                writer.write("\n");
            }
            System.out.println("Adding data");
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        AnnotationTools e = new AnnotationTools();
    }
}
