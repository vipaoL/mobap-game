/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import java.io.IOException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.*;

/**
 * @author vipaol
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
    public static final int printCategory_info = 0;
    public static final int printCategory_err = 1;
    

    public Main() {
        menuCanvas = new mnCanvas();
        sWidth = menuCanvas.getWidth();
        sHeight = menuCanvas.getHeight();
        thiss = this;
        set(menuCanvas);
    }

    public void startApp() {
        //throw new NullPointerException();
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
    
    public static void main(String[] args) {
        
    }
    
    public static void showAlert(Throwable ex) {
        showAlert(ex.toString());
    }
    public static void showAlert(String text) {
        try {
            //AlertType.ERROR.playSound(thiss.display);
            Image alertImage = null;
            try {
                alertImage = Image.createImage("/driver.png");
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
            Alert alert = new Alert("Oh no!", text, alertImage, AlertType.ERROR);
            alert.setTimeout(3000);
            //thiss.display.setCurrent(alert, current);
            } catch(IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void clear() {
        System.gc();
    }

    public static void print(String text, int category) {
        if (category == printCategory_info) {
            text = "info: " + text;
        }
        if (category == printCategory_err) {
            text = "ERROR: " + text;
        }
        System.out.println(text);
    }
    public static void print(int i) {
        print("" + i);
    }
    public static void print(String text) {
        print(text, printCategory_info);
    }
}
