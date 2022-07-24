/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.World;
import at.emini.physics2D.util.FXVector;
import java.io.IOException;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author vipaol
 */
public class AboutScreen extends GameCanvas implements Runnable {
    String url = "https://github.com/vipaoL/mobap-game";
    String urlPrew = "github: vipaoL/mobap-game";
    String[] strings = {"J2ME game on emini", "physics engine"};
    String[] menuOpts = {urlPrew, "Version: " + Main.thiss.getAppProperty("MIDlet-Version"), "Back"};
    int counter = 17;
    int scW = getWidth();
    int scH = getHeight();
    int qrOffsetH = 0;
    int menuBtnsOffsetH = 0;
    int qrSide = 0;
    int qrMargin = 0;
    private static int fontSizeCache = -1;
    Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    int fontH = font.getHeight();
    Font font2 = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    int font2H = font2.getHeight();
    Font font3 = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    int font3H = font3.getHeight();

    boolean paused = false;
    boolean stopped = false;
    
    Graphics g;
    Image qr;
    

    private static final int millis = 50;
    
    private GenericMenu menu = new GenericMenu();

    public AboutScreen() {
        super(true);
        setFullScreenMode(true);
        (new Thread(this, "about canvas")).start();
    }

    public void start() {
        g = getGraphics();
        showNotify();
        stopped = false;
    }

    protected void showNotify() {
        scW = getWidth();
        scH = getHeight();
        
        qrSide = scH - font3H - font2H * (strings.length + menuOpts.length);
        qrMargin = fontH/2;
        if (qrSide > scW - qrMargin*2) {
            qrSide = scW - qrMargin*2;
        }
        
        try {
            qr = scale(Image.createImage("qr.png"), qrSide, qrSide);
        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                qr = scale(Image.createImage("/qr.png"), qrSide, qrSide);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (font.getHeight() * (strings.length + menuOpts.length) + qrSide > scH) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        }
        if (font.getHeight() * (strings.length + menuOpts.length) > scH) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }
        fontH = font.getHeight();
        qrOffsetH = font2H + fontH * strings.length + qrMargin;
        menuBtnsOffsetH = qrOffsetH + qrSide + qrMargin;
        menu.loadParams(0, menuBtnsOffsetH, scW, scH - menuBtnsOffsetH, menuOpts, 0, 2, 2, fontSizeCache);
        fontSizeCache = menu.getFontSize();
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
        start();

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
        g.setColor(255, 255, 255);
        g.setFont(font2);
        g.drawString("About:", scW/2, 0, Graphics.HCENTER | Graphics.TOP);
        int offset = font2H;
        for (int i = 0; i < strings.length; i++) {
            g.setFont(font);
            g.drawString(strings[i], scW/2, offset, Graphics.HCENTER | Graphics.TOP);
            offset+=fontH;
        }
        
        try {
            g.drawImage(qr, scW / 2, qrOffsetH, Graphics.HCENTER | Graphics.TOP);
        } catch (NullPointerException ex) {
            g.drawLine(qrMargin, qrOffsetH, scW - qrMargin, qrOffsetH);
            g.drawLine(qrMargin, qrOffsetH + qrSide, scW - qrMargin, qrOffsetH + qrSide);
            g.drawLine(qrMargin, qrOffsetH, qrMargin, qrOffsetH + qrSide);
            g.drawLine(scW - qrMargin, qrOffsetH, scW - qrMargin, qrOffsetH + qrSide);
            g.setFont(font3);
            g.drawString("Your ad could be here.", scW / 2, qrOffsetH + (qrSide - font3H) / 2, Graphics.HCENTER|Graphics.TOP);
        }
        menu.paint(g);
        menu.tick();
    }

    private void input() {
        int keyStates = getKeyStates();
        if (menu.handleKeyStates(keyStates)) {
            selectPressed();
        }
    }

    public void keyReleased(int keyCode) {
        int gameAction = getGameAction(keyCode);
    }

    public void keyPressed(int keyCode) {
        if(menu.handleKeyPressed(keyCode)) {
            selectPressed();
        }
    }

    protected void pointerPressed(int x, int y) {
        menu.handlePointer(x, y - menuBtnsOffsetH);
    }

    protected void pointerDragged(int x, int y) {
        menu.handlePointer(x, y - menuBtnsOffsetH);
    }

    protected void pointerReleased(int x, int y) {
        if (menu.handlePointer(x, y - menuBtnsOffsetH)) {
            selectPressed();
        }
    }
    void openLink() {
        Main.log(url);
        try {
            if (Main.thiss.platformRequest(url)) {
                Main.exit();
            }
        } catch (ConnectionNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    void selectPressed() {
        int selected = menu.selected;
        if (selected == 0) {
            openLink();
        }
        if (selected == 1) {
            counter+=1;
            if (counter == 20) {
                MenuCanvas.isWorldgenEnabled = true;
                GameplayCanvas test = new GameplayCanvas();
                World test3 = new World();
                test3.setGravity(FXVector.newVector(10, 100));
                GraphicsWorld test2 = new GraphicsWorld(test3);
                GraphicsWorld.bg = true;
                test.setWorld(test2);
                test.uninterestingDebug = true;
                Main.set(test);
            }
        }
        if (selected == 2) {
            stopped = true;
            Main.set(new MenuCanvas());
        }
    }

    // for scaling qr code to screen size
    public Image scale(Image original, int newWidth, int newHeight) {

        int[] rawInput = new int[original.getHeight() * original.getWidth()];
        original.getRGB(rawInput, 0, original.getWidth(), 0, 0, original.getWidth(), original.getHeight());

        int[] rawOutput = new int[newWidth * newHeight];

        // YD compensates for the x loop by subtracting the width back out
        int YD = (original.getHeight() / newHeight) * original.getWidth() - original.getWidth();
        int YR = original.getHeight() % newHeight;
        int XD = original.getWidth() / newWidth;
        int XR = original.getWidth() % newWidth;
        int outOffset = 0;
        int inOffset = 0;

        for (int y = newHeight, YE = 0; y > 0; y--) {
            for (int x = newWidth, XE = 0; x > 0; x--) {
                rawOutput[outOffset++] = rawInput[inOffset];
                inOffset += XD;
                XE += XR;
                if (XE >= newWidth) {
                    XE -= newWidth;
                    inOffset++;
                }
            }
            inOffset += YD;
            YE += YR;
            if (YE >= newHeight) {
                YE -= newHeight;
                inOffset += original.getWidth();
            }
        }
        rawInput = null;
        return Image.createRGBImage(rawOutput, newWidth, newHeight, true);

    }
}
