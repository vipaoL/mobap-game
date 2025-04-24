package utils;

import mobileapplication3.game.GraphicsWorld;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.Settings;

public class MobappGameSettings {
    private static final String
    		RECORD_STORE_SETTINGS = "gamesettings",
		    GRAPHICS_FOR_HIRES = "btrGr",
            LANDSCAPE_COLOR = "landscapeCol",
            LEGACY_DRAWING_METHOD = "oldDrawing",
            PHYSICS_PRECISION = "PhyPrecision",
            DETAIL_LEVEL = "DetailLvl",
            FRAME_TIME = "FrameTime",
		    SHOW_FPS = "showFPS",
		    SHOW_BG = "enBG",
		    BATTERY_INDICATOR = "Batt";

    public static final int DYNAMIC_PHYSICS_PRECISION = -1, AUTO_PHYSICS_PRECISION = 0;
    public static final int
            DEFAULT_PHYSICS_PRECISION = AUTO_PHYSICS_PRECISION,
            DEFAULT_DETAIL_LEVEL = 1,
            DEFAULT_FRAME_TIME = 16;
    public static final int MAX_PHYSICS_PRECISION = 16, MAX_DETAIL_LEVEL = 3, MAX_FRAME_TIME = 100;

    private static String mgstructsFolderPath = null;
    private static String detailLevel = Settings.UNDEF;

    private static Settings settingsInst = null;

    private MobappGameSettings() { }

    private static Settings getSettingsInst() {
    	if (settingsInst == null) {
    		settingsInst = new Settings(new String[]{
                    GRAPHICS_FOR_HIRES,
                    LANDSCAPE_COLOR,
                    LEGACY_DRAWING_METHOD,
                    PHYSICS_PRECISION,
                    DETAIL_LEVEL,
                    FRAME_TIME,
    	            SHOW_FPS,
    	            SHOW_BG,
    	            BATTERY_INDICATOR
    	        }, RECORD_STORE_SETTINGS);
    	}
    	return settingsInst;
    }

    public static boolean isBattIndicatorEnabled() {
        return getSettingsInst().getBool(BATTERY_INDICATOR);
    }

    public static boolean isBattIndicatorEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(BATTERY_INDICATOR, defaultValue);
    }

    public static void setBattIndicatorEnabled(boolean b) {
    	getSettingsInst().set(BATTERY_INDICATOR, b);
    }

    public static boolean toggleBattIndicator() {
    	return getSettingsInst().toggleBool(BATTERY_INDICATOR);
    }

    ///

    public static boolean isBGEnabled() {
        return getSettingsInst().getBool(SHOW_BG);
    }

    public static boolean isBGEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(SHOW_BG, defaultValue);
    }

    public static void setBGEnabled(boolean b) {
    	getSettingsInst().set(SHOW_BG, b);
    }

    public static boolean toggleBG() {
    	return getSettingsInst().toggleBool(SHOW_BG);
    }

    ///

    public static boolean isFPSShown() {
        return getSettingsInst().getBool(SHOW_FPS);
    }

    public static boolean isFPSShown(boolean defaultValue) {
        return getSettingsInst().getBool(SHOW_FPS, defaultValue);
    }

    public static void setFPSShown(boolean b) {
    	getSettingsInst().set(SHOW_FPS, b);
    }

    public static boolean toggleFPSShown() {
    	return getSettingsInst().toggleBool(SHOW_FPS);
    }

    ///

    public static boolean isBetterGraphicsEnabled() {
        return getSettingsInst().getBool(GRAPHICS_FOR_HIRES);
    }

    public static boolean isBetterGraphicsEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(GRAPHICS_FOR_HIRES, defaultValue);
    }

    public static void setBetterGraphicsEnabled(boolean b) {
        getSettingsInst().set(GRAPHICS_FOR_HIRES, b);
    }

    public static boolean toggleBetterGraphics() {
        return getSettingsInst().toggleBool(GRAPHICS_FOR_HIRES);
    }

    ///

    public static int getLandscapeColor() {
        return getSettingsInst().getInt(LANDSCAPE_COLOR, GraphicsWorld.DEFAULT_LANDSCAPE_COLOR);
    }

    public static void setLandscapeColor(int value) {
        getSettingsInst().set(LANDSCAPE_COLOR, String.valueOf(value));
    }

    ///

    public static int getPhysicsPrecision() {
        return getSettingsInst().getInt(PHYSICS_PRECISION, DEFAULT_PHYSICS_PRECISION);
    }

    public static void setPhysicsPrecision(int value) {
        getSettingsInst().set(PHYSICS_PRECISION, String.valueOf(value));
    }

    ///

    public static int getDetailLevel() {
        int value;
        if (detailLevel.equals(Settings.UNDEF)) {
            value = getSettingsInst().getInt(DETAIL_LEVEL, DEFAULT_DETAIL_LEVEL);
            detailLevel = String.valueOf(value);
        } else {
            value = Integer.valueOf(detailLevel).intValue();
        }
        return value;
    }

    public static void setDetailLevel(int value) {
        detailLevel = String.valueOf(value);
        getSettingsInst().set(DETAIL_LEVEL, String.valueOf(value));
    }

    ///

    public static int getFrameTime() {
        return Mathh.constrain(1, getSettingsInst().getInt(FRAME_TIME, DEFAULT_FRAME_TIME), MAX_FRAME_TIME);
    }

    public static void setFrameTime(int valueMs) {
        getSettingsInst().set(FRAME_TIME, String.valueOf(Mathh.constrain(1, valueMs, MAX_FRAME_TIME)));
    }

    ///

    public static boolean isLegacyDrawingMethodEnabled() {
        return getSettingsInst().getBool(LEGACY_DRAWING_METHOD);
    }

    public static boolean isLegacyDrawingMethodEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(LEGACY_DRAWING_METHOD, defaultValue);
    }

    public static void setLegacyDrawingMethodEnabled(boolean b) {
        getSettingsInst().set(LEGACY_DRAWING_METHOD, b);
    }

    public static boolean toggleLegacyDrawingMethod() {
        return getSettingsInst().toggleBool(LEGACY_DRAWING_METHOD);
    }

    ///
}
