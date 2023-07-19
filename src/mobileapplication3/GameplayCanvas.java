/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.*;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author vipaol
 */
public class GameplayCanvas extends GameCanvas implements Runnable {
    
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
    public static boolean isBusy = false;
    public static boolean uninterestingDebug = false;
    boolean isWorldLoaded = false;
    
    static boolean paused = false;
    static boolean stopped = false;
    public static boolean lock = false;
    
    public static boolean shouldWait = false;
    public static boolean isWaiting = false;
    
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
    int fps = 0;
    
    // touchscreen
    int pointerX = 0, pointerY = 0;
    boolean pauseTouched = false;
    boolean menuTouched = false;
    // motor state
    boolean accel = false;
    
    // counters
    public static int points = 0;
    int gameoverCountdown;
    static int timeFlying = 0;
    int timeMotorTurnedOff = 50;
    
    static short[][] currentEffects = new short[1][];
    
    // fonts
    Font smallfont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    int sFontH = smallfont.getHeight();
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
    FlipCounter flipCounter;

    public GameplayCanvas() {
        super(false);
        setLoadingProgress(5);
        log("gcanvas constructor");
        setFullScreenMode(true);
        (new Thread(this, "game canvas")).start();
    }
    
    public GameplayCanvas(GraphicsWorld w) {
        super(false);
        setLoadingProgress(5);
        log("gcanvas constructor");
        setFullScreenMode(true);
        world = w;
        (new Thread(this, "game canvas")).start();
    }
    
    private void init() {
        stopped = false;
        paused = false;
        isBusy = false;
        shouldWait = false;
        isWaiting = false;
        
        log("gcanvas init");
        
        if (world == null) {
            // new world
            setDefaultWorld();
        } else {
            // init an existing world
            initWorld();
        }
        
        Main.logMessageDelay = 50;
        currentEffects = new short[1][];
        log("gcanvas:starting thread");
    }
    
    void reset() {
        Main.log("reset");
        points = 0;
        gameoverCountdown = 0;
        if (MenuCanvas.isWorldgenEnabled) {
            worldgen = new WorldGen(world);
            flipCounter = new FlipCounter();
            Main.log("wg started");
        }
        setLoadingProgress(50);
        world.addCar();
        setLoadingProgress(60);
    }
    
    private void initWorld() {
        world.setGravity(FXVector.newVector(0, 250 * gameSpeedMultiplier));
        setLoadingProgress(40);
        world.refreshScreenParameters();
        reset();
        isWorldLoaded = true;
    }
    
    private void setDefaultWorld() {
        Main.log("gCanv:reading world");
        PhysicsFileReader reader = new PhysicsFileReader("/void.phy");
        setLoadingProgress(25);
        
        Main.log("gCanv:loading world");
        World w = World.loadWorld(reader);

        Main.log("gCanv:new grWorld");
        // there's siemens c65 stucks if obfucsation is enabled
        world = new GraphicsWorld(w);

        Main.log("gCanv:setting world");
        initWorld();

        Main.log("gCanv:closing reader");
        reader.close();
    }

