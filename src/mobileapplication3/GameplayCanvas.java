/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.*;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author vipaol
 */
public class GameplayCanvas extends Canvas implements Runnable {

    int KEY_ACTION_RIGHT = -7;
    int KEY_ACTION_LEFT = -6;

    int hintCountdown = 120;
    String[] menuhint = {"MENU:", "here(touch),", "D, #"};
    String[] pausehint = {"PAUSE:", "here(touch), *,", "B, right soft"};
    int TEN_FX = FXUtil.toFX(10);
    
    // for displaying hints only on first start
    static boolean isFirstStart = true;
    int carAngle = 0;
    private final int millis = 50;
    static boolean stopped = false;
    public static boolean isDrawingNow = true;
    boolean accel = false;
    Vector waitingForDynamic = new Vector();
    Vector waitingTime = new Vector();
    static int timeFlying = 0;
    int timeMotorTurnedOff = 50;
    public static boolean uninterestingDebug = false;
    int gameoverCountdown;
    Font smallfont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    int sFontH = smallfont.getHeight();
    Font mediumfont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    int mFontH = mediumfont.getHeight();
    Font largefont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    int lFontH = largefont.getHeight();
    Font currentFont = largefont;
    int scW;
    int scH;
    int maxScSide;
    boolean leftContacts = false;
    boolean rightContacts = false;
    static boolean paused = false;
    WorldGen worldgen;
    int speedMultipiler = 1;
    int speed = 0;
    int carVelocitySqr;
    boolean pauseTouched = false;
    boolean menuTouched = false;
    static int flipIndicator = 255; // for coloring
    boolean isWorldLoaded = false;

    public GameplayCanvas() {
        Main.log("gcanvas constructor");
        setFullScreenMode(true);
        //scW = getWidth();
        //scH = getHeight();
        //repaint();
        Main.log("gcanvas:starting thread");
        (new Thread(this, "game canvas")).start();
    }
    public GraphicsWorld w;

    public void setWorld(GraphicsWorld world) {
        stopped = false;
        Main.log("gamecanvas:setWorld()");
        this.w = world;
        w.setGravity(FXVector.newVector(0, 250));
        restart();
        isWorldLoaded = true;
    }
    
    

    protected void showNotify() {
        Main.log("showNotify");
        scW = getWidth();
        scH = getHeight();
        maxScSide = Math.max(scW, scH);
        Main.sWidth = scW;
        Main.sHeight = scH;
        if (isWorldLoaded) {
            w.refreshScreenParameters();
        }
        stopped = false;
        if (worldgen != null) {
            worldgen.resume();
        }
    }

    protected void hideNotify() {
        Main.log("hideNotify");
        paused = true;
        if (worldgen != null) {
            worldgen.pause();
        }
        repaint();
    }

    /*public synchronized void end() {
        //stopped = true;
    }*/

