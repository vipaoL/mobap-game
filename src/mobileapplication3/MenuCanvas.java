/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

import utils.Logger;
import utils.MgStruct;
import utils.MobappGameSettings;
import utils.Settings;

/**
 *
 * @author vipaol
 */
public class MenuCanvas extends GenericMenu implements Runnable {
	
    private String[] menuOptions = {
    		"",
    		"Play",
    		"Ext Structs",
    		"Levels",
    		"About",
    		"Settings",
    		"Exit",
    		""};
    
    private final int[] statemap = new int[menuOptions.length]; // array with states of all buttons (active/inactive/enabled)
    private static int defaultSelected = 1; // currently selected option in menu
    private static int fontSizeCache = -1; // reduce some operations on next menu showing
    
    // states
    private boolean isInited = false;
    private boolean waitingToStartGame = false;
    private boolean isGameStarted = false;
    
    private MgStruct mgStruct; // for loading external structures
    
    private static boolean areExtStructsLoaded = false;

    public MenuCanvas() {
        Logger.log("menu:constr");
        setFullScreenMode(true);
        //repaint();
        (new Thread(this, "menu canvas")).start();
    }
    
    public MenuCanvas(int counterX, int counterY) {
        Logger.log("menu:constructor");
        setFullScreenMode(true);
        //repaint();
        (new Thread(this, "menu canvas")).start();
    }
    
    private void init() {
        Logger.log("menu:init");
        
        sizeChanged(getWidth(), getHeight());
        
        if (Logger.isOnScreenLogEnabled()) {
            Logger.enableOnScreenLog(Main.sHeight);
        }
        
        // menu initialization
        loadParams(Main.sWidth, Main.sHeight, menuOptions, 1, menuOptions.length - 2, defaultSelected, fontSizeCache);
        fontSizeCache = getFontSize();
        loadStatemap(statemap);
        if (areExtStructsLoaded) { // highlight and change label of "Ext Structs" btn if it already loaded
            setStateFor(1, 2);
            menuOptions[2] = "Reload";
        }
        isInited = true;
    }
    
    protected void sizeChanged(int w, int h) {
        super.sizeChanged(w, h);
    }
    
    public void run() {
        long sleep = 0; // for FPS/TPS control
        long start = 0; //
        
        if (!isInited) {
            init();
        }

        while (!isStopped) { // *** main cycle of menu drawing ***
            if (waitingToStartGame) {
                startGame();
                return;
            }
            if (!isPaused) {
                start = System.currentTimeMillis();
                if (w != getWidth()) { // refreshing some parameters when screen rotated
                    fontSizeCache = -1;
                    showNotify();
                }
                repaint(); // refresh picture on screen
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
        g.setColor(0);
        g.fillRect(0, 0, w, h);
        if (isInited) {
            super.paint(g);
            tick();
        }
    }

    public void startGame() {
        if (isGameStarted) {
            return;
        }
        isGameStarted = true;
        waitingToStartGame = false;
        Logger.log("menu:startGame()");
        repaint();
        try {
            isStopped = true;
            log("menu:new gCanvas");
            GameplayCanvas gameCanvas = new GameplayCanvas();
            log("menu:setting gCanvas displayable");
            Main.set(gameCanvas);
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.enableOnScreenLog(h);
            Logger.log("ex in startGame():");
            Logger.log(ex.toString());
            repaint();
        }
    }
    
    public void keyPressed(int keyCode) {         // Keyboard
        if (keyCode == GameCanvas.KEY_STAR | keyCode == -10) {
            if (!Logger.isOnScreenLogEnabled()) {
                Logger.enableOnScreenLog(h);
            } else {
                Logger.disableOnScreenLog();
            }
        }
        super.keyPressed(keyCode);
    }
    
    
    void selectPressed() { // Do something when pressed an option in the menu
        defaultSelected = selected;
        if (selected == 1) { // Play
            Logger.log("menu:selected == 1 -> gen = true");
            WorldGen.isEnabled = true;
            repaint();
            waitingToStartGame = true;
        }
        if (selected == 2) { // Ext Structs / Reload
            loadMG();
        }
        if (selected == 3) { // Levels
            isStopped = true;
            WorldGen.isEnabled = false;
            Main.set(new Levels());
        }
        if (selected == 4) { // About
            isStopped = true;
            Main.set(new AboutScreen());
        }
        if (selected == 5) { // Settings
            isStopped = true;
            Main.set(new SettingsScreen());
        }
        if (selected == 6) { // Exit
            isStopped = true;
            Main.exit();
        }
    }
    
    void log(String s) {
        Logger.log(s);
        repaint();
    }

    private void loadMG() {
        (new Thread(new Runnable() {
            public void run() {
                menuOptions[2] = "Loading...";
                setStateFor(1, 2);
                mgStruct = new MgStruct();
                if (mgStruct.loadFromFiles()) {
                    areExtStructsLoaded = true;
                    menuOptions[2] = (MgStruct.loadedStructsNumber - MgStruct.loadedFromResNumber) + " loaded";
                    setColorEnabledOption(0x0099ff00);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    menuOptions[2] = "Reload";
                } else {
                    areExtStructsLoaded = false;
                    if (!mgStruct.loadCancelled) {
                        menuOptions[2] = "Nothing loaded";
                    } else {
                        menuOptions[2] = "Cancelled";
                    }
                    setColorEnabledOption(0x00880000);
                }
            }
        })).start();
    }
    
}
