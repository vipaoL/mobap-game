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

    boolean r = false;

    int waiting = 0;
    int hintCountdown = 60;
    String[] menuhint = {"menu:", "here(touch),", "9, #"};
    String[] pausehint = {"pause:", "here(touch),", "right soft btn"};
    static boolean firstStart = true;
    int ang = 0;
    private final int millis = 50;
    //private World world;
    //private Thread thread;
    static boolean stopped = false;
    public static boolean isDrawingNow = true;
    boolean accel = false;
    //public Body centrCor = new Body(0, -390, centroidCorrector, true);
    //public Body boll = new Body(300, -200, ball2, true);
    Vector waitingForDynamic = new Vector();
    Vector waitingTime = new Vector();
    static int flying = 0;
    int motorTdOff = 50;
    boolean debug = false;
    //String text = "text";
    int prevX = 0;
    //Motor leftmotor;
    //Motor rightmotor;
    //Motor carbodymotor;
    //public static Landscape l;
    int gameoverCountdown = 0;
    Font smallfont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    int sFontH = smallfont.getHeight();
    Font mediumfont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    int mFontH = mediumfont.getHeight();
    Font largefont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    int lFontH = largefont.getHeight();
    Font currentFont = largefont;
    int scW = 2;
    int scH = 2;
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

    public GameplayCanvas() {
        setFullScreenMode(true);
        scW = getWidth();
        scH = getHeight();
    }
    public GraphicsWorld w;

    public void setWorld(GraphicsWorld world) {
        stopped = false;
        Main.print("gamecanvas:setWorld()");
        this.w = world;
        w.setGravity(FXVector.newVector(0, 250));
        //l = world.getLandscape();
        restart();
        (new Thread(this, "game canvas")).start();
    }
    
    

    protected void showNotify() {
        scW = getWidth();
        scH = getHeight();
        Main.sWidth = scW;
        Main.sHeight = scH;
        w.refreshScreenParameters();
        stopped = false;
    }

    protected void hideNotify() {
        paused = true;
    }

    /*public synchronized void end() {
        //stopped = true;
    }*/

    public void run() {
        long sleep = 0;
        long start = 0;
        int tick = 0;
        int tenFX = FXUtil.toFX(10);
        gameoverCountdown = 0;
        Contact[][] contacts = new Contact[3][];
        //paused = firstStart;
        //world.setTimestepFX(FXUtil.toFX(16));
        if (DebugMenu.isDebugEnabled & DebugMenu.music) {
            Sound sound = new Sound();
            sound.startBgMusic();
        }
        
        while(false & w == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        while (!stopped) {
            if (scW != getWidth()) {
                showNotify();
            }
            if (!paused && worldgen.isReady()) {
                isDrawingNow = true;
                start = System.currentTimeMillis();
                try {
                    contacts[0] = w.getContactsForBody(w.leftwheel);
                    contacts[1] = w.getContactsForBody(w.rightwheel);
                    contacts[2] = w.getContactsForBody(w.carbody);
                    ang = 360 - FXUtil.angleInDegrees2FX(w.carbody.rotation2FX());
                    leftContacts = contacts[0][0] != null;
                    rightContacts = contacts[1][0] != null;
                } catch (NullPointerException ex) {
                    contacts[0] = new Contact[0];
                    contacts[1] = new Contact[0];
                    contacts[2] = new Contact[0];
                }
                if ((!leftContacts & !rightContacts) & !debug) {
                    flying += 1;
                } else {
                    flying = 0;
                }
                
                FXVector velFX = w.carbody.velocityFX();
                carVelocitySqr = velFX.xAsInt() * velFX.xAsInt() + velFX.yAsInt() * velFX.yAsInt();
                if (carVelocitySqr > 1000000) {
                            speedMultipiler = 2;
                            speed = 3;
                            //m = 0;
                        } else {
                            speedMultipiler = 20;
                            speed = 0;
                            if (carVelocitySqr > 100000) {
                                speed = 1;
                                //m = 0;
                                speedMultipiler = 15;
                                if (carVelocitySqr > 500000) {
                                    //speedMultipiler = 8;
                                    speed = 2;
                                }
                            }
                        }

                if (accel) {
                    motorTdOff = 0;
                    if (flying > 2 & !debug) {
                        if (w.carbody.rotationVelocity2FX() > 50000000) {
                            w.carbody.applyTorque(FXUtil.toFX(-w.carbody.rotationVelocity2FX()/16000));
                        } else {
                            w.carbody.applyTorque(FXUtil.toFX(-10000));
                        }
                    } else {/*
                        FXVector velFX = w.carbody.velocityFX();
                        carVelocitySqr = velFX.xAsInt() * velFX.xAsInt() + velFX.yAsInt() * velFX.yAsInt();
                        
                        //int carVelocitySqr = 0;
                        //leftwheel.applyTorque(FXUtil.toFX(-40000));
                        //int m = 8;
                        if (carVelocitySqr > 1600000) {
                            speedMultipiler = 2;
                            speed = 3;
                            //m = 0;
                        } else {
                            speedMultipiler = 4;
                            speed = 0;
                            if (carVelocitySqr > 5000) {
                                speed = 1;
                                //m = 0;
                                speedMultipiler = 12;
                                if (carVelocitySqr > 400000) {
                                    speedMultipiler = 8;
                                    speed = 2;
                                }
                            }
                        }*/
                        //carbody.applyMomentum(new FXVector(FXUtil.divideFX(FXUtil.toFX(Mathh.cos(ang - 75) * m), tenFX), FXUtil.divideFX(-FXUtil.toFX(Mathh.sin(ang - 75) * m), tenFX)));
                        //rightwheel.applyTorque(FXUtil.toFX(-40000));
                        //boll.applyTorque(FXUtil.toFX(-40000));

                        int FXSinAngM = 0;
                        int FXCosAngM = 0;
                        
                        if (debug) {
                            ang += 15;
                        }

                        FXSinAngM = FXUtil.divideFX(FXUtil.toFX(Mathh.sin(ang - 15) * speedMultipiler), tenFX * 5);
                        FXCosAngM = FXUtil.divideFX(FXUtil.toFX(Mathh.cos(ang - 15) * speedMultipiler), tenFX * 5);
                        
                        //carbody.applyMomentum(new FXVector(FXUtil.divideFX(FXUtil.toFX(Mathh.cos(ang - 75) * carVelocitySqr), tenFX * 8000), FXUtil.divideFX(-FXUtil.toFX(Mathh.sin(ang - 75) * carVelocitySqr), tenFX * 8000)));
                        w.carbody.applyMomentum(new FXVector(FXCosAngM, -FXSinAngM));


                        if (leftContacts & rightContacts) {
                            //leftwheel.applyMomentum(new FXVector(FXCosAngM, -FXSinAngM));
                            //rightwheel.applyMomentum(new FXVector(FXCosAngM, -FXCosAngM));

                        } else {
                            if (leftContacts) {
                                //leftwheel.applyMomentum(new FXVector(FXCosAngM, -FXSinAngM));
                            }
                            if (rightContacts & !debug) {
                                w.carbody.applyTorque(FXUtil.toFX(-4000));
                                //rightwheel.applyMomentum(new FXVector(FXUtil.divideFX(FXUtil.toFX(Mathh.cos(ang - 15) * speedMultipiler), tenFX * 5), FXUtil.divideFX(-FXUtil.toFX(Mathh.sin(ang - 15) * speedMultipiler), tenFX * 5)));
                                ////leftwheel.applyMomentum(new FXVector(FXUtil.divideFX(FXUtil.toFX(Mathh.cos(ang) * speedMultipiler), tenFX * 5), FXUtil.divideFX(-FXUtil.toFX(Mathh.sin(ang) * speedMultipiler), tenFX * 5)));
                            }
                            //carbody.applyForce(new FXVector(FXUtil.toFX(sin(ang)), FXUtil.toFX(cos(ang))), 100);
                            if (w.getContactsForBody(w.carbody)[0] != null & !debug) {
                                w.carbody.applyTorque(FXUtil.toFX(-4000));
                            }
                        }
                    }
                } else {
                    if (motorTdOff < 40 | debug) {
                        try {
                            if (w.carbody.angularVelocity2FX() > 0) {
                                w.carbody.applyTorque(FXUtil.toFX(w.carbody.angularVelocity2FX() / 4000));
                            }
                            if (flying < 2 & !debug) {
                                w.carbody.applyMomentum(new FXVector(-w.carbody.velocityFX().xFX/5, -w.carbody.velocityFX().yFX/5));
                            }
                            motorTdOff++;
                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                waiting = waitingForDynamic.size();
                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < contacts[j].length; i++) {
                        if (contacts[j][i] != null) {
                            Body body = contacts[j][i].body1();
                            if (!waitingForDynamic.contains(body)) {
                                waitingForDynamic.addElement(body);
                                waitingTime.addElement(new Integer(20));
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
                    if (GraphicsWorld.carY > 2000 + worldgen.getLowestY() | (ang > 140 & ang < 220 & w.carbody.getContacts()[0] != null)) {
                        if (gameoverCountdown < 8) {
                            gameoverCountdown++;
                        } else {
                            if (!debug) {
                                openMenu();
                            } else {
                                restart();
                            }
                        }
                    } else {
                        if (gameoverCountdown > 0) {
                            gameoverCountdown--;
                        } else {
                            gameoverCountdown = 0;
                        }
                    }
                    for (int i = 0; i < w.getBodyCount(); i++) {
                        Body[] bs = w.getBodies();
                        if (bs[i].positionFX().yAsInt() > 20000 + worldgen.getLowestY()) {
                            w.removeBody(bs[i]);
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
            if (hintCountdown > 0) {
                hintCountdown--;
            }
        }
    }

    protected void paint(Graphics g) {
        w.draw(g);
        drawGUI(g);
    }
    
    private void drawGUI(Graphics g) {
        if (firstStart & hintCountdown > 0) {
            int color = 255 * hintCountdown / 60;
            g.setColor(color/2, color, color/2);
            g.fillRect(0, 0, scW/3, scH/6);
            g.fillRect(scW*2/3, 0, scW/3, scH/6);
            g.setColor(0, 0, color);
            g.setFont(smallfont);
            for (int i = 0; i < menuhint.length; i++) {
                g.drawString(menuhint[i], scW/6, i * sFontH + scH / 12 - sFontH*menuhint.length/2, Graphics.HCENTER | Graphics.TOP);
            }
            for (int i = 0; i < pausehint.length; i++) {
                g.drawString(pausehint[i], scW*5/6, i * sFontH + scH / 12 - sFontH*pausehint.length/2, Graphics.HCENTER | Graphics.TOP);
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
                        g.setColor(64, 64, 0);
                        break;
                    case 2:
                        g.setColor(255, 64, 0);
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
                g.drawString(String.valueOf(w.carbody.positionFX().xAsInt()), 0, debugTextOffset, 0); 
                debugTextOffset += currentFont.getHeight();
            }
            if (DebugMenu.showAngle) {
                if (flying > 0) {
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
        r = false;
        if (flying > 0) {
            flying = Math.max(5, flying);
        }
    }

    protected void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        //text = "Last key: " + gameAction + " " + keyCode;
        if (keyCode == KEY_ACTION_RIGHT) {
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
            r = true;
        } else 
        if ((keyCode == KEY_STAR | gameAction == GAME_B)) {
            if (DebugMenu.isDebugEnabled & DebugMenu.cheat) {
                FXVector pos = w.carbody.positionFX();
                int carX = pos.xAsInt();
                int carY = pos.yAsInt();
                worldgen.line(carX - 200, carY + 200, carX + 2000, carY + 0);
            }
        } else {
            accel = true;
        }
    }

    public void openMenu() {
        firstStart = false;
        MenuCanvas.isWorldgenEnabled = false;
        stopped = true;
        Main.set(new MenuCanvas());
    }

    protected void pointerPressed(int x, int y) {
        if (x > scW * 2 / 3 & y < scH / 6) {
            pauseTouched = true;
        } else if (x < scW / 3 & y < scH / 6) {
            menuTouched = true;
        } else {
            accel = true;
        }
    }
    
    protected void pointerDragged(int x, int y) {
        pauseTouched = false;
        menuTouched = false;
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
        gameoverCountdown = 0;
        worldgen = new WorldGen(w);
        if (MenuCanvas.isWorldgenEnabled) {
            worldgen.start();
        } else {
            w.addCar();
        }
    }
    private void setFont(Font font, Graphics g) {
        g.setFont(font);
        currentFont = font;
    }
}
