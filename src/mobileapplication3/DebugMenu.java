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
        "show log",
        "simulation mode",
        "GAMING MODE",
        "what?",
        "music",
        "10 FPS screen",
        "back"
    };
    
    // array with states of all buttons (active/inactive/enabled)
    private final int[] statemap = new int[menuOpts.length]; 
    boolean stopped = false;
    boolean isPaused = false;
    private int scW = Main.sWidth, scH = Main.sHeight;
    private static int fontSizeCache = -1;
    public static boolean isDebugEnabled = true;
    public static boolean closerWorldgen = false;
    public static boolean coordinates = false;
    public static boolean discoMode = false;
    public static boolean speedo = false;
    public static boolean cheat = false;
    public static boolean music = false;
    public static boolean showFontSize = false;
    public static boolean mgstructOnly = false;
    public static boolean dontCountFlips = false;
    public static boolean showAngle = false;
    public static boolean showLinePoints = false;
    public static boolean simulationMode = false;
    public static boolean showFPS = false;
    public static boolean oneFrameTwoTicks = false;
    public static boolean whatTheGame = false;
    
    public DebugMenu() {
        super(false);
        setFullScreenMode(true);
        paint();
        (new Thread(this, "debug menu")).start();
    }
    
    private void init() {
        sizeChanged(getWidth(), getHeight());
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
        
        if (menu.isInited) {
            menu.paint(g);
            menu.tick();
        }
        
        if (!menu.isKnownButton) {
            g.setColor(127, 127, 127);
            g.drawString("Unknown keyCode=" + menu.lastKeyCode, scW, scH, Graphics.BOTTOM | Graphics.RIGHT);
        }
        flushGraphics();
    }
    void selectPressed() {
        int selected = menu.selected;
        switch (selected) {
            case 0:
                isDebugEnabled = !isDebugEnabled;
                showFPS = isDebugEnabled;
                showFontSize = isDebugEnabled;
                menu.setIsSpecialOptnActivated(isDebugEnabled);
                break;
            case 2:
                Main.isScreenLogEnabled = !Main.isScreenLogEnabled;
                if (Main.isScreenLogEnabled) { // TODO: <s>move to Main.java to make common toggleLog()</s> create Logger.java
                    Main.enableLog(scH); // in MenuCanvas also uses this code
                } else {
                    Main.disableLog();
                }   break;
            case 3:
                simulationMode = !simulationMode;
                break;
            case 4:
                discoMode = !discoMode;
                GraphicsWorld.bg = discoMode;
                break;
            case 5:
                whatTheGame = !whatTheGame;
                break;
            case 6:
                music = !music;
                if (music) {
                    Sound sound = new Sound();
                    sound.startBgMusic();
                }   break;
            case 7:
                oneFrameTwoTicks = !oneFrameTwoTicks;
                break;
            default:
                break;
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
            menu.setEnabledFor(Main.isScreenLogEnabled, 2);
            menu.setEnabledFor(simulationMode, 3);
            menu.setEnabledFor(discoMode, 4);
            menu.setEnabledFor(whatTheGame, 5);
            menu.setStateFor(/*music*/-1, 6); // set "music" as inactive button. it's buggy
            menu.setEnabledFor(oneFrameTwoTicks, 7);
        } else {
            for (int i = 2; i < menuOpts.length - 1; i++) {
                menu.setStateFor(-1, i);
            }
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
}
