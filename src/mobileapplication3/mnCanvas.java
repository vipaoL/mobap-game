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
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author vipaol
 */
public class mnCanvas extends GameCanvas implements Runnable {
    int k = 20;
    int delay = 0;
    String[] menuOptions = {"-", "Play", "Ext Structs", "Levels", "About", "Debug", "Exit", "-"};
    private final int[] statemap = {0, 0, 0, 0, 0, 0, 0, 0};
    static int selected = 1;
    boolean pressed = false;
    int scW = getWidth();
    int scH = getHeight();
    int tick = 5;
    public static boolean debug = false;
    Graphics g;
    Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    int fontH = font.getHeight();
    boolean paused = false;
    private GenericMenu menu = new GenericMenu();
    public static boolean music = false;
    public static boolean extStructs = false;
    
    String DEFAULT_LEVEL = "";

    boolean stopped = false;

    private static final int millis = 50;
    int offset = 0;
    
    public static boolean wg = false;

    public mnCanvas() {
        super(false);
        setFullScreenMode(true);
        scW = getWidth();
        scH = getHeight();
        (new Thread(this, "menu canvas")).start();
    }

    public void start() {
        delay = 5;
        stopped = false;
    }
    
    protected void showNotify() {
        scW = getWidth();
        scH = getHeight();
        g = getGraphics();
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, scW, scH);
        
        if (font.getHeight() * menuOptions.length > scH) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        }
        if (font.getHeight() * menuOptions.length > scH) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }
        fontH = font.getHeight();
        menu.loadParams(scW, scH, menuOptions, 1, menuOptions.length - 2, selected);
        menu.loadStatemap(statemap);
        if (extStructs) {
            menu.setStateFor(1, 2);
        }
        menu.setSpecialOption(menuOptions.length - 3);
        paused = false;
    }

    protected void hideNotify() {
        paused = true;
    }

    public void destroyApp(boolean unconditional) {
        stopped = true;
        Main.exit();
    }

    public void run() {
        start();
        //stopped = false;

        long sleep = 0;
        long start = 0;

        while (!stopped) {
            start = System.currentTimeMillis();
            input();
            if (scW != getWidth()) {
                showNotify();
            }
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

    public void paint(Graphics g) {
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, scW, scH);
        /*
        g.setColor(255, 255, 255);
        offset = 0;
        for (int i = 0; i < menuOptions.length; i++) {
            g.setFont(font);
            if (i == selected) {
                if (pressed) {
                    g.setColor(224, 56, 56);
                    g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, font.getSize()));
                } else {
                    g.setColor(255, 64, 64);
                }
                offset = Mathh.sin(tick * 360 / 10);
            } else {
                g.setColor(255, 255, 255);
                offset = 0;
            }
            if (i == 4 & debug) {
                g.setColor(255, 255, 0);
            }
            k = (scH + scH / (menuOptions.length + 1)) / (menuOptions.length + 1);
            g.drawString(menuOptions[i], scW / 2, k * (i + 1) - font.getHeight() / 2 - scH / (menuOptions.length + 1) / 2 + offset*Font.getDefaultFont().getHeight() / 8000, Graphics.HCENTER | Graphics.TOP);
        }
        if (tick > 9) {
            tick = 0;
        } else {
            tick++;
        }
        //flushGraphics();
        */
        menu.setIsSpecialOptnActivated(debug);
        menu.paint(g);
        menu.tick();
    }

    private void input() {
        int keyStates = getKeyStates();
        /*if (delay < 1) {
            delay = 5;
            if ((keyStates & (RIGHT_PRESSED | FIRE_PRESSED)) != 0) {
                    selectPressed();
            } else if ((keyStates & UP_PRESSED) != 0) {
                if (selected > 1) {
                        selected--;
                    } else {
                        selected = menuOptions.length - 2;
                    }
            } else if ((keyStates & DOWN_PRESSED) != 0) {
                if (selected < menuOptions.length - 2) {
                        selected++;
                    } else {
                        selected = 1;
                    }
            }
        } else delay --;
        if (keyStates == 0) delay = 0;
        */
        if (menu.handleKeyStates(keyStates)) {
            selectPressed();
        }
    }

    /*public void keyReleased(int keyCode) {
        int gameAction = getGameAction(keyCode);
    }*/

    public void keyPressed(int keyCode) {
        if(menu.handleKeyPressed(keyCode)) {
            selectPressed();
        }
    }
    
    public void keyReleased(int keyCode) {
        menu.handleKeyReleased(keyCode);
    }

    public void startLevel(String path) {
        Main.print("menu:startLevel()");
        try {
            stopped = true;
            PhysicsFileReader reader = new PhysicsFileReader("/void.phy");
            gCanvas gameCanvas = new gCanvas();
            gameCanvas.setWorld(new GraphicsWorld(World.loadWorld(reader)));
            reader.close();
            Main.set(gameCanvas);
        } catch (NullPointerException ex) {
            Main.showAlert(ex.toString());
        }
    }
    
    protected void pointerPressed(int x, int y) {
        /*pressed = true;
        //k = scH / menuOptions.length;
        selected = y / k;
        //selected = menuOptions.length * (y + fontH) / scH;
        if (selected == 0) {
            selected = 1;
        }
        if (selected > 5) {
            selected = 5;
        }*/
        menu.setIsPressedNow(true);
        menu.handlePointer(x, y);
    }
    protected void pointerDragged(int x, int y) {
        /*selected = y / k;
        //selected = menuOptions.length * (y + fontH) / scH;
        if (selected == 0) {
            selected = 1;
        }
        if (selected > 5) {
            selected = 5;
        }*/
        menu.handlePointer(x, y);
    }
    protected void pointerReleased(int x, int y) {
        /*pressed = false;
        selected = y / k;
        //selected = menuOptions.length * y / scH;
        if (selected == 0) {
            selected = 1;
        } else if (selected > 5) {
            selected = 5;
        }else {
            selectPressed();
        }*/
        menu.setIsPressedNow(false);
        if (menu.handlePointer(x, y)) {
            selectPressed();
        }
    }
    
    void selectPressed() {
        selected = menu.selected;
        if (selected == 1) {
            Main.print("menu:selected == 1 -> gen = true");
            wg = true;
            startLevel(DEFAULT_LEVEL);
        }
        if (selected == 2) {
            menu.setStateFor(1, 2);
            extStructs = true;
        }
        if (selected == 3) {
            stopped = true;
            wg = false;
            Main.set(new Levels());
        }
        if (selected == 4) {
            Main.set(new about());
        }
        if (selected == 5) {
            //debug = !debug;
            //if (debug) Main.showAlert("Ну всё.");
            Main.set(new DebugMenu());
        }
        if (selected == 6) Main.exit();
    }
    public GraphicsWorld readWorldFile(String path) {
        //GraphicsWorld gameWorld;
        if (path == "") {
            return setDefaultWorld();
        } else {
                try {
                InputStream is;
                FileConnection fc = (FileConnection) Connector.open(path);
                is = fc.openInputStream();
                //mCanvas.text += is.available();
                PhysicsFileReader reader = new PhysicsFileReader(is);
                return new GraphicsWorld(World.loadWorld(reader));
                //reader.close();
                //is.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    private GraphicsWorld setDefaultWorld() {
        
            PhysicsFileReader reader = new PhysicsFileReader("/void.phy");
            return new GraphicsWorld(World.loadWorld(reader));
            //reader.close();
        
    }
}
