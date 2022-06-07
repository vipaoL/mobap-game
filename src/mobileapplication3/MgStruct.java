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
    short secondSupportedFileVer = 1;
    int[] args = {0, /*1*/2, /*2*/4, /*3*/7, /*4*/9, /*5*/10};

    int bufSizeInCells = 0;
    int[] structSizes = new int[32];
    //int bufSizeInShort = 0;
    short[][][] structBuffer = new short[32][][];
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
    public void readFile(String path) {
        System.out.println("file path: " + path);
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
