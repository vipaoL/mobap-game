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
 * @author vipaol
 */
public class GameplayCanvas extends Canvas implements Runnable {
    
    String[] menuhint = {"MENU:", "here(touch),", "D, #"};
    String[] pausehint = {"PAUSE:", "here(touch), *,", "B, right soft"};
    int hintTime = 120; // in ticks
    public static int gameSpeedMultiplier = 2;
    int forceMultiplier = gameSpeedMultiplier*gameSpeedMultiplier;
    
    public static final short EFFECT_SPEED = 0;
    
    // to prevent siemens' bug which calls hideNotify right after showing canvas
    private static final int PAUSE_DELAY = 5;
    private int pauseDelay = PAUSE_DELAY;
    private boolean previousPauseState = false;
    
    // state and mode
    static boolean isFirstStart = true; // for displaying hints only on first start
    public static boolean isDrawingNow = true;
    public static boolean uninterestingDebug = false;
    boolean isWorldLoaded = false;
    
    static boolean paused = false;
    static boolean stopped = false;
    public static boolean lock = false;
    
    // screen
    int scW, scH, maxScSide;
    
    // car
    boolean leftWheelContacts = false;
    boolean rightWheelContacts = false;
    int carVelocitySqr, speedMultipiler;
    int carAngle = 0;
    
    // indicators
    static int flipIndicator = 255; // for blinking counter when flip done
    int loadingProgress = 0;
    int speedoState = 0;
    
    // touchscreen
    int pointerX = 0, pointerY = 0;
    boolean pauseTouched = false;
    boolean menuTouched = false;
    // motor state
    boolean accel = false;
    
    // list of all bodies car touched (for falling platforms)
    Vector waitingForDynamic = new Vector();
    Vector waitingTime = new Vector();
    
    // counters
    int gameoverCountdown;
    static int timeFlying = 0;
    int timeMotorTurnedOff = 50;
    
    static short[][] currentEffects = new short[1][];
    
    // fonts
    Font smallfont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    int sFontH = smallfont.getHeight();
    //Font mediumfont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    //int mFontH = mediumfont.getHeight();
    Font largefont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    int lFontH = largefont.getHeight();
    Font currentFont = largefont;
    int currentFontH = currentFont.getHeight();
    
    // some constants
    int KEY_SOFT_RIGHT = -7;
    int KEY_SOFT_LEFT = -6;
    int TEN_FX = FXUtil.toFX(10);
    
    GraphicsWorld world;
    WorldGen worldgen;

    public GameplayCanvas() {
        log("gcanvas constructor");
        setLoadingProgress(15);
        setFullScreenMode(true);
        scW = getWidth();
        scH = getHeight();
        if (Math.max(scW, scH) >= GraphicsWorld.BIGSCREEN_SIDE) {
            GraphicsWorld.bigScreen = true;
        }
        currentEffects = new short[1][];
        repaint();
        log("gcanvas:starting thread");
        (new Thread(this, "game canvas")).start();
    }

    public void setWorld(GraphicsWorld w) {
        stopped = false;
        log("gamecanvas:setWorld()");
        this.world = w;
        this.world.setGravity(FXVector.newVector(0, 250 * gameSpeedMultiplier));
        reset();
        isWorldLoaded = true;
    }
    
    public void setDefaultWorld() {
        Main.log("gCanv:reading world");
        PhysicsFileReader reader = new PhysicsFileReader("/void.phy");
        setLoadingProgress(25);
        
        Main.log("gCanv:loading world");
        World w = World.loadWorld(reader);
        setLoadingProgress(30);

        Main.log("gCanv:new grWorld");
        // there's siemens c65 stucks if obfucsation is enabled
        GraphicsWorld grWorld = new GraphicsWorld(w);
        setLoadingProgress(50);

        Main.log("gCanv:setting world");
        setWorld(grWorld);
        setLoadingProgress(75);

        Main.log("gCanv:closing reader");
        reader.close();
    }

