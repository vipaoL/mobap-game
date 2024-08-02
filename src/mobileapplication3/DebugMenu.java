/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mobileapplication3;

import javax.microedition.lcdui.Graphics;

import utils.Logger;

/**
 *
 * @author vipaol
 */
public class DebugMenu extends GenericMenu implements Runnable {
    private static final String[] MENU_OPTS = {
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
    private final int[] statemap = new int[MENU_OPTS.length];
    private static int fontSizeCache = -1;
    public static boolean isDebugEnabled = false;
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
        setFullScreenMode(true);
        (new Thread(this, "debug menu")).start();
    }
    
    private void init() {
        sizeChanged(getWidth(), getHeight());
        statemap[1] = GenericMenu.STATE_INACTIVE; // set "-----" separator as inactive button
        loadParams(Main.sWidth, Main.sHeight, MENU_OPTS, statemap, fontSizeCache);
        fontSizeCache = getFontSize();
        setSpecialOption(0);
        refreshStates();
    }
    
    protected void sizeChanged(int w, int h) {
        fontSizeCache = -1;
        super.sizeChanged(w, h);
    }

    public void run() {
        long sleep;
        long start;
        
        paint();
        
        if (!isMenuInited()) {
            init();
        }

        while (!isStopped) {
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
        g.fillRect(0, 0, Math.max(w, h), Math.max(w, h));
        
        if (isMenuInited()) {
            super.paint(g);
            tick();
        }
        
        flushGraphics();
    }
    void selectPressed() {
        int selected = this.selected;
        switch (selected) {
            case 0:
                isDebugEnabled = !isDebugEnabled;
                showFPS = isDebugEnabled;
                showFontSize = isDebugEnabled;
                setIsSpecialOptnActivated(isDebugEnabled);
                Logger.logToStdout(isDebugEnabled);
                break;
            case 2:
                if (!Logger.isOnScreenLogEnabled()) {
                    Logger.enableOnScreenLog(h);
                } else {
                    Logger.disableOnScreenLog();
                }
                break;
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
        if (selected == MENU_OPTS.length - 1) {
            isStopped = true;
            Main.set(new SettingsScreen());
        } else {
            refreshStates();
        }
    }
    void refreshStates() {
        setIsSpecialOptnActivated(DebugMenu.isDebugEnabled);
        if (DebugMenu.isDebugEnabled) {
            setEnabledFor(Logger.isOnScreenLogEnabled(), 2);
            setEnabledFor(simulationMode, 3);
            setEnabledFor(discoMode, 4);
            setEnabledFor(whatTheGame, 5);
            setStateFor(/*music*/GenericMenu.STATE_INACTIVE, 6); // set "music" as inactive button. it's buggy
            setEnabledFor(oneFrameTwoTicks, 7);
        } else {
            for (int i = 2; i < MENU_OPTS.length - 1; i++) {
                setStateFor(-1, i);
            }
        }
    }
}