    // game thread with main cycle and preparing
    public void run() {
        
        // wait for stopping previous game instance if it still running
        while (lock) {            
            stopped = true;
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
        stopped = false;
        lock = true;
        isWaiting = false;
        
        init();
        
        setLoadingProgress(80);
        
        try {
        log("gcanvas:thread started");
        
        long sleep = 0;
        long start = 0;
        int tick = 0;
        
        Contact[][] contacts = new Contact[3][];
        
        // init music player if enabled
        if (DebugMenu.isDebugEnabled & DebugMenu.music) {
            log("Starting sound");
            Sound sound = new Sound();
            sound.startBgMusic();
        }
        
        setLoadingProgress(100);
        
        log("thread:starting game cycle");
        
        Main.logMessageDelay = 0;
        
        // Main game cycle
        while (!stopped) {
            // catch screen rotation
            if (scW != getWidth()) {
                showNotify();
                world.refreshScreenParameters();
            }
            
            if (!paused) {
                fps = 1000 / ((int) (System.currentTimeMillis() - start));
                start = System.currentTimeMillis();
                
                isBusy = true;
                world.tick();
                if (!DebugMenu.oneFrameTwoTicks || tick%2 == 0) {
                    paint();
                }
                isBusy = false;
                
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
                        if ((!leftWheelContacts && world.getContactsForBody(world.carbody)[0] != null) || rightWheelContacts) {
                            int force = -6000;
                            if (rightWheelContacts) {
                                force *= 2;
                            }
                            world.carbody.applyTorque(FXUtil.toFX(force*forceMultiplier));
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
                
                // if touched an interactive object (falling platform, effect plate)
                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < contacts[j].length; i++) {
                        if (contacts[j][i] != null) {
                            Body body = contacts[j][i].body1();
                            // default value, will be overwritten if other is available
                            int bodyType = MUserData.TYPE_FALLING_PLATFORM;
                            
                            MUserData bodyUserData = null;
                            try {
                                bodyUserData = (MUserData) body.getUserData();
                                bodyType = bodyUserData.bodyType;
                            } catch (ClassCastException ex) {
                                
                            } catch (NullPointerException ex) {
                                
                            }
                            switch (bodyType) {
                                // add fall countdown timer on falling platform
                                case MUserData.TYPE_FALLING_PLATFORM:
                                    if (!world.waitingForDynamic.contains(body) & body != world.carbody & body != world.leftwheel & body != world.rightwheel) {
                                        world.waitingForDynamic.addElement(body);
                                        world.waitingTime.addElement(new Integer(600));
                                        if (uninterestingDebug) world.removeBody(body);
                                    }
                                    break;
                                // apply effect if touched an effect plate
                                case MUserData.TYPE_ACCELERATOR:
                                    giveEffect(bodyUserData.data);
                                    GraphicsWorld.currColWheel = bodyUserData.color;
                                    break;
                                default:
                                    log("unknown bodyType:" + bodyType);
                                    break;
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
                
                if (DebugMenu.simulationMode) {
                    world.rightwheel.setDynamic(false);
                    world.carbody.setDynamic(false);
                    world.leftwheel.setDynamic(false);

                    world.carbody.translate(new FXVector(FXUtil.ONE_FX*100, 0), 0);
                    world.leftwheel.translate(new FXVector(FXUtil.ONE_FX*100, 0), 0);
                    world.rightwheel.translate(new FXVector(FXUtil.ONE_FX*100, 0), 0);
                }
                
                if (pauseDelay > 0)
                    pauseDelay--;
                
                if (MenuCanvas.isWorldgenEnabled) {
                    flipCounter.tick();
                }

                if (tick < 3) {
                    tick++;
                } else {
                    tick = 0;
                    // start the final countdown and open main menu if the car
                    // lies upside down or fell out of the world
                    int lowestY = 5000;
                    if (worldgen != null) {
                        lowestY = worldgen.getLowestY();
                    }
                    if (GraphicsWorld.carY > 2000 + lowestY || (carAngle > 140 && carAngle < 220 && world.carbody.getContacts()[0] != null)) {
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
                    
                    world.tickBodies();
                }
                
                while (shouldWait) {
                    isWaiting = true;
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                if (isWaiting && paused){
                    paint();
                }
                
                isWaiting = false;

                sleep = Main.TICK_DURATION - (System.currentTimeMillis() - start);
                sleep = Math.max(sleep, 0);
            } else {
                // if paused
                if (paused) {
                    sleep = 200;
                } else {
                    sleep = 0;
                }
            }
            // fps/tps control
            try {
                if (sleep > 0) {
                    Thread.sleep(sleep);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lock = false;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    void paint() {
        Graphics g = getGraphics();
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, maxScSide, maxScSide);
        if (loadingProgress < 100) {
            drawLoading(g);
        } else {
            world.drawWorld(g);
        }
        drawHUD(g);
        flushGraphics();
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
        int debugTextOffset = 0;
        if (DebugMenu.isDebugEnabled) {
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
            // car angled
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
        // show coordinates of car if enabled
        if (DebugMenu.coordinates) {
            g.setColor(127, 127, 127);
            g.drawString(GraphicsWorld.carX + " " + GraphicsWorld.carY, 0, debugTextOffset, 0); 
            debugTextOffset += currentFontH;
        }
        
        if (DebugMenu.showFPS) {
            g.setColor(0, 255, 0);
            if (fps < 19) {
                g.setColor(127, 127, 0);
                if (fps < 15) {
                    g.setColor(255, 0, 0);
                }
            }
            if (DebugMenu.oneFrameTwoTicks) {
                g.drawString("FPS:" + fps/2, 0, debugTextOffset, 0);
            } else {
                g.drawString("FPS:" + fps, 0, debugTextOffset, 0);
            }
            debugTextOffset += currentFontH;
        }
        
        try {
            if (DebugMenu.isDebugEnabled) {
                switch (WorldGen.currStep) {
                    case 0:
                        g.setColor(0, 255, 0);
                        break;
                    case 1:
                        g.setColor(127, 127, 255);
                        break;
                    case 2:
                        g.setColor(127, 127, 0);
                        break;
                    case 3:
                        g.setColor(255, 0, 0);
                        break;
                    case 4:
                        g.setColor(127, 0, 191);
                        break;
                    case 5:
                        g.setColor(127, 63, 0);
                        break;
                    default:
                        break;
                }
                g.drawString("wg: mspt" + WorldGen.mspt + " sgs" + worldgen.getSegmentCount() + " step:" + WorldGen.currStep, 0, debugTextOffset, 0);
                debugTextOffset += currentFontH;
            }
        } catch(NullPointerException ex) {
            
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
        
        // score counter
        if (MenuCanvas.isWorldgenEnabled && world != null) {
            g.setColor(flipIndicator, flipIndicator, 255);
            setFont(largefont, g);
            g.drawString(String.valueOf(points), world.halfScWidth, world.scHeight - currentFontH * 3 / 2,
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
            
            if (shouldWait) {
                g.setColor(127, 0, 0);
            }
            for (int i = 0; i <= scH; i++) {
                g.drawLine(scW / 2, 0, d * i, scH);
            }
            if (shouldWait) {
                g.setColor(255, 0, 0);
                g.drawString("Thread is locked", 0, 0, 0);
                g.drawString("(WorldGen is busy)", 0, g.getFont().getHeight(), 0);
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
        log(percents + "%");
        paint();
    }
    
    private void setFont(Font font, Graphics g) {
        g.setFont(font);
        currentFont = font;
        currentFontH = currentFont.getHeight();
    }
    
    // local method for autorefreshing screen after each logging
    private void log(String text) {
        Main.log(text);
        if (Main.isScreenLogEnabled) paint();
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
    
    void resume() {
        paused = false;
        if (worldgen != null) {
            worldgen.resume();
        }
        paint();
    }

    // also used as pause
    protected void hideNotify() {
        log("hideNotify");
        paused = true;
        if (worldgen != null) {
            worldgen.pause();
        }
        paint();
        // to prevent siemens' bug that calls hideNotify right after showing canvas
        if (pauseDelay > 0) {
            if (previousPauseState == false) {
                resume();
            }
        }
    }
    
    protected void showNotify() {
        log("showNotify");
        
        // idk if it needed, but why not?
        sizeChanged(getWidth(), getHeight());
        
        paint();
        
        // to prevent siemens' bug that calls hideNotify right after showing canvas
        pauseDelay = PAUSE_DELAY;
        previousPauseState = paused;
    }
    
    protected void sizeChanged(int w, int h) {
        scW = w;
        scH = h;
        maxScSide = Math.max(scW, scH);
        Main.sWidth = scW;
        Main.sHeight = scH;
        if (world != null) {
            world.refreshScreenParameters();
        }
        paint();
    }
    
    private void pauseButtonPressed() {
        if (!paused) {
            pauseDelay = 0;
            hideNotify();
            paint();
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
    
    class FlipCounter {
        boolean flipWaiting = false;
        boolean backFlipWaiting = false;
        boolean step1Done = false;
        boolean step2Done = false;

        int backFlipsCount = 0;
        int upperAng = 0;

        void tick() {
            if (DebugMenu.dontCountFlips) {
                return;
            }
            if (world.carbody.rotationVelocity2FX() >= 0) {
                if (flipWaiting) {
                    flipWaiting = false;
                    step1Done = false;
                    step2Done = false;
                }
                backFlipWaiting = true;
            } else {
                if (backFlipWaiting) {
                    backFlipsCount = 0;
                    backFlipWaiting = false;
                    step1Done = false;
                    step2Done = false;
                }
                flipWaiting = true;
            }
            int ang = world.carbody.rotation2FX();
            if (!step1Done) {
                if (ang < 13176794 | ang > 92237561) {
                    step1Done = true;
                }
            } else {
                // cancel when touched the ground
                if (GameplayCanvas.timeFlying < 1 & !GameplayCanvas.uninterestingDebug) {
                    step2Done = false;
                    backFlipsCount = 0;
                    return;
                }
                if (!step2Done) {
                    if (!(ang < 13176794 | ang > 92237561))
                        step2Done = true;
                } else {
                    if ((ang < 13176794 | ang > 92237561)) {
                        if (world.carbody.rotationVelocity2FX() >= 0) {
                            if (backFlipWaiting) {
                                backFlipsCount++;
                                if (backFlipsCount > 1) {
                                    points += 1;
                                    backFlipsCount = 0;
                                    indicateFlip();
                                }
                            }
                        } else {
                            if (flipWaiting) {
                                points += 1;
                                indicateFlip();
                            }
                            backFlipsCount = 0;
                            //FXUtil.
                        }
                        step1Done = false;
                        step2Done = false;
                    }
                }
            }
        }

        // blink point counter on flip
        public void indicateFlip() {
            flipIndicator = 0;
        }
    }
}
