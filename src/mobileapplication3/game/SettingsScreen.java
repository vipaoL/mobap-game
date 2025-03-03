package mobileapplication3.game;

import mobileapplication3.platform.Battery;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.ui.RootContainer;
import utils.MobappGameSettings;

public class SettingsScreen extends GenericMenu implements Runnable {
    private static final int
            PHYSICS_PRECISION = 0,
            DETAIL_LEVEL = 1,
            HI_RES_GRAPHICS = 2,
            SHOW_FPS = 3,
            BG = 4,
            BATTERY = 5,
            DEBUG = 6,
            ABOUT = 7,
            BACK = 8;

    private static String[] menuOpts = new String[BACK + 1];
        
        // array with states of all buttons (active/inactive/enabled)
        private final int[] statemap = new int[menuOpts.length];
        private boolean batFailed = false;
        
        public SettingsScreen() {
        	loadParams(menuOpts, statemap);
		}
        
        public void init() {
            getFontSize();
            
            setSpecialOption(DEBUG); // highlight "Debug settings" if enabled
            setIsSpecialOptnActivated(DebugMenu.isDebugEnabled);
            
            refreshStates();
            (new Thread(this, "debug menu")).start();
        }

        public void run() {
            long sleep;
            long start;
            
            if (!isMenuInited()) {
                init();
            }

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
                case PHYSICS_PRECISION:
                    int value = MobappGameSettings.getPhysicsPrecision() * 2;
                    if (value > MobappGameSettings.MAX_PHYSICS_PRECISION) {
                        value = MobappGameSettings.DYNAMIC_PHYSICS_PRECISION;
                    } else if (value == MobappGameSettings.DYNAMIC_PHYSICS_PRECISION) {
                        value = 1;
                    }
                    MobappGameSettings.setPhysicsPrecision(value);
                    break;
                case DETAIL_LEVEL:
                    MobappGameSettings.setDetailLevel(MobappGameSettings.getDetailLevel() % MobappGameSettings.MAX_DETAIL_LEVEL + 1);
                    break;
                case HI_RES_GRAPHICS:
                    MobappGameSettings.toggleBetterGraphics();
                    break;
                case SHOW_FPS:
                	MobappGameSettings.toggleFPSShown();
                	break;
                case BG:
                	MobappGameSettings.toggleBG();
                	break;
                case BATTERY:
                	if (!MobappGameSettings.isBattIndicatorEnabled()) {
                		if (!Battery.checkAndInit()) {
                			batFailed = true;
                			Logger.log("Battery init failed");
                    		break;
                    	} else {
                    		int batLevel = Battery.getBatteryLevel();
                    		if (batLevel == Battery.ERROR) {
                    			String err = "Can't get battery level";
                    			menuOpts[selected] = err;
                    			Logger.log(err);
                    			break;
                    		} else {
                    			menuOpts[selected] = "Battery: " + batLevel + "%";
                    			Logger.log("bat method: " + Battery.getMethod());
                    		}
                    	}
                	}
                	MobappGameSettings.toggleBattIndicator();
                	break;
                case DEBUG:
                    isStopped = true;
                    RootContainer.setRootUIComponent(new DebugMenu());
                    return;
                case ABOUT:
                    isStopped = true;
                    RootContainer.setRootUIComponent(new AboutScreen());
                    return;
                case BACK:
                    isStopped = true;
                    RootContainer.setRootUIComponent(new MenuCanvas());
                    return;
                default:
                    break;
            }
            refreshStates();
        }

        void refreshStates() {
            int physicsPrecision = MobappGameSettings.getPhysicsPrecision();
            int detailLvl = MobappGameSettings.getDetailLevel();
            menuOpts[PHYSICS_PRECISION] = "Physics precision: " + (physicsPrecision == 0 ? "Dynamic" : String.valueOf(physicsPrecision));
            menuOpts[DETAIL_LEVEL] = "Detail level: " + detailLvl;
            menuOpts[HI_RES_GRAPHICS] = "Graphics for hi-res screens";
            menuOpts[SHOW_FPS] = "Show FPS";
            menuOpts[BG] = "Enable background";
            menuOpts[BATTERY] = "Show battery level";
            menuOpts[DEBUG] = "Debug settings";
            menuOpts[ABOUT] = "About";
            menuOpts[BACK] = "Back";
            setEnabledFor(physicsPrecision != MobappGameSettings.DEFAULT_PHYSICS_PRECISION, PHYSICS_PRECISION);
            setEnabledFor(detailLvl != MobappGameSettings.DEFAULT_DETAIL_LEVEL, DETAIL_LEVEL);
        	setEnabledFor(MobappGameSettings.isBetterGraphicsEnabled(), HI_RES_GRAPHICS);
        	setEnabledFor(MobappGameSettings.isFPSShown(), SHOW_FPS);
        	setEnabledFor(MobappGameSettings.isBGEnabled(), BG);
        	if (!batFailed) {
        		setEnabledFor(MobappGameSettings.isBattIndicatorEnabled(), BATTERY);
        	} else {
        		setStateFor(STATE_INACTIVE, BATTERY);
        	}
        }
    }