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
public class AboutScreen extends GameCanvas implements Runnable, GenericMenu.Feedback {
    String url = "https://github.com/vipaoL/mobap-game";
    String urlPrew = "github: vipaoL/mobap-game";
    String[] strings = {"J2ME game on emini", "physics engine"};
    String[] menuOpts = {"", //there is qr code
        urlPrew,
        "Version: " + Main.thiss.getAppProperty("MIDlet-Version"),
        "Back"};
    int counter = 17;
    int scW = getWidth();
    int scH = getHeight();
    int offset = 0;
    int qrOffsetH = 0;
    int extraVerticalMargin = 0;
    int menuBtnsOffsetH = 0;
    int qrSide = 0;
    int margin = 0;
    private static int fontSizeCache = -1;
    Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    int fontH = font.getHeight();
    Font font2 = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    int font2H = font2.getHeight();
    Font font3 = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    int font3H = font3.getHeight();

    boolean paused = false;
    boolean stopped = false;
    boolean bigQRIsDrawn = false;
    
    Graphics g = getGraphics();
    Image qr, qrBig;
    
    private GenericMenu menu = new GenericMenu(this);

    public AboutScreen() {
        super(false);
        setFullScreenMode(true);
        if (Main.PRE_VERSION >= 0) {
            for (int i = 0; i < menuOpts.length; i++) {
                if (menuOpts[i].startsWith("Version: ")) {
                    menuOpts[i] += "-pre" + Main.PRE_VERSION;
                    break;
                }
            }
        }
        String commitHash = Main.thiss.getAppProperty("Commit");
        if (commitHash != null) {
            for (int i = 0; i < menuOpts.length; i++) {
                if (menuOpts[i].startsWith("Version: ")) {
                    menuOpts[i] += "-" + commitHash;
                    break;
                }
            }
        }
        (new Thread(this, "about canvas")).start();
    }

