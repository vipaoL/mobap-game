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
public class DebugMenu extends GameCanvas implements Runnable {
    
    private static final int millis = 50;
    private GenericMenu menu = new GenericMenu();
    private String[] menuOpts = {
        "Enable debug options",
        "-----",
        "closer worldgen trigger",
        "show X-coordinate",
        "show speedometer",
        "cheat(*)",
        "music",
        "show font size",
        ".mgstruct only",
        "show points of lines",
        "show car rotation",
        "back"};
    private final int[] statemap = new int[menuOpts.length];
    boolean stopped = false;
    private int scW = 0, scH;
    private static int fontSizeCache = -1;
    public static boolean isDebugEnabled = false;
    public static boolean closerWorldgen = false;
    public static boolean xCoord = false;
    public static boolean speedo = false;
    public static boolean cheat = false;
    public static boolean music = false;
    public static boolean fontSize = false;
    public static boolean mgstructOnly = false;
    public static boolean dontCountFlips = false;
    public static boolean showAngle = false;
    public static boolean showLinePoints = false;
    
    public DebugMenu() {
        super(true);
        statemap[1] = -1; // set "-----" separator as inactive button
        setFullScreenMode(true);
        (new Thread(this, "about canvas")).start();
    }

    public void run() {
        long sleep = 0;
        long start = 0;

        while (!stopped) {
            start = System.currentTimeMillis();
            input();
            if (scW != getWidth()) {
                scW = getWidth();
                scH = getHeight();
                System.out.println(statemap != null);
                menu.loadParams(scW, scH, menuOpts, statemap, fontSizeCache);
                fontSizeCache = menu.getFontSize();
                menu.setSpecialOption(0);
                refreshStates();
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
        try {
            menu.paint(g);
            menu.tick();
        } catch (NullPointerException ex) {
            
        }
    }
    void selectPressed() {
        int selected = menu.selected;
        if (selected == 0) {
            isDebugEnabled = !isDebugEnabled;
            menu.setIsSpecialOptnActivated(isDebugEnabled);
        }
        if (selected == 2) {
            closerWorldgen = !closerWorldgen;
        }
        if (selected == 3) {
            xCoord = !xCoord;
        }
        if (selected == 4) {
            speedo = !speedo;
        }
        if (selected == 5) {
            cheat = !cheat;
        }
        if (selected == 6) {
            music = !music;
        }
        if (selected == 7) {
            fontSize = !fontSize;
        }
        if (selected == 8) {
            mgstructOnly = !mgstructOnly;
        }
        if (selected == 9) {
            showLinePoints = !showLinePoints;
        }
        if (selected == 10) {
            showAngle = !showAngle;
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
            menu.setEnabledFor(closerWorldgen, 2);
            menu.setEnabledFor(xCoord, 3);
            menu.setEnabledFor(speedo, 4);
            menu.setEnabledFor(cheat, 5);
            menu.setEnabledFor(music, 6);
            menu.setEnabledFor(fontSize, 7);
            menu.setEnabledFor(mgstructOnly, 8);
            menu.setEnabledFor(showLinePoints, 9);
            menu.setEnabledFor(showAngle, 10);
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
}