    // game thread with main cycle and preparing
    public void run() {
        log("gcanvas:thread started");
        
        long sleep = 0;
        long start = 0;
        int tick = 0;
        Contact[][] contacts = new Contact[3][];
        
        // while world is loading, wait and draw loading screen
        while (!isWorldLoaded) {
            repaint();
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        log("thread:world loaded");
        
        // tell screen size to world when it loaded
        world.refreshScreenParameters();
        // init music player if enabled
        if (DebugMenu.isDebugEnabled & DebugMenu.music) {
            log("Starting sound");
            Sound sound = new Sound();
            sound.startBgMusic();
        }
        
        // wait for stopping previous game instance if it still running
        setLoadingProgress(80);
        while (lock) {            
            stopped = true;
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        lock = true;
        
        setLoadingProgress(85);
        // continue updating loading screen until worldgen is loaded
        log("thread:waiting for wg");
        while(!worldgen.isReady()) {
            repaint();
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        setLoadingProgress(100);
        
        log("thread:starting game cycle");
        
        // Main game cycle
        while (!stopped) {
            // catch screen rotation
            if (scW != getWidth()) {
                showNotify();
                world.refreshScreenParameters();
            }
            
            if (!paused && worldgen.isReady()) {
                isDrawingNow = true;
                start = System.currentTimeMillis();
                // chech if car contacts with the ground or sth else
                contacts[0] = world.getContactsForBody(world.leftwheel);
                contacts[1] = world.getContactsForBody(world.rightwheel);
                contacts[2] = world.getContactsForBody(world.carbody);
                leftWheelContacts = contacts[0][0] != null;
                rightWheelContacts = contacts[1][0] != null;
                if ((!leftWheelContacts & !rightWheelContacts)) {
                    timeFlying += 1;
                } else {
                    timeFlying = 0;
                }
                
                // set motor power according to car speed
                // (fast start and saving limited speed)
                FXVector velFX = world.carbody.velocityFX();
                int vX = velFX.xAsInt() / gameSpeedMultiplier;
                int vY = velFX.yAsInt() / gameSpeedMultiplier;
                if (currentEffects[EFFECT_SPEED] != null) {
                    if (currentEffects[EFFECT_SPEED][0] > 0) {
                        vX = vX * 100 / currentEffects[EFFECT_SPEED][2];
                        vY = vY * 100 / currentEffects[EFFECT_SPEED][2];
                    }
                }
                carVelocitySqr = vX * vX + vY * vY;
                if (carVelocitySqr > 1000000) {
                    speedMultipiler = 2;
                    speedoState = 2;
                } else if (carVelocitySqr > 100000) {
                    speedoState = 1;
                    speedMultipiler = 15;
                } else {
                    speedMultipiler = 20;
                    speedoState = 0;
                }
                if (uninterestingDebug) {
                    timeFlying = 0;
                    speedMultipiler = 30;
                }
                
                speedMultipiler *= forceMultiplier;
                
                // getting car angle
                carAngle = 360 - FXUtil.angleInDegrees2FX(world.carbody.rotation2FX());

                // when the motor is turned on
                if (accel) {
                    timeMotorTurnedOff = 0;
                    if (timeFlying > 2) {
                        // apply rotational force
                        if (world.carbody.rotationVelocity2FX() > 50000000) {
                            world.carbody.applyTorque(FXUtil.toFX(-world.carbody.rotationVelocity2FX()*forceMultiplier/16000));
                        } else {
                            world.carbody.applyTorque(FXUtil.toFX(-10000*forceMultiplier));
                        }
                    } else {
                        // apply motor force when on the ground
                        int directionOffset = 0;
                        if (currentEffects[EFFECT_SPEED] != null) {
                            if (currentEffects[EFFECT_SPEED][0] > 0) {
                                directionOffset = currentEffects[EFFECT_SPEED][1];
                                speedMultipiler = speedMultipiler * currentEffects[EFFECT_SPEED][2] / 100;
                                Main.log(speedMultipiler);
                            }
                        }
                        int motorForceX = FXUtil.divideFX(FXUtil.toFX(Mathh.cos(carAngle - 15 + directionOffset) * speedMultipiler), TEN_FX * 5);
                        int motorForceY = FXUtil.divideFX(FXUtil.toFX(Mathh.sin(carAngle - 15 + directionOffset) * speedMultipiler), TEN_FX * 5);
                        world.carbody.applyMomentum(new FXVector(motorForceX, -motorForceY));
                        if ((!leftWheelContacts & world.getContactsForBody(world.carbody)[0] != null) | rightWheelContacts) {
                            world.carbody.applyTorque(FXUtil.toFX(-6000*forceMultiplier));
                        }
                    }
                } else {
                    // brake for two seconds after motor turning off
                    if (timeMotorTurnedOff < 40 & !uninterestingDebug) {
                        try {
                            if (world.carbody.angularVelocity2FX() > 0) {
                                world.carbody.applyTorque(FXUtil.toFX(world.carbody.angularVelocity2FX() * gameSpeedMultiplier / 4000));
                            }
                            if (timeFlying < 2) {
                                world.carbody.applyMomentum(new FXVector(-world.carbody.velocityFX().xFX*gameSpeedMultiplier/5, -world.carbody.velocityFX().yFX*gameSpeedMultiplier/5));
                            }
                            timeMotorTurnedOff++;
                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                
                // adding timer on each body car touched
                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < contacts[j].length; i++) {
                        if (contacts[j][i] != null) {
                            Body body = contacts[j][i].body1();
                            // as default value, will be overwritten if available
                            int bodyType = MUserData.TYPE_FALLING_PLATFORM;
                            MUserData bodyUserData = null;
                            try {
                                bodyUserData = (MUserData) body.getUserData();
                                bodyType = bodyUserData.bodyType;
                            } catch (ClassCastException ex) {
                                
                            } catch (NullPointerException ex) {
                                
                            }
                            if (bodyType == MUserData.TYPE_FALLING_PLATFORM) {
                                if (!waitingForDynamic.contains(body) & body != world.carbody & body != world.leftwheel & body != world.rightwheel) {
                                    waitingForDynamic.addElement(body);
                                    waitingTime.addElement(new Integer(20));
                                    if (uninterestingDebug) world.removeBody(body);
                                }
                            } else if (bodyType == MUserData.TYPE_ACCELERATOR) {
                                giveEffect(bodyUserData.data);
                                GraphicsWorld.currColWheel = bodyUserData.color;
                            } else {
                                log("unknown bodyType:" + bodyType);
                            }
                        }
                    }
                }
                
                for (int i = 0; i < currentEffects.length; i++) {
                    if (currentEffects[i] != null) {
                        if (currentEffects[i][0] > 0) {
                            currentEffects[i][0]--;
                            log("effect" + i + "," + currentEffects[i][0] + " ticks left");
                        } else if (currentEffects[i][0] == 0) {
                            currentEffects[i] = null;
                        }
                    }
                }
                
                if (pauseDelay > 0)
                    pauseDelay--;

                if (tick < 5) {
                    tick++;
                } else {
                    tick = 1;
                    // ticking timers on each body car touched and set it as dynamic
                    // for falling platforms
                    for (int i = 0; i < waitingForDynamic.size(); i++) {
                        if (Integer.parseInt(String.valueOf(waitingTime.elementAt(i))) > 0) {
                            waitingTime.setElementAt(new Integer(Integer.parseInt(String.valueOf(waitingTime.elementAt(i))) - 10), i);
                        } else {
                            ((Body) waitingForDynamic.elementAt(i)).setDynamic(true);
                            waitingForDynamic.removeElementAt(i);
                            waitingTime.removeElementAt(i);
                        }
                    }
                    // start the final countdown and open main menu if the car
                    // lies upside down or fell out of the world
                    if (GraphicsWorld.carY > 2000 + worldgen.getLowestY() | (carAngle > 140 & carAngle < 220 & world.carbody.getContacts()[0] != null)) {
                        if (gameoverCountdown < 8) {
                            gameoverCountdown++;
                        } else {
                            if (uninterestingDebug) {
                                reset();
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
                    
                    // removing all that fell out the world or got too left
                    // TODO: move to WorldGen.java
                    for (int i = 0; i < world.getBodyCount(); i++) {
                        Body[] bodies = world.getBodies();
                        if (bodies[i].positionFX().yAsInt() > 20000 + worldgen.getLowestY() | world.carbody.positionFX().xAsInt() - bodies[i].positionFX().xAsInt() > GraphicsWorld.viewField * 2) {
                            world.removeBody(bodies[i]);
                        }
                    }
                }
                
                world.tick();
                repaint();
                
                isDrawingNow = false;

                sleep = Main.TICK_DURATION - (System.currentTimeMillis() - start);
                sleep = Math.max(sleep, 0);
            } else {
                // if paused
                if (paused) {
                    sleep = 200;
                } else {
                    sleep = 5;
                }
            }
            // fps/tps control
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        lock = false;
    }

    protected void paint(Graphics g) {
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, maxScSide, maxScSide);
        if (isWorldLoaded) {
            world.drawWorld(g);
        } else {
            drawLoading(g);
        }
        drawHUD(g);
    }
    
    // point counter, very beautiful pause menu,
    // debug info, on-screen log, game over screen
    private void drawHUD(Graphics g) {
        // show hint on first start
        if (isFirstStart & hintTime > 0) {
            int color = 255 * hintTime / 120;
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
                hintTime--;
            }
        }
        
        // draw some debug info if debug is enabled
        if (DebugMenu.isDebugEnabled) {
            int debugTextOffset = 0;
            setFont(smallfont, g);
            // speedometer
            if (DebugMenu.speedo) {
                switch (speedoState) {
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
                g.fillRect(0, debugTextOffset, currentFontH * 5, currentFontH);
                g.setColor(255, 255, 255);
                g.drawString(String.valueOf(carVelocitySqr), 0, debugTextOffset, 0);
                debugTextOffset += currentFontH;
            }
            // show coordinates of car
            if (DebugMenu.coordinates) {
                g.setColor(255, 255, 255);
                g.drawString(GraphicsWorld.carX + " " + GraphicsWorld.carY, 0, debugTextOffset, 0); 
                debugTextOffset += currentFontH;
            }
            // car angle
            if (DebugMenu.showAngle) {
                if (timeFlying > 0) {
                    g.setColor(0, 0, 255);
                } else {
                    g.setColor(255, 255, 255);
                }
                g.drawString(String.valueOf(FXUtil.angleInDegrees2FX(world.carbody.rotation2FX())), 0, debugTextOffset, 0);
                debugTextOffset += currentFontH;
            }
        }
        // game over screen
        if (gameoverCountdown > 1) {
            g.setFont(largefont);
            g.setColor(255, 0, 0);
            g.drawString("!", scW / 2, scH / 3 + currentFontH / 2, Graphics.HCENTER | Graphics.TOP);
            if (!DebugMenu.isDebugEnabled) {
                g.setColor(191, 191, 191);
            }
            for (int i = 1; i < gameoverCountdown; i++) {
                g.fillRect(0, 0, scW, scH*i/7/2 + 1);
                g.fillRect(0, scH - scH*i/7/2, scW, scH - 1);
            }
        }
        
        // draw on-screen log if enabled
        if (Main.isScreenLogEnabled) {
            g.setColor(150, 255, 150);
            setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL), g);
            for (int i = 0; i < Main.onScreenLog.length; i++) {
                try {
                    g.drawString(Main.onScreenLog[i], 0, currentFontH * i, Graphics.TOP | Graphics.LEFT);
                } catch (NullPointerException ex) {
                    
                } catch (IllegalArgumentException ex) {
                    
                }
            }
        }
        
        if (!isWorldLoaded) {
            return; // stop drawing GUI, if
        } // world and woldgen are not loaded yet, else
        // we'll get errors executing code below
        
        // score counter
        if (MenuCanvas.isWorldgenEnabled) {
            g.setColor(flipIndicator, flipIndicator, 255);
            setFont(largefont, g);
            g.drawString(String.valueOf(world.points), world.halfScWidth, world.scHeight - currentFontH * 3 / 2,
                    Graphics.HCENTER | Graphics.TOP);
            
            // coloring when flip
            if (flipIndicator < 255 & !DebugMenu.dontCountFlips) {
                flipIndicator+=64;
                if (flipIndicator >= 255) {
                    flipIndicator = 255;
                }
            }
        }
        
        // draw beautiful(isn't it?) pause screen
        if (paused) {
            int d = 6 * scH / 240;
            // change color if debug enabled
            if (!DebugMenu.isDebugEnabled) {
                g.setColor(0, 0, 255);
            } else {
                g.setColor(0, 255, 0);
            }
            
            for (int i = 0; i <= scH; i++) {
                g.drawLine(scW / 2, 0, d * i, scH);
            }
            setFont(largefont, g);
            g.setColor(255, 255, 255);
            g.drawString("PAUSED", scW / 2, scH / 3 + currentFontH / 2, Graphics.HCENTER | Graphics.TOP);
        }
    }
    
    private void drawLoading(Graphics g) {
        g.setColor(255, 255, 255);
        int l = scW * 2 / 3;
        int h = scH / 24;
        g.drawRect(scW / 2 - l / 2, scH * 2 / 3, l, h);
        g.fillRect(scW / 2 - l / 2, scH * 2 / 3, l*loadingProgress/100, h);
    }
    public void setLoadingProgress(int percents) {
        loadingProgress = percents;
    }
    
    // does it need any comments?
    private void setFont(Font font, Graphics g) {
        g.setFont(font);
        currentFont = font;
        currentFontH = currentFont.getHeight();
    }
    
    // local method for autorefreshing screen after each logging
    private void log(String text) {
        Main.log(text);
        if (Main.isScreenLogEnabled) repaint();
    }
    /*private void log(String text, int value) {
        Main.log(text, value);
        if (Main.isScreenLogEnabled) repaint();
    }
    private void log(int value) {
        Main.log(value);
        if (Main.isScreenLogEnabled) repaint();
    }*/
    
    // blink point counter on flip
    public static void indicateFlip() {
        flipIndicator = 0;
    }
    
    public void giveEffect(short[] data) {
        int id = data[0];
        int dataLength = data.length - 1;
        currentEffects[id] = new short[dataLength];
        for (int i = 1; i < data.length; i++) {
            currentEffects[id][i - 1] = data[i];
        }
    }
    
    public void openMenu() {
        stopped = true;
        isFirstStart = false;
        uninterestingDebug = false;
        MenuCanvas.isWorldgenEnabled = false;
        Main.set(new MenuCanvas());
    }

    // reset some parameters, init worldgen
    public void reset() {
        Main.log("reset");
        gameoverCountdown = 0;
        worldgen = new WorldGen(world);
        Main.log("wg inited, starting");
        if (MenuCanvas.isWorldgenEnabled) {
            worldgen.start();
            Main.log("wg started");
        } else {
            world.addCar();
        }
    }
    
    void resume() {
        paused = false;
        if (worldgen != null) {
            worldgen.resume();
        }
        repaint();
    }

    // also used as pause
    protected void hideNotify() {
        log("hideNotify");
        paused = true;
        if (worldgen != null) {
            worldgen.pause();
        }
        repaint();
        // to prevent siemens' bug that calls hideNotify right after showing canvas
        if (pauseDelay > 0) {
            if (previousPauseState == false) {
                resume();
            }
        }
    }
    
    protected void showNotify() {
        log("showNotify");
        refreshScreenParameters();
        repaint();
        // to prevent siemens' bug that calls hideNotify right after showing canvas
        pauseDelay = PAUSE_DELAY;
        previousPauseState = paused;
    }
    
    private void pauseButtonPressed() {
        if (!paused) {
            pauseDelay = 0;
            hideNotify();
            repaint();
        } else {
            resume();
        }
    }

    // keyboard events
    protected void keyReleased(int keyCode) {
        // turn off motor
        accel = false;
        if (timeFlying > 0) {
            timeFlying = Math.max(5, timeFlying);
        }
    }
    protected void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        // pause
        if (keyCode == KEY_SOFT_RIGHT/* | keyCode == GenericMenu.SIEMENS_KEYCODE_RIGHT_SOFT*/) {
            pauseButtonPressed();
        } else // menu
        if (keyCode == KEY_POUND | gameAction == GAME_D) {
            openMenu();
        } else  // pause too. i'll rework it later
        if ((keyCode == KEY_STAR | gameAction == GAME_B)) {
            pauseButtonPressed();
            // no cheats. only pause
            /*if (DebugMenu.isDebugEnabled & DebugMenu.cheat) {
                FXVector pos = w.carbody.positionFX();
                int carX = pos.xAsInt();
                int carY = pos.yAsInt();
                worldgen.line(carX - 200, carY + 200, carX + 2000, carY + 0);
            }*/
        } else {
            // if not one of action buttons, turn on motor
            accel = true;
        }
    }

    // touchscreen events
    protected void pointerPressed(int x, int y) {
        if (x > scW * 2 / 3 & y < scH / 6) {
            pauseTouched = true;
        } else if (x < scW / 3 & y < scH / 6) {
            menuTouched = true;
        } else {
            // if not on buttons, turn on the motor
            accel = true;
        }
        pointerX = x;
        pointerY = y;
    }
    protected void pointerDragged(int x, int y) {
        if (pauseTouched | menuTouched) {
            if (x - pointerX > 3 | y - pointerY > 3) {
                Main.log((x - pointerX) + "dx/dy" + (y - pointerY));
                Main.log("btnPress cancelled:dragged");
                pauseTouched = false;
                menuTouched = false;
            }
        }
        pointerX = x;
        pointerY = y;
    }
    protected void pointerReleased(int x, int y) {
        if (pauseTouched) {
            pauseButtonPressed();
        }
        if (menuTouched) {
            openMenu();
        }
        pauseTouched = false;
        menuTouched = false;
        // turn off the motor
        accel = false;
    }
    void refreshScreenParameters() {
        scW = getWidth();
        scH = getHeight();
        maxScSide = Math.max(scW, scH);
        Main.sWidth = scW;
        Main.sHeight = scH;
        if (isWorldLoaded) {
            world.refreshScreenParameters();
        }
    }

    /*public synchronized void end() {
        //stopped = true;
    }*/
}
