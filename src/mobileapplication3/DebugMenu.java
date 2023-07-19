/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mobileapplication3;

import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author vipaol
 */
public class DebugMenu extends GameCanvas implements Runnable, GenericMenu.Feedback {
    private GenericMenu menu = new GenericMenu(this);
    private String[] menuOpts = {
        "Enable debug options",
        "-----",
        "show coordinates",
        "GAMING MODE",
        "show speedometer",
        "show log",
        "music",
        "show font size",
        ".mgstruct only",
        "10 FPS screen",
        "simulation mode",
        "back"
    };
    
    // array with states of all buttons (active/inactive/enabled)
    private final int[] statemap = new int[menuOpts.length]; 
    boolean stopped = false;
    boolean isPaused = false;
    private int scW = 0, scH;
    private static int fontSizeCache = -1;
    public static boolean isDebugEnabled = false;
    public static boolean closerWorldgen = false;
    public static boolean coordinates = false;
    public static boolean discoMode = false;
    public static boolean speedo = false;
    public static boolean cheat = false;
    public static boolean music = false;
    public static boolean fontSize = false;
    public static boolean mgstructOnly = false;
    public static boolean dontCountFlips = false;
    public static boolean showAngle = false;
    public static boolean showLinePoints = false;
    public static boolean simulationMode = false;
    public static boolean showFPS = false;
    public static boolean oneFrameTwoTicks = false;
    
    public DebugMenu() {
        super(false);
        setFullScreenMode(true);
        (new Thread(this, "debug menu")).start();
    }
    
    private void init() {
        if (scW == 0 || scH == 0) {
            sizeChanged(getWidth(), getHeight());
        }
        statemap[1] = -1; // set "-----" separator as inactive button
        menu.loadParams(scW, scH, menuOpts, statemap, fontSizeCache);
        fontSizeCache = menu.getFontSize();
        menu.setSpecialOption(0);
        refreshStates();
    }
    
    public boolean getIsPaused() {
        return isPaused;
    }
    
    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }
    
    protected void hideNotify(){
        isPaused = true;
        menu.handleHideNotify();
    }
    
    protected void sizeChanged(int w, int h) {
        Main.sWidth = scW = w;
        Main.sHeight = scH = h;
        fontSizeCache = -1;
        menu.reloadCanvasParameters(scW, scH);
        paint();
    }
    
    protected void showNotify(){
        isPaused = false;
        menu.handleShowNotify();
    }

    public void run() {
        long sleep;
        long start;
        
        paint();
        
        if (!menu.isInited) {
            init();
        }

        while (!stopped) {
            if (!isPaused) {
                start = System.currentTimeMillis();
                input();
                paint();

                sleep = Main.TICK_DURATION - (System.currentTimeMillis() - start);
                sleep = Math.max(sleep, 0);
            } else {
                sleep = 200;
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
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, Math.max(scW, scH), Math.max(scW, scH));
        menu.paint(g);
        menu.tick();
        if (!menu.isKnownButton) {
            g.setColor(127, 127, 127);
            g.drawString("Unknown keyCode=" + menu.lastKeyCode, scW, scH, Graphics.BOTTOM | Graphics.RIGHT);
        }
        flushGraphics();
    }
    void selectPressed() {
        int selected = menu.selected;
        if (selected == 0) {
            isDebugEnabled = !isDebugEnabled;
            showFPS = isDebugEnabled;
            menu.setIsSpecialOptnActivated(isDebugEnabled);
        }
        if (selected == 2) {
            coordinates = !coordinates;
        }
        if (selected == 3) {
            discoMode = !discoMode;
            GraphicsWorld.bg = discoMode;
        }
        if (selected == 4) {
            speedo = !speedo;
        }
        if (selected == 5) {
            Main.isScreenLogEnabled = !Main.isScreenLogEnabled;
            if (Main.isScreenLogEnabled) { // TODO: move to Main.java to make common toggleLog()
                Main.enableLog(scH); // in MenuCanvas also used this code
            } else {
                Main.disableLog();
            }
        }
        if (selected == 6) {
            music = !music;
            if (music) {
                Sound sound = new Sound();
                sound.startBgMusic();
            }
        }
        if (selected == 7) {
            fontSize = !fontSize;
        }
        if (selected == 8) {
            if (MgStruct.loadedStructsNumber > 0) {
                mgstructOnly = !mgstructOnly;
            } else {
                mgstructOnly = false;
            }
        }
        if (selected == 9) {
            oneFrameTwoTicks = !oneFrameTwoTicks;
        }
        if (selected == 10) {
            simulationMode = !simulationMode;
        }
        if (selected == menuOpts.length - 1) {
            stopped = true;
            Main.set(new MenuCanvas());
        } else {
            refreshStates();
        }
    }
    void refreshStates() {
        menu.setIsSpecialOptnActivated(DebugMenu.isDebugEnabled);
        if (DebugMenu.isDebugEnabled) {
            menu.setEnabledFor(coordinates, 2);
            menu.setEnabledFor(discoMode, 3);
            menu.setEnabledFor(speedo, 4);
            menu.setEnabledFor(Main.isScreenLogEnabled, 5);
            menu.setEnabledFor(music, 6);
            menu.setStateFor(-1, 6); // set "music" as inactive button. it's buggy
            menu.setEnabledFor(fontSize, 7);
            //menu.setEnabledFor(mgstructOnly, 8);
            menu.setStateFor(-1, 8); // set ".mgstruct only" as inactive button. it's buggy
            menu.setEnabledFor(oneFrameTwoTicks, 9);
            menu.setEnabledFor(simulationMode, 10);
        } else {
            for (int i = 2; i < menuOpts.length - 1; i++) {
                menu.setStateFor(-1, i);
            }
        }
    }
    private void input() {
        int keyStates = getKeyStates();
        if (menu.handleKeyStates(keyStates)) {
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
    public void keyPressed(int keyCode) {
        if(menu.handleKeyPressed(keyCode)) {
            selectPressed();
        }
    }
    public void keyReleased(int keyCode) {
        menu.handleKeyReleased(keyCode);
    }

    public void recheckInput() {
        input();
    }
}
