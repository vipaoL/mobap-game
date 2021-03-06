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
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author vipaol
 */
public class Levels extends GameCanvas implements Runnable/*, CommandListener*/ {

    Enumeration drives;
    String prefix = "file:///";
    String root = "C:/";
    String sep = "/";

    private Command select, back;

    Enumeration list;

    boolean stopped = false;
    Vector v;

    int scW = this.getWidth();
    int scH = this.getHeight();
    int tick = 0;
    int selected = 1;
    int delay = 10;
    private static int fontSizeCache = -1;
    Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    boolean paused = false;
    private GenericMenu menu = new GenericMenu();

    Levels() {
        super(true);
        setFullScreenMode(true);
        select = new Command("Select", Command.OK, 1);
        back = new Command("Back", Command.BACK, 2);
        (new Thread(this, "level picker")).start();
    }

    public void start() {
        //drives = FileSystemRegistry.listRoots();
        stopped = false;
        v = new Vector();

        try {
            drives = getRoots();
            v.addElement("---levels---");
            getLevels();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        v.addElement("--Back--");
        showNotify();

        //addCommand(select);
        //addCommand(back);
        //setCommandListener(this);

        //runner = new Thread(this);
        //runner.start();
    }

    protected void showNotify() {
        scW = this.getWidth();
        scH = this.getHeight();
        if (font.getHeight() * v.size() >= scH) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
            if (font.getHeight() * v.size() >= scH) {
                font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            }
        }
        if (font.getSize() != Font.SIZE_SMALL) {
            for (int i = 1; i < v.size() - 1; i++) {
                if (font.stringWidth((String) v.elementAt(i)) >= scW) {
                    font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
                    if (font.stringWidth((String) v.elementAt(i)) >= scW) {
                        font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
                        break;
                    }
                }
            }
        }
        menu.loadParams(scW, scH, v, 1, v.size() - 1, selected, fontSizeCache);
        fontSizeCache = menu.getFontSize();
        paused = false;
    }

    protected void hideNotify() {
        paused = true;
    }

    public void paint(Graphics g) {
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, scW, scH);
        menu.paint(g);
        menu.tick();
    }

    public void getLevels() {
        if (drives.hasMoreElements()) {
            while (drives.hasMoreElements()) {
                checkDrive((String) drives.nextElement());
            }
        } else {
            
        }
        try {
            String path = System.getProperty("fileconn.dir.photos");
            listFiles(path);
            path = System.getProperty("fileconn.dir.graphics");
            listFiles(path);
        } catch (SecurityException ex) {
            ex.printStackTrace();
            //Main.showAlert(ex);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            //Main.showAlert(ex);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            //Main.showAlert(ex);
        }
    }
    
    void checkDrive(String root) {
        String path = prefix + root;
        try {
            listFiles(path);
            path = prefix + root + "other" + sep;
            listFiles(path);
            //path = prefix + root + "predefgallery" + sep + "predefgraphics" + sep + "Levels/";
            //listFiles(path);
        } catch (SecurityException ex) {
            //ex.printStackTrace();
            //Main.showAlert(ex);
        } catch (IllegalArgumentException ex) {
            //ex.printStackTrace();
            //Main.showAlert(ex);
        }
    }

    boolean listFiles(String path) {
        if (path != null) {
            path += "Levels" + sep;
            try {
                FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
                if (fc.exists() & fc.isDirectory()) {
                    list =  fc.list();
                    while (list.hasMoreElements()) {
                        v.addElement(path + list.nextElement());
                    }
                    return true;
                }
            } catch (IOException ex) {
                //Main.showAlert(ex);
                //ex.printStackTrace();
            } catch (IllegalArgumentException ex) {
                //Main.showAlert(ex);
                //ex.printStackTrace();
            } catch (SecurityException ex) {
                
            }
        }
        return false;
    }

    public void run() {
        start();
        long sleep = 0;
        long start = 0;
        long millis = 50;

        while (!stopped) {
            if (scW != getWidth()) {
                showNotify();
            }
            if (!paused) {
                start = System.currentTimeMillis();
                input();
                repaint();

                sleep = millis - (System.currentTimeMillis() - start);
                sleep = Math.max(sleep, 0);

                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void startLevel(String path) {
        GameplayCanvas gameCanvas = new GameplayCanvas();
        gameCanvas.setWorld(readWorldFile(path));
        Main.set(gameCanvas);
    }

    private Enumeration getRoots() {
        return FileSystemRegistry.listRoots();
    }

    /*public void commandAction(Command cmd, Displayable display) {
        if (cmd == select) {
            selectPressed();
        }
        if (cmd == back) {
            mnCanvas m = new mnCanvas();
            Main.set(m);
            m.start();
        }
    }*/
    
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

    public void selectPressed() {
        selected = menu.selected;
        stopped = true;
        if (selected == v.size() - 1) {
            Main.set(new MenuCanvas());
        } else {
            try {
                startLevel((String) v.elementAt(selected));
            } catch (NullPointerException ex) {
                Main.showAlert(ex.toString());
            } catch (SecurityException ex) {
                
            }
        }
    }

    public GraphicsWorld readWorldFile(String path) {
        //GraphicsWorld gameWorld;
        PhysicsFileReader reader;
        try {
            InputStream is;
            FileConnection fc = (FileConnection) Connector.open(path);
            is = fc.openInputStream();
            //mCanvas.text += is.available();
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
}
