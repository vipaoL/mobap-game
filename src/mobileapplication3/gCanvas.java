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
public class gCanvas extends Canvas implements Runnable {

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
    boolean accel = false;
    //public Body centrCor = new Body(0, -390, centroidCorrector, true);
    //public Body boll = new Body(300, -200, ball2, true);
    Vector waitingForDynamic = new Vector();
    Vector waitingTime = new Vector();
    static int flying = 0;
    int motorTdOff = 50;
    //String text = "text";
    UserData orig;
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
    int scW = 2;
    int scH = 2;
    boolean leftContacts = false;
    boolean rightContacts = false;
    static boolean paused = false;
    WorldGen worldgen;
    int speedMultipiler = 1;
    boolean pauseTouched = false;
    boolean menuTouched = false;

    public gCanvas() {
        setFullScreenMode(true);
        scW = getWidth();
        scH = getHeight();
    }
    public GraphicsWorld w;

    public void setWorld(GraphicsWorld world) {
        stopped = false;
        Main.print("gamecanvas:setWorld()");
        this.w = world;
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

    public synchronized void end() {
        //stopped = true;
    }

    public void run() {
        long sleep = 0;
        long start = 0;
        int tick = 0;
        int tenFX = FXUtil.toFX(10);
        gameoverCountdown = 0;
        Contact[][] contacts = new Contact[3][];
        //paused = firstStart;
        //world.setTimestepFX(FXUtil.toFX(16));
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
                if (!leftContacts & !rightContacts) {
                    flying += 1;
                } else {
                    flying = 0;
                }

                if (accel) {
                    motorTdOff = 0;
                    if (flying > 2) {
                        if (w.carbody.rotationVelocity2FX() < 50000000 & w.carbody.rotationVelocity2FX() > 8000000) {
                            w.carbody.applyTorque(FXUtil.toFX(-w.carbody.rotationVelocity2FX()/6000));
                        } else {
                            w.carbody.applyTorque(FXUtil.toFX(-4000));
                        }
                    } else {
                        FXVector velFX = w.carbody.velocityFX();
                        int carVelocitySqr = velFX.xAsInt() * velFX.xAsInt() + velFX.yAsInt() * velFX.yAsInt();
                        
                        //int carVelocitySqr = 0;
                        //leftwheel.applyTorque(FXUtil.toFX(-40000));
                        //int m = 8;
                        if (carVelocitySqr > 1600000) {
                            speedMultipiler = 1;
                            //m = 0;
                        } else {
                            speedMultipiler = 6;
                            if (carVelocitySqr > 100000) {
                                //m = 0;
                                speedMultipiler = 8;
                                if (carVelocitySqr > 400000) {
                                    speedMultipiler = 6;
                                }
                            }
                        }
                        //carbody.applyMomentum(new FXVector(FXUtil.divideFX(FXUtil.toFX(Mathh.cos(ang - 75) * m), tenFX), FXUtil.divideFX(-FXUtil.toFX(Mathh.sin(ang - 75) * m), tenFX)));
                        //rightwheel.applyTorque(FXUtil.toFX(-40000));
                        //boll.applyTorque(FXUtil.toFX(-40000));

                        int FXSinAngM = 0;
                        int FXCosAngM = 0;

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
                            if (rightContacts) {
                                w.carbody.applyTorque(FXUtil.toFX(-4000));
                                //rightwheel.applyMomentum(new FXVector(FXUtil.divideFX(FXUtil.toFX(Mathh.cos(ang - 15) * speedMultipiler), tenFX * 5), FXUtil.divideFX(-FXUtil.toFX(Mathh.sin(ang - 15) * speedMultipiler), tenFX * 5)));
                                ////leftwheel.applyMomentum(new FXVector(FXUtil.divideFX(FXUtil.toFX(Mathh.cos(ang) * speedMultipiler), tenFX * 5), FXUtil.divideFX(-FXUtil.toFX(Mathh.sin(ang) * speedMultipiler), tenFX * 5)));
                            }
                            //carbody.applyForce(new FXVector(FXUtil.toFX(sin(ang)), FXUtil.toFX(cos(ang))), 100);
                            if (w.getContactsForBody(w.carbody)[0] != null) {
                                w.carbody.applyTorque(FXUtil.toFX(-4000));
                            }
                        }
                    }
                } else {
                    if (motorTdOff < 40) {
                        try {
                            if (w.carbody.angularVelocity2FX() > 0) {
                                w.carbody.applyTorque(FXUtil.toFX(w.carbody.angularVelocity2FX() / 10000));
                            }
                            if (flying == 0) {
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
                                waitingTime.addElement(new Integer(40));
                            }
                        }
                    }
                }                

                w.tick();
                repaint();

                sleep = millis - (System.currentTimeMillis() - start);
                sleep = Math.max(sleep, 0);

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
                            openMenu();
                            //restart();
                        }
                    } else {
                        if (gameoverCountdown > 0) {
                            gameoverCountdown--;
                        } else {
                            gameoverCountdown = 0;
                        }
                    }
                }

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
        if (mnCanvas.debug) {
            //g.setColor(255, 255, 255);
            g.setFont(smallfont);
            //text += " " + speedMultipiler;
            g.drawString(String.valueOf(GraphicsWorld.carX), 0, 0, 0);                  //  debug text
            
            //text = "";
        }
        //g.drawString(String.valueOf(w.carbody.rotationVelocity2FX()), 0, 0, 0);
        if (gameoverCountdown > 1) {
            g.setFont(largefont);
            g.setColor(255, 0, 0);
            g.drawString("!", scW / 2, scH / 3 + largefont.getHeight() / 2, Graphics.HCENTER | Graphics.TOP);
            if (!mnCanvas.debug) {
                g.setColor(255, 255, 255);
            }
            for (int i = 1; i < gameoverCountdown; i++) {
                g.fillRect(0, 0, scW, scH*i/7/2 + 1);
                g.fillRect(0, scH - scH*i/7/2, scW, scH - 1);
            }
        }
        if (paused & !worldgen.resettingPosition) {
            g.setColor(0, 0, 255);
            int d = 6 * scH / 240;
            for (int i = 0; i <= scH; i++) {
                //g.drawLine(0, i * d - 1, w, -w + i*d - 1);
                //g.drawLine(0, i * d, w, -w + i*d);
                //g.drawLine(0, -w + i*d, w, i * d);
                g.drawLine(scW / 2, 0, d * i, scH);
            }
            g.setFont(largefont);
            g.setColor(255, 255, 255);
            g.drawString("PAUSED", scW / 2, scH / 3 + largefont.getHeight() / 2, Graphics.HCENTER | Graphics.TOP);
        }
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
    }

    protected void keyReleased(int keyCode) {
        int gameAction = getGameAction(keyCode);
        accel = false;
        r = false;
    }

    protected void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        //text = "Last key: " + gameAction + " " + keyCode;
        if (gameAction == KEY_POUND | gameAction == GAME_D) {
            openMenu();
            r = true;
        } else {
            if (gameAction == KEY_STAR | gameAction == GAME_B) {
            openMenu();
            r = true;
            } else {
                accel = true;
            }
        }

        if (keyCode == KEY_ACTION_RIGHT) {
            accel = false;
            if (!paused) {
                hideNotify();
                repaint();
            } else {
                paused = false;
                showNotify();
            }
        }

    }

    public void openMenu() {
        firstStart = false;
        mnCanvas.wg = false;
        worldgen.stop();
        stopped = true;
        Main.set(new mnCanvas());
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
        accel = false;
    }

    public void restart() {
        gameoverCountdown = 0;
        if (worldgen != null) {
            worldgen.stop();
        }
        worldgen = new WorldGen(w);
        if (mnCanvas.wg) {
            worldgen.start();
            worldgen.resetToQue();
        } else {
            w.addCar();
        }
    }

}