    protected void showNotify() {
        scW = getWidth();
        scH = getHeight();
        
        qrSide = scH/* - font2H*/ - fontH * (strings.length + menuOpts.length + 1);
        margin = fontH/2;
        if (qrSide > scW - margin*2) {
            qrSide = scW - margin*2;
        }
        
        int headerAndQrH = fontH * (strings.length) + 3*margin + qrSide;
        int buttonsFontH = menu.findOptimalFont(scW, scH - headerAndQrH - margin, menuOpts);
        extraVerticalMargin = (scH - (headerAndQrH + (3*menuOpts.length/2)*buttonsFontH + margin)) / 4;
        if (extraVerticalMargin < 0) {
            extraVerticalMargin = 0;
        }
        
        try {
            qr = scale(Image.createImage("/qr.png"), qrSide, qrSide);
        } catch (IOException ex) {
            try {
                qr = scale(Image.createImage("resource://qr.png"), qrSide, qrSide);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try {
            qrBig = scale(Image.createImage("/qr.png"), Math.min(scW, scH), Math.min(scW, scH));
        } catch (IOException ex) {
            try {
                qrBig = scale(Image.createImage("resource://qr.png"), Math.min(scW, scH), Math.min(scW, scH));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, Math.max(getWidth(), getHeight()), Math.max(getWidth(), getHeight()));
        drawHeaderAndQR(g);
        menuBtnsOffsetH = offset;
        if (!menu.isInited) {
            menu.loadParams(0,
                    menuBtnsOffsetH,
                    scW,
                    scH - menuBtnsOffsetH - margin - extraVerticalMargin,
                    menuOpts, 0,
                    menuOpts.length - 1,
                    menuOpts.length - 1,
                    fontSizeCache);
            
            fontSizeCache = menu.getFontSize();
            menu.setFirstDrawable(1);
        } else {
            menu.reloadCanvasParameters(0, menuBtnsOffsetH, scW, scH - menuBtnsOffsetH);
        }
        paused = false;
        repaint();
        menu.handleShowNotify();
    }

    protected void hideNotify() {
        paused = true;
        menu.handleHideNotify();
    }

    public void destroyApp(boolean unconditional) {
        stopped = true;
        Main.exit();
    }
    
    public void setIsPaused(boolean isPaused) {
        this.paused = isPaused;
    }

    public void run() {
        long sleep;
        long start;

        while (!stopped) {
            if (!paused) {
                start = System.currentTimeMillis();
                input();
                // catch screen rotation
                if (scW != getWidth()) {
                    fontSizeCache = -1;
                    showNotify();
                }
                
                //   if qr isn't selected, repaint on each frame
                //
                //   if big qr is open, draw it oncely,
                // and then we don't need to refresh screen
                if (menu.selected != 0 | !bigQRIsDrawn) {
                    repaint();
                }

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
    
    public void paint(Graphics g) {
        if (menu.selected != 0) {
            g.setColor(0, 0, 0);
            g.fillRect(0, 0, scW, scH);
            drawHeaderAndQR(g);
            menu.paint(g);
            menu.tick();
            bigQRIsDrawn = false;
        } else {
            drawBigQR(g);
        }
    }
    
    void drawHeaderAndQR(Graphics g) {
        g.setColor(255, 255, 255);
        
        offset = margin + extraVerticalMargin;
        //g.setFont(font2);
        //g.drawString("About:", scW/2, 0, Graphics.HCENTER | Graphics.TOP);
        //offset += font2H;
        for (int i = 0; i < strings.length; i++) {
            g.setFont(font);
            g.drawString(strings[i], scW/2, offset, Graphics.HCENTER | Graphics.TOP);
            offset += fontH;
        }
        offset += margin + extraVerticalMargin;
        try {
            g.drawImage(qr, scW / 2, offset, Graphics.HCENTER | Graphics.TOP);
        } catch (NullPointerException ex) {
            g.drawLine(margin, offset, scW - margin, offset);
            g.drawLine(margin, offset + qrSide, scW - margin, offset + qrSide);
            g.drawLine(margin, offset, margin, offset + qrSide);
            g.drawLine(scW - margin, offset, scW - margin, offset + qrSide);
            g.setFont(font3);
            int x = scW / 2;
            int y = offset + qrSide / 2;
            g.drawString("Your ad", x, y, Graphics.HCENTER|Graphics.BOTTOM);
            g.drawString("could be here.", x, y, Graphics.HCENTER|Graphics.TOP);
        }
        offset += qrSide;
        offset += margin + extraVerticalMargin;
        //g.drawLine(0, offset, scW, offset);
    }
    
    void drawBigQR(Graphics g) {
        try {
            g.drawImage(qrBig, scW / 2, scH / 2, Graphics.HCENTER | Graphics.VCENTER);
        } catch (NullPointerException ex) {
            g.drawLine(margin, offset, scW - margin, offset);
            g.drawLine(margin, offset + qrSide, scW - margin, offset + qrSide);
            g.drawLine(margin, offset, margin, offset + qrSide);
            g.drawLine(scW - margin, offset, scW - margin, offset + qrSide);
            g.setFont(font3);
            int x = scW / 2;
            int y = scH / 2;
            g.drawString("Your ad", x, y, Graphics.HCENTER|Graphics.BOTTOM);
            g.drawString("could be here.", x, y, Graphics.HCENTER|Graphics.TOP);
        }
        bigQRIsDrawn = true;
    }

    private void input() {
        int keyStates = getKeyStates();
        if (menu.handleKeyStates(keyStates)) {
            selectPressed();
        }
    }

    public void keyReleased(int keyCode) {
        menu.handleKeyReleased(keyCode);
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
        if (selected == menuOpts.length - 3) {
            openLink();
        }
        if (selected == menuOpts.length - 2) {
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
        if (selected == menuOpts.length - 1) {
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
    public boolean getIsPaused() {
        return paused;
    }

    public void recheckInput() {
        input();
    }
}
