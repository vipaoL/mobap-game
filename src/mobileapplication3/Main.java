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
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.*;

/**
 * @author vipaol
 */
public class Main extends MIDlet {
    
    // for numbering snapshots. e.g.: '1', '2', '3', ... .
    // '-1' if release
    public static int PRE_VERSION = -1;
    
    // enable or disable on-screen log on start
    public static boolean isScreenLogEnabled = false;
    // time for one frame. 1000ms / 50ms = 20fps
    public static final int TICK_DURATION = 50;
    static int sWidth, sHeight;
    public static Main thiss;
    
    public static String[] onScreenLog = new String[1];
    public static int onScreenLogOffset = 0;
    public static int lastWroteI = 0;
    public static boolean isScreenLogInited = false;
    public static int logMessageDelay = 50;
    
    private boolean isStartedAlready = false;

    public void startApp() {
        if (isStartedAlready) {
            log("Main:startApp:already started");
            return;
        }
        
        isStartedAlready = true;
        
        thiss = this;
        Main.log("Main:constr");
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
                    log("Can't load alert image");
                }
            }
            alert = new Alert("Oh no!", text, alertImage, AlertType.ERROR);
            if (duration > 0) {
                alert.setTimeout(duration);
            }
            Display.getDisplay(thiss).setCurrent(alert); // TODO: implement nextDisplayable
        } catch(IllegalArgumentException ex) {
            ex.printStackTrace();
            log("Can't show alert:" + text);
        }
        log(text);
    }
    
    /*public static void logErr(String text, int value) {
        
    }*/

    public static void log(String text, int value) {
        if (isScreenLogEnabled | DebugMenu.isDebugEnabled) {
            log(text + value);
        }
    }
    public static void log(int i) {
        if (isScreenLogEnabled | DebugMenu.isDebugEnabled) {
            log(String.valueOf(i));
        }
    }
     public static void log(String text) {
        if (DebugMenu.isDebugEnabled) {
            System.out.println(text);
        }
        if (isScreenLogEnabled) {
            if (onScreenLog[onScreenLogOffset] != null) {
                for (int i = 0; i < onScreenLog.length - 1; i++) {
                    onScreenLog[i] = onScreenLog[i + 1];
                }
            }
            onScreenLog[onScreenLogOffset] = text;
            lastWroteI = onScreenLogOffset;
            if (onScreenLogOffset < onScreenLog.length - 1) {
                onScreenLogOffset++;
            }
            try { // slowing for log readability
                if (logMessageDelay > 0) {
                    Thread.sleep(logMessageDelay);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    public static boolean logReplaceLast(String prevMsg, String newMsg) {
        if (DebugMenu.isDebugEnabled) {
            System.out.println(newMsg);
        }
        if (onScreenLog[lastWroteI] == null) {
            return false;
        }
        
        if (onScreenLog[lastWroteI].equals(prevMsg)) {
            onScreenLog[lastWroteI] = newMsg;
            return true;
        } else {
            return false;
        }
    }
    public static void enableLog(int screenHeight) {
        isScreenLogEnabled = true;
        if (!isScreenLogInited) {
            onScreenLogOffset = 0;
            onScreenLog = new String[screenHeight/Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL).getHeight()];
            isScreenLogInited = true;
            log("log enabled");
        }
    }
    public static void disableLog() {
        log("disabling log...");
        isScreenLogEnabled = false;
        if (isScreenLogInited) {
            onScreenLog = new String[1];
            isScreenLogInited = false;
            onScreenLogOffset = 0;
        }
    }
}
