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
    int ang = 0;
    private final int millis = 50;
    //private World world;
    //private Thread thread;
    public boolean stopped = false;
    public boolean accel = false;
    static Shape boxRectangle;
    static Shape ball;
    //Shape ball2 = Shape.createCircle(100);
    //Shape centroidCorrector = Shape.createCircle(1);
    public static Body carbody;
    public static Body leftwheel;
    public static Body rightwheel;
    //public Body centrCor = new Body(0, -390, centroidCorrector, true);
    //public Body boll = new Body(300, -200, ball2, true);
    Vector waitingForDynamic = new Vector();
    Vector waitingTime = new Vector();
    static int flying = 0;
    int motorTdOff = 0;
    String text = "text";
    UserData orig;
    int prevX = 0;
    //Motor leftmotor;
    //Motor rightmotor;
    //Motor carbodymotor;
    public static Landscape l;
    int gameoverCountdown = 0;
    Font smallfont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    Font mediumfont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    Font largefont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    int scW = 2;
    int scH = 2;
    boolean leftContacts = true;
    boolean rightContacts = true;
    static boolean paused = false;
    WorldGen worldgen = new WorldGen();
    int speedMultipiler = 1;

    public gCanvas() {
        setFullScreenMode(true);
        scW = getWidth();
        scH = getHeight();
        (new Thread(this, "game canvas")).start();
    }
    public static GraphicsWorld world;

    public void setWorld(GraphicsWorld world) {
        Main.print("gamecanvas:setWorld()");

        this.world = world;
        
        l = world.getLandscape();
        if (mnCanvas.wg) {
            worldgen.start();
        }
        restart();

    }
    
    static void addCar() {
        addCar(65, -400, FXUtil.TWO_PI_2FX/360*30/*, FXVector.newVector(0, 0), 0*/);
    }

    static void addCar(int spawnX, int spawnY, int ang2FX/*, FXVector velFX, int rVel2FX*/) {
        int carbodyLength = 240;
        int carbodyHeight = 40;
        int wheelRadius = 40;

        boxRectangle = Shape.createRectangle(carbodyLength, carbodyHeight);
        boxRectangle.setMass(1);
        boxRectangle.setFriction(0);
        boxRectangle.setElasticity(0);
        carbody = new Body(spawnX, spawnY, boxRectangle, true);
        carbody.setRotation2FX(ang2FX);
        //carbody.;
        //carbody.setRotatable(false);
        Main.print("" + carbody.rotation2FX());
        //carbody.setDynamic(false);
        
        long longAng2FX = ang2FX;
        int ang = (int) (longAng2FX * 360 / FXUtil.TWO_PI_2FX) + 1;
        Main.print("" + ang);
        

        ball = Shape.createCircle(wheelRadius);
        ball.setElasticity(0);
        ball.setFriction(100);
        ball.setMass(1);
        //ball.correctCentroid();
        int lwX = spawnX - (carbodyLength / 2 - wheelRadius)*Mathh.cos(ang) / 1000;
        int lwY = spawnY + wheelRadius / 2 - (carbodyLength / 2 - wheelRadius) * Mathh.sin(ang) / 1000;
        int rwX = spawnX + (carbodyLength / 2 - wheelRadius)*Mathh.cos(ang) / 1000;
        int rwY = spawnY + wheelRadius / 2 + (carbodyLength / 2 - wheelRadius) * Mathh.sin(ang) / 1000;
        leftwheel = new Body(lwX, lwY, ball, true);
        rightwheel = new Body(rwX, rwY, ball, true);
        //leftwheel.setDynamic(false);
        //rightwheel.setDynamic(false);
        
        world.removeBody(carbody);
        world.removeBody(leftwheel);
        world.removeBody(leftwheel);
        
        world.addBody(carbody);
        carbody.addCollisionLayer(1);
        
        world.addBody(leftwheel);
        world.addBody(rightwheel);
        leftwheel.addCollisionLayer(1);
        rightwheel.addCollisionLayer(1);

        Joint leftjoint = new Joint(carbody, leftwheel, FXVector.newVector(-carbodyLength / 2 + wheelRadius, wheelRadius*2/3), FXVector.newVector(0, 0), false);
        Joint rightjoint = new Joint(carbody, rightwheel, FXVector.newVector(carbodyLength / 2 - wheelRadius, wheelRadius*2/3), FXVector.newVector(0, 0), false);
        world.addConstraint(leftjoint);
        world.addConstraint(rightjoint);
        
        //leftmotor = new Motor(leftwheel, -80 * FXUtil.ONE_2FX, FXUtil.toFX(5000));
        //rightmotor = new Motor(rightwheel, -80 * FXUtil.ONE_2FX, FXUtil.toFX(5000));
        //carbodymotor = new Motor(carbody, 0 * FXUtil.ONE_2FX, FXUtil.toFX(500));

        //world.addConstraint(leftmotor);
        //world.addConstraint(rightmotor);
        //world.addConstraint(carbodymotor);
    }

    protected void showNotify() {
        stopped = false;
    }

    protected void hideNotify() {
        paused = true;
    }

    public synchronized void end() {
        stopped = true;
    }

    public void run() {
        while (!worldgen.isReady()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        long sleep = 0;
        long start = 0;
        int t = 0;
        int tenFX = FXUtil.toFX(10);
        Runtime rt = Runtime.getRuntime();
        gameoverCountdown = 0;
        Contact[][] contacts = new Contact[3][];
        
        
        //world.setLandscape(l);
        //world.setTimestepFX(FXUtil.toFX(16));
        //Body[] bodies = world.getBodies();
        while (!stopped) {
            if (world != null && !paused) {
                //Main.print("" + carbody.rotation2FX());
                //int a = FXUtil.ONE_2FX;
                text += GraphicsWorld.carX + " " + GraphicsWorld.carY + " ";
                text += rt.freeMemory() + "/" + rt.totalMemory();
                start = System.currentTimeMillis();
                try {
                    contacts[0] = world.getContactsForBody(leftwheel);
                    contacts[1] = world.getContactsForBody(rightwheel);
                    contacts[2] = world.getContactsForBody(carbody);
                } catch (NullPointerException ex) {
                    Main.print("ждём автомобиль");
                    contacts[0] = new Contact[1];
                    contacts[1] = new Contact[1];
                    contacts[2] = new Contact[1];
                    Main.print("contacts[0].length:", contacts[0].length);
                }
                ang = 360 - FXUtil.angleInDegrees2FX(carbody.rotation2FX());

                leftContacts = contacts[0][0] != null;

                rightContacts = contacts[1][0] != null;

                if (!leftContacts & !rightContacts) {
                    flying += 1;
                } else {
                    flying = 0;
                }

                if (accel) {
                    motorTdOff = 0;
                    //carbody.applyTorque(FXUtil.toFX(-5000));
                    if (flying > 2) {
                        carbody.applyTorque(FXUtil.toFX(-2000));
                    } else {
                        FXVector velFX = carbody.velocityFX();
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
                        carbody.applyMomentum(new FXVector(FXCosAngM, -FXSinAngM));


                        if (leftContacts & rightContacts) {
                            //leftwheel.applyMomentum(new FXVector(FXCosAngM, -FXSinAngM));
                            //rightwheel.applyMomentum(new FXVector(FXCosAngM, -FXCosAngM));

                        } else {
                            if (leftContacts) {
                                //leftwheel.applyMomentum(new FXVector(FXCosAngM, -FXSinAngM));
                            }
                            if (rightContacts) {
                                //rightwheel.applyMomentum(new FXVector(FXUtil.divideFX(FXUtil.toFX(Mathh.cos(ang - 15) * speedMultipiler), tenFX * 5), FXUtil.divideFX(-FXUtil.toFX(Mathh.sin(ang - 15) * speedMultipiler), tenFX * 5)));
                                ////leftwheel.applyMomentum(new FXVector(FXUtil.divideFX(FXUtil.toFX(Mathh.cos(ang) * speedMultipiler), tenFX * 5), FXUtil.divideFX(-FXUtil.toFX(Mathh.sin(ang) * speedMultipiler), tenFX * 5)));
                            }
                            //carbody.applyForce(new FXVector(FXUtil.toFX(sin(ang)), FXUtil.toFX(cos(ang))), 100);
                            if (world.getContactsForBody(carbody)[0] != null) {
                                carbody.applyTorque(FXUtil.toFX(-8000));
                            }
                        }
                    }
                } else {
                    //text = "false";
                    //leftmotor.setParameter(0, 0, true, false, false);
                    //rightmotor.setParameter(0, 0, true, false, false);
                    if (motorTdOff < 40) {
                        if (carbody.angularVelocity2FX() > 0) {
                            carbody.applyTorque(FXUtil.toFX(carbody.angularVelocity2FX() / 10000));
                        }
                        if (flying == 0) {
                            carbody.applyMomentum(new FXVector(-carbody.velocityFX().xFX/8, -carbody.velocityFX().yFX/8));
                        }
                        motorTdOff++;
                    }
                }

                waiting = waitingForDynamic.size();
                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < contacts[j].length; i++) {
                        //contacts[0].body1().setDynamic(true);
                        if (contacts[j][i] != null) {
                            //contacts[i].body1().setDynamic(true);
                            Body body = contacts[j][i].body1();
                            //contacts[i].body1().setDynamic(true);
                            if (!waitingForDynamic.contains(body)) {
                                waitingForDynamic.addElement(body);
                                waitingTime.addElement(new Integer(40));
                            }
                        }
                    }
                }                

                world.tick();
                repaint();

                sleep = millis - (System.currentTimeMillis() - start);
                sleep = Math.max(sleep, 0);

                if (t < 5) {
                    t++;
                } else {
                    t = 1;
                    for (int i = 0; i < waitingForDynamic.size(); i++) {
                        if (Integer.parseInt(String.valueOf(waitingTime.elementAt(i))) > 0) {
                            waitingTime.setElementAt(new Integer(Integer.parseInt(String.valueOf(waitingTime.elementAt(i))) - 10), i);
                        } else {
                            ((Body) waitingForDynamic.elementAt(i)).setDynamic(true);
                            waitingForDynamic.removeElementAt(i);
                            waitingTime.removeElementAt(i);
                        }
                    }
                    if (GraphicsWorld.carY > 2000 + worldgen.getLowestY() | (ang > 140 & ang < 220 & carbody.getContacts()[0] != null)) {
                        if (gameoverCountdown < 8) {
                            gameoverCountdown++;
                        } else {
                            restart();
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
        }
    }

    protected void paint(Graphics g) {
        world.draw(g);
        if (mnCanvas.debug) {
            //g.setColor(255, 255, 255);
            g.setFont(smallfont);
            //text += "b:" + world.getBodyCount();
            //text += "s:" + l.segmentCount();
            text += " " + speedMultipiler;
            g.drawString(text, 0, 0, 0);                  //  debug text
            text = "";
        }
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
        mnCanvas.wg = false;
        worldgen.stop();
        stopped = true;
        Main.set(new mnCanvas());
    }

    protected void pointerPressed(int x, int y) {
        if (x > scW * 2 / 3 & y < scH / 6) {
            if (!paused) {
                hideNotify();
                //paused = true;
                repaint();
            } else {
                stopped = false;
                paused = false;
                showNotify();
            }
        } else if (x < scW / 3 & y < scH / 6) {
            stopped = true;
            openMenu();
        } else {
            accel = true;
        }
    }

    protected void pointerReleased(int x, int y) {
        accel = false;
    }

    public void restart() {
        gameoverCountdown = 0;
        if (mnCanvas.wg) {
            worldgen.resetToQue();
        } else {
            addCar();
        }
    }

}
