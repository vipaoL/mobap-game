/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.game;

import java.util.Vector;

import at.emini.physics2D.Body;
import at.emini.physics2D.Contact;
import at.emini.physics2D.UserData;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import mobileapplication3.platform.Battery;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.Records;
import mobileapplication3.platform.Sound;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.CanvasComponent;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.Keys;
import utils.MobappGameSettings;

/**
 *
 * @author vipaol
 */
public class GameplayCanvas extends CanvasComponent implements Runnable {
    public static final int TICK_DURATION = 50;
	private static final String[] MENU_HINT = {"MENU:", "here(touch), #", "D, left soft"};
    private static final String[] PAUSE_HINT = {"PAUSE:", "here(touch), *,", "B, right soft"};
    public static final short EFFECT_SPEED = 0;
    private static final int BATT_UPD_PERIOD = 10000;
	private static final int GAME_MODE_ENDLESS = 1, GAME_MODE_LEVEL = 2;
	private static final int GAME_OVER_DAMAGE = 8;
    
    // to prevent siemens' bug which calls hideNotify right after showing canvas
    private static final int PAUSE_DELAY = 5;
    private int pauseDelay = PAUSE_DELAY;
    private boolean wasPaused = false;
    
    // state and mode
	private int gameMode = GAME_MODE_ENDLESS;
    private static boolean isFirstStart = true; // show hints only on first start
    public boolean uninterestingDebug = false;
    public boolean shouldWait = false;
    public boolean isWaiting = false;
    private boolean isWorldLoaded = false;
    private int hintVisibleTimer = 120; // in ticks
    private boolean showFPS = false;
    private boolean battIndicator = false;
    private int batLevel;

    private boolean paused = false;
    private boolean stopped = false;
    private boolean isStopping = false;
    private boolean gameOver = false;
    private boolean feltUnderTheWorld = false;
    
    // screen
    private int scW, scH;
    private int maxScSide;

	// car state
    private int carVelocitySqr;
    private int carAngle = 0;
	public int carSpawnX = 100;
	public int carSpawnY = -400;
    // motor state
    private boolean motorTurnedOn = false;

    // indicators
    private int flipIndicator = 255; // blink the counter after flip
	private int posResetIndicator = 0; // show when wg moves the world
    private int loadingProgress = 0;
    private int speedoState = 0;
    private int tickTime;
    private int fps;
    private int tps;
    private String statusMessage = null;

	// debug
	private int debugTextOffset;
    
    // touchscreen
    private int pointerX = 0, pointerY = 0;
    private boolean pauseTouched = false;
    private boolean menuTouched = false;
    
    // counters
    public int points = 0;
    private int damage;
    public int timeFlying = 10;
    private int ticksMotorTurnedOff = 50;
    private long lastBigTickTime;
    private int bgTick = 0;
    private int framesFromLastFPSMeasure = 0;
    private int ticksFromLastTPSMeasure = 0;
    
    public short[][] currentEffects = new short[1][];

    // fonts
    private static final Font smallfont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    private static final Font largefont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private Font currentFont = largefont;
    private int currentFontH = currentFont.getHeight();
    
    private GraphicsWorld world;
    private WorldGen worldgen;
    private FlipCounter flipCounter;
	private IUIComponent prevScreen = null;

    private Thread gameThread = null;
	private int baseTimestepFX;

	private Vector deferredStructures = null;

    public GameplayCanvas() {
        loadingProgress = 5;
        log("gcanvas constructor");
        repaintOnlyOnFlushGraphics = true;
    }
    
    public GameplayCanvas(GraphicsWorld w) {
    	this();
        world = w;
		world.setGame(this);
    }

	public GameplayCanvas(IUIComponent prevScreen) {
		this();
		this.prevScreen = prevScreen;
		world = new GraphicsWorld();
		world.setGame(this);
	}

	public GameplayCanvas addDeferredStructure(short[][] structureData) {
		if (worldgen == null) {
			if (deferredStructures == null) {
				deferredStructures = new Vector();
			}
			deferredStructures.addElement(structureData);
		} else {
			worldgen.addDeferredStructure(structureData);
		}
		return this;
	}

	public GameplayCanvas loadLevel(short[][] levelData) {
		gameMode = GAME_MODE_LEVEL;
		StructurePlacer.place(world, false, levelData, 0, 0);
		if (levelData[0][0] == ElementPlacer.LEVEL_START) {
			carSpawnX = levelData[0][1];
			carSpawnY = levelData[0][2];
		}
		world.removeBodies = false;
		return this;
	}
    
    public void init() {
        log("starting game thread");
        gameThread = new Thread(this, "game canvas");
        gameThread.start();
    }
    
