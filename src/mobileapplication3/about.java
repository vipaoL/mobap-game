/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

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
public class about extends GameCanvas implements Runnable {
    Image qr;
    int qrSide = 0;
    int qrMargin = 0;

    int k = 20;
    int delay = 0;
    String url = "https://github.com/ViPaOl/mobap-game";
    String urlPrew = "github: /ViPaOl/mobap-game";
    String[] strings = {"J2ME game on emini", "physics engine", urlPrew, "Version: " + Main.thiss.getAppProperty("MIDlet-Version"), "Back"};
    int strsOnTop = 2;
    int selected = 2;
    int scW = getWidth();
    int scH = getHeight();
    int t = 5;
    public static boolean debug = false;
    Graphics g;
    Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    int fontH = font.getHeight();
    Font font2 = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    int font2H = font2.getHeight();
    Font font3 = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    int font3H = font3.getHeight();
    boolean paused = false;
    int offset2 = 0;

    String DEFAULT_LEVEL = "";

    boolean stopped = false;

    private static final int millis = 50;
    int offset = 0;

    public static boolean wg = false;

    public about() {
        super(true);
        setFullScreenMode(true);
        (new Thread(this, "about canvas")).start();
    }

    public void start() {
        qrMargin = fontH/2;
        scW = getWidth();
        scH = getHeight();
        qrSide = scH - font3H - font2H * (strings.length);
        if (qrSide > scW - qrMargin*2) {
            qrSide = scW - qrMargin*2;
        }
        
        try {
            qr = scale(Image.createImage("/qr.png"), qrSide, qrSide);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        delay = 5;
        stopped = false;
        g = getGraphics();
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (font.getHeight() * strings.length > scH) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        }
        if (font.getHeight() * strings.length > scH) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }
        fontH = font.getHeight();
        offset2 = font2H + fontH * strsOnTop + qrSide + qrMargin * 2;
    }

    protected void showNotify() {
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
        //stopped = false;

        long sleep = 0;
        long start = 0;

        while (!stopped) {
            start = System.currentTimeMillis();
            input();
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
        int canvH = scH;
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, scW - t, scH);
        g.setColor(255, 255, 255);
        g.setFont(font2);
        g.drawString("About:", scW/2, 0, Graphics.HCENTER | Graphics.TOP);
        int offset2 = font2H;
        for (int i = 0; i < strsOnTop; i++) {
            g.setFont(font);
            g.drawString(strings[i], scW/2, offset2, Graphics.HCENTER | Graphics.TOP);
            offset2+=fontH;
        }
        offset2+=qrMargin;
        
        try {
            g.drawImage(qr, scW / 2, offset2, Graphics.HCENTER | Graphics.TOP);
        } catch (NullPointerException ex) {
            g.drawLine(qrMargin, offset2, scW - qrMargin, offset2);
            g.drawLine(qrMargin, offset2 + qrSide, scW - qrMargin, offset2 + qrSide);
            g.drawLine(qrMargin, offset2, qrMargin, offset2 + qrSide);
            g.drawLine(scW - qrMargin, offset2, scW - qrMargin, offset2 + qrSide);
            g.setFont(font3);
            g.drawString("Your ad could be here.", scW / 2, offset2 + (qrSide - font3H) / 2, Graphics.HCENTER|Graphics.TOP);
        }
        offset = 0;
        offset2 += qrSide + qrMargin;
        canvH = scH - offset2;
        int l =  strings.length - strsOnTop;
        for (int i = 0; i < l; i++) {
            if (i == selected) {
                g.setColor(255, 64, 64);
                offset = Mathh.sin(t * 360 / 10);
            } else {
                g.setColor(255, 255, 255);
                offset = 0;
            }
            if (i == 3 & debug) {
                g.setColor(255, 255, 0);
            }
            g.setFont(font);
            k = (canvH + canvH / (l + 1)) / (l + 1);
            g.drawString(strings[i+strsOnTop], scW / 2, k * (i + 1) - font.getHeight() / 2 - canvH / (l + 1) / 2 + offset * Font.getDefaultFont().getHeight() / 8000 + offset2, Graphics.HCENTER | Graphics.TOP);
        }
        if (t > 9) {
            t = 0;
        } else {
            t++;
        }
        //flushGraphics();
    }

    private void input() {
        int keyStates = getKeyStates();
        if (delay < 1) {
            delay = 5;
            if ((keyStates & (RIGHT_PRESSED | FIRE_PRESSED)) != 0) {
                selectPressed();
            } else if ((keyStates & UP_PRESSED) != 0) {
                if (selected > 0) {
                    selected--;
                } else {
                    selected = strings.length - 3;
                }
            } else if ((keyStates & DOWN_PRESSED) != 0) {
                if (selected < strings.length - 3) {
                    selected++;
                } else {
                    selected = 0;
                }
            }
        } else {
            delay--;
        }
        if (keyStates == 0) {
            delay = 0;
        }
    }

    public void keyReleased(int keyCode) {
        int gameAction = getGameAction(keyCode);
    }

    public void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        if (gameAction == KEY_NUM1) {
            selected = 0;
            selectPressed();
        }
        if (gameAction == KEY_NUM2) {
            selected = 1;
            selectPressed();
        }
        if (gameAction == KEY_NUM3) {
            selected = 2;
            selectPressed();
        }
    }

    protected void pointerPressed(int x, int y) {
        //k = scH / menuOptions.length;
        selected = (y-offset2) / k;
        //selected = menuOptions.length * (y + fontH) / scH;
        if (selected <= 0) {
            selected = 0;
        }
        if (selected > 2) {
            selected = 2;
        }
    }

    protected void pointerDragged(int x, int y) {
        selected = (y-offset2) / k;
        //selected = menuOptions.length * (y + fontH) / scH;
        if (selected <= 0) {
            selected = 0;
        }
        if (selected > 2) {
            selected = 2;
        }
    }

    protected void pointerReleased(int x, int y) {
        selected = (y-offset2) / k;
        //selected = menuOptions.length * y / scH;
        if (selected <= 0) {
            selected = 0;
        } else if (selected > 2) {
            selected = 2;
        } else {
            selectPressed();
        }
    }
    void openLink() {
        Main.print(url);
        try {
            Main.thiss.platformRequest(url);
        } catch (ConnectionNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    void selectPressed() {
        if (selected == 0) {
            openLink();
        }
        if (selected == 1) {
            t+=1;
        }
        if (selected == 2) {
            stopped = true;
            Main.set(new mnCanvas());
        }
    }

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
