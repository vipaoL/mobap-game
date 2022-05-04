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
public class Levels extends GameCanvas implements Runnable, CommandListener {

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
    int k = 20;
    int selected = 1;
    int delay = 10;
    String xoba = "";
    Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    //Thread runner;
    boolean paused = false;

    Levels() {
        super(true);
        setFullScreenMode(true);
        scW = this.getWidth();
        scH = this.getHeight();
        select = new Command("Select", Command.OK, 1);
        back = new Command("Back", Command.BACK, 2);
        (new Thread(this, "level picker")).start();
    }

    public void start() {
        //drives = FileSystemRegistry.listRoots();
        stopped = false;
        v = new Vector();

        drives = getRoots();
        v.addElement("---levels---");
        //try {
        getLevels();

        v.addElement("--Back--");
        showNotify();

        //addCommand(select);
        //addCommand(back);
        setCommandListener(this);

        //runner = new Thread(this);
        //runner.start();
    }

    protected void showNotify() {
        scW = this.getWidth();
        scH = this.getHeight();
        if (font.getHeight() * v.size() > scH) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        }
        if (font.getHeight() * v.size() > scH) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }
        paused = false;
    }

    protected void hideNotify() {
        paused = true;
    }

    public void paint(Graphics g) {
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, scW, scH);
        g.setColor(255, 255, 255);
        int offset = 0;
        for (int i = 0; i < v.size(); i++) {
            if (i == selected) {
                g.setColor(255, 64, 64);
                offset = Mathh.sin(tick * 360 / 10);
            } else {
                g.setColor(255, 255, 255);
                offset = 0;
            }
            g.setFont(font);

            if (i == 4) {
                font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            }
            k = (scH + scH / (v.size() + 1)) / (v.size() + 1);
            g.drawString((String) v.elementAt(i), scW / 2, k * (i + 1) - font.getHeight() / 2 - scH / (v.size() + 1) / 2 + offset * Font.getDefaultFont().getHeight() / 8000 + font.getHeight() / 2, Graphics.HCENTER | Graphics.TOP);
        }
        if (tick > 9) {
            tick = 0;
        } else {
            tick++;
        }
    }

    public void getLevels() {
        if (drives.hasMoreElements()) {
            while (drives.hasMoreElements()) {
                checkDrive((String) drives.nextElement());
            }
        } else {
            
        }
        String path = System.getProperty("fileconn.dir.photos") + "Levels/";
        listFiles(path);
        path = System.getProperty("fileconn.dir.graphics") + "Levels/";
        listFiles(path);
    }
    
    void checkDrive(String root) {
        String path = prefix + root + "Levels/";
        try {
            listFiles(path);
            path = prefix + root + "other" + sep + "Levels/";
            listFiles(path);
            //path = prefix + root + "predefgallery" + sep + "predefgraphics" + sep + "Levels/";
            //listFiles(path);
        } catch (SecurityException ex) {
            //ex.printStackTrace();
            //Main.showAlert(ex);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            //Main.showAlert(ex);
        }
    }

    boolean listFiles(String path) {
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
            Main.showAlert(ex);
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            Main.showAlert(ex);
            ex.printStackTrace();
        }
        return false;
    }

    public void run() {
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

    private void input() {
        int keyStates = getKeyStates();
        if (delay < 1) {
            delay = 5;
            if ((keyStates & (RIGHT_PRESSED | FIRE_PRESSED)) != 0) {
                stopped = true;
                selectPressed();
            } else if ((keyStates & UP_PRESSED) != 0) {
                if (selected > 1) {
                    selected--;
                } else {
                    selected = v.size() - 1;
                }
            } else if ((keyStates & DOWN_PRESSED) != 0) {
                if (selected < v.size() - 1) {
                    selected++;
                } else {
                    selected = 1;
                }
            }
        } else {
            delay--;
        }
        if (keyStates == 0) {
            delay = 0;
        }
    }

    public void startLevel(String path) {
        gCanvas gameCanvas = new gCanvas();
        gameCanvas.setWorld(readWorldFile(path));
        Main.set(gameCanvas);
    }

    private Enumeration getRoots() {
        return FileSystemRegistry.listRoots();
    }

    public void commandAction(Command cmd, Displayable display) {
        if (cmd == select) {
            selectPressed();
        }
        if (cmd == back) {
            mnCanvas m = new mnCanvas();
            Main.set(m);
            m.start();
        }
    }

    protected void pointerPressed(int x, int y) {
        selected = y / k;
        //selected = v.size() * y / scH;
        if (selected == 0) {
            selected = 1;
        }
    }

    protected void pointerDragged(int x, int y) {
        selected = y / k;
        //selected = v.size() * y / scH;
        if (selected == 0) {
            selected = 1;
        }
    }

    protected void pointerReleased(int x, int y) {
        selected = y / k;
        //selected = v.size() * y / scH;
        if (selected == 0) {
            selected = 1;
        } else {
            selectPressed();
        }
    }

    public void selectPressed() {
        stopped = true;
        if (selected == v.size() - 1) {
            //runner = null;
            mnCanvas m = new mnCanvas();
            Main.set(m);
            m.start();
        } else {
            try {
                startLevel((String) v.elementAt(selected));
            } catch (NullPointerException ex) {
                Main.showAlert(ex.toString());
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