    private void reset() {
        log("resetting the world");
        points = 0;
        damage = 0;
		WorldGen.isEnabled = gameMode == GAME_MODE_ENDLESS;
        if (WorldGen.isEnabled) {
        	log("starting wg");
            worldgen = new WorldGen(this, world);
			if (deferredStructures != null) {
				while (!deferredStructures.isEmpty()) {
					worldgen.addDeferredStructure((short[][]) deferredStructures.elementAt(0));
					deferredStructures.removeElementAt(0);
				}
				deferredStructures = null;
			}
            flipCounter = new FlipCounter();
            log("wg started");
        }
        setLoadingProgress(50);
		int x = carSpawnX;
		if (WorldGen.isEnabled) {
			x = -3000;
			//x = -1114;
		}
		world.addCar(x, carSpawnY, FXUtil.TWO_PI_2FX / 360 * 30);
        setLoadingProgress(60);
    }
    
    private void initWorld() {
    	log("initing world");
        world.setGravity(FXVector.newVector(0, 1000));
        world.getLandscape().getBody().shape().setElasticity(5);
        setLoadingProgress(40);
        reset();
        isWorldLoaded = true;
    }
    
    private void setDefaultWorld() {
        setLoadingProgress(25);
        
        log("creating world");
        // there siemens c65 stucks if obfuscation is enabled
        world = new GraphicsWorld();
		world.setGame(this);

        setLoadingProgress(30);
        log("setting the world");
        initWorld();
    }

