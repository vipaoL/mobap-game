/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author vipaol
 */
public class MenuCanvas extends GameCanvas implements Runnable, GenericMenu.Feedback {
    
    String[] menuOptions = {"-", "Play", "Ext Structs", "Levels", "About", "Debug", "Exit", "-"};
    
    private final int[] statemap = new int[menuOptions.length]; // array with states of all buttons (active/inactive/enabled)
    static int selected = 1; // currently selected option in menu
    private int scW = Main.sWidth, scH = Main.sHeight; // screeen width and height
    private static int fontSizeCache = -1; // reduce some operations on next menu showing
    
    // states
    boolean isPaused = false;
    boolean isStopped = false;
    boolean isInited = false;
    boolean waitingToStartGame = false;
    boolean isGameStarted = false;
    
    private GenericMenu menu = new GenericMenu(this); // some generic code for drawing menus
    private MgStruct mgStruct; // for loading external structures
    
    // TODO: move to GameplayCanvas
    public static boolean isWorldgenEnabled = false;
    
    static boolean areExtStructsLoaded = false;

    public MenuCanvas() {
        super(false);
        Logger.log("menu:constr");
        setFullScreenMode(true);
        //repaint();
        (new Thread(this, "menu canvas")).start();
    }
    
    public MenuCanvas(int counterX, int counterY) {
        super(false);
        Logger.log("menu:constructor");
        setFullScreenMode(true);
        //repaint();
        (new Thread(this, "menu canvas")).start();
    }
    
    private void init() {
        Logger.log("menu:init");
        menu.setIsSpecialOptnActivated(DebugMenu.isDebugEnabled);
        
        sizeChanged(getWidth(), getHeight());
        
        if (Logger.isOnScreenLogEnabled()) {
            Logger.enableOnScreenLog(scH);
        }
        
        // menu initialization
        menu.loadParams(scW, scH, menuOptions, 1, menuOptions.length - 2, selected, fontSizeCache);
        fontSizeCache = menu.getFontSize();
        menu.loadStatemap(statemap);
        if (areExtStructsLoaded) { // highlight and change label of "Ext Structs" btn if it already loaded
            menu.setStateFor(1, 2);
            menuOptions[2] = "Reload";
        }
        menu.setSpecialOption(menuOptions.length - 3); // to be able to highlight "Debug" option
        isInited = true;
    }
    
    // init and refreshing screen parameters
    protected void showNotify() {
        Logger.log("menu:showNotify");
        sizeChanged(getWidth(), getHeight());
        
        // enable screen refreshing
        isPaused = false;
        menu.handleShowNotify();
    }
    
    protected void sizeChanged(int w, int h) {
        Main.sWidth = scW = w;
        Main.sHeight = scH = h;
        menu.reloadCanvasParameters(scW, scH);
        if (Settings.bigScreen == Settings.UNDEF) {
            if (Math.max(scW, scH) >= GraphicsWorld.BIGSCREEN_SIDE) {
                Settings.bigScreen = Settings.TRUE;
            } else {
                Settings.bigScreen = Settings.FALSE;
            }
        }
        repaint();
    }

    protected void hideNotify() {
        Logger.log("menu:hideNotify");
        isPaused = true;
        menu.handleHideNotify();
    }

    public void destroyApp(boolean unconditional) {
        isStopped = true;
        Main.exit();
    }
    
    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
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
                if (scW != getWidth()) { // refreshing some parameters when screen rotated
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
        g.fillRect(0, 0, scW, scH);
        if (isInited) {
            menu.paint(g);
            menu.tick();
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
            Logger.enableOnScreenLog(scH);
            Logger.log("ex in startGame():");
            Logger.log(ex.toString());
            repaint();
        }
    }
    
    public void keyPressed(int keyCode) {         // Keyboard
        if (keyCode == GameCanvas.KEY_STAR | keyCode == -10) {
            if (!Logger.isOnScreenLogEnabled()) {
                Logger.enableOnScreenLog(scH);
            } else {
                Logger.disableOnScreenLog();
            }
        }
        if(menu.handleKeyPressed(keyCode)) {
            selectPressed();
        }
    }
    public void keyReleased(int keyCode) {
        menu.handleKeyReleased(keyCode);
    }
    
    protected void pointerPressed(int x, int y) { // Touch screen
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
    
    
    void selectPressed() { // Do something when pressed an option in the menu
        selected = menu.selected;
        if (selected == 1) { // Play
            Logger.log("menu:selected == 1 -> gen = true");
            isWorldgenEnabled = true;
            repaint();
            waitingToStartGame = true;
        }
        if (selected == 2) { // Ext Structs / Reload
            loadMG();
        }
        if (selected == 3) { // Levels
            isStopped = true;
            isWorldgenEnabled = false;
            Main.set(new Levels());
        }
        if (selected == 4) { // About
            isStopped = true;
            Main.set(new AboutScreen());
        }
        if (selected == 5) { // Debug
            isStopped = true;
            Main.set(new DebugMenu());
        }
        if (selected == 6) { // Exit
            isStopped = true;
            Main.exit();
        }
    }

    public boolean getIsPaused() {
        return isPaused;
    }
    
    void log(String s) {
        Logger.log(s);
        repaint();
    }

    private void loadMG() {
        (new Thread(new Runnable() {
            public void run() {
                menuOptions[2] = "Loading...";
                menu.setStateFor(1, 2);
                mgStruct = new MgStruct();
                if (mgStruct.loadFromFiles()) {
                    areExtStructsLoaded = true;
                    menuOptions[2] = (MgStruct.loadedStructsNumber - MgStruct.loadedFromResNumber) + " loaded";
                    menu.setColorEnabledOption(0x0099ff00);
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
                    menu.setColorEnabledOption(0x00880000);
                }
            }
        })).start();
    }
    
}
