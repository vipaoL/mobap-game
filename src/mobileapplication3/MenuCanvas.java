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
    int scW, scH; // screeen width and height
    private static int fontSizeCache = -1; // reduce some operations on next menu showing
    
    // states
    boolean isPaused = false;
    boolean isStopped = false;
    boolean isInited = false;
    boolean waitingToStartGame = false;
    boolean isGameStarted = false;
    
    private Graphics g;
    private GenericMenu menu; // some generic code for drawing menus
    private MgStruct mgStruct; // for loading external structures
    
    public static boolean isWorldgenEnabled = false;
    static boolean areExtStructsLoaded = false;

    public MenuCanvas() /*throws ClassNotFoundException*/ {
        super(false);
        setFullScreenMode(true);
        scW = getWidth();
        scH = getHeight();
        if (Main.isScreenLogEnabled) {
            Main.enableLog(scH);
        } else {
            Main.disableLog();
        }
        Main.log("menu:constructor");
        menu = new GenericMenu(this);
        (new Thread(this, "menu canvas")).start();
    }
    
    // init and refreshing screen parameters
    protected void showNotify() {
        Main.log("menu:showNotify");
        // screen initialization
        scW = getWidth();
        scH = getHeight();
        g = getGraphics();
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, Math.max(scW, scH), Math.max(scW, scH));
        
        if (!isInited) {
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
        } else {
            menu.reloadCanvasParameters(scW, scH);
        }
        
        // enable screen refreshing
        isPaused = false;
        menu.handleShowNotify();
    }

    protected void hideNotify() {
        Main.log("menu:hideNotify");
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
        
        //showNotify(); // init

        while (!isStopped) { // *** main cycle of menu drawing ***
            if (waitingToStartGame) {
                startGame();
                return;
            }
            if (!isPaused) {
                start = System.currentTimeMillis();
                input(); // listen keyboard
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
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, scW, scH);
        menu.setIsSpecialOptnActivated(DebugMenu.isDebugEnabled);
        menu.paint(g);
        menu.tick();
    }

    public void startGame() {
        if (isGameStarted) {
            return;
        }
        isGameStarted = true;
        waitingToStartGame = false;
        Main.log("menu:startGame()");
        repaint();
        try {
            isStopped = true;
            Main.log("menu:new CGanvas");
            repaint();
            GameplayCanvas gameCanvas = new GameplayCanvas();
            Main.log("menu:setting gcanvas displayable");
            repaint();
            Main.set(gameCanvas);
            gameCanvas.setLoadingProgress(5);
            gameCanvas.setDefaultWorld();
        } catch (Exception ex) {
            ex.printStackTrace();
            Main.log(ex.toString());
            repaint();
        }
    }
    
    private void input() {                        // Keyboard
        int keyStates = getKeyStates();
        if (menu.handleKeyStates(keyStates)) {
            selectPressed();
        }
    }
    public void keyPressed(int keyCode) {
        if (keyCode == GameCanvas.KEY_STAR | keyCode == -10) {
            Main.isScreenLogEnabled = !Main.isScreenLogEnabled;
            if (Main.isScreenLogEnabled) {
                Main.enableLog(scH);
            } else {
                Main.disableLog();
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
            Main.log("menu:selected == 1 -> gen = true");
            isWorldgenEnabled = true;
            repaint();
            waitingToStartGame = true;
        }
        if (selected == 2) { // Ext Structs / Reload
            menu.setStateFor(1, 2);
            mgStruct = new MgStruct();
            if (mgStruct.loadFromFiles()) {
                areExtStructsLoaded = true;
                menuOptions[2] = (MgStruct.structsInBufferNumber - MgStruct.loadedStructsFromResNumber) + " loaded";
                menu.setColorEnabledOption(0x0099ff00);
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

    public void recheckInput() {
        input();
    }
}
