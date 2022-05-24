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
    private String[] menuOpts = {"Enable debug options", "test1", "back"};
    boolean stopped = false;
    int scW = 0;
    int scH;
    
    public DebugMenu() {
        super(true);
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
                menu.loadParams(scW, scH, menuOpts);
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
        menu.paint(g);
        menu.tick();
    }
    void selectPressed() {
        int selected = menu.selected;
        if (selected == 0) {
            mnCanvas.debug = !mnCanvas.debug;
        }
        if (selected == 1) {
            
        }
        if (selected == menuOpts.length - 1) {
            stopped = true;
            Main.set(new mnCanvas());
        }
    }
    private void input() {
        int keyStates = getKeyStates();
        if (menu.keyPressed(keyStates)) {
            selectPressed();
        }
    }
    protected void pointerPressed(int x, int y) {
        menu.setIsPressedNow(true);
        menu.pointer(x, y);
    }

    protected void pointerDragged(int x, int y) {
        menu.pointer(x, y);
    }

    protected void pointerReleased(int x, int y) {
        menu.setIsPressedNow(false);
        if (menu.pointer(x, y)) {
            selectPressed();
        }
    }
}
