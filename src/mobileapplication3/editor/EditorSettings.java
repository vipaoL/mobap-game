/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor;

import mobileapplication3.platform.Settings;

/**
 *
 * @author vipaol
 */
public class EditorSettings {
    public static final String
    		RECORD_STORE_SETTINGS = "editorsettings",

            IS_SETUP_WIZARD_COMPLETED = "setupDone",
            GAME_FOLDER_PATH = "MGPath",
            KB_SMOOTH_SCROLLING = "kbSmScrl",
            TRANSPARENCY = "trnspcy",
            LISTS_KEY_REPEATS = "listKRepeats",
            AUTO_SAVE = "autoSave",
            WHAT_TO_LOAD_AUTOMATICALLY = "alwLoad",
    		SHOW_LOG = "showLog",
    		KINETIC_SCROLL = "knScrl";

    public static final int
            OPTION_ALWAYS_LOAD_NONE = 0,
            OPTION_ALWAYS_LOAD_LIST = 1,
            OPTION_ALWAYS_LOAD_THUMBNAILS = 2;

    private static String gameFolderPath = null;
    private static int whatToLoadAutomatically = -1;

    private static Settings settingsInst = null;

    public static void resetSettings() {
    	getSettingsInst().resetSettings();
    }

    private static Settings getSettingsInst() {
    	if (settingsInst == null) {
    		settingsInst = new Settings(new String[]{
    	            IS_SETUP_WIZARD_COMPLETED,
    	            GAME_FOLDER_PATH,
                    KB_SMOOTH_SCROLLING,
                    TRANSPARENCY,
    	            LISTS_KEY_REPEATS,
    	            AUTO_SAVE,
                    WHAT_TO_LOAD_AUTOMATICALLY,
                    SHOW_LOG,
                    KINETIC_SCROLL
    	        }, RECORD_STORE_SETTINGS);
    	}
    	return settingsInst;
    }

    ///

    public static boolean getKineticScrollingEnabled() {
        return getSettingsInst().getBool(KINETIC_SCROLL);
    }

    public static boolean getKineticScrollingEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(KINETIC_SCROLL, defaultValue);
    }

    public static void setKineticScrollingEnabled(boolean b) {
    	getSettingsInst().set(KINETIC_SCROLL, b);
    }

    ///

    public static boolean getOnScreenLogEnabled() {
        return getSettingsInst().getBool(SHOW_LOG);
    }

    public static boolean getOnScreenLogEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(SHOW_LOG, defaultValue);
    }

    public static void setOnScreenLogEnabled(boolean b) {
    	getSettingsInst().set(SHOW_LOG, b);
    }

    ///

    public static int getWhatToLoadAutomatically() {
        if (whatToLoadAutomatically < 0) {
            whatToLoadAutomatically = getSettingsInst().getInt(WHAT_TO_LOAD_AUTOMATICALLY, OPTION_ALWAYS_LOAD_NONE);
        }

        return whatToLoadAutomatically;
    }

    public static void setWhatToLoadAutomatically(int option) {
        getSettingsInst().set(WHAT_TO_LOAD_AUTOMATICALLY, String.valueOf(option));
        whatToLoadAutomatically = option;
    }

    ///

    public static boolean getAutoSaveEnabled() {
        return getSettingsInst().getBool(AUTO_SAVE);
    }

    public static boolean getAutoSaveEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(AUTO_SAVE, defaultValue);
    }

    public static void setAutoSaveEnabled(boolean b) {
    	getSettingsInst().set(AUTO_SAVE, b);
    }

    public static boolean toggleAutoSaveEnabled() {
    	return getSettingsInst().toggleBool(AUTO_SAVE);
    }

    ///

    public static boolean getKeyRepeatedInListsEnabled() {
        return getSettingsInst().getBool(LISTS_KEY_REPEATS);
    }

    public static boolean getKeyRepeatedInListsEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(LISTS_KEY_REPEATS, defaultValue);
    }

    public static void setKeyRepeatedInListsEnabled(boolean b) {
    	getSettingsInst().set(LISTS_KEY_REPEATS, b);
    }

    public static boolean toggleKeyRepeatedInListsEnabled() {
    	return getSettingsInst().toggleBool(LISTS_KEY_REPEATS);
    }

    ///

    public static boolean getTransparencyEnabled() {
        return getSettingsInst().getBool(TRANSPARENCY);
    }

    public static boolean getTransparencyEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(TRANSPARENCY, defaultValue);
    }

    public static void setTransparencyEnabled(boolean b) {
        getSettingsInst().set(TRANSPARENCY, b);
    }

    public static boolean toggleTransparency() {
        return getSettingsInst().toggleBool(TRANSPARENCY);
    }

    ///

    public static boolean getKbSmoothScrollingEnabled() {
        return getSettingsInst().getBool(KB_SMOOTH_SCROLLING);
    }

    public static boolean getKbSmoothScrollingEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(KB_SMOOTH_SCROLLING, defaultValue);
    }

    public static void setKbSmoothScrollingEnabled(boolean b) {
    	getSettingsInst().set(KB_SMOOTH_SCROLLING, b);
    }

///

    public static String getLevelsFolderPath() {
    	return getLevelsFolderPath(EditorSettings.getGameFolderPath());
    }

    public static String getLevelsFolderPath(String gameFolderPath) {
    	return gameFolderPath + "Levels/";
    }

    public static String getStructsFolderPath() {
    	return getStructsFolderPath(EditorSettings.getGameFolderPath());
    }

    public static String getStructsFolderPath(String gameFolderPath) {
    	return gameFolderPath + "MGStructs/";
    }

    public static String getGameFolderPath() {
        if (gameFolderPath == null) {
            gameFolderPath = getSettingsInst().getStr(GAME_FOLDER_PATH);
        }
        return gameFolderPath;
    }

    public static void setGameFolderPath(String path) {
    	getSettingsInst().set(GAME_FOLDER_PATH, path);
    	gameFolderPath = path;
    }

    ///

    public static boolean isSetupWizardCompleted() {
        return getSettingsInst().getBool(IS_SETUP_WIZARD_COMPLETED);
    }

    public static void setIsSetupWizardCompleted(boolean b) {
    	getSettingsInst().set(IS_SETUP_WIZARD_COMPLETED, b);
    }
}
