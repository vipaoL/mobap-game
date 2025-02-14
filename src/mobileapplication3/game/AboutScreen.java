/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.game;

import java.io.IOException;

import at.emini.physics2D.World;
import at.emini.physics2D.util.FXVector;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.Image;
import mobileapplication3.platform.ui.RootContainer;

/**
 *
 * @author vipaol
 */
public class AboutScreen extends GenericMenu implements Runnable {
    private static final String URL = "https://github.com/vipaoL/mobap-game";
    private static final String URL_PREVIEW = "GitHub: vipaoL/mobap-game";
    private static final String URL2 = "https://t.me/mobapp_game";
    private static final String URL2_PREVIEW = "TG: @mobapp_game";
    private static final String[] STRINGS = {"A cross-platform game", "on emini physics engine"};
    private static final String[] MENU_OPTS = {""/*there is the qr code*/,
        URL_PREVIEW,
        URL2_PREVIEW,
        "Version: " + Platform.getAppVersion(),
        "Back"};
    private int counter = 17;
    private int scW, scH;
    private int qrSide = 0;
    private int margin = 0;
    private Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL),
            font3 = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private int fontH = font.getHeight();
    private boolean bigQRIsDrawn = false;
    
    private Image qr, qrBig;

    public AboutScreen() {
    	loadParams(MENU_OPTS, 0, MENU_OPTS.length - 1, MENU_OPTS.length - 1);
    }
    
    public void init() {
        setFirstDrawable(1);
        (new Thread(this, "about")).start();
    }
    
    protected void onSetBounds(int x0, int y0, int w, int h) {
    	super.onSetBounds(x0, y0, w, h);
        if (scW == w && scH == h && qr != null) {
            return;
        }

        scW = w;
        scH = h;
        qrSide = scH - fontH * (STRINGS.length + MENU_OPTS.length + 3);
        qrSide = Math.max(qrSide, 66);
        qrSide = Math.min(qrSide, w*7/8);

        margin = fontH/2;

        int headerAndQrH = drawHeaderAndQR(null);
        int menuH = scH - headerAndQrH;
        loadCanvasParams(0, h - menuH, scW, menuH);
        
        try {
            qr = Image.createImage("/qr.png").scale(qrSide, qrSide);
        } catch (IOException ex) {
            try {
                qr = Image.createImage("resource://qr.png").scale(qrSide, qrSide);
            } catch (IOException e) {
                ex.printStackTrace();
                e.printStackTrace();
            }
        }
        
        try {
            qrBig = Image.createImage("/qr.png").scale(Math.min(scW, scH), Math.min(scW, scH));
        } catch (IOException ex) {
            try {
                qrBig = Image.createImage("resource://qr.png").scale(Math.min(scW, scH), Math.min(scW, scH));
            } catch (IOException e) {
                ex.printStackTrace();
                e.printStackTrace();
            }
        }
    }

    public void destroyApp(boolean unconditional) {
        isStopped = true;
        Platform.exit();
    }
    
    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public void run() {
        long sleep;
        long start;
        String commitHash = Platform.getAppProperty("Commit");
        if (commitHash != null) {
            for (int i = 0; i < MENU_OPTS.length; i++) {
                if (MENU_OPTS[i].startsWith("Version: ")) {
                    MENU_OPTS[i] += "-" + commitHash;
                    break;
                }
            }
        }

        while (!isStopped) {
            if (!isPaused) {
                start = System.currentTimeMillis();
                repaint();
                sleep = GameplayCanvas.TICK_DURATION - (System.currentTimeMillis() - start);
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
    
    protected void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
    	//   if qr isn't selected, repaint on each frame
        //
        //   if big qr is open, draw it oncely,
        // and then we don't need to refresh screen
        if (bigQRIsDrawn) {
            return;
        }
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, scW, scH);
        int headerAndQrH = drawHeaderAndQR(g);
        int menuH = scH - headerAndQrH;
        super.onPaint(g, x0, y0 + h - menuH, w, menuH, forceInactive);
        tick();
        
        if (selected == 0) {
            drawBigQR(g);
        } else {
            bigQRIsDrawn = false;
        }
    }
    
    int drawHeaderAndQR(Graphics g) {
        if (g != null) {
            g.setColor(255, 255, 255);
        }
        
        int offset = margin;
        for (int i = 0; i < STRINGS.length; i++) {
            if (g != null) {
                g.setFont(font);
                g.drawString(STRINGS[i], scW/2, offset, Graphics.HCENTER | Graphics.TOP);
            }
            offset += fontH;
        }
        offset += margin;
        if (g != null && selected != 0) {
            try {
                g.drawImage(qr, scW / 2, offset, Graphics.HCENTER | Graphics.TOP);
            } catch (NullPointerException ex) {
            	int leftX = scW / 2 - qrSide / 2;
            	int rightX = scW / 2 + qrSide / 2;
                g.drawLine(leftX, offset, rightX, offset);
                g.drawLine(leftX, offset + qrSide, rightX, offset + qrSide);
                g.drawLine(leftX, offset, leftX, offset + qrSide);
                g.drawLine(rightX, offset, rightX, offset + qrSide);
                g.setFont(font3);
                int x = scW / 2;
                int y = offset + qrSide / 2;
                g.drawString("Your ad", x, y, Graphics.HCENTER|Graphics.BOTTOM);
                g.drawString("could be here.", x, y, Graphics.HCENTER|Graphics.TOP);
            }
        }
        offset += qrSide;
        return offset;
    }
    
    void drawBigQR(Graphics g) {
        try {
            g.drawImage(qrBig, scW / 2, scH / 2, Graphics.HCENTER | Graphics.VCENTER);
        } catch (NullPointerException ex) {
            bigQRIsDrawn = true;
        }
    }

    void openLink(String url) {
        Logger.log(url);
        if (Platform.platformRequest(url)) {
            Platform.exit();
        }
    }

    void selectPressed() {
        int selected = this.selected;
        if (selected == MENU_OPTS.length - 4) {
            openLink(URL);
        }
        if (selected == MENU_OPTS.length - 3) {
            openLink(URL2);
        }
        if (selected == MENU_OPTS.length - 2) {
            counter+=1;
            if (counter >= 20) {
                isStopped = true;
                
                WorldGen.isEnabled = true;
                
                World test3 = new World();
                test3.setGravity(FXVector.newVector(10, 100));
                GraphicsWorld.bgOverride = true;
                GraphicsWorld test2 = new GraphicsWorld(test3);
                GameplayCanvas test = new GameplayCanvas(test2);
                GameplayCanvas.uninterestingDebug = true;
                RootContainer.setRootUIComponent(test);
            }
        }
        if (selected == MENU_OPTS.length - 1) {
            isStopped = true;
            RootContainer.setRootUIComponent(new SettingsScreen());
        }
    }
}
