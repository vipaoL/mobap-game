/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.*;
import at.emini.physics2D.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.midlet.*;

/**
 * @author steamuser
 */
public class Main extends MIDlet {

    public static GraphicsWorld gameWorld;
    public static mCanvas gameCanvas;
    public static mnCanvas menuCanvas;
    public static int sWidth = 240;
    public static int sHeight = 320;
    Display display = Display.getDisplay(this);
    public static Main inst;
    

    public Main() {
        
        
        gameCanvas = new mCanvas();
        menuCanvas = new mnCanvas();
        sWidth = gameCanvas.getWidth();
        sHeight = gameCanvas.getHeight();

        //readWorldFile("/rsc/game_world_test.phy");
        //gameCanvas.setWorld(gameWorld);
        inst = this;
        
        set(menuCanvas);
    }

    public void startApp() {
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        notifyDestroyed();
    }
    
    public static void set(Displayable d) {
        Display.getDisplay(inst).setCurrent(d);
    }
    
    public static void exit() {
        inst.destroyApp(true);
    }
    
    public static void readWorldFile(String path) {
        PhysicsFileReader reader = null;
        if (path == null) {
            path = "/rsc/game_world_test.phy";
            reader = new PhysicsFileReader(path);
        } else {
            try {
                FileConnection fc= (FileConnection) Connector.open(path);
                InputStream is= fc.openInputStream();
                reader = new PhysicsFileReader(is);
                is.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (reader == null) {
            path = "/rsc/game_world_test.phy";
            reader = new PhysicsFileReader(path);
        }
        gameWorld = new GraphicsWorld(World.loadWorld(reader));
        reader.close();
    }

}
