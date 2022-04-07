/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.World;
import at.emini.physics2D.util.PhysicsFileReader;
import com.sun.midp.io.j2me.storage.File;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author vipaol
 */
public class mnCanvas extends GameCanvas implements Runnable {
    int delay = 0;
    String[] menuOptions = {"-", "Play", "Exit", "Debug", "Levels/testlevel.phy", "-"};
    int selected = 1;
    int scW = getWidth();
    int scH = getHeight();
    int t = 5;
    public static boolean debug = false;
    Graphics g;
    
    String DEFAULT_LEVEL = null;

    public boolean stopped = false;

    private static final int millis = 50;

    public mnCanvas() {
        super(true);
        g = getGraphics();
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, getWidth(), getHeight());
        setFullScreenMode(true);
        scW = getWidth();
        scH = getHeight();
        this.start();
    }

    public void start() {
        Thread runner = new Thread(this);
        runner.start();
    }

    public void destroyApp(boolean unconditional) {
        stopped = true;
        Main.exit();
    }

    public void run() {
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
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, scW - t, scH);
        g.setColor(255, 255, 255);
        int offset = 0;
        for (int i = 0; i < menuOptions.length; i++) {
            if (i == selected) {
                g.setColor(255, 64, 64);
                offset = sin(t * 360 / 10);
            } else {
                g.setColor(255, 255, 255);
                offset = 0;
            }
            if (i == 3 & debug) {
                g.setColor(255, 255, 0);
            }
            Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
            if (font.getHeight() * menuOptions.length > scH) {
                font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
            }
            if (font.getHeight() * menuOptions.length > scH) {
                font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            }
            g.setFont(font);
            
            if (i == 4) font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.drawString(menuOptions[i], scW / 2, (scH + font.getHeight() * 0) / (menuOptions.length + 0) * (i) + offset*Font.getDefaultFont().getHeight() / 8000 + font.getHeight() / 2, Graphics.HCENTER | Graphics.TOP);
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
                    if (selected == 1) startLevel(DEFAULT_LEVEL);
                    if (selected == 2) Main.exit();
                    if (selected == 3) debug = !debug;
                    if (selected == 4) {
                        String root = File.getStorageRoot();
                        //String root = "file:///c:/";
                        String sep = System.getProperty("file.separator");
                        mCanvas.text += ("sep: " + sep + " root: " + root);
                        if (sep == null) {
                            sep = "/";
                        }
                        String path = root + "Levels" + sep + "testlevel.phy";
                        mCanvas.text += "\npath: " + path;
                        startLevel(path);
                    }
            } else if ((keyStates & UP_PRESSED) != 0) {
                if (selected > 1) {
                        selected--;
                    } else {
                        selected = menuOptions.length - 2;
                    }
            } else if ((keyStates & DOWN_PRESSED) != 0) {
                if (selected < menuOptions.length - 2) {
                        selected++;
                    } else {
                        selected = 1;
                    }
            }
        } else delay --;
        if (keyStates == 0) delay = 0;
    }

    public void keyReleased(int keyCode) {
        int gameAction = getGameAction(keyCode);
    }

    public void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        switch (gameAction) {
            case Canvas.DOWN:
                
            case Canvas.UP:
                
            case Canvas.FIRE | Canvas.RIGHT | Canvas.KEY_NUM3:
                
        }
    }

    public void startLevel(String path) {
        stopped = true;
        Main.readWorldFile(path);
        Main.gameCanvas = new mCanvas();
        Main.gameCanvas.setWorld(Main.gameWorld);
        Main.set(Main.gameCanvas);
    }
    
    private int sin_t[] = {0, 174, 342, 500, 643, 766, 866, 940, 985, 1000};
    public int sinus(int t) {
        int k;
        k = (int) (t / 10);
        if (t % 10 == 0) {
            return sin_t[k];
        } else {
            return (int) ((sin_t[k + 1] - sin_t[k]) * (t % 10) / 10 + sin_t[k]);
        }
    }

    public int sin(int t) {
        int sign = 1;
        t = t % 360;//Учтем период синуса
        if (t < 0)//Учтем нечетность синуса
        {
            t = -t;
            sign = -1;
        }
//Воспользуемся формулами приведения
        if (t <= 90) {
            return sign * sinus(t);
        } else if (t <= 180) {
            return sign * sinus(180 - t);
        } else if (t <= 270) {
            return -sign * sinus(t - 180);
        } else {
            return -sign * sinus(360 - t);
        }
    }

    public int cos(int t) {
        t = t % 360;//Учтем период синуса
        if (t < 0) {
            t = -t;
        }//Учтем четность косинуса
//Воспользуемся формулами приведения
        if (t <= 90) {
            return sinus(90 - t);
        } else if (t <= 180) {
            return -sinus(t - 90);
        } else if (t <= 270) {
            return -sinus(270 - t);
        } else {
            return sinus(t - 270);
        }
    }

}
