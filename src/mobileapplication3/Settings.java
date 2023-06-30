/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

/**
 *
 * @author vipaol
 */
public class Settings {
    public static final byte UNDEF = -1;
    public static final byte FALSE = 0;
    public static final byte TRUE = 1;
    public static byte bigScreen = UNDEF;
    
    public static void readSettings() {
        // TODO: read from rms
        
        // if empty, loadDefaults();
    }
    
    public static void loadDefaults() {
        bigScreen = UNDEF;
    }
}
