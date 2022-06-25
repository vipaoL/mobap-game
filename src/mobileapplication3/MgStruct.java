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

    static int bufSizeInCells = 0;
    static int[] structSizes = new int[32];
    static short[][][] structBuffer = new short[32][][];
    static int structBufSizeInCells = 0;
    static boolean isInited = false;
    
    String prefix = "file:///";
    String root = "C:/";
    String sep = "/";
    
    public MgStruct() {
        if (!isInited) {
            //load();
        }
        isInited = true;
    }
    
    void load() {
        structBuffer = new short[32][][];
        structBufSizeInCells = 0;
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
    }
    
    boolean readFilesInFolder(String path) {
        if (path != null) {
            path += "MGStructs" + sep;
            Main.print(path);
            try {
                FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
                if (fc.exists() & fc.isDirectory()) {
                    Enumeration list =  fc.list();
                    while (list.hasMoreElements()) {
                        String name = list.nextElement().toString();
                        if (!name.startsWith("-")) readFile(path + name);
                        else Main.print("struct file \"" + name + "\" is disabled by name prefix \"-\"");
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
        Main.print(path);
        try {
            FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
            DataInputStream dis = fc.openDataInputStream();
            readFromDataInputStream(dis);
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
        }
    }

    public void readFromDataInputStream(DataInputStream dis) throws IOException {
        short fVervion = dis.readShort(); // file format version
        if (fVervion == supportedFileVer | fVervion == secondSupportedFileVer) {
            int length = 16;
            if (fVervion != 0) {
                length = dis.readShort();
            }
            Main.print("read: ver=" + fVervion + " length=" + length);
            short[][] buffer = new short[length][];
            //structSizes = new int[length];
            bufSizeInCells = 0;
            for (int c = 0; true; c++) {
                short id = dis.readShort();
                if (id == 0) {
                    Main.print("break");
                    break;
                }
                short[] data = new short[args[id] + 1]; // {1, 0, 0, 100, 0} // - e.g line
                data[0] = id;
                for (int i = 0; i < args[id]; i++) {
                    data[i + 1] = dis.readShort();
                    Main.print(id + "data" + data[i + 1]);
                }
                buffer[c] = data;
                bufSizeInCells++;
                Main.print(". ");
            }
            saveStructToBuffer(buffer);
        } else {
            Main.print("Unsupported version number: " + fVervion);
        }
        dis.close();
    }

    /*void saveToBuffer(short[] data) {
        buffer[bufSizeInCells] = data;
        bufSizeInCells++;
        //bufSizeInShort += data.length;
        changed = true;
    }*/
    void saveStructToBuffer(short[][] data) {
        structBuffer[structBufSizeInCells] = data;
        structSizes[structBufSizeInCells] = bufSizeInCells;
        structBufSizeInCells++;
        bufSizeInCells = 0;
    }
}
