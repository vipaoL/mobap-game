/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 *
 * @author vipaol
 */
public class RecordStores {
    public static final String RECORD_STORE_NAME_SETTINGS = "settings";
    
    public static boolean writeStringToStore(String settings, String recordStoreName) {
        System.out.println("writing to RMS: " + settings);
        RecordStore rs = null;
        boolean ret = true;
        
        try {
            try {
                RecordStore.deleteRecordStore(recordStoreName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            rs = RecordStore.openRecordStore(recordStoreName, true);
            byte[] data = settings.getBytes("UTF-8");
            System.out.println("recordId=" + rs.addRecord(data, 0, data.length));
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }
        
        try {
            rs.closeRecordStore();
        } catch(Exception e) {
            
        }
        
        return ret;
    }
    
    public static String readStringFromStore(String recordStoreName) {
        System.out.println("reading store " + recordStoreName);
        RecordStore rs = null;
        String ret = null;
        
        try {
            rs = RecordStore.openRecordStore(recordStoreName, false);
            byte[] data = rs.getRecord(1);
            if (data != null && data.length != 0) {
                ret = new String(data, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            rs.closeRecordStore();
        } catch(Exception e) {
            
        }
        
        System.out.println("read from " + recordStoreName + " store: " + ret);
        return ret;
    }
    
    // 
    void oj() {
        RecordStore rs = null;

        try {
            rs = RecordStore.openRecordStore(RECORD_STORE_NAME_SETTINGS, false);
            
        } catch( RecordStoreException e ) {
            //logger.exception( rsName, e );
        } finally {
            try {
                rs.closeRecordStore();
            } catch( RecordStoreException e ){
                // игнорируем это исключение
            }
        }
    }
    
}
