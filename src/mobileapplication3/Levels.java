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
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author vipaol
 */
public class Levels extends GameCanvas implements Runnable, GenericMenu.Feedback {

    Vector levelNames = new Vector();

    int scW = 0;
    int scH = 0;
    
    boolean paused = false;
    boolean stopped = false;
    
    private static int fontSizeCache = -1;
    private GenericMenu menu = new GenericMenu(this);
    
    FileUtils files = new FileUtils("Levels");

    Levels() {
        super(false);
        Main.log("Levels:constructor");
        setFullScreenMode(true);
        repaint();
        (new Thread(this, "level picker")).start();
    }

    public void start() {
        stopped = false;
        levelNames = new Vector();
        Main.log("Levels:start()");
        repaint();
        try {
            levelNames.addElement("---levels---");
            getLevels();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            levelNames.setElementAt("no read permission", 0);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        // TODO: separate with pages -----------------------!
        levelNames.addElement("--Back--");
        menu.loadParams(scW, scH, levelNames, 1, levelNames.size() - 1, levelNames.size() - 1, fontSizeCache);
        fontSizeCache = menu.getFontSize();
        showNotify();
    }
    
    public void getLevels() {
        Main.log("Levels:getLevels()");
        Enumeration list;
        while (true) {            
            list =  files.getNextList();
            
            // if no more files, break the cycle
            if (list == null) {
                break;
            } else {
                while (list.hasMoreElements()) {
                    levelNames.addElement(files.path + list.nextElement());
                }
            }
        }
    }
    
    public void startLevel(String path) {
        GameplayCanvas gameCanvas = new GameplayCanvas();
        gameCanvas.setWorld(readWorldFile(path));
        Main.set(gameCanvas);
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
        stopped = true;
        if (menu.selected == levelNames.size() - 1) {
            Main.set(new MenuCanvas());
        } else {
            try {
                startLevel((String) levelNames.elementAt(menu.selected));
            } catch (NullPointerException ex) {
                Main.showAlert(ex.toString());
            } catch (SecurityException ex) {
                
            }
        }
    }
    

    public void run() {
        start();
        Main.log("Levels:started");
        long sleep = 0;
        long start = 0;

        showNotify();
        
        paused = false;
        while (!stopped) {
            if (scH != getHeight()) {
                fontSizeCache = -1;
                showNotify();
            }
            if (!paused) {
                start = System.currentTimeMillis();
                input();
                repaint();

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
    
    public void paint(Graphics g) {
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, scW, scH);
        menu.paint(g);
        menu.tick();
    }
    
    protected void showNotify() {
        paused = false;
        scW = getWidth();
        scH = getHeight();
        getGraphics().fillRect(0, 0, Math.max(scW, scH), Math.max(scW, scH));
        menu.reloadCanvasParameters(scW, scH);
        menu.handleShowNotify();
    }

    protected void hideNotify() {
        paused = true;
        menu.handleHideNotify();
    }
    
    public void setIsPaused(boolean isPaused) {
        this.paused = isPaused;
    }
    
    
    private void input() {
        int keyStates = getKeyStates();
        if (menu.handleKeyStates(keyStates)) {
            selectPressed();
        }
    }
    
    public void keyPressed(int keyCode) {
        if(menu.handleKeyPressed(keyCode)) {
            selectPressed();
        }
    }
    public void keyReleased(int keyCode) {
        menu.handleKeyReleased(keyCode);
    }

    protected void pointerPressed(int x, int y) {
        menu.handlePointer(x, y);
    }

    protected void pointerDragged(int x, int y) {
        menu.handlePointer(x, y);
    }

    protected void pointerReleased(int x, int y) {
        if (menu.handlePointer(x, y)) {
            selectPressed();
        }
    }

    public boolean getIsPaused() {
        return paused;
    }

    public void recheckInput() {
        input();
    }
}