    // game thread with main cycle and preparing
    public void run() {
        try {
            log("game thread started");

            shouldWait = false;
            isWaiting = false;
            timeFlying = 10;

            boolean wasPaused = true;
            tickTime = TICK_DURATION;
            int prevTickTime = TICK_DURATION;
			int physicsIterationsSetting = MobappGameSettings.DEFAULT_PHYSICS_PRECISION;
			boolean lockPhysicsPrecision;
			boolean dynamicPhysicsPrecision;
			int maxFrameTime = MobappGameSettings.DEFAULT_FRAME_TIME;
			try {
            	log("reading settings");
                physicsIterationsSetting = MobappGameSettings.getPhysicsPrecision();
				maxFrameTime = MobappGameSettings.getFrameTime();
    	        showFPS = MobappGameSettings.isFPSShown(showFPS);
    	        battIndicator = MobappGameSettings.isBattIndicatorEnabled(battIndicator) && Battery.checkAndInit();
            } catch (Throwable ex) {
    			Platform.showError("Can't read settings", ex);
    		}
			dynamicPhysicsPrecision = physicsIterationsSetting == MobappGameSettings.DYNAMIC_PHYSICS_PRECISION;
			lockPhysicsPrecision = !dynamicPhysicsPrecision && physicsIterationsSetting != MobappGameSettings.AUTO_PHYSICS_PRECISION;
            int physicsIterations = physicsIterationsSetting;
			if (physicsIterations <= 0) {
				physicsIterations = 2;
			}

            if (world == null) {
                // new world
                setDefaultWorld();
            } else {
                // re-init an existing world
                initWorld();
            }

            world.refreshScreenParameters(scW, scH);
            
            Logger.setLogMessageDelay(50);
            currentEffects = new short[1][];
            
            setLoadingProgress(80);

            long sleep = 0;
            long start = 0;
            int bigTickN = 0;

            // init music player if enabled
            if (DebugMenu.music) {
                log("starting sound");
                Sound sound = new Sound();
                sound.start();
            }
            
            if (DebugMenu.simulationMode) {
                world.rightWheel.setDynamic(false);
                world.carbody.setDynamic(false);
                world.leftWheel.setDynamic(false);
            }

            setLoadingProgress(100);

            log("starting game cycle");

            Logger.setLogMessageDelay(0);
            baseTimestepFX = world.getTimestepFX();
            long lastFPSMeasureTime = System.currentTimeMillis();
            long lastBattUpdateTime = 0;
			int physicsIterationsUnalteredCount = 0;

			Object wgLock;
			if (worldgen != null) {
				wgLock = worldgen.lock;
			} else {
				wgLock = new Object();
			}

			try {
				for (int i = 0; !hasParent() && i < 30; i++) {
					Thread.sleep(100);
				}
			} catch (InterruptedException e) { }

            // Main game cycle
            while (!stopped && hasParent()) {
            	try {
	                if (!paused) {
	                	// FPS & TPS counter
	                	int dtFromLastFPSMeasure = (int) (System.currentTimeMillis() - lastFPSMeasureTime);
	                	if (dtFromLastFPSMeasure > 1000) {
	                		lastFPSMeasureTime = System.currentTimeMillis();
	                		fps = framesFromLastFPSMeasure * 1000 / dtFromLastFPSMeasure;
	                		framesFromLastFPSMeasure = 0;
	                		tps = ticksFromLastTPSMeasure * 1000 / dtFromLastFPSMeasure;
	                		ticksFromLastTPSMeasure = 0;
	                	}

						if (!lockPhysicsPrecision) {
							if (framesFromLastFPSMeasure == 0) {
								int prevValue = physicsIterations;
								if (fps != 0) {
									physicsIterations = Math.max(1, 140 / fps + 1);
									if (physicsIterations == prevValue) {
										physicsIterationsUnalteredCount++;
										if (!dynamicPhysicsPrecision && physicsIterationsUnalteredCount >= 3) {
											Logger.log("locking precision multiplier: ", physicsIterations);
											lockPhysicsPrecision = true;
										}
									} else {
										physicsIterationsUnalteredCount = 0;
									}
								} else {
									physicsIterationsUnalteredCount = 0;
								}
							}
						}

                        // Adjust physics engine tick time to current TPS
	                    if (!wasPaused) {
	                        tickTime = (int) (System.currentTimeMillis() - start);
                            world.setTimestepFX(Math.max(1, baseTimestepFX * Math.min((tickTime + prevTickTime + 1) / 2, 100) / 50 / physicsIterations));
                        } else {
	                        wasPaused = false;
	                    }

						prevTickTime = tickTime;
	                    start = System.currentTimeMillis();
	
	                    // Tick and draw
						Contact[][] carContacts = getCarContacts();
						synchronized (wgLock) {
							setSimulationArea();

							// Check if the car contacts with the ground or with something else
                            for (int i = 0; i < physicsIterations; i++) {
								world.tick();
								// Check if the car contacts with custom bodies (accelerators, falling platforms, ...)
								carContacts = getCarContacts();
								tickCustomBodyInteractions(carContacts);
								ticksFromLastTPSMeasure++;
							}
							paint();
						}

                        boolean leftWheelContacts = carContacts[0][0] != null;
                        boolean carBodyContacts = carContacts[1][0] != null;
                        boolean rightWheelContacts = carContacts[2][0] != null;
	                    
	                    // some things should be performed once at a fixed interval (50ms, or 20 times per second)
	                    boolean bigTick = start - lastBigTickTime > TICK_DURATION;
	                    if (bigTick) {
		                    if ((!leftWheelContacts && !rightWheelContacts)) {
		                        timeFlying += 1;
		                    } else {
		                        timeFlying = 0;
		                    }
	
		                    // Hide keyboard/touch buttons hint
		                    if (isWorldLoaded) {
	                            hintVisibleTimer--;
	                        }
	
		                    // Prevent pause right after resume to work around some Siemens bug
	                        if (pauseDelay > 0) {
	                            pauseDelay--;
	                        }
	
	                        // flip counter and debug posReset indicator
	                        if (WorldGen.isEnabled) {
	                            // highlight the score counter on flip
	                            if (flipIndicator < 255) {
	                                flipIndicator+=64;
	                                if (flipIndicator >= 255) {
	                                    flipIndicator = 255;
	                                }
	                            }
	                            flipCounter.tick();

								if (posResetIndicator > 0) {
									posResetIndicator-=16;
									if (posResetIndicator <= 0) {
										posResetIndicator = 0;
									}
								}
	                        }

	                        // move the car to the right in the simulation mode
	                        if (DebugMenu.simulationMode) {
	                            world.carbody.translate(new FXVector(FXUtil.ONE_FX*100, 0), 0);
	                            world.leftWheel.translate(new FXVector(FXUtil.ONE_FX*100, 0), 0);
	                            world.rightWheel.translate(new FXVector(FXUtil.ONE_FX*100, 0), 0);
	                        }
	
	                        // tick effect timers (speed, slowness, ...)
	                        tickEffects();
	
	                        // distribute some tasks over the ticks to offload the CPU
	                        if (bigTickN < 3) {
	                        	if (bigTickN == 1) {
	                        		// tick the timers of falling platforms, removing bodies felt out of the world
	                        		world.tickCustomBodies();
	                        	}

                                bigTickN++;
                            } else {
	                            bigTickN = 0;
	                            tickDamage();
	                            if (System.currentTimeMillis() - lastBattUpdateTime > BATT_UPD_PERIOD && battIndicator) {
	                            	batLevel = Battery.getBatteryLevel();
	                            	lastBattUpdateTime = System.currentTimeMillis();
	                            }
	                        }
	                        lastBigTickTime = start;
	                    }
	
	                    // getting car angle
	                    carAngle = 360 - FXUtil.angleInDegrees2FX(world.carbody.rotation2FX());
	
	                    // Gas and brake
						boolean isNotFlying = timeFlying <= 2;
						if (motorTurnedOn) {
	                        ticksMotorTurnedOff = 0;
	                        // apply motor force when on the ground
	                        if (isNotFlying || uninterestingDebug) {
	                        	// set motor power according to car speed
	                            // (start quickly and limit max speed)
	                            FXVector velFX = world.carbody.velocityFX();
	                            int vX = velFX.xAsInt();
	                            int vY = velFX.yAsInt();
	                            if (currentEffects[EFFECT_SPEED] != null) {
	                                if (currentEffects[EFFECT_SPEED][0] > 0) {
	                                    vX = vX * 100 / currentEffects[EFFECT_SPEED][2];
	                                    vY = vY * 100 / currentEffects[EFFECT_SPEED][2];
	                                }
	                            }

                                int speedMultipiler;
                                if (uninterestingDebug) {
	                                speedMultipiler = 250000;
	                            } else {
		                            carVelocitySqr = (vX * vX + vY * vY) / 4;
		                            if (carVelocitySqr > 1000000) {
		                                speedMultipiler = 16000;
		                                speedoState = 2;
		                            } else if (carVelocitySqr > 100000) {
		                                speedMultipiler = 123000;
		                                speedoState = 1;
		                            } else {
		                                speedMultipiler = 160000;
		                                speedoState = 0;
		                            }
	                            }

	                            int directionOffset = 0;
	                            if (currentEffects[EFFECT_SPEED] != null) {
	                                if (currentEffects[EFFECT_SPEED][0] > 0) {
	                                    directionOffset = currentEffects[EFFECT_SPEED][1];
	                                    speedMultipiler = speedMultipiler * currentEffects[EFFECT_SPEED][2] / 100;
	                                }
	                            }
	                            int motorForceX = Mathh.cos(carAngle - 15 + directionOffset) * speedMultipiler / 50;
	                            int motorForceY = Mathh.sin(carAngle - 15 + directionOffset) * speedMultipiler / -50;
	                            world.carbody.applyMomentum(new FXVector(convertByTimestep(motorForceX), convertByTimestep(motorForceY)));

	                            if ((!leftWheelContacts && carBodyContacts) || rightWheelContacts) {
	                                int torque;
	                                if (rightWheelContacts) {
	                                    torque = -100000000;
	                                } else {
	                                	torque = -50000000;
	                                }
	                                world.carbody.applyTorque(convertByTimestep(torque));
	                            }
	                        } else {
	                            // apply rotational force
	                            if (world.carbody.rotationVelocity2FX() < 100000000) {
	                            	int torque = convertByTimestep(-80000000);
	                            	if (carBodyContacts && carAngle > 170 && carAngle < 300) {
	                            		torque = torque << 1;
	                            	}
	                            	world.carbody.applyTorque(torque);
	                            }
	                        }
	                    } else {
	                        // brake for two seconds after the motor is turned off
	                        if (ticksMotorTurnedOff < 40 && !uninterestingDebug) {
	                            try {
	                                if (world.carbody.angularVelocity2FX() > 0) {
	                                    world.carbody.applyTorque(2 * convertByTimestep(world.carbody.angularVelocity2FX()));
	                                }
	                                if (isNotFlying && !uninterestingDebug) {
	                                    world.carbody.applyMomentum(new FXVector(convertByTimestep(-world.carbody.velocityFX().xFX/2), convertByTimestep(-world.carbody.velocityFX().yFX/2)));
	                                }
	                                if (bigTick) {
	                                	ticksMotorTurnedOff++;
										if (isNotFlying) {
											ticksMotorTurnedOff++;
										}
	                                }
	                            } catch (NullPointerException ex) {
	                                Logger.log(ex);
	                            }
	                        }
	                    }

						if (worldgen != null && world.carX + world.viewField > worldgen.lastX) {
							shouldWait = true;
							Logger.log("wg can't keep up, locking game thread...");
						}

	                    while (shouldWait) {
	                        isWaiting = true;
	                        Thread.yield();
	                        try {
	                            Thread.sleep(1);
	                        } catch (InterruptedException ex) {
	                            Logger.log(ex);
	                        }
	                    }
	
	                    isWaiting = false;

	                    Thread.yield();
	                    sleep = maxFrameTime - (System.currentTimeMillis() - start);
	                    sleep = Math.max(sleep, 0);
	                } else {
	                    // Pause screen
	                    wasPaused = true;
	                    sleep = 200;
	                    if (isVisible) {
	                        paint();
	                    }
	                }

	                // FPS/TPS control
	                try {
	                    if (sleep > 0) {
	                        Thread.sleep(sleep);
	                    } else if (System.currentTimeMillis() == start) {
	                    	Thread thread = Thread.currentThread();
	                    	while (System.currentTimeMillis() == start) {
	                    		synchronized (thread) {
									thread.wait(0, 30);
								}
	                    	}
	                    }
	                } catch (InterruptedException e) {
	                    Logger.log(e);
	                }
            	} catch (Exception ex) {
            		Logger.log(ex);
            	}
            }
        } catch (NullPointerException ex) {
			Platform.showError(ex);
        }
        Logger.log("game thread stopped");
    }

