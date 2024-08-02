package utils;

public class MobappGameSettings {
    private static final String
		    IS_SETUP_WIZARD_COMPLETED = "wizardCompleted",
			MGSTRUCTS_FOLDER_PATH = "mgPath",
		    IS_BETTER_GRAPHICS_ENABLED = "btrGr",
		    ANIMS = "anims";
    
    private static String mgstructsFolderPath = null;
    
    private static Settings settingsInst = null;
    
    private static Settings getSettingsInst() {
    	if (settingsInst == null) {
    		settingsInst = new Settings(new String[]{
    	            IS_SETUP_WIZARD_COMPLETED,
    	            IS_BETTER_GRAPHICS_ENABLED,
    	            MGSTRUCTS_FOLDER_PATH,
    	            ANIMS
    	        });
    	}
    	return settingsInst;
    }
    
    public static boolean isSetupWizardCompleted() {
        return getSettingsInst().getBool(IS_SETUP_WIZARD_COMPLETED);
    }
    
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

    public static void setIsSetupWizardCompleted(boolean b) {
    	getSettingsInst().set(IS_SETUP_WIZARD_COMPLETED, b);
    }
}
