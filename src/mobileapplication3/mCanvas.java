/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.*;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author steamuser
 */
public class mCanvas extends Canvas implements Runnable {
    
    int KEY_ACTION_RIGHT = -7;
    int KEY_ACTION_LEFT = -6;
    
    boolean r = false;

    int waiting = 0;
    int ang = 0;
    private static final int millis = 50;
    //private World world;
    private Thread thread;
    private boolean stopped = false;
    public static boolean accel = false;
    static Shape boxRectangle = Shape.createRectangle(100, 30);
    Shape ball = Shape.createCircle(20);
    Shape ball2 = Shape.createCircle(100);
    Shape centroidCorrector = Shape.createCircle(1);
    public static Body carbody = new Body(0, -405, boxRectangle, true);
    public Body leftwheel = new Body(-45, -390, ball, true);
    public Body rightwheel = new Body(45, -390, ball, true);
    public Body centrCor = new Body(0, -390, centroidCorrector, true);
    public Body boll = new Body(300, -200, ball2, true);
    Vector waitingForDynamic = new Vector();
    Vector waitingTime = new Vector();
    int flying = 0;
    int motorTdOff = 0;
    GraphicsWorld worldsave;
    GraphicsWorld worldOrig;
    static String text = "";
    UserData orig;

    public mCanvas() {
        setFullScreenMode(true);
    }
    private GraphicsWorld world;

    public void setWorld(GraphicsWorld werld) {
        
        world = werld;
        orig = world.getUserData();
        if (world == null) worldOrig = world;
        if (worldsave == null) worldsave = world;

        ball.setElasticity(0);
        ball.setFriction(0);
        ball.setMass(10);
        boxRectangle.setMass(30);
        centroidCorrector.setMass(80);
        ball.correctCentroid();

        world.addBody(carbody);
        world.addBody(leftwheel);
        world.addBody(rightwheel);
        world.addBody(centrCor);
        world.addBody(boll);
        carbody.addCollisionLayer(1);
        leftwheel.addCollisionLayer(1);
        rightwheel.addCollisionLayer(1);
        centrCor.addCollisionLayer(1);
        
        Spring leftSpring = new Spring(carbody, leftwheel, FXVector.newVector(-45, 20), FXVector.newVector(0, 0), 0);
        Spring leftSpring2 = new Spring(carbody, leftwheel, FXVector.newVector(-45, 20), FXVector.newVector(0, 0), 0);
        Spring rightSpring = new Spring(carbody, rightwheel, FXVector.newVector(45, 20), FXVector.newVector(0, 0), 0);
        Spring rightSpring2 = new Spring(carbody, rightwheel, FXVector.newVector(45, 20), FXVector.newVector(0, 0), 0);
        leftSpring.setCoefficient(0);
        leftSpring2.setCoefficient(0);
        rightSpring2.setCoefficient(0);
        rightSpring.setCoefficient(0);

        Joint leftjoint = new Joint(carbody, leftwheel, FXVector.newVector(-45, 20), FXVector.newVector(0, 0), false);
        Joint rightjoint = new Joint(carbody, rightwheel, FXVector.newVector(45, 20), FXVector.newVector(0, 0), false);
        Joint centrcorjoint = new Joint(carbody, centrCor, FXVector.newVector(0, 20), FXVector.newVector(0, 0), true);
        world.addConstraint(leftjoint);
        world.addConstraint(rightjoint);
//        world.addConstraint(leftSpring);
//        world.addConstraint(leftSpring2);
//        world.addConstraint(rightSpring);
//        world.addConstraint(rightSpring2);
        world.addConstraint(centrcorjoint);
    }