	private void tickDamage() {
		// add damage if the car lies upside down or fell out of the world
		int lowestY = getLowestSafeY();
		feltUnderTheWorld = world.carY > 2000 + lowestY;
		if (feltUnderTheWorld || (carAngle > 140 && carAngle < 220 && world.carbody.getContacts()[0] != null) || gameOver) {
		    if (uninterestingDebug) {
		        damage = 0;
		    }
		    if (damage < GAME_OVER_DAMAGE) {
		        damage++;
		    } else {
		    	gameOver();
		    }
		} else {
		    if (damage > 0) {
		        damage--;
		    } else {
		        damage = 0;
		    }
		}
	}
    
    private int convertByTimestep(int valueInDefaultTimestep) {
    	return valueInDefaultTimestep / TICK_DURATION * tickTime;
    }

	private int getLowestSafeY() {
		return world.lowestY;
	}

	private void setSimulationArea() {
		world.refreshCarPos(); 
		world.setSimulationArea(world.carX - world.viewField, world.carX + world.viewField);
	}
    
    private Contact[][] getCarContacts() {
		return new Contact[][] {
			world.getContactsForBody(world.leftWheel),
			world.getContactsForBody(world.carbody),
			world.getContactsForBody(world.rightWheel)};
	}

	private void tickCustomBodyInteractions(Contact[][] carContacts) {
    	// if touched an interactive object (falling platform, effect plate)
        for (int j = 0; j < carContacts.length; j++) {
            for (int i = 0; i < carContacts[j].length; i++) {
                if (carContacts[j][i] != null) {
                    Body body = carContacts[j][i].body1();
                    if (body == world.leftWheel || body == world.carbody || body == world.rightWheel) {
                    	body = carContacts[j][i].body2();
                    }
                    if (body == null) {
                        continue;
                    }
                    UserData userData = body.getUserData();
                    if (!(userData instanceof MUserData)) {
                        continue;
                    }
                    MUserData bodyUserData = (MUserData) body.getUserData();
                    int bodyType = bodyUserData.bodyType;
                    switch (bodyType) {
                        // add fall countdown timer on falling platform
                        case MUserData.TYPE_FALLING_PLATFORM:
                            if (!world.waitingForDynamic.contains(body)) {
                                world.waitingForDynamic.addElement(body);
                                // The constructor Integer(int) was deprecated in Java 9
                                // Integer.valueOf() only accepts String in Java 1.3
                                // So this is the only way?
                                world.waitingTime.addElement(Integer.valueOf(String.valueOf(600)));
                                if (uninterestingDebug) world.removeBody(body);
                            }
                            break;
                        // apply effect if touched an effect plate
                        case MUserData.TYPE_ACCELERATOR:
                            giveEffect(bodyUserData.data);
                            world.setWheelColor(bodyUserData.color);
                            break;
						case MUserData.TYPE_LEVEL_FINISH:
							if (!isPopupShown()) {
								showPopup(new LevelCompletedScreen(this));
							}
							break;
						case MUserData.TYPE_LAVA:
							world.destroyCar();
							stop(true, false, 1000);
							break;
                    }
                }
            }
        }
    }
    
