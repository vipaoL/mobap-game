/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author vipaol
 */
public class MgStruct {

    short supportedFileVer = 0;
    int[] args = {0, 2, 4, 7};
    
    int bufSizeInCells = 0;
    int[] structSizes = new int[16];
    //int bufSizeInShort = 0;
    short[][][] structBuffer = new short[16][][];
    int structBufSizeInCells = 0;
    boolean changed = true;

    /*public void writeFile() {
        short data[] = {0, 1, 50, 50, 100, 150, 2, 50, 50, 30, 90, 60, 100, 100, 0};
        try {
            FileConnection fc = (FileConnection) Connector.open("file:///root1/Levels/test.mgstruct");
            if (!fc.exists()) {
                fc.create();
            }
            OutputStream os = fc.openOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            for (int i = 0; i < data.length; i++) {
                dos.writeShort(data[i]);
            }
            dos.close();
            os.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }*/
    
    /*public void readFile(String path) {
        try {
            FileConnection fc = (FileConnection) Connector.open("file:///root1/Levels/test.mgstruct", Connector.READ);
            if (fc.exists()) {
                buffer = new short[16][];
                DataInputStream dis = fc.openDataInputStream();
                short fVervion = dis.readShort(); // file format version, for future
                if (fVervion == supportedFileVer) {
                    while (true) {
                        short id = dis.readShort();
                        if (id == 0) {
                            break;
                        }
                        short[] data = new short[args[id] + 1];
                        data[0] = id;
                        for (int i = 0; i < args[id]; i++) {
                            data[i + 1] = dis.readShort();
                        }
                        saveToBuffer(data);
                        Main.print(". ");

                    }
                } else {
                    Main.showAlert("Unsupported version number: " + fVervion);
                }
                dis.close();
                saveStructToBuffer(buffer);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }*/
    
    public void readRes(String path) {
        try {
            //FileConnection fc = (FileConnection) Connector.open("file:///root1/Levels/test.mgstruct", Connector.READ);
            if (true/*fc.exists()*/) {
                InputStream is = getClass().getResourceAsStream(path);
                DataInputStream dis = new DataInputStream(is);
                short fVervion = dis.readShort(); // file format version, for future
                if (fVervion == supportedFileVer) {
                    short[][] buffer = new short[16][];
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
                            Main.print(id + "data" +data[i+1]);
                        }
                        buffer[c] = data;
                        bufSizeInCells++;
                        Main.print(". ");
                    }
                    saveStructToBuffer(buffer);
                } else {
                    Main.showAlert("Unsupported version number: " + fVervion);
                }
                dis.close();
                is.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
