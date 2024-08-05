/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.World;
import at.emini.physics2D.util.FXVector;
import utils.Logger;

import java.io.IOException;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 *
 * @author vipaol
 */
public class AboutScreen extends GenericMenu implements Runnable {
    private static final String URL = "https://github.com/vipaoL/mobap-game";
    private static final String URL_PREVIEW = "github: vipaoL/mobap-game";
    private static final String[] STRINGS = {"J2ME game on emini", "physics engine"};
    private static final String[] MENU_OPTS = {""/*there is qr code*/,
        URL_PREVIEW,
        "Version: " + Main.thiss.getAppProperty("MIDlet-Version"),
        "Back"};
    private int counter = 17;
    private int scW = Main.sWidth, scH = Main.sHeight;
    private int extraVerticalMargin = 0;
    private int qrSide = 0;
    private int margin = 0;
    private static int fontSizeCache = -1;
    private Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL),
            font3 = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private int fontH = font.getHeight();
    private boolean bigQRIsDrawn = false;
    
    private Image qr, qrBig;

    public AboutScreen() {
        setFullScreenMode(true);
        getGraphics().fillRect(0, 0, scW, scH);
        flushGraphics();
        (new Thread(this, "about canvas")).start();
    }
    
    private void init() {
        int menuBtnsOffsetH = drawHeaderAndQR(null);
        loadParams(0,
                menuBtnsOffsetH,
                scW,
                scH - menuBtnsOffsetH - margin - extraVerticalMargin,
                MENU_OPTS, 0,
                MENU_OPTS.length - 1,
                MENU_OPTS.length - 1,
                fontSizeCache);

        fontSizeCache = getFontSize();
        setFirstDrawable(1);
    }
    
    protected void sizeChanged(int w, int h) {
        if (scW == w && scH == h && qr != null) {
            return;
        }
        Main.sWidth = scW = w;
        Main.sHeight = scH = h;
        qrSide = scH/* - font2H*/ - fontH * (STRINGS.length + MENU_OPTS.length + 1);
        margin = fontH/2;
        if (qrSide > scW - margin*2) {
            qrSide = scW - margin*2;
        }
        
        int headerAndQrH = fontH * (STRINGS.length) + 3*margin + qrSide;
        int buttonsFontH = findOptimalFont(scW, scH - headerAndQrH - margin, MENU_OPTS);
        extraVerticalMargin = (scH - (headerAndQrH + (3*MENU_OPTS.length/2)*buttonsFontH + margin)) / 4;
        if (extraVerticalMargin < 0) {
            extraVerticalMargin = 0;
        }
        
        try {
            qr = scale(Image.createImage("/qr.png"), qrSide, qrSide);
        } catch (IOException ex) {
            try {
                qr = scale(Image.createImage("resource://qr.png"), qrSide, qrSide);
            } catch (IOException e) {
                ex.printStackTrace();
                e.printStackTrace();
            }
        }
        
        try {
            qrBig = scale(Image.createImage("/qr.png"), Math.min(scW, scH), Math.min(scW, scH));
        } catch (IOException ex) {
            try {
                qrBig = scale(Image.createImage("resource://qr.png"), Math.min(scW, scH), Math.min(scW, scH));
            } catch (IOException e) {
                ex.printStackTrace();
                e.printStackTrace();
            }
        }
        
        int menuBtnsOffsetH = drawHeaderAndQR(null);
        reloadCanvasParameters(0, menuBtnsOffsetH, scW, scH - menuBtnsOffsetH);
    }

    public void destroyApp(boolean unconditional) {
        isStopped = true;
        Main.exit();
    }
    
    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public void run() {
        long sleep;
        long start;
        
        if (Main.PRE_VERSION >= 0) {
            for (int i = 0; i < MENU_OPTS.length; i++) {
                if (MENU_OPTS[i].startsWith("Version: ")) {
                    MENU_OPTS[i] += "-pre" + Main.PRE_VERSION;
                    break;
                }
            }
        }
        String commitHash = Main.thiss.getAppProperty("Commit");
        if (commitHash != null) {
            for (int i = 0; i < MENU_OPTS.length; i++) {
                if (MENU_OPTS[i].startsWith("Version: ")) {
                    MENU_OPTS[i] += "-" + commitHash;
                    break;
                }
            }
        }
        
        sizeChanged(getWidth(), getHeight());
        
        if (!isMenuInited()) {
            init();
        }

        while (!isStopped) {
            if (!isPaused) {
                start = System.currentTimeMillis();
                // catch screen rotation
                if (scW != getWidth()) {
                    fontSizeCache = -1;
                    sizeChanged(getWidth(), getHeight());
                }
                
                //   if qr isn't selected, repaint on each frame
                //
                //   if big qr is open, draw it oncely,
                // and then we don't need to refresh screen
                if (selected != 0 || !bigQRIsDrawn) {
                    paint();
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
    
    public void paint() {
        Graphics g = getGraphics();
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, scW, scH);
        drawHeaderAndQR(g);
        super.paint(g);
        tick();
        
        if (selected == 0) {
            drawBigQR(g);
        } else {
            bigQRIsDrawn = false;
        }
        flushGraphics();
    }
    
    int drawHeaderAndQR(Graphics g) {
        if (g != null) {
            g.setColor(255, 255, 255);
        }
        
        int offset = margin + extraVerticalMargin;
        for (int i = 0; i < STRINGS.length; i++) {
            if (g != null) {
                g.setFont(font);
                g.drawString(STRINGS[i], scW/2, offset, Graphics.HCENTER | Graphics.TOP);
            }
            offset += fontH;
        }
        offset += margin + extraVerticalMargin;
        
        if (g != null) {
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
        }
        offset += qrSide;
        offset += margin + extraVerticalMargin;
        //g.drawLine(0, offset, scW, offset);
        return offset;
    }
    
    void drawBigQR(Graphics g) {
        try {
            g.drawImage(qrBig, scW / 2, scH / 2, Graphics.HCENTER | Graphics.VCENTER);
        } catch (NullPointerException ex) {
            bigQRIsDrawn = true;
        }
    }

    void openLink() {
        Logger.log(URL);
        try {
            if (Main.thiss.platformRequest(URL)) {
                Main.exit();
            }
        } catch (ConnectionNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    void selectPressed() {
        int selected = this.selected;
        if (selected == MENU_OPTS.length - 3) {
            openLink();
        }
        if (selected == MENU_OPTS.length - 2) {
            counter+=1;
            if (counter == 20) {
                isStopped = true;
                
                WorldGen.isEnabled = true;
                
                World test3 = new World();
                test3.setGravity(FXVector.newVector(10, 100));
                GraphicsWorld.bgOverride = true;
                GraphicsWorld test2 = new GraphicsWorld(test3);
                GameplayCanvas test = new GameplayCanvas(test2);
                GameplayCanvas.uninterestingDebug = true;
                Main.set(test);
            }
        }
        if (selected == MENU_OPTS.length - 1) {
            isStopped = true;
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
