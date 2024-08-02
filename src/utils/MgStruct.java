/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import mobileapplication3.Logger;
import mobileapplication3.Main;

/**
 *
 * @author vipaol
 */
public class MgStruct {

    static final short[] SUPPORTED_FILE_FORMAT_VERSIONS = {0, 1};
    
    /*
     * 0 - end of file
     * 1 - end point of structure
     * 2 - line
     * 3 - circle/arc
     * 4 - breakable line
     * 5 - breakable circle (not implemented yet)
     * 6 - sinus (not implemented yet)
     * 7 - accellerator
     */
    static int[] argsNumber = {0, /*1*/2, /*2*/4, /*3*/7, /*4*/9, /*5*/10, /*6*/6, /*7*/8};
    static final int STRUCTURE_STORAGE_SIZE = 32;

    public static short[][][] structStorage = new short[STRUCTURE_STORAGE_SIZE][][];
    public static int loadedStructsNumber = 0;
    static boolean isInited = false;
    public static int loadedFromResNumber = 0;
    public boolean loadCancelled = false;
    
    String prefix = "file:///";
    String root = "C:/";
    String sep = "/";
    
    public MgStruct() {
        Logger.log("MGStruct constructor");
        if (!isInited) {
            Logger.log("mgs init");
            for (int i = 1; readRes("/" + i + ".mgstruct"); i++) {
                Logger.log(i + ".mgstruct");
            }

            Logger.log("MGStruct:loaded " + loadedStructsNumber);
            loadedFromResNumber = loadedStructsNumber;
        }
        Logger.log("inited");
        isInited = true;
    }
    
    public boolean readRes(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            DataInputStream dis = new DataInputStream(is);
            readFromDataInputStream(dis);
            is.close();
            return true;
        } catch (IOException ex) {
            return false;
        } catch (NullPointerException ex) {
            return false;
        }
    }
    
    public boolean loadFromFiles() {
        Logger.log("mgs load()");
        loadCancelled = false;
        FileUtils files = new FileUtils("MGStructs");
        files.setPrefixToDisable("-");
        int loadedFromFiles = 0;
        loadedStructsNumber = loadedFromResNumber;
        while (true) {
            DataInputStream dis = null;
            try {
                dis = files.loadNext();
            } catch (SecurityException sex) {
                Logger.log("mgs:load cancelled");
                sex.printStackTrace();
                loadCancelled = true;
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            } catch (NoClassDefFoundError err) {
                Main.showAlert(err);
                return false;
            }
            if (dis != null) {
                try {
                    if (readFromDataInputStream(dis)) {
                        loadedFromFiles += 1;
                        System.out.println("LOADED!!!");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                break;
            }
        }
        if (loadedFromFiles > 0) {
            loadCancelled = false;
            loadedStructsNumber = loadedFromResNumber + loadedFromFiles;
        } else {
            loadedStructsNumber = loadedFromResNumber;
        }
        Logger.log("mg:loaded: " + loadedFromFiles);
        return loadedFromFiles > 0;
    }

    public boolean readFromDataInputStream(DataInputStream dis) throws IOException {
        try {
            short fileFormatVervion = dis.readShort(); // read file format version
            if (isArrContain(SUPPORTED_FILE_FORMAT_VERSIONS, fileFormatVervion)) {
                // number of primitives in the structure
                int length = 16;
                if (fileFormatVervion > 0) {
                    length = dis.readShort();
                }
                Logger.log("read: ver=" + fileFormatVervion + " length=" + length);
                
                short[][] structure = new short[length][];
                for (int c = 0; true; c++) {
                    short id = dis.readShort();
                    
                    // structID 0 is end of file
                    if (id == 0) {
                        break;
                    }
                    
                    // reading a primitive, e.g., line or circle
                    short[] data = new short[argsNumber[id] + 1]; // {2, 0, 0, 100, 0} // - e.g.: line
                    // first cell is ID of primitive, next cells are arguments
                    data[0] = id;
                    for (int i = 1; i < data.length; i++) {
                        data[i] = dis.readShort();
                    }
                    structure[c] = data;
                }
                saveStructToStorage(structure);
                dis.close();
                return true;
            } else {
                Logger.log("Unsupported file format version: " + fileFormatVervion);
                dis.close();
                return false;
            }
        } catch(ArrayIndexOutOfBoundsException ex) {
            Logger.log("error parsing file");
            ex.printStackTrace();
            dis.close();
            return false;
        }
    }
    
    void saveStructToStorage(short[][] data) {
        Logger.log("savivg new structure, id=" + loadedStructsNumber);
        structStorage[loadedStructsNumber] = data;
        loadedStructsNumber++;
    }
    
    boolean isArrContain(short[] arr, short a) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == a) {
                return true;
            }
        }
        return false;
    }
}
