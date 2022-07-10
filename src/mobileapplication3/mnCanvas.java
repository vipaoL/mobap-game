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
    String[] menuOptions = {"-", "Play", "Ext Structs", "Levels", "About", "Debug", "Exit", "-"};
    private final int[] statemap = {0, 0, 0, 0, 0, 0, 0, 0};
    static int selected = 1;
    int scW = getWidth();
    int scH = getHeight();
    private static int fontSizeCache = -1;
    Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    int fontH = font.getHeight();
    boolean paused = false;
    public static boolean debug = false;
    public static boolean music = false;
    public static boolean extStructs = false;
    Graphics g;
    private GenericMenu menu = new GenericMenu();
    MgStruct mgStruct = new MgStruct();

    boolean stopped = false;

    private static final int millis = 50;
    
    public static boolean wg = false;

    public mnCanvas() {
        super(false);
        setFullScreenMode(true);
        scW = getWidth();
        scH = getHeight();
        (new Thread(this, "menu canvas")).start();
    }
    
    protected void showNotify() {
        scW = getWidth();
        scH = getHeight();
        g = getGraphics();
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, Math.max(scW, scH), Math.max(scW, scH));
        
        if (font.getHeight() * menuOptions.length > scH) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        }
        if (font.getHeight() * menuOptions.length > scH) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }
        fontH = font.getHeight();
        menu.loadParams(scW, scH, menuOptions, 1, menuOptions.length - 2, selected, fontSizeCache);
        fontSizeCache = menu.getFontSize();
        menu.loadStatemap(statemap);
        if (extStructs) {
            menu.setStateFor(1, 2);
            menuOptions[2] = "Reload";
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
        menu.setIsSpecialOptnActivated(debug);
        menu.paint(g);
        menu.tick();
    }

    private void input() {
        int keyStates = getKeyStates();
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

    public void startLevel() {
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
    
    void selectPressed() {
        selected = menu.selected;
        if (selected == 1) {
            Main.print("menu:selected == 1 -> gen = true");
            wg = true;
            startLevel();
        }
        if (selected == 2) {
            menu.setStateFor(1, 2);
            if (mgStruct.load()) {
                extStructs = true;
                menuOptions[2] = "Reload";
                menu.setColorEnabledOption(0x0099ff00);
            } else {
                extStructs = false;
                menuOptions[2] = "Error, 0 loaded";
                menu.setColorEnabledOption(0x00880000);
            }
        }
        if (selected == 3) {
            stopped = true;
            wg = false;
            Main.set(new Levels());
        }
        if (selected == 4) {
            stopped = true;
            Main.set(new about());
        }
        if (selected == 5) {
            stopped = true;
            Main.set(new DebugMenu());
        }
        if (selected == 6) {
            stopped = true;
            Main.exit();
        }
    }
}
