/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.World;
import at.emini.physics2D.util.PhysicsFileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author vipaol
 */
public class Levels extends GenericMenu implements Runnable {

    private Vector levelNames = new Vector();
    
    private static int fontSizeCache = -1;
    
    private FileUtils files = new FileUtils("Levels");

    public Levels() {
        Logger.log("Levels:constructor");
        setFullScreenMode(true);
        paint();
        (new Thread(this, "level picker")).start();
    }

    public void init() {
        isStopped = false;
        levelNames = new Vector();
        Logger.log("Levels:start()");
        try {
            levelNames.addElement("---levels---");
            getLevels();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
            levelNames.setElementAt("no read permission", 0);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        // TODO: separate with pages -----------------------!
        levelNames.addElement("--Back--");
        loadParams(Main.sWidth, Main.sHeight, levelNames, 1, levelNames.size() - 1, levelNames.size() - 1, fontSizeCache);
        fontSizeCache = getFontSize();
        showNotify();
    }
    
    public void getLevels() {
        Logger.log("Levels:getLevels()");
        Enumeration list;
        while (true) {            
            list = files.getNextList();
            // if no more directories to look into, break the cycle
            if (list == null) {
                break;
            } else {
                while (list.hasMoreElements()) {
                    levelNames.addElement(files.path + list.nextElement());
                }
            }
        }
    }
    
    public void startLevel(final String path) {
        (new Thread(new Runnable() {
            public void run() {
                GameplayCanvas gameCanvas = new GameplayCanvas(readWorldFile(path));
                Main.set(gameCanvas);
                isStopped = true;
            }
        })).start();
    }
    
    public GraphicsWorld readWorldFile(String path) {
        PhysicsFileReader reader;
        try {
            InputStream is;
            is = files.fileToInputStream(path);
            reader = new PhysicsFileReader(is);
            GraphicsWorld w = new GraphicsWorld(World.loadWorld(reader));
            reader.close();
            is.close();
            return w;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public void selectPressed() {
        if (selected == levelNames.size() - 1) {
            isStopped = true;
            Main.set(new MenuCanvas());
        } else {
            try {
                startLevel((String) levelNames.elementAt(selected));
            } catch (NullPointerException ex) {
                Main.showAlert(ex);
            } catch (SecurityException ex) {
                Main.showAlert(ex);
            }
        }
    }
    

    public void run() {
        sizeChanged(getWidth(), getHeight());
        init();
        Logger.log("Levels:run()");
        long sleep = 0;
        long start = 0;
        
        isPaused = false;
        while (!isStopped) {
            if (h != getHeight()) {
                fontSizeCache = -1;
                showNotify();
            }
            if (!isPaused) {
                start = System.currentTimeMillis();
                paint();

                sleep = Main.TICK_DURATION - (System.currentTimeMillis() - start);
                sleep = Math.max(sleep, 0);
            } else {
                sleep = 100;
            }
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void paint() {
        Graphics g = getGraphics();
        g.setColor(0);
        g.fillRect(0, 0, w, h);
        super.paint(g);
        tick();
        flushGraphics();
    }
}
