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
            UNDEF = "";
    
    private static char SEP = '\n';
    
    private String[] settingsKeysVals;
    private String[] keys;
    
    public Settings(String[] keys) {
    	Logger.log("settings init: " + Utils.arrayToString(keys));
    	this.keys = keys;
    }
    
    public void saveToRMS() {
        if (!RecordStores.writeStringToStore(getCurrentSettingsAsStr(), RecordStores.RECORD_STORE_NAME_SETTINGS)) {
            Main.set(new Alert("Error!", "Can't save settings to RMS", null, AlertType.ERROR));
        }
    }
    
    public void loadDefaults() {
    	settingsKeysVals = new String[keys.length * 2];
    	for (int i = 0; i < keys.length; i++) {
			settingsKeysVals[i*2] = keys[i];
			settingsKeysVals[i*2 + 1] = UNDEF;
		}
    }
    
    public void loadFromRMS() {
        loadFromString(RecordStores.readStringFromStore(RecordStores.RECORD_STORE_NAME_SETTINGS));
    }
    
    public void loadFromString(String str) {
    	Logger.log("load from string: " + str);
        loadDefaults();
        Logger.log("loading settings from string: " + str);
        if (str == null) {
            return;
        }
        
        String[] keyValueCouples = Utils.split(str.substring(0, str.length() - 1), "" + SEP);
        //settingsKeysVals = new String[(keyValueCouples.length) * 2];
        for (int i = 0; i < keyValueCouples.length; i++) {
        	Logger.log("reading setting: " + keyValueCouples[i]);
            int splitterIndex = keyValueCouples[i].indexOf(' ');
            String key = keyValueCouples[i].substring(0, splitterIndex);
            String value = keyValueCouples[i].substring(splitterIndex + 1);
            for (int j = 0; j < settingsKeysVals.length / 2; j++) {
                if (key.equals(settingsKeysVals[j*2])) {
                    settingsKeysVals[i*2 + 1] = value;
                }
            }
            
            Logger.log("loaded from string:: " + Utils.arrayToString(settingsKeysVals));
        }
    }
    
    public String getCurrentSettingsAsStr() {
        if (settingsKeysVals == null) {
            Logger.log("null. loading from RMS");
            loadFromRMS();
        }
        
        Logger.log("packing current settings to string. length*2=" + settingsKeysVals.length);
        //assert ((settingsKeysVals.length % 2) == 0);
        StringBuffer sb = new StringBuffer(settingsKeysVals.length*5);
        for (int i = 0; i < settingsKeysVals.length / 2; i++) {
            sb.append(settingsKeysVals[i*2]);
            sb.append(" ");
            sb.append(settingsKeysVals[i*2 + 1]);
            sb.append(SEP);
        }
        Logger.log("settings packed (strlen=" + sb.length() + "): " + sb.toString());
        return sb.toString();
    }

    public boolean set(String key, String value) {
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
    
    public boolean set(String key, boolean value) {
        return set(key, toStr(value));
    }
    
    public String getStr(String key) {
        if (settingsKeysVals == null) {
            loadFromRMS();
        }
        
        for (int i = 0; i < settingsKeysVals.length / 2; i++) {
            if (settingsKeysVals[i*2].equals(key)) {
            	String value = settingsKeysVals[i*2 + 1];
            	if (value.equals(null)) {
            		value = UNDEF;
            	}
                return value;
            }
        }
        return null;
    }
    
    public boolean toggleBool(String key) {
        boolean newValue = !getBool(key);
        set(key, newValue);
        return newValue;
    }
    
    public boolean getBool(String key) {
    	return TRUE.equals(getStr(key));
    }
    
    public boolean getBool(String key, boolean defaultValue) {
    	String value = getStr(key);
    	if (value == null) {
    		set(key, defaultValue);
    		return TRUE.equals(getStr(key));
    	}
        return TRUE.equals(value);
    }
    
    public int getInt(String key) {
    	String value = getStr(key);
    	if (UNDEF.equals(value)) {
    		return 0;
    	}
    	return Integer.parseInt(value);
    }
    
    private String toStr(boolean b) {
        return b ? "1" : "0";
    }
}
