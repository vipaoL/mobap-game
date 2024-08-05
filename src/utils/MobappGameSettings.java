package utils;

public class MobappGameSettings {
    private static final String
		    IS_SETUP_WIZARD_COMPLETED = "wizardCompleted",
			MGSTRUCTS_FOLDER_PATH = "mgPath",
		    IS_BETTER_GRAPHICS_ENABLED = "btrGr",
    		IS_FPS_UNLOCKED = "unlFPS",
		    SHOW_FPS = "showFPS",
		    SKIP_SECOND_FRAMES = "skipSecFrames",
		    SHOW_BG = "enBG";
    
    private static String mgstructsFolderPath = null;
    
    private static Settings settingsInst = null;
    
    private static Settings getSettingsInst() {
    	if (settingsInst == null) {
    		settingsInst = new Settings(new String[]{
    	            IS_SETUP_WIZARD_COMPLETED,
    	            MGSTRUCTS_FOLDER_PATH,
    	            IS_BETTER_GRAPHICS_ENABLED,
    	            IS_FPS_UNLOCKED,
    	            SHOW_FPS,
    	            SKIP_SECOND_FRAMES,
    	            SHOW_BG
    	        });
    	}
    	return settingsInst;
    }
    
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
    
    
    public static boolean isSecFramesSkipEnabled() {
        return getSettingsInst().getBool(SKIP_SECOND_FRAMES);
    }
    
    public static boolean isSecFramesSkipEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(SKIP_SECOND_FRAMES, defaultValue);
    }
    
    public static void setSecFramesSkipEnabled(boolean b) {
    	getSettingsInst().set(SKIP_SECOND_FRAMES, b);
    }
    
    public static boolean toggleSecFramesSkipEnabled() {
    	return getSettingsInst().toggleBool(SKIP_SECOND_FRAMES);
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
    
    public static boolean isFPSUnlocked() {
        return getSettingsInst().getBool(IS_FPS_UNLOCKED);
    }
    
    public static boolean isFPSUnlocked(boolean defaultValue) {
        return getSettingsInst().getBool(IS_FPS_UNLOCKED, defaultValue);
    }
    
    public static void setFPSUnlocked(boolean b) {
    	getSettingsInst().set(IS_FPS_UNLOCKED, b);
    }
    
    public static boolean toggleFPSUnlocked() {
    	return getSettingsInst().toggleBool(IS_FPS_UNLOCKED);
    }
    
    ///
    
    public static boolean isBetterGraphicsEnabled() {
        return getSettingsInst().getBool(IS_BETTER_GRAPHICS_ENABLED);
    }
    
    public static boolean isBetterGraphicsEnabled(boolean defaultValue) {
        return getSettingsInst().getBool(IS_BETTER_GRAPHICS_ENABLED, defaultValue);
    }
    
    public static void setBetterGraphicsEnabled(boolean b) {
    	getSettingsInst().set(IS_BETTER_GRAPHICS_ENABLED, b);
    }
    
    public static boolean toggleBetterGraphics() {
    	return getSettingsInst().toggleBool(IS_BETTER_GRAPHICS_ENABLED);
    }
    
    ///
    
    public static String getMgstructsFolderPath() {
        if (mgstructsFolderPath == null) {
            mgstructsFolderPath = getSettingsInst().getStr(MGSTRUCTS_FOLDER_PATH);
        }
        
        Logger.log("mgstructsFolderPath=" + mgstructsFolderPath);
        return mgstructsFolderPath;
    }

    public static void setMgstructsFolderPath(String path) {
    	getSettingsInst().set(MGSTRUCTS_FOLDER_PATH, path);
    }
    
    ///
    
    public static boolean isSetupWizardCompleted() {
        return getSettingsInst().getBool(IS_SETUP_WIZARD_COMPLETED);
    }

    public static void setIsSetupWizardCompleted(boolean b) {
    	getSettingsInst().set(IS_SETUP_WIZARD_COMPLETED, b);
    }
}
