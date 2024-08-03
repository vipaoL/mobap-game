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

import utils.Logger;

/**
 * @author vipaol
 */
public class Main extends MIDlet {
    
    // for numbering snapshots. e.g.: '1', '2', '3', ... .
    // '-1' if release
    // TODO: move to manifest
    public static int PRE_VERSION = -1;
    
    // time for one frame. 1000ms / 50ms = 20fps
    public static final int TICK_DURATION = 50;
    public static int sWidth, sHeight;
    public static Main thiss;
    
    
    private boolean isStartedAlready = false;

    public void startApp() {
        if (isStartedAlready) {
            Logger.log("Main:startApp:already started");
            return;
        }
        
        isStartedAlready = true;
        
        thiss = this;
        Logger.log("Main:constr");
        MenuCanvas menuCanvas = new MenuCanvas();
        set(menuCanvas);
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
        System.gc();
        sWidth = d.getWidth();
        sHeight = d.getHeight();
    }
    
    public static void set(Alert a) {
        Display.getDisplay(thiss).setCurrent(a, Display.getDisplay(thiss).getCurrent());
        System.gc();
        sWidth = a.getWidth();
        sHeight = a.getHeight();
    }
    
    public static void showAlert(Throwable ex) {
        ex.printStackTrace();
        showAlert(ex.toString());
    }
    public static void showAlert(String text) {
        showAlert(text, -1);
    }
    public static void showAlert(String text, int duration) {
        Alert alert = null;
        try {
            //AlertType.ERROR.playSound(thiss.display);
            Image alertImage = null;
            try {
                alertImage = Image.createImage("/driver.png");
            } catch (IOException ex1) {
                try {
                    alertImage = Image.createImage("resourse://driver.png");
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.log("Can't load alert image");
                }
            }
            alert = new Alert("Oh no!", text, alertImage, AlertType.ERROR);
            if (duration > 0) {
                alert.setTimeout(duration);
            }
            Display.getDisplay(thiss).setCurrent(alert); // TODO: implement nextDisplayable
        } catch(IllegalArgumentException ex) {
            ex.printStackTrace();
            Logger.log("Can't show alert:" + text);
        }
        Logger.log(text);
    }
    
}
