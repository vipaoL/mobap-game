/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

/**
 *
 * @author vipaol
 */
public class MgStruct {

    static short supportedFileVer = 0;
    static short secondSupportedFileVer = 1;
    static int[] args = {0, /*1*/2, /*2*/4, /*3*/7, /*4*/9, /*5*/10};
    static final int structArrayBufferSize = 32;

    static short[][][] structBuffer = new short[structArrayBufferSize][][];
    static int[] structSizes = new int[structArrayBufferSize];
    static int loadedStructsNumber = 0;
    private int bufSizeInCells = 0;
    static boolean isInited = false;
    static int loadedStructsFromResNumber = 0;
    
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
            loadedStructsFromResNumber = loadedStructsNumber;
        }
        Main.log("inited");
        isInited = true;
    }
    
    boolean load() {
        Main.log("mgs load()");
        //structBuffer = new short[structArrayBufferSize][][];
        loadedStructsNumber = loadedStructsFromResNumber;
        try {
            String path = System.getProperty("fileconn.dir.photos");
            readFilesInFolder(path);
            path = System.getProperty("fileconn.dir.graphics");
            readFilesInFolder(path);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        Enumeration roots = FileSystemRegistry.listRoots();
        while (roots.hasMoreElements()) {
            root = (String) roots.nextElement();
            String path = prefix + root;
            try {
                readFilesInFolder(path);
                path = prefix + root + "other" + sep;
                readFilesInFolder(path);
            } catch (SecurityException ex) {
            } catch (IllegalArgumentException ex) {
            }
        }
        return loadedStructsNumber > 0;
    }
    
    boolean readFilesInFolder(String path) {
        if (path != null) {
            path += "MGStructs" + sep;
            Main.log(path);
            try {
                FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
                if (fc.exists() & fc.isDirectory()) {
                    Enumeration list =  fc.list();
                    while (list.hasMoreElements()) {
                        String name = list.nextElement().toString();
                        if (!name.startsWith("-")) readFile(path + name);
                        else Main.log("struct file \"" + name + "\" is disabled by name prefix \"-\"");
                    }
                    return true;
                }
            } catch (IOException ex) {
                //Main.showAlert(ex);
                //ex.printStackTrace();
            } catch (IllegalArgumentException ex) {
                //Main.showAlert(ex);
                //ex.printStackTrace();
            }
        }
        return false;
    }
    
    public void readFile(String path) {
        Main.log(path);
        try {
            FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
            DataInputStream dis = fc.openDataInputStream();
            if (!readFromDataInputStream(dis)) {
                Main.showAlert("Failed to load file: " + fc.getName());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
        Main.log("savivg new structure with id=" + loadedStructsNumber);
        structBuffer[loadedStructsNumber] = data;
        structSizes[loadedStructsNumber] = bufSizeInCells;
        loadedStructsNumber++;
        bufSizeInCells = 0;
    }
}
