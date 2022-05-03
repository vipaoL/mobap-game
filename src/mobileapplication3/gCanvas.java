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
    static Shape carbodyShape;
    static Shape wheelShape;
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
    int motorTdOff = 50;
    //String text = "text";
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
    boolean leftContacts = false;
    boolean rightContacts = false;
    static boolean paused = false;
    WorldGen worldgen = new WorldGen();;
    int speedMultipiler = 1;
    boolean pauseTouched = false;
    boolean menuTouched = false;

    public gCanvas() {
        setFullScreenMode(true);
        scW = getWidth();
        scH = getHeight();
    }
    public static GraphicsWorld world;

    public void setWorld(GraphicsWorld world) {
        Main.print("gamecanvas:setWorld()");
        this.world = world;
        l = world.getLandscape();
        
        if (mnCanvas.wg) {
            worldgen = new WorldGen();
            worldgen.start();
        }
        restart();
        (new Thread(this, "game canvas")).start();
    }
    
    static void addCar() {
        int x = 0;
        if (mnCanvas.wg) {
            x = -8000;
        }
        addCar(x, -400, FXUtil.TWO_PI_2FX/360*30, null);
    }

    static void addCar(int spawnX, int spawnY, int ang2FX, Object[] vel) {
        int carbodyLength = 240;
        int carbodyHeight = 40;
        int wheelRadius = 40;

        carbodyShape = Shape.createRectangle(carbodyLength, carbodyHeight);
        carbodyShape.setMass(1);
        carbodyShape.setFriction(0);
        carbodyShape.setElasticity(0);
        carbodyShape.correctCentroid();
        carbody = new Body(spawnX, spawnY, carbodyShape, true);
        carbody.setRotation2FX(ang2FX);
        
        long longAng2FX = ang2FX;
        int ang = (int) (longAng2FX * 360 / FXUtil.TWO_PI_2FX) + 1;

        wheelShape = Shape.createCircle(wheelRadius);
        wheelShape.setElasticity(0);
        wheelShape.setFriction(100);
        wheelShape.setMass(1);
        wheelShape.correctCentroid();
        int lwX = spawnX - (carbodyLength / 2 - wheelRadius)*Mathh.cos(ang) / 1000;
        int lwY = spawnY + wheelRadius / 2 - (carbodyLength / 2 - wheelRadius) * Mathh.sin(ang) / 1000;
        int rwX = spawnX + (carbodyLength / 2 - wheelRadius)*Mathh.cos(ang) / 1000;
        int rwY = spawnY + wheelRadius / 2 + (carbodyLength / 2 - wheelRadius) * Mathh.sin(ang) / 1000;
        leftwheel = new Body(lwX, lwY, wheelShape, true);
        rightwheel = new Body(rwX, rwY, wheelShape, true);
        
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
        
        if (vel != null) {
            FXVector velFX = (FXVector) vel[0];
            int rVel2FX = ((Integer) vel[1]).intValue();
            carbody.angularVelocity2FX(rVel2FX);
        }
    }

    protected void showNotify() {
        scW = getWidth();
        scH = getHeight();
        Main.sWidth = scW;
        Main.sHeight = scH;
        world.refreshScreenParameters();
        stopped = false;
    }

    protected void hideNotify() {
        paused = true;
    }

    public synchronized void end() {
        stopped = true;
    }

    public void run() {
        long sleep = 0;
        long start = 0;
        int tick = 0;
        int tenFX = FXUtil.toFX(10);
        gameoverCountdown = 0;
        Contact[][] contacts = new Contact[3][];
        //world.setTimestepFX(FXUtil.toFX(16));
        while(false & world == null) {
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
                    contacts[0] = world.getContactsForBody(leftwheel);
                    contacts[1] = world.getContactsForBody(rightwheel);
                    contacts[2] = world.getContactsForBody(carbody);
                    ang = 360 - FXUtil.angleInDegrees2FX(carbody.rotation2FX());
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


                        /*if (leftContacts & rightContacts) {
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
                        }*/
                    }
                } else {
                    if (motorTdOff < 40) {
                        try {
                            if (carbody.angularVelocity2FX() > 0) {
                                carbody.applyTorque(FXUtil.toFX(carbody.angularVelocity2FX() / 10000));
                            }
                            if (flying == 0) {
                                carbody.applyMomentum(new FXVector(-carbody.velocityFX().xFX/8, -carbody.velocityFX().yFX/8));
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

                world.tick();
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
                    if (GraphicsWorld.carY > 2000 + worldgen.getLowestY() | (ang > 140 & ang < 220 & carbody.getContacts()[0] != null)) {
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
        }
    }

    protected void paint(Graphics g) {
        world.draw(g);
        if (mnCanvas.debug) {
            //g.setColor(255, 255, 255);
            g.setFont(smallfont);
            //text += " " + speedMultipiler;
            g.drawString(String.valueOf(GraphicsWorld.carX), 0, 0, 0);                  //  debug text
            //text = "";
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
        if (mnCanvas.wg) {
            worldgen.resetToQue();
        } else {
            addCar();
        }
    }

}
