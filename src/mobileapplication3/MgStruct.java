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

    static short supportedFileVer = 0;
    static short secondSupportedFileVer = 1;
    
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
    static int[] args = {0, /*1*/2, /*2*/4, /*3*/7, /*4*/9, /*5*/10, /*6*/5, /*7*/8};
    static final int structArrayBufferSize = 32;

    static short[][][] structBuffer = new short[structArrayBufferSize][][];
    static int[] structSizes = new int[structArrayBufferSize];
    static int structsInBufferNumber = 0;
    private int bufSizeInCells = 0;
    static boolean isInited = false;
    static int loadedStructsFromResNumber = 0;
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

            Main.log("MGStruct:read completed. loaded " + structsInBufferNumber);
            loadedStructsFromResNumber = structsInBufferNumber;
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
        structsInBufferNumber = loadedStructsFromResNumber;
        while (true) {
            DataInputStream dis = null;
            try {
                dis = files.loadNext();
            } catch (SecurityException sex) {
                Main.log("mgs:load cancelled");
                sex.printStackTrace();
                loadCancelled = true;
                structsInBufferNumber = loadedStructsFromResNumber;
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
        return loaded > 0;
    }

    public boolean readFromDataInputStream(DataInputStream dis) throws IOException {
        try {
            short fVervion = dis.readShort(); // read file format version
            if (fVervion == supportedFileVer | fVervion == secondSupportedFileVer) {
                int length = 16;
                if (fVervion != 0) {
                    length = dis.readShort();
                }
                Main.log("read: ver=" + fVervion + " length=" + length);
                short[][] buffer = new short[length][];
                //structSizes = new int[length];
                bufSizeInCells = 0;
                for (int c = 0; true; c++) {
                    short id = dis.readShort();
                    if (id == 0) {
                        //Main.print("break");
                        break;
                    }
                    short[] data = new short[args[id] + 1]; // {2, 0, 0, 100, 0} // - e.g.: line
                    data[0] = id;
                    for (int i = 0; i < args[id]; i++) {
                        data[i + 1] = dis.readShort();
                        //Main.print(id + "data" + data[i + 1]);
                    }
                    buffer[c] = data;
                    bufSizeInCells++;
                    //Main.print(". ");
                }
                saveStructToBuffer(buffer);
                dis.close();
                return true;
            } else {
                Main.log("Unsupported version number: " + fVervion);
                dis.close();
                return false;
            }
        } catch(ArrayIndexOutOfBoundsException ex) {
            dis.close();
            return false;
        }
    }

    /*void saveToBuffer(short[] data) {
        buffer[bufSizeInCells] = data;
        bufSizeInCells++;
        //bufSizeInShort += data.length;
        changed = true;
    }*/
    void saveStructToBuffer(short[][] data) {
        Main.log("savivg new structure with id=" + structsInBufferNumber);
        structBuffer[structsInBufferNumber] = data;
        structSizes[structsInBufferNumber] = bufSizeInCells;
        structsInBufferNumber++;
        bufSizeInCells = 0;
    }
}