    public void run() {
        Main.log("gcanvas:thread started");
        // while world is loading, draw loading screen
        while (!isWorldLoaded) {
            repaint();
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        Main.log("thread:world loaded");
        
        w.refreshScreenParameters();
        
        long sleep = 0;
        long start = 0;
        int tick = 0;
        gameoverCountdown = 0;
        Contact[][] contacts = new Contact[3][];
        
        // init music player if enabled
        if (DebugMenu.isDebugEnabled & DebugMenu.music) {
            Main.log("Starting sound");
            Sound sound = new Sound();
            sound.startBgMusic();
        }
        
        // continue updating loading screen until worldgen is loaded
        Main.log("thread:waiting for wg");
        while(!worldgen.isReady()) {
            repaint();
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
        Main.log("thread:starting game cycle");
        while (!stopped) {
            if (scW != getWidth()) {
                showNotify();
                w.refreshScreenParameters();
            }
            if (!paused && worldgen.isReady()) {
                isDrawingNow = true;
                start = System.currentTimeMillis();
                contacts[0] = w.getContactsForBody(w.leftwheel);
                contacts[1] = w.getContactsForBody(w.rightwheel);
                contacts[2] = w.getContactsForBody(w.carbody);
                carAngle = 360 - FXUtil.angleInDegrees2FX(w.carbody.rotation2FX());
                leftContacts = contacts[0][0] != null;
                rightContacts = contacts[1][0] != null;
                if ((!leftContacts & !rightContacts)) {
                    timeFlying += 1;
                } else {
                    timeFlying = 0;
                }
                
                FXVector velFX = w.carbody.velocityFX();
                carVelocitySqr = velFX.xAsInt() * velFX.xAsInt() + velFX.yAsInt() * velFX.yAsInt();
                if (carVelocitySqr > 1000000) {
                    speedMultipiler = 2;
                    speed = 2;
                } else if (carVelocitySqr > 100000) {
                    speed = 1;
                    speedMultipiler = 15;
                } else {
                    speedMultipiler = 20;
                    speed = 0;
                }
                
                if (uninterestingDebug) {
                    timeFlying = 0;
                    speedMultipiler = 30;
                }

                if (accel) {
                    timeMotorTurnedOff = 0;
                    if (timeFlying > 2) {
                        if (w.carbody.rotationVelocity2FX() > 50000000) {
                            w.carbody.applyTorque(FXUtil.toFX(-w.carbody.rotationVelocity2FX()/16000));
                        } else {
                            w.carbody.applyTorque(FXUtil.toFX(-10000));
                        }
                    } else {
                        int FXSinAngM = 0;
                        int FXCosAngM = 0;

                        FXSinAngM = FXUtil.divideFX(FXUtil.toFX(Mathh.sin(carAngle - 15) * speedMultipiler), TEN_FX * 5);
                        FXCosAngM = FXUtil.divideFX(FXUtil.toFX(Mathh.cos(carAngle - 15) * speedMultipiler), TEN_FX * 5);
                        w.carbody.applyMomentum(new FXVector(FXCosAngM, -FXSinAngM));


                        if ((!leftContacts & w.getContactsForBody(w.carbody)[0] != null) | rightContacts) {
                            w.carbody.applyTorque(FXUtil.toFX(-4000));
                        }
                    }
                } else {
                    if (timeMotorTurnedOff < 40 & !uninterestingDebug) {
                        try {
                            if (w.carbody.angularVelocity2FX() > 0) {
                                w.carbody.applyTorque(FXUtil.toFX(w.carbody.angularVelocity2FX() / 4000));
                            }
                            if (timeFlying < 2) {
                                w.carbody.applyMomentum(new FXVector(-w.carbody.velocityFX().xFX/5, -w.carbody.velocityFX().yFX/5));
                            }
                            timeMotorTurnedOff++;
                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < contacts[j].length; i++) {
                        if (contacts[j][i] != null) {
                            Body body = contacts[j][i].body1();
                            if (!waitingForDynamic.contains(body) & body != w.carbody & body != w.leftwheel & body != w.rightwheel) {
                                waitingForDynamic.addElement(body);
                                waitingTime.addElement(new Integer(20));
                                if (uninterestingDebug) w.removeBody(body);
                            }
                        }
                    }
                }                

                

                if (tick < 5) {
                    tick++;
                } else {
                    tick = 1;
                    
                    for (int i = 0; i < waitingForDynamic.size(); i++) {
                        if (Integer.parseInt(String.valueOf(waitingTime.elementAt(i))) > 0) {
                            waitingTime.setElementAt(new Integer(Integer.parseInt(String.valueOf(waitingTime.elementAt(i))) - 10), i);
                        } else {
                            ((Body) waitingForDynamic.elementAt(i)).setDynamic(true);
                            waitingForDynamic.removeElementAt(i);
                            waitingTime.removeElementAt(i);
                        }
                    }
                    if (GraphicsWorld.carY > 2000 + worldgen.getLowestY() | (carAngle > 140 & carAngle < 220 & w.carbody.getContacts()[0] != null)) {
                        if (gameoverCountdown < 8) {
                            gameoverCountdown++;
                        } else {
                            if (uninterestingDebug) {
                                restart();
                            } else {
                                openMenu();
                            }
                        }
                    } else {
                        if (gameoverCountdown > 0) {
                            gameoverCountdown--;
                        } else {
                            gameoverCountdown = 0;
                        }
                    }
                    for (int i = 0; i < w.getBodyCount(); i++) { // removing all that fell out the world or got too left
                        Body[] bodies = w.getBodies();
                        if (bodies[i].positionFX().yAsInt() > 20000 + worldgen.getLowestY() | w.carbody.positionFX().xAsInt() - bodies[i].positionFX().xAsInt() > GraphicsWorld.viewField * 2) {
                            w.removeBody(bodies[i]);
                        }
                    }
                }
                
                w.tick();
                repaint();
                
                isDrawingNow = false;

                sleep = millis - (System.currentTimeMillis() - start);
                sleep = Math.max(sleep, 0);

                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    protected void paint(Graphics g) {
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, maxScSide, maxScSide);
        if (isWorldLoaded) {
            w.draw(g);
        }
        drawGUI(g);
    }
    
    private void drawGUI(Graphics g) {
        if (isFirstStart & hintCountdown > 0) {
            int color = 255 * hintCountdown / 120;
            g.setColor(color/4, color/2, color/4);
            if (Main.isScreenLogEnabled) {
                //g.setColor(color/2, color/2, color/2);
            }
            g.fillRect(0, 0, scW/3, scH/6);
            g.fillRect(scW*2/3, 0, scW/3, scH/6);
            g.setColor(color/4, color/4, color);
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL));
            for (int i = 0; i < menuhint.length; i++) {
                g.drawString(menuhint[i], scW/6, i * sFontH + scH / 12 - sFontH*menuhint.length/2, Graphics.HCENTER | Graphics.TOP);
            }
            for (int i = 0; i < pausehint.length; i++) {
                g.drawString(pausehint[i], scW*5/6, i * sFontH + scH / 12 - sFontH*pausehint.length/2, Graphics.HCENTER | Graphics.TOP);
            }
            if (isWorldLoaded) {
                hintCountdown--;
            }
        }
        
        if (DebugMenu.isDebugEnabled) {               // draw some debug info if debug is enabled
            int debugTextOffset = 0;
            if (DebugMenu.speedo) {
                switch (speed) {
                    case 0:
                        g.setColor(0, 255, 0);
                        break;
                    case 1:
                        g.setColor(255, 255, 0);
                        break;
                    default:
                        g.setColor(255, 0, 0);
                        break;
                }
                setFont(smallfont, g);
                g.fillRect(0, debugTextOffset, currentFont.getHeight() * 5, currentFont.getHeight());
                g.setColor(255, 255, 255);
                g.drawString(String.valueOf(carVelocitySqr), 0, debugTextOffset, 0);
                debugTextOffset += currentFont.getHeight();
            }
            if (DebugMenu.xCoord) {
                g.setColor(255, 255, 255);
                g.drawString(GraphicsWorld.carX + " " + GraphicsWorld.carY, 0, debugTextOffset, 0); 
                debugTextOffset += currentFont.getHeight();
            }
            if (DebugMenu.showAngle) {
                if (timeFlying > 0) {
                    g.setColor(0, 0, 255);
                } else {
                    g.setColor(255, 255, 255);
                }
                g.drawString(String.valueOf(FXUtil.angleInDegrees2FX(w.carbody.rotation2FX())), 0, debugTextOffset, 0);
                debugTextOffset += currentFont.getHeight();
            }
        }
        if (gameoverCountdown > 1) { // game over screen
            g.setFont(largefont);
            g.setColor(255, 0, 0);
            g.drawString("!", scW / 2, scH / 3 + largefont.getHeight() / 2, Graphics.HCENTER | Graphics.TOP);
            if (!DebugMenu.isDebugEnabled) {
                g.setColor(191, 191, 191);
            }
            for (int i = 1; i < gameoverCountdown; i++) {
                g.fillRect(0, 0, scW, scH*i/7/2 + 1);
                g.fillRect(0, scH - scH*i/7/2, scW, scH - 1);
            }
        }
        if (Main.isScreenLogEnabled) {
            g.setColor(150, 255, 150);
            Font font = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL);
            g.setFont(font);
            for (int i = 0; i < Main.onScreenLog.length; i++) {
                try {
                    g.drawString(Main.onScreenLog[i], 0, font.getHeight() * i, Graphics.TOP | Graphics.LEFT);
                } catch (NullPointerException ex) {
                    
                } catch (IllegalArgumentException ex) {
                    
                }
            }
        }
        if (!isWorldLoaded) {
            return;
        }
        if (MenuCanvas.isWorldgenEnabled) { // points
            g.setColor(flipIndicator, flipIndicator, 255);
            g.setFont(largefont);
            g.drawString(String.valueOf(w.points), w.halfScWidth, w.scHeight - mFontH * 3 / 2,
                    Graphics.HCENTER | Graphics.TOP);
            
            if (flipIndicator < 255 & !DebugMenu.dontCountFlips) { // coloring when flip
                flipIndicator+=64;
                if (flipIndicator >= 255) {
                    flipIndicator = 255;
                }
            }
        }
        if (paused & !worldgen.isResettingPosition) { // pause screen
            g.setColor(0, 0, 255);
            int d = 6 * scH / 240;
            for (int i = 0; i <= scH; i++) {
                //g.drawLine(0, i * d - 1, w, -w + i*d - 1);
                //g.drawLine(0, i * d, w, -w + i*d);
                //g.drawLine(0, -w + i*d, w, i * d);
                if (DebugMenu.isDebugEnabled) {
                    g.setColor(255 * i / scH % 255, 0, 0);
                }
                g.drawLine(scW / 2, 0, d * i, scH);
            }
            g.setFont(largefont);
            g.setColor(255, 255, 255);
            g.drawString("PAUSED", scW / 2, scH / 3 + largefont.getHeight() / 2, Graphics.HCENTER | Graphics.TOP);
        }
    }
    
    public static void indicateFlip() {
        flipIndicator = 0;
    }

    protected void keyReleased(int keyCode) {
        int gameAction = getGameAction(keyCode);
        accel = false;
        if (timeFlying > 0) {
            timeFlying = Math.max(5, timeFlying);
        }
    }

    protected void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        if (keyCode == KEY_ACTION_RIGHT/* | keyCode == GenericMenu.SIEMENS_KEYCODE_RIGHT_SOFT*/) {
            if (!paused) {
                hideNotify();
                repaint();
            } else {
                paused = false;
                showNotify();
            }
        } else
        if (keyCode == KEY_POUND | gameAction == GAME_D) {
            openMenu();
        } else 
        if ((keyCode == KEY_STAR | gameAction == GAME_B)) {
            if (!paused) {
                hideNotify();
                repaint();
            } else {
                paused = false;
                showNotify();
            }
            /*if (DebugMenu.isDebugEnabled & DebugMenu.cheat) {
                FXVector pos = w.carbody.positionFX();
                int carX = pos.xAsInt();
                int carY = pos.yAsInt();
                worldgen.line(carX - 200, carY + 200, carX + 2000, carY + 0);
            }*/
        } else {
            accel = true;
        }
    }

    public void openMenu() {
        isFirstStart = false;
        MenuCanvas.isWorldgenEnabled = false;
        uninterestingDebug = false;
        stopped = true;
        Main.set(new MenuCanvas());
    }

    int pointerX = 0;
    int pointerY = 0;
    protected void pointerPressed(int x, int y) {
        if (x > scW * 2 / 3 & y < scH / 6) {
            pauseTouched = true;
        } else if (x < scW / 3 & y < scH / 6) {
            menuTouched = true;
        } else {
            accel = true;
        }
        pointerX = x;
        pointerY = y;
    }
    
    protected void pointerDragged(int x, int y) {
        if (pointerX != x | pointerY != y) {
            pauseTouched = false;
            menuTouched = false;
        }
        pointerX = x;
        pointerY = y;
    }

    protected void pointerReleased(int x, int y) {
        if (pauseTouched) {
            if (!paused) {
                hideNotify();
                repaint();
            } else {
                stopped = false;
                paused = false;
                showNotify();
            }
        }
        if (menuTouched) {
            stopped = true;
            openMenu();
        }
        pauseTouched = false;
        menuTouched = false;
        accel = false;
    }

    public void restart() {
        Main.log("restart");
        gameoverCountdown = 0;
        worldgen = new WorldGen(w);
        Main.log("wg inited, starting");
        if (MenuCanvas.isWorldgenEnabled) {
            worldgen.start();
            Main.log("wg started");
        } else {
            w.addCar();
        }
    }
    private void setFont(Font font, Graphics g) {
        g.setFont(font);
        currentFont = font;
    }
}