    private void tickEffects() {
    	for (int i = 0; i < currentEffects.length; i++) {
            if (currentEffects[i] != null) {
                if (currentEffects[i][0] > 0) {
                    currentEffects[i][0]--;
                } else if (currentEffects[i][0] == 0) {
                    currentEffects[i] = null;
                }
            }
        }
	}
    
    public boolean drawAsBG(Graphics g) {
    	if (feltUnderTheWorld || world.currColBodies == 0 && world.currColBg == 0) {
    		return false;
    	}

		world.setTimestepFX(baseTimestepFX / 7);
    	world.refreshCarPos();
    	setSimulationArea();
    	world.tickCustomBodies();
    	tickEffects();
		for (int i = 0; i < 7; i++) {
			world.tick();
		}
    	tickCustomBodyInteractions(getCarContacts());
    	tickDamage();
    	if (worldgen != null) {
    		worldgen.tick();
    	}

        if (worldgen != null) {
            world.drawWorld(g, worldgen.getStructures(), worldgen.getStructuresRingBufferOffset(), worldgen.getStructuresCount());
        } else {
			world.drawWorld(g, null, 0, 0);
		}

        if (world.carY > getLowestSafeY()) {
    		if (bgTick % 10 == 0) {
    			dimColors();
    			bgTick = 0;
    		} else {
    			bgTick++;
    		}
    	}
    	return true;
    }
    
    private void drawBg(Graphics g) {
    	g.setColor(0, 0, 0);
        g.fillRect(0, 0, maxScSide, maxScSide);
    }
    
