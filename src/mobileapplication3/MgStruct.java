/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

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
    static int[] argsNumber = {0, /*1*/2, /*2*/4, /*3*/7, /*4*/9, /*5*/10, /*6*/5, /*7*/8};
    static final int STRUCTURE_STORAGE_SIZE = 32;

    static short[][][] structStorage = new short[STRUCTURE_STORAGE_SIZE][][];
    static int loadedStructsNumber = 0;
    static boolean isInited = false;
    static int loadedFromResNumber = 0;
    boolean loadCancelled = false;
    
    String prefix = "file:///";
    String root = "C:/";
    String sep = "/";
    
    public MgStruct() {
        Main.log("MGStruct constructor");
        if (!isInited) {
            Main.log("mgs init");
            for (int i = 1; readRes("/" + i + ".mgstruct"); i++) {
                Main.log(i + ".mgstruct");
            }

            Main.log("MGStruct:read completed. loaded " + loadedStructsNumber);
            loadedFromResNumber = loadedStructsNumber;
        }
        Main.log("inited");
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
        Main.log("mgs load()");
        loadCancelled = false;
        FileUtils files = new FileUtils("MGStructs");
        files.setPrefixToDisable("-");
        int loaded = 0;
        loadedStructsNumber = loadedFromResNumber;
        while (true) {
            DataInputStream dis = null;
            try {
                dis = files.loadNext();
            } catch (SecurityException sex) {
                Main.log("mgs:load cancelled");
                sex.printStackTrace();
                loadCancelled = true;
                loadedStructsNumber = loadedFromResNumber;
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            } catch (NoClassDefFoundError err) {
                Main.showAlert(err);
                return false;
            }
            if (dis != null) {
                try {
                    if (readFromDataInputStream(dis)) {
                        loaded += 1;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                break;
            }
        }
        if (loaded > 0) {
            loadCancelled = false;
        }
        return loaded > 0;
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
                Main.log("read: ver=" + fileFormatVervion + " length=" + length);
                
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
                    for (int i = 0; i < argsNumber[id]; i++) {
                        data[i + 1] = dis.readShort();
                    }
                    structure[c] = data;
                }
                saveStructToStorage(structure);
                dis.close();
                return true;
            } else {
                Main.log("Unsupported file format version: " + fileFormatVervion);
                dis.close();
                return false;
            }
        } catch(ArrayIndexOutOfBoundsException ex) {
            dis.close();
            return false;
        }
    }
    
    void saveStructToStorage(short[][] data) {
        Main.log("savivg new structure, id=" + loadedStructsNumber);
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
