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
    int[] args = {0, 4, 7};
    short[][] buffer = new short[16][];
    int bufSizeInCells = 0;
    int bufSizeInShort = 0;
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
    public void readFile() {
        try {
            //FileConnection fc = (FileConnection) Connector.open("file:///root1/Levels/test.mgstruct", Connector.READ);
            if (true/*fc.exists()*/) {
                InputStream is = getClass().getResourceAsStream("/test.mgstruct");
                DataInputStream dis = new DataInputStream(is);
                //is.read(data);
                /*while(dis.available() > -120) {
                    data = dis.readShort();
                    System.out.println(data);
                    System.out.println(dis.available());
                }*/
                short fVervion = dis.readShort();
                if (fVervion == supportedFileVer) {
                    while (true) {
                        short id = dis.readShort();
                        if (id == 0) {
                            break;
                        } else {
                            short[] data = new short[args[id] + 1];
                            data[0] = id;
                            for (int i = 1; i < args[id] + 1; i++) {
                                data[i] = dis.readShort();
                            }
                            saveToBuffer(data);
                            System.out.print(". ");
                        }
                    }
                }
                dis.close();
                is.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    void saveToBuffer(short[] data) {
        buffer[bufSizeInCells] = data;
        bufSizeInCells++;
        bufSizeInShort += data.length;
        changed = true;
    }
}
