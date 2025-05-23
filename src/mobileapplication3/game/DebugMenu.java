/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mobileapplication3.game;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Sound;
import mobileapplication3.platform.ui.RootContainer;
import utils.MobappGameSettings;

/**
 *
 * @author vipaol
 */
public class DebugMenu extends GenericMenu implements Runnable {
    private static final String[] MENU_OPTS = {
        "Enable debug",
        "Show log",
        "Structure debug",
        "Simulation mode",
        "GAMING MODE",
        "What?",
        "Physics precision",
        "Music",
        "Back"
    };

    public static boolean isDebugEnabled = false;
    public static boolean closerWorldgen = false;
    public static boolean coordinates = false;
    public static boolean discoMode = false;
    public static boolean speedo = false;
    public static boolean cheat = false;
    public static boolean music = false;
    public static boolean showFontSize = false;
    public static boolean mgstructOnly = false;
    public static boolean dontCountFlips = false;
    public static boolean showAngle = false;
    public static boolean showLinePoints = false;
    public static boolean simulationMode = false;
    public static boolean whatTheGame = false;
    public static boolean showContacts = false;
    public static boolean structureDebug = false;
    
    public DebugMenu() {
        loadParams(MENU_OPTS);
        loadStatemap(new int[MENU_OPTS.length]);
	}
    
    public void init() {
        getFontSize();
        setSpecialOption(0);
        refreshStates();
        (new Thread(this, "debug menu")).start();
    }

    public void run() {
        long sleep;
        long start;

        while (!isStopped) {
            if (!isPaused) {
                start = System.currentTimeMillis();
                
                repaint();
                tick();

                sleep = GameplayCanvas.TICK_DURATION - (System.currentTimeMillis() - start);
                sleep = Math.max(sleep, 0);
            } else {
                sleep = 200;
            }
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    void selectPressed() {
        int selected = this.selected;
        switch (selected) {
            case 0:
                isDebugEnabled = !isDebugEnabled;
                coordinates = isDebugEnabled;
                setIsSpecialOptnActivated(isDebugEnabled);
                Logger.logToStdout(isDebugEnabled);
                break;
            case 1:
            	RootContainer.enableOnScreenLog = !RootContainer.enableOnScreenLog;
                if (RootContainer.enableOnScreenLog) {
                    Logger.enableOnScreenLog(h);
                } else {
                    Logger.disableOnScreenLog();
                }
                break;
            case 2:
                structureDebug = !structureDebug;
                break;
            case 3:
                simulationMode = !simulationMode;
                break;
            case 4:
                discoMode = !discoMode;
                GraphicsWorld.bgOverride = discoMode;
                break;
            case 5:
                whatTheGame = !whatTheGame;
                break;
            case 6:
                int value = MobappGameSettings.getPhysicsPrecision();
                if (value == MobappGameSettings.AUTO_PHYSICS_PRECISION) {/*
                    value = MobappGameSettings.DYNAMIC_PHYSICS_PRECISION;
                } else if (value == MobappGameSettings.DYNAMIC_PHYSICS_PRECISION) {*/
                    value = 1;
                } else {
                    value *= 2;
                    if (value > MobappGameSettings.MAX_PHYSICS_PRECISION) {
                        value = MobappGameSettings.AUTO_PHYSICS_PRECISION;
                    }
                }
                MobappGameSettings.setPhysicsPrecision(value);
                break;
            case 7:
                music = !music;
                if (music) {
                    Sound sound = new Sound();
                    sound.start();
                }   break;
            default:
                break;
        }
        if (selected == MENU_OPTS.length - 1) {
            isStopped = true;
            RootContainer.setRootUIComponent(new SettingsScreen());
        } else {
            refreshStates();
        }
    }
    void refreshStates() {
        int physicsPrecision = MobappGameSettings.getPhysicsPrecision();
        MENU_OPTS[6] = "Physics precision: ";
        if (physicsPrecision == MobappGameSettings.AUTO_PHYSICS_PRECISION) {
            MENU_OPTS[6] += "Auto";
        } else if (physicsPrecision == MobappGameSettings.DYNAMIC_PHYSICS_PRECISION) {
            MENU_OPTS[6] += "Dynamic";
        } else {
            MENU_OPTS[6] += String.valueOf(physicsPrecision);
        }
        setIsSpecialOptnActivated(DebugMenu.isDebugEnabled);
        setEnabledFor(RootContainer.enableOnScreenLog, 1);
        setEnabledFor(structureDebug, 2);
        setEnabledFor(simulationMode, 3);
        setEnabledFor(discoMode, 4);
        setEnabledFor(whatTheGame, 5);
        setEnabledFor(physicsPrecision != MobappGameSettings.DEFAULT_PHYSICS_PRECISION, 6);
        setStateFor(/*music*/GenericMenu.STATE_INACTIVE, 7); // disable this option. it's not ready yet
    }
}
