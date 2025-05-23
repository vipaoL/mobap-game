/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.game;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.Keys;
import utils.MgStruct;

/**
 *
 * @author vipaol
 */
public class MenuCanvas extends GenericMenu implements Runnable {
	
    private final String[] menuOptions = {
    		"",
    		"Play",
    		"Load Structures",
    		"Levels",
    		"Editor",
    		"Records",
    		"Settings",
    		"Exit",
    		""
	};

    private static int defaultSelected = 1; // currently selected option in menu
    
    // states
    private boolean isInited = false;
    private boolean isGameStarted = false;
    private int c = 0;
    
    private MgStruct mgStruct;
    private GameplayCanvas bg = null;
    
    private static boolean areExtStructsLoaded = false;

	private Thread menuThread = null;
    
    public MenuCanvas(GameplayCanvas bg) {
    	this();
    	this.bg = bg;
    	if (bg != null) {
    		bgColor = COLOR_TRANSPARENT;
    	}
    }

    public MenuCanvas() {
        Logger.log("menu:constr");
        // menu initialization
        loadParams(menuOptions, defaultSelected);
        // placeholders
        setFirstReachable(1);
        setLastReachable(menuOptions.length - 2);
    }
    
    public void init() {
        Logger.log("menu:init");
        
        if (areExtStructsLoaded) { // highlight and change label of "Ext Structs" btn if it already loaded
            setStateFor(1, 2);
            menuOptions[2] = "Reload";
        }
        try {
            Class.forName("mobileapplication3.editor.Editor");
        } catch (ClassNotFoundException ex) {
        	setStateFor(STATE_INACTIVE, 4);
        }
        isInited = true;
    }

    public void postInit() {
        if (menuThread == null) {
            menuThread = new Thread(this, "menu canvas");
            menuThread.start();
        }
    }

    public void run() {
        long sleep = 0; // for FPS/TPS control
        long start = 0; //

        while (!isStopped) { // *** main cycle of menu drawing ***
            if (!isPaused && hasParent()) {
                start = System.currentTimeMillis();
                repaint(); // refresh picture on screen
                sleep = GameplayCanvas.TICK_DURATION - (System.currentTimeMillis() - start);
                sleep = Math.max(sleep, 0);
            } else {
                sleep = 100;
            }

            if (c == 3) {
                if (bg != null) {
                    bg.startAgain();
                    bg.disablePointCounter();
                    RootContainer.setRootUIComponent(bg);
                    bg = null;
                    stop();
                }
            }

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        menuThread = null;
    }

    protected void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        try {
            if (bg != null) {
            	if (!bg.drawAsBG(g)) {
            		bg = null;
            	}
            }
            if (isInited) {
                super.onPaint(g, x0, y0, w, h, forceInactive);
                tick();
            }
        } catch (Exception ex) { }
    }
    
    protected void onSetBounds(int x0, int y0, int w, int h) {
    	super.onSetBounds(x0, y0, w, h);
    	if (bg != null) {
    		bg.setSize(w, h);
    	}
    }

    private synchronized void startGame() {
        if (isGameStarted) {
            return;
        }
        isGameStarted = true;
        Logger.log("menu:startGame()");
        repaint();
        try {
            stopBG();
            log("menu:new gCanvas");
            GameplayCanvas gameCanvas = new GameplayCanvas();
            log("menu:setting gCanvas displayable");
            RootContainer.setRootUIComponent(gameCanvas);
            stop();
        } catch (Exception ex) {
            ex.printStackTrace();
            Platform.showError(ex);
            Logger.enableOnScreenLog(h);
            Logger.log("ex in startGame():");
            Logger.log(ex);
            repaint();
            isGameStarted = false;
        }
    }
    
    public boolean handleKeyPressed(int keyCode, int count) {         // Keyboard
        if (keyCode == Keys.KEY_STAR | keyCode == -10) {
            if (!Logger.isOnScreenLogEnabled()) {
                Logger.enableOnScreenLog(h);
            } else {
                Logger.disableOnScreenLog();
            }
        } else if (keyCode == Keys.KEY_NUM9) {
            c++;
        }
        return super.handleKeyPressed(keyCode, count);
    }

    public boolean handlePointerClicked(int x, int y) {
        if (x < w / 32 && y < h / 32) {
            c++;
            return true;
        } else {
            return super.handlePointerClicked(x, y);
        }
    }

    void selectPressed() { // Do something when pressed an option in the menu
        defaultSelected = selected;
        if (selected == 1) { // Play
            startGame();
        }
        if (selected == 2) { // Ext Structs / Reload
            loadMG();
        }
        if (selected == 3) { // Levels
            stop();
            RootContainer.setRootUIComponent(new Levels());
        }
        if (selected == 4) { // Editor
        	stop();
        	try {
				Class.forName("mobileapplication3.editor.Editor").newInstance();
			} catch (Exception ex) {
				Logger.log("Can't open editor: " + ex);
			}
        	Logger.log("opened editor");
        }
        if (selected == 5) { // Records
            stop();
            RootContainer.setRootUIComponent(new RecordsScreen());
        }
        if (selected == 6) { // Settings
        	stop();
            RootContainer.setRootUIComponent(new SettingsScreen());
        }
        if (selected == 7) { // Exit
        	stop();
            Platform.exit();
        }
    }
    
    private void stop() {
    	isStopped = true;
        stopBG();
    }

    private void stopBG() {
    	if (bg != null) {
        	bg.stop(false, true);
        }
    }
    
    private void log(String s) {
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
