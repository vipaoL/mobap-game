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

    private final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    private Image qr, qrBig;

    private final int fontH = font.getHeight();
    private int w, h;
    private int headerH, qrSide, menuH;
    private boolean bigQRIsDrawn = false;
    private int counter = 17;

    public void init() {
        loadParams(MENU_OPTS, 0, MENU_OPTS.length - 1, MENU_OPTS.length - 1);
        setFirstDrawable(1);
    }

    public void postInit() {
        (new Thread(this, "about")).start();
    }

    protected void onSetBounds(int x0, int y0, int w, int h) {
    	super.onSetBounds(x0, y0, w, h);
        if (this.w == w && this.h == h && qr != null) {
            return;
        }

        this.w = w;
        this.h = h;
        qrSide = this.h - fontH * (STRINGS.length + MENU_OPTS.length) * 2;
        qrSide = Math.max(qrSide, 66);
        qrSide = Math.min(qrSide, w*7/8);
        int totalTextH = this.h - qrSide;

        menuH = totalTextH * MENU_OPTS.length / (MENU_OPTS.length + STRINGS.length);
        headerH = totalTextH - menuH;
        loadCanvasParams(0, h - menuH, this.w, menuH);

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
            qrBig = Image.createImage("/qr.png").scale(Math.min(this.w, this.h), Math.min(this.w, this.h));
        } catch (IOException ex) {
            try {
                qrBig = Image.createImage("resource://qr.png").scale(Math.min(this.w, this.h), Math.min(this.w, this.h));
            } catch (IOException e) {
                ex.printStackTrace();
                e.printStackTrace();
            }
        }
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
        //   if big qr is open, draw it once,
        // and then we don't need to refresh screen anymore
        if (bigQRIsDrawn) {
            return;
        }
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, this.w, this.h);
        g.setFont(font);
        drawHeader(g);
        drawQR(g);
        super.onPaint(g, x0, y0 + h - menuH, w, menuH, forceInactive);
        tick();

        if (selected == 0) {
            drawBigQR(g);
        } else {
            bigQRIsDrawn = false;
        }
    }

    private void drawHeader(Graphics g) {
        g.setColor(0xffffff);
        int dY = headerH / (STRINGS.length + 1);
        for (int i = 0; i < STRINGS.length; i++) {
            int y = (i + 1) * dY;
            g.drawString(STRINGS[i], w /2, y, Graphics.HCENTER | Graphics.VCENTER);
        }
    }

    private void drawQR(Graphics g) {
        if (selected != 0) {
            try {
                g.drawImage(qr, w / 2, headerH, Graphics.HCENTER | Graphics.TOP);
            } catch (NullPointerException ex) {
                int leftX = w / 2 - qrSide / 2;
                int topY = headerH;
                int d = qrSide;
                while (d > 0) {
                    int c = 255 - 255 * d / qrSide;
                    g.setColor(c, c, c);
                    g.fillRect(leftX, topY, d, d);
                    d -= 2;
                    topY++;
                    leftX++;
                }

                int x = w / 2;
                int y = headerH + qrSide / 2;
                g.setColor(0x000000);
                g.drawString("Your ad", x, y, Graphics.HCENTER|Graphics.BOTTOM);
                g.drawString("could be here.", x, y, Graphics.HCENTER|Graphics.TOP);
            }
        }
    }

    private void drawBigQR(Graphics g) {
        try {
            g.drawImage(qrBig, w / 2, h / 2, Graphics.HCENTER | Graphics.VCENTER);
        } catch (NullPointerException ex) {
            bigQRIsDrawn = true;
        }
    }

    private void openLink(String url) {
        Logger.log(url);
        if (Platform.platformRequest(url)) {
            Platform.exit();
        }
    }

    protected void selectPressed() {
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
                World test3 = new World();
                test3.setGravity(FXVector.newVector(10, 100));
                GraphicsWorld.bgOverride = true;
                GraphicsWorld test2 = new GraphicsWorld(test3);
                GameplayCanvas test = new GameplayCanvas(test2).disablePointCounter();
                test.uninterestingDebug = true;
                RootContainer.setRootUIComponent(test);
            }
        }
        if (selected == MENU_OPTS.length - 1) {
            isStopped = true;
            RootContainer.setRootUIComponent(new SettingsScreen());
        }
    }
}
