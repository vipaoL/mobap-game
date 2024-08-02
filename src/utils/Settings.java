/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import mobileapplication3.Main;

/**
 *
 * @author vipaol
 */
public class Settings {
    public static final String
            TRUE = "1",
            FALSE = "0",
            IS_SETUP_WIZARD_COMPLETED = "wizardCompleted",
    		MGSTRUCTS_FOLDER_PATH = "mgPath",
            IS_BETTER_GRAPHICS_ENABLED = "btrGr",
            ANIMS = "anims";
    
    private static char SEP = '\n';
    
    private static String mgstructsFolderPath = null;
    
    private static String[] settingsKeysVals;

    public static boolean isSetupWizardCompleted() {
        return getBool(IS_SETUP_WIZARD_COMPLETED);
    }
    
    public static boolean isBetterGraphicsEnabled() {
        return getBool(IS_BETTER_GRAPHICS_ENABLED);
    }
    
    public static boolean isBetterGraphicsEnabled(boolean defaultValue) {
        return getBool(IS_BETTER_GRAPHICS_ENABLED, defaultValue);
    }
    
    public static void setBetterGraphicsEnabled(boolean b) {
    	setBool(IS_BETTER_GRAPHICS_ENABLED, b);
        saveToRMS();
    }
    
    public static String getMgstructsFolderPath() {
        if (mgstructsFolderPath == null) {
            mgstructsFolderPath = getSettingValue(MGSTRUCTS_FOLDER_PATH);
        }
        
        System.out.println("mgstructsFolderPath=" + mgstructsFolderPath);
        return mgstructsFolderPath;
    }

    public static void setMgstructsFolderPath(String path) {
        setStr(MGSTRUCTS_FOLDER_PATH, path);
        saveToRMS();
    }

    public static void setIsSetupWizardCompleted(boolean b) {
        setBool(IS_SETUP_WIZARD_COMPLETED, b);
        saveToRMS();
    }
    
    public static void saveToRMS() {
        if (!RecordStores.writeStringToStore(getCurrentSettingsAsStr(), RecordStores.RECORD_STORE_NAME_SETTINGS)) {
            Main.set(new Alert("Error!", "Can't save settings to RMS", null, AlertType.ERROR));
        }
    }
    
    public static void loadDefaults() {
        System.out.println("loading default settings");
        settingsKeysVals = new String[]{
            IS_SETUP_WIZARD_COMPLETED, FALSE,
            IS_BETTER_GRAPHICS_ENABLED, null,
            MGSTRUCTS_FOLDER_PATH, null,
            ANIMS, TRUE
        };
        mgstructsFolderPath = null;
    }
    
    public static void loadFromRMS() {
        loadFromString(RecordStores.readStringFromStore(RecordStores.RECORD_STORE_NAME_SETTINGS));
    }
    
    public static void loadFromString(String str) {
        loadDefaults();
        System.out.println("loading settings from string: " + str);
        if (str == null) {
            return;
        }
        
        String[] keyValueCouples = Utils.split(str.substring(0, str.length() - 1), "" + SEP);
        //settingsKeysVals = new String[(keyValueCouples.length) * 2];
        for (int i = 0; i < keyValueCouples.length; i++) {
            System.out.println("reading setting: " + keyValueCouples[i]);
            int splitterIndex = keyValueCouples[i].indexOf(' ');
            String key = keyValueCouples[i].substring(0, splitterIndex);
            String value = keyValueCouples[i].substring(splitterIndex + 1);
            for (int j = 0; j < settingsKeysVals.length / 2; j++) {
                if (key.equals(settingsKeysVals[j*2])) {
                    settingsKeysVals[i*2 + 1] = value;
                }
            }
            
            //settingsKeysVals[i*2] = key;
            //settingsKeysVals[i*2 + 1] = value;
        }
    }
    
    public static String getCurrentSettingsAsStr() {
        if (settingsKeysVals == null) {
            System.out.println("null. loading from RMS");
            loadFromRMS();
        }
        
        System.out.println("packing current settings to string. length*2=" + settingsKeysVals.length);
        
        //assert ((settingsKeysVals.length % 2) == 0);
        StringBuffer sb = new StringBuffer(settingsKeysVals.length*5);
        for (int i = 0; i < settingsKeysVals.length / 2; i++) {
            sb.append(settingsKeysVals[i*2]);
            sb.append(" ");
            sb.append(settingsKeysVals[i*2 + 1]);
            sb.append(SEP);
        }
        System.out.println("settings packed (strlen=" + sb.length() + "): " + sb.toString());
        return sb.toString();
    }
    
    public static void toggleBool(String key) {
        boolean currentValue = getBool(key);
        setBool(key, !currentValue);
    }
    
    // TODO move to exceptions
    public static boolean setBool(String key, boolean value) {
        return setStr(key, toStr(value));
    }
    // TODO move to exceptions
    public static boolean setStr(String key, String value) {
        if (settingsKeysVals == null) {
            loadFromRMS();
        }
        
        for (int i = 0; i < settingsKeysVals.length / 2; i++) {
            if (settingsKeysVals[i*2].equals(key)) {
                settingsKeysVals[i*2 + 1] = value;
                saveToRMS();
                return true;
            }
        }
        
        return false;
    }
    
    public static String getSettingValue(String key) {
        if (settingsKeysVals == null) {
            loadFromRMS();
        }
        
        System.out.println("length = " + settingsKeysVals.length);
        
        for (int i = 0; i < settingsKeysVals.length / 2; i++) {
            System.out.println("i=" + i
                    + " key=" + settingsKeysVals[i*2]
                    + " value=" + settingsKeysVals[i*2 + 1]);
            if (settingsKeysVals[i*2].equals(key)) {
                return settingsKeysVals[i*2 + 1];
            }
        }
        return null;
    }
    
    public static boolean getBool(String key) {
    	return TRUE.equals(getSettingValue(key));
    }
    
    public static boolean getBool(String key, boolean defaultValue) {
    	String value = getSettingValue(key);
    	if (value == null) {
    		setBool(key, defaultValue);
    		return TRUE.equals(getSettingValue(key));
    	}
        return TRUE.equals(value);
    }
    
    public static int getInt(String key) {
        return Integer.parseInt(getSettingValue(key));
    }
    
    private static String toStr(boolean b) {
        return b ? "1" : "0";
    }
}