    protected void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
    	drawBg(g);
        if (loadingProgress < 100) {
            drawLoading(g);
            Logger.paint(g);
        } else {
            if (worldgen != null) {
                world.drawWorld(g, worldgen.getStructures(), worldgen.getStructuresRingBufferOffset(), worldgen.getStructuresCount());
            } else {
				world.drawWorld(g, null, 0, 0);
			}
            drawHUD(g);
        }
    }

    private synchronized void paint() {
    	if (!gameOver) {
	    	try {
	    		Graphics g = getUGraphics();
		        paint(g);
		        flushGraphics();
		        framesFromLastFPSMeasure++;
	        } catch (Exception ignored) { }
    	}
    }

    private String nameBody(Body body) {
        if (body == null) {
            return " ";
        } else if (body == world.getLandscape().getBody()) {
            return "GND";
        } else if (body == world.leftWheel) {
            return "Lw";
        } else if (body == world.carbody) {
            return "Cb";
        } else if (body == world.rightWheel) {
            return "Rw";
        } else {
            return "?";
        }
    }

    private String contactsToString(Contact[] contacts) {
        StringBuffer ret = new StringBuffer(" ");
        for (int i = 0; i < contacts.length; i++) {
            if (contacts[i] != null) {
                ret.append(nameBody(contacts[i].body1()));
                ret.append("-");
                ret.append(nameBody(contacts[i].body2()));
            }
            ret.append(" ");
        }
        return ret.toString();
    }
    
    // point counter, very beautiful pause menu,
    // debug info, on-screen log, game over screen
    private void drawHUD(Graphics g) {
        // show hint on first start
        if (isFirstStart && hintVisibleTimer > 0) {
            int color = 255 * hintVisibleTimer / 120;
            g.setColor(color/4, color/2, color/4);
			int btnW = scW/3;
			int btnH = scH/6;
			int btnRoundingD = Math.min(btnW, btnH) / 4;
            g.fillRoundRect(0, 0, btnW, btnH, btnRoundingD, btnRoundingD);
            g.fillRoundRect(w - btnW, 0, btnW, btnH, btnRoundingD, btnRoundingD);
            g.setColor(color/4, color/4, color);
            setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL), g);
            for (int i = 0; i < MENU_HINT.length; i++) {
                g.drawString(MENU_HINT[i], scW/6, i * currentFontH + scH / 12 - currentFontH*MENU_HINT.length/2, Graphics.HCENTER | Graphics.TOP);
            }
            for (int i = 0; i < PAUSE_HINT.length; i++) {
                g.drawString(PAUSE_HINT[i], scW*5/6, i * currentFontH + scH / 12 - currentFontH*PAUSE_HINT.length/2, Graphics.HCENTER | Graphics.TOP);
            }
        }
        
        // draw some debug info if debug is enabled
        setFont(smallfont, g);
        debugTextOffset = 0;
        if (battIndicator) {
			if (batLevel < 6) {
				g.setColor(0x00ff00);
			} else if (batLevel < 10) {
				g.setColor(0xff8000);
			} else if (batLevel < 30) {
        		g.setColor(0xffff00);
        	} else {
        		g.setColor(0x00ff00);
        	}

			drawDebugText(g, "BAT: " + batLevel + "%");
		}

        g.setColor(0xffffff);
        if (DebugMenu.showContacts) {
            Contact[][] contacts = getCarContacts();
            String[] names = {"LW", "CB", "RW"};
            for (int i = 0; i < contacts.length; i++) {
				drawDebugText(g, names[i] + contactsToString(contacts[i]));
			}
        }
        if (DebugMenu.isDebugEnabled) {
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
				drawDebugText(g, String.valueOf(carVelocitySqr));
			}
            // car angle
            if (DebugMenu.showAngle) {
                if (timeFlying > 0) {
                    g.setColor(0, 0, 255);
                } else {
                    g.setColor(255, 255, 255);
                }
				drawDebugText(g, String.valueOf(FXUtil.angleInDegrees2FX(world.carbody.rotation2FX())));
			}
        }
        // show coordinates of car if enabled
        if (DebugMenu.coordinates) {
            g.setColor(127, 127, 127);
			drawDebugText(g, world.carX + " " + world.carY);
		}

        if (showFPS) {
            g.setColor(0, 255, 0);
            if (tps < 100) {
                g.setColor(255, 200, 0);
                if (fps < 45) {
                    g.setColor(255, 0, 0);
                }
            }
			drawDebugText(g, "FPS:" + fps + " TPS:" + tps);
		}

        try {
            if (DebugMenu.isDebugEnabled) {
                switch (worldgen.currStep) {
					case WorldGen.STEP_IDLE:
                        g.setColor(0, 255, 0);
                        break;
					case WorldGen.STEP_ADD:
                        g.setColor(127, 127, 255);
                        break;
					case WorldGen.STEP_RES_POS:
                        g.setColor(255, 0, 0);
                        break;
					case WorldGen.STEP_CLEAN_SGS:
                        g.setColor(127, 127, 0);
                        break;
                    default:
                        break;
                }
				drawDebugText(g, "wg: mspt" + worldgen.mspt + " step:" + worldgen.currStep);
				drawDebugText(g, "sgs" + worldgen.getSegmentCount() + " bds" + world.getBodyCount());
			}
        } catch (NullPointerException ignored) { }

        // game over screen
        if (damage > 1 && !gameOver) {
            g.setFont(largefont);
            g.setColor(255, 0, 0);
            g.drawString("!", scW / 2, scH / 3 + currentFontH / 2, Graphics.HCENTER | Graphics.TOP);
            g.setColor(0, 0, Math.min(127 * (GAME_OVER_DAMAGE - damage) / GAME_OVER_DAMAGE, 255));
            g.fillRect(0, 0, scW, scH* damage / GAME_OVER_DAMAGE /2 + 1);
            g.fillRect(0, scH - scH* damage / GAME_OVER_DAMAGE /2, scW, scH - 1);
        }
        
        // score counter and debug posReset indicator
        if (WorldGen.isEnabled && world != null) {
            g.setColor(flipIndicator, flipIndicator, 255);
            setFont(largefont, g);
            g.drawString(String.valueOf(points), scW/2, scH - currentFontH * 3 / 2,
                    Graphics.HCENTER | Graphics.TOP);
			if (DebugMenu.isDebugEnabled && posResetIndicator > 0) {
				g.setColor(posResetIndicator, 0, 0);
				int d = h / 20;
				g.fillArc(x0, y0 + h - d, d, d, 0, 360);
			}
        }
        
        // draw beautiful(isn't it?) pause screen
        if (paused) {
            int d = scH / 40;
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

	private void drawDebugText(Graphics g, String str) {
		g.drawString(str, 0, debugTextOffset, 0);
		debugTextOffset += currentFontH;
	}

	private void drawLoading(Graphics g) {
        g.setColor(255, 255, 255);
        int l = scW * 2 / 3;
        int h = scH / 24;
        g.drawRect(scW / 2 - l / 2, scH * 2 / 3, l, h);
        g.fillRect(scW / 2 - l / 2, scH * 2 / 3, l*loadingProgress/100, h);
        if (statusMessage != null) {
        	g.drawString(statusMessage, this.w/2, this.h, HCENTER | BOTTOM);
        }
    }
    private void setLoadingProgress(int percents) {
        loadingProgress = percents;
        Logger.log(percents + "%");
        paint();
    }
    
    private void setFont(Font font, Graphics g) {
        g.setFont(font);
        currentFont = font;
        currentFontH = currentFont.getHeight();
    }
    
    // log and repaint
    private void log(String text) {
    	statusMessage = text;
        Logger.log(text);
        if (Thread.currentThread() == gameThread && (Logger.isOnScreenLogEnabled() || loadingProgress < 100)) {
        	paint();
        }
    }
    
    private void giveEffect(short[] data) {
        int id = data[0];
        int dataLength = data.length - 1;
        currentEffects[id] = new short[dataLength];
        for (int i = 1; i < data.length; i++) {
            currentEffects[id][i - 1] = data[i];
        }
    }
    
    private void gameOver() {
    	if (gameOver) {
    		return;
    	}

    	if (feltUnderTheWorld) {
    		stop(true, false);
    		return;
    	}

    	gameOver = true;
    	motorTurnedOn = false;
    	world.destroyCar();
    	dimColors();
    	stop(true, false);
    }
    
    private void dimColors() {
    	world.currColLandscape = dimColor(world.currColLandscape, 80);
    	world.currColBodies = dimColor(world.currColBodies, 80);
    	if (world.currColBodies > 0) {
    		world.currColBg = dimColor(Math.max(0x000015, world.currColBg), 105);
    	} else {
    		world.currColBg = dimColor(world.currColBg, 70);
    	}
    }

    private int dimColor(int color, int percent) {
    	int r = getColorRedComponent(color) * percent / 100;
    	int g = getColorGreenComponent(color) * percent / 100;
    	int b = getColorBlueComponent(color) * percent / 100;
    	r = Mathh.constrain(0, r, 255);
    	g = Mathh.constrain(0, g, 255);
    	b = Mathh.constrain(0, b, 255);
    	return (r << 16) + (g << 8) + b;
    }
    
    private int getColorRedComponent(int color) {
    	return (color >> 16) & 0xff;
    }
    
    private int getColorGreenComponent(int color) {
    	return (color >> 8) & 0xff;
    }
    
    private int getColorBlueComponent(int color) {
    	return color & 0xff;
    }

	public void stop(final boolean openMenu, boolean blockUntilCompleted) {
		stop(openMenu, blockUntilCompleted, 0);
	}

    public void stop(final boolean openMenu, final boolean blockUntilCompleted, final int delay) {
    	log("stopping game thread");
        if (isStopping) {
        	return;
        }

        final GameplayCanvas inst = this;
        Runnable stopperRunnable = new Runnable() {
            public void run() {
				if (!blockUntilCompleted) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException ex) {
						Logger.log(ex);
					}
				}

				isStopping = true;
				stopped = true;
				isFirstStart = false;

				if (gameMode == GAME_MODE_ENDLESS && !(DebugMenu.isDebugEnabled || DebugMenu.simulationMode)) {
					new Thread(new Runnable() {
						public void run() {
							try {
								Records.saveRecord(points, 9);
							} catch (Exception ex) {
								Platform.showError("Can't save record:", ex);
							}
						}
					}, "record saver").start();
				}
            	if (worldgen != null) {
            		worldgen.stop();
            	}
                boolean successed = gameThread == null;
                while (!successed) {
                    try {
                        gameThread.join();
                        successed = true;
                    } catch (InterruptedException ex) {
						Logger.log(ex);
                    }
                }
                log("game: stopped");
                if (openMenu) {
					if (prevScreen == null) {
						RootContainer.setRootUIComponent(new MenuCanvas(inst));
					} else {
						RootContainer.setRootUIComponent(prevScreen);
					}
                }
            }
        };

        if (blockUntilCompleted) {
        	stopperRunnable.run();
        } else {
        	new Thread(stopperRunnable).start();
        }
    }
    
    private void resume() {
        paused = false;
        if (worldgen != null) {
            worldgen.resume();
        }
    }

	public void onPosReset() {
		posResetIndicator = 255;
	}

    public void onHide() {
        log("onHide");
        paused = true;
        if (worldgen != null) {
            worldgen.pause();
        }
        // to prevent siemens' bug that calls hideNotify right after showing canvas
        if (pauseDelay > 0) {
            if (!wasPaused) {
                resume();
            }
        }
    }

    public void onShow() {
        log("onShow");
        
        // to prevent siemens' bug that calls hideNotify right after showing canvas
        pauseDelay = PAUSE_DELAY;
        wasPaused = paused;
    }
    
    protected void onSetBounds(int x0, int y0, int w, int h) {
        scW = w;
        scH = h;
        maxScSide = Math.max(scW, scH);
        if (world != null) {
            world.refreshScreenParameters(w, h);
        }
    }
    
    private void pauseButtonPressed() {
        if (!paused) {
            pauseDelay = 0;
            onHide();
        } else {
            resume();
        }
    }

    // keyboard events
    public boolean handleKeyReleased(int keyCode, int count) {
    	if (gameOver) {
            return false;
        }

        // turn off motor
        motorTurnedOn = false;
        if (timeFlying > 0) {
            timeFlying = Math.max(5, timeFlying);
        }
        return true;
    }
    
    public boolean handleKeyPressed(int keyCode, int count) {
		if (gameOver) {
			return false;
		}

        int gameAction = RootContainer.getAction(keyCode);
        // menu
        if (keyCode == Keys.KEY_SOFT_LEFT || keyCode == Keys.KEY_POUND || keyCode == Keys.KEY_NUM0 || gameAction == Keys.GAME_D) {
            stop(true, false);
        } else if (keyCode == Keys.KEY_SOFT_RIGHT || keyCode == GenericMenu.SE_KEY_BACK) {
			pauseButtonPressed();
		} else if (keyCode == Keys.KEY_STAR || gameAction == Keys.GAME_B) {
			pauseButtonPressed();
		} else if (keyCode == Keys.KEY_NUM6) {
			world.destroyCar();
		} else {
			// any other button turns the motor on
			motorTurnedOn = true;
		}

        return true;
    }
    
    public boolean handleKeyRepeated(int keyCode, int pressedCount) { return !gameOver; }

    // touch events
    public boolean handlePointerPressed(int x, int y) {
        if (x > scW * 2 / 3 && y < scH / 6) {
            pauseTouched = true;
        } else if (x < scW / 3 && y < scH / 6) {
            menuTouched = true;
        } else {
            // if not on buttons, turn on the motor
        	if (!gameOver) {
        		motorTurnedOn = true;
        	}
        }
        pointerX = x;
        pointerY = y;
        return !gameOver;
    }
    public boolean handlePointerDragged(int x, int y) {
    	if (gameOver) {
            return false;
        }
        if (pauseTouched || menuTouched) {
            if (x - pointerX > 3 || y - pointerY > 3) {
                pauseTouched = false;
                menuTouched = false;
            }
        }
        pointerX = x;
        pointerY = y;
        return true;
    }
    public boolean handlePointerReleased(int x, int y) {
        if (pauseTouched) {
            pauseButtonPressed();
        }
        if (menuTouched) {
            stop(true, false);
        }
        pauseTouched = false;
        menuTouched = false;
        // turn off the motor
        motorTurnedOn = false;
        return !gameOver;
    }

    public boolean canBeFocused() {
		return true;
	}

    private class FlipCounter {
        int step = 0;
        boolean flipDirection = false;
        boolean prevFlipDirection = false;

        void tick() {
            if (DebugMenu.dontCountFlips) {
                return;
            }
            flipDirection = world.carbody.rotationVelocity2FX() >= 0;
            if (flipDirection != prevFlipDirection || timeFlying < 1 && !uninterestingDebug) {
                step = 0;
            }
            prevFlipDirection = flipDirection;

            int ang = carAngle;
            boolean isInNormalPos = ang < 45 || ang > 315;
            if (isInNormalPos && step % 2 == 0) {
                step++;
                if (step > 1) {
                    if (flipDirection) {
                        if ((step - 1) % 4 == 0) {
                            afterFlip();
                        }
                    } else {
                        afterFlip();
                    }
                }
            } else if (!isInNormalPos && step % 2 != 0) {
                step++;
            }
        }

        // blink point counter after flip and increment the score
        public void afterFlip() {
            flipIndicator = 0;
            points++;
        }
    }
}