    protected void showNotify() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
        stopped = false;
    }

    protected void hideNotify() {
        //world = null;
        stopped = true;
    }

    public synchronized void end() {
        stopped = true;
    }

    public void run() {
        long sleep = 0;
        long start = 0;
        int tim = 0;
        //Body[] bodies = world.getBodies();
        while (true)
        if (world != null && !stopped) {
            start = System.currentTimeMillis();
            
            Contact[] contacts = world.getContactsForBody(leftwheel);
            ang = 360 - FXUtil.angleInDegrees2FX(carbody.rotation2FX());
            
            boolean leftContacts;
            boolean rightContacts;
            
            if (contacts[0] != null) {
                leftContacts = true;
            } else {
                leftContacts = false;
            }
            
            if (world.getContactsForBody(rightwheel)[0] != null) {
                rightContacts = true;
            } else {
                rightContacts = false;
            }

            if (!leftContacts & !rightContacts) {
                flying += 1;
            } else {
                flying = 0;
            }

            if (accel) {
                motorTdOff = 0;
                //carbody.applyTorque(FXUtil.toFX(-5000));
                if (flying > 10) {
                    carbody.applyTorque(FXUtil.toFX(-50000));
                } else {
                    FXVector velFX = carbody.velocityFX();
                    int carVelocitySqr = velFX.xAsInt() * velFX.xAsInt() + velFX.yAsInt() * velFX.yAsInt();
                    //int carVelocitySqr = 0;
                    //leftwheel.applyTorque(FXUtil.toFX(-40000));
                    int speedMultipiler;
                    if (carVelocitySqr > 400000) {
                        speedMultipiler = 0;
                    } else {
                        speedMultipiler = 1;
                        if (carVelocitySqr > 50000) {
                            speedMultipiler = 2;
                            if (carVelocitySqr > 200000) {
                                speedMultipiler = 3;
                            }
                        }
                    }
                    //rightwheel.applyTorque(FXUtil.toFX(-40000));
                    //boll.applyTorque(FXUtil.toFX(-40000));
                    
                    int FXSinAngM = 0;
                    int FXCosAngM = 0;
                    
                    if (leftContacts) {
                        FXSinAngM = FXUtil.toFX(sin(ang - 15)*speedMultipiler);
                        FXCosAngM = FXUtil.toFX(cos(ang - 15)*speedMultipiler);
                    }
                    
                    if (leftContacts & rightContacts) {
                        leftwheel.applyMomentum(new FXVector(FXCosAngM, -FXSinAngM));
                        rightwheel.applyMomentum(new FXVector(FXCosAngM, -FXCosAngM));
                        
                    } else {
                        if (leftContacts) {
                            leftwheel.applyMomentum(new FXVector(FXCosAngM, -FXSinAngM));
                        }
                        if (rightContacts) {
                            rightwheel.applyMomentum(new FXVector(FXUtil.toFX(cos(ang+75)*speedMultipiler), -FXUtil.toFX(sin(ang+75)*speedMultipiler)));
                            leftwheel.applyMomentum(new FXVector(FXUtil.toFX(cos(ang - 30)*speedMultipiler), -FXUtil.toFX(sin(ang - 30)*speedMultipiler)));
                        }
                        //carbody.applyForce(new FXVector(FXUtil.toFX(sin(ang)), FXUtil.toFX(cos(ang))), 100);
                        if (world.getContactsForBody(carbody)[0] != null) {
                            carbody.applyTorque(FXUtil.toFX(-10000));
                        }
                    }
                }
            } else {
                if (motorTdOff < 40) {
                    if (carbody.angularVelocity2FX() > 0) {
                        carbody.applyTorque(FXUtil.toFX(carbody.angularVelocity2FX() / 1000));
                    }
                    motorTdOff++;
                }
            }

            waiting = waitingForDynamic.size();
            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < contacts.length; i++) {
                    //contacts[0].body1().setDynamic(true);
                    if (contacts[i] != null) {
                        //contacts[i].body1().setDynamic(true);
                        Body body = contacts[i].body1();
                        //contacts[i].body1().setDynamic(true);
                        if (!waitingForDynamic.contains(body)) {
                            waitingForDynamic.addElement(body);
                            waitingTime.addElement(new Integer(20));
                        }
                    }
                }
                if (j == 1) {
                    contacts = world.getContactsForBody(rightwheel);
                } else {
                    contacts = world.getContactsForBody(carbody);
                }
            }
            
            for (int i = 0; i < waitingForDynamic.size(); i++) {
                if (Integer.parseInt(String.valueOf(waitingTime.elementAt(i))) < 0) {
                    Body body = (Body) waitingForDynamic.elementAt(i);
                    body.setDynamic(true);
                    waitingForDynamic.removeElementAt(i);
                    waitingTime.removeElementAt(i);
                } else {
                    waitingTime.setElementAt(new Integer(Integer.parseInt(String.valueOf(waitingTime.elementAt(i))) - 4), i);
                }
            }

            world.tick();
            repaint();

            sleep = millis - (System.currentTimeMillis() - start);
            sleep = Math.max(sleep, 0);

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (r == true) restart();
        }
    }

    protected void paint(Graphics g) {
        world.draw(g);
        if (mnCanvas.debug) {
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
            GraphicsWorld.setText(text);                  //  debug text
        }
        if (stopped) {
            g.setColor(64, 0, 0);
            int hn = Main.sHeight;
            int w = Main.sWidth;
            int d = 6 * w / 240;
            for (int i = 0; i <= hn; i++) {
                g.drawLine(0, i * d - 1, w, -w + i*d - 1);
                g.drawLine(0, i * d, w, -w + i*d);
                g.drawLine(0, i * d + 1, w, -w + i*d - 1);
                //g.drawLine(0, -w + i*d, w, i * d);
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
        if  (gameAction == KEY_POUND | gameAction == GAME_D) {
            restart();
            r = true;
        } else {
            accel = true;
        }

        if (keyCode == KEY_ACTION_RIGHT) {
            accel = false;
            if (!stopped) {
                hideNotify();
                repaint();
            }
            else showNotify();
        }

    }
    
    
    
    
    
    
    
    
    
    
    

    public void restart() {
        /*hideNotify();
        world = worldOrig;
        setWorld(world);
        showNotify();
        
        world.setUserData(null);


        stopped = true;
        world = null;
        thread = null;
        
        PhysicsFileReader reader = new PhysicsFileReader("/rsc/game_world_test.phy");
        GraphicsWorld gameWorld = new GraphicsWorld(World.loadWorld(reader));
        mCanvas gameCanvas = new mCanvas();
        gameCanvas.setWorld(gameWorld);
        Main.set(gameCanvas); */
        thread = null;
        world = null;
        Main.set(new mnCanvas());
        
        
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
