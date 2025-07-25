package mobileapplication3.game;

import mobileapplication3.platform.Battery;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.IUIComponent;
import utils.MobappGameSettings;

import java.util.Random;

public class SettingsScreen extends GenericMenu implements Runnable {
    private static final int
            LANDSCAPE_COLOR = 0,
            HI_RES_GRAPHICS = 1,
            LEGACY_DRAWING_METHOD = 2,
            FRAME_TIME = 3,
            SHOW_FPS = 4,
            BATTERY = 5,
            DEBUG = 6,
            PLATFORM_SETTINGS = 7,
            ABOUT = 8,
            BACK = 9;

    private static final int[] LANDSCAPE_COLORS = {GraphicsWorld.DEFAULT_LANDSCAPE_COLOR, 0x44aaff, 0xaaaaff, 0xffffff, 0x44ffaa, 0xaaff44, 0xff44aa, 0xffaa44};
    private static final String[] LANDSCAPE_COLOR_NAMES = {"blue", "light blue", "pale blue", "white", "light green", "lime", "pink", "orange"};

    private static String[] menuOpts = new String[BACK + 1];

    // array with states of all buttons (active/inactive/enabled)
    private final int[] statemap = new int[menuOpts.length];
    private boolean batFailed = false;

    public SettingsScreen() {
        bgColor = COLOR_TRANSPARENT;
        loadParams(menuOpts);
        loadStatemap(statemap);
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

    protected void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        if (selected == LANDSCAPE_COLOR) {
            // show landscape color
            int color = MobappGameSettings.getLandscapeColor();
            Random random = new Random(color);
            int minScreenSide = Math.min(w, h);
            int d = minScreenSide / 16 + random.nextInt(minScreenSide / 8);
            int x;
            do {
                x = random.nextInt(w - d);
            } while (Mathh.strictIneq(w / 3, x + d / 2, w * 2 / 3));
            int y = random.nextInt(h - d);
            g.setColor(color);
            g.fillArc(x, y, d, d, 0, 360);
        }
        super.onPaint(g, x0, y0, w, h, forceInactive);
    }

    void selectPressed() {
        int selected = this.selected;
        int value;
        switch (selected) {
            case LEGACY_DRAWING_METHOD:
                MobappGameSettings.toggleLegacyDrawingMethod();
                break;
            case FRAME_TIME:
                value = MobappGameSettings.getFrameTime();
                int newFrameTime = value;
                if (newFrameTime <= 1) {
                    newFrameTime = MobappGameSettings.MAX_FRAME_TIME;
                } else {
                    int prevFps = 1000 / value;
                    while (1000 / newFrameTime <= prevFps) {
                        newFrameTime--;
                    }
                }
                MobappGameSettings.setFrameTime(newFrameTime);
                break;
            case HI_RES_GRAPHICS:
                MobappGameSettings.toggleBetterGraphics();
                break;
            case SHOW_FPS:
                MobappGameSettings.toggleFPSShown();
                break;
            case LANDSCAPE_COLOR:
                MobappGameSettings.setLandscapeColor(LANDSCAPE_COLORS[(findArrayIndex(LANDSCAPE_COLORS, MobappGameSettings.getLandscapeColor()) + 1) % LANDSCAPE_COLORS.length]);
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
            case PLATFORM_SETTINGS:
                RootContainer.setRootUIComponent(getPlatformSettings());
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
        int frameTime = MobappGameSettings.getFrameTime();
        menuOpts[LEGACY_DRAWING_METHOD] = "Legacy drawing method";
        menuOpts[FRAME_TIME] = "FPS: " + round(1000f / frameTime) + " (" + frameTime + "ms/frame)";
        menuOpts[HI_RES_GRAPHICS] = "Graphics for hi-res screens";
        menuOpts[SHOW_FPS] = "Show FPS";
        menuOpts[LANDSCAPE_COLOR] = "Landscape color: " + LANDSCAPE_COLOR_NAMES[findArrayIndex(LANDSCAPE_COLORS, MobappGameSettings.getLandscapeColor())];
        menuOpts[BATTERY] = "Show battery level";
        menuOpts[DEBUG] = "Debug settings";
        menuOpts[PLATFORM_SETTINGS] = "Platform settings";
        menuOpts[ABOUT] = "About";
        menuOpts[BACK] = "Back";
        setEnabledFor(frameTime != MobappGameSettings.DEFAULT_FRAME_TIME, FRAME_TIME);
        setEnabledFor(MobappGameSettings.isLegacyDrawingMethodEnabled(), LEGACY_DRAWING_METHOD);
        setEnabledFor(MobappGameSettings.isBetterGraphicsEnabled(), HI_RES_GRAPHICS);
        setEnabledFor(MobappGameSettings.isFPSShown(), SHOW_FPS);
        setEnabledFor(findArrayIndex(LANDSCAPE_COLORS, MobappGameSettings.getLandscapeColor()) != 0, LANDSCAPE_COLOR);
        if (!batFailed) {
            setEnabledFor(MobappGameSettings.isBattIndicatorEnabled(), BATTERY);
        } else {
            setStateFor(STATE_INACTIVE, BATTERY);
        }
        try {
            Class.forName("PlatformSettingsScreen");
            menuOpts[PLATFORM_SETTINGS] = String.valueOf(getPlatformSettings());
        } catch (ClassNotFoundException ex) {
            setStateFor(STATE_INACTIVE, PLATFORM_SETTINGS);
        }
    }

    // round to two decimal places
    private double round(float d) {
        return (Math.floor(d * 100 + 0.5)) / 100;
    }

    private int findArrayIndex(int[] arr, int a) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == a) {
                return i;
            }
        }
        return 0;
    }

    private static IUIComponent getPlatformSettings() {
        try {
            return (IUIComponent) Class.forName("PlatformSettingsScreen").newInstance();
        } catch (Exception ex) {
            Platform.showError(ex);
            return null;
        }
    }
}