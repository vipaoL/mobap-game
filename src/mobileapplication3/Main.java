/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.*;
import at.emini.physics2D.util.*;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.*;

/**
 * @author steamuser
 */
public class Main extends MIDlet {

    //public static GraphicsWorld gameWorld;
    //public static mCanvas gameCanvas;
    mnCanvas menuCanvas;
    //static Levels levelPicker;
    static int sWidth = 240;
    static int sHeight = 320;
    Display display = Display.getDisplay(this);
    public static Main thiss;
    public static Displayable current;
    

    public Main() {
        
        
        //gameCanvas = new mCanvas();
        menuCanvas = new mnCanvas();
        //levelPicker = new Levels();
        sWidth = menuCanvas.getWidth();
        sHeight = menuCanvas.getHeight();

        //readWorldFile("/rsc/game_world_test.phy");
        //gameCanvas.setWorld(gameWorld);
        thiss = this;
        
    }

    public void startApp() {
        set(menuCanvas);
        //menuCanvas.start();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        notifyDestroyed();
    }
    
    public static void exit() {
        thiss.destroyApp(true);
    }
    
    public static void set(Displayable d) {
        Display.getDisplay(thiss).setCurrent(d);
        current = d;
        clear();
    }
    
    public static void showAlert(String text) {
        try {
            AlertType.ERROR.playSound(thiss.display);
        
        //you need to import javax.microedition.lcdui;
            Image alertImage = null;
            try {
                alertImage = Image.createImage("/driver.png");
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
            Alert alert = new Alert("О нет!", text, alertImage, AlertType.INFO);
            alert.setTimeout(3000);    // for 3 seconds
            //thiss.display.setCurrent(alert, current);    // so that it goes to back to your canvas after displaying the alert
            } catch(IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void clear() {
        System.gc();
    }

    public static void print(String text, int category) {
        if (category == 0) {
            text = "[INFO]: ";
        }
        System.out.println(text);
    }
    public static void print(String text) {
        text = "info: " + text;
        System.out.println(text);
    }
}
