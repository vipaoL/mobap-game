/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import java.io.IOException;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

/**
 *
 * @author vipaol
 */
public class FileUtils {
    String prefix = "file:///";
    String root = "C:/";
    String sep = "/";
    
    public Enumeration getRoots() {
        return FileSystemRegistry.listRoots();
    }
    void load(String folderName) {
        //structBuffer = new short[32][][];
        try {
                String path = System.getProperty("fileconn.dir.photos");
                readFilesInFolder(path, folderName);
                path = System.getProperty("fileconn.dir.graphics");
                readFilesInFolder(path, folderName);
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
                    readFilesInFolder(path, folderName);
                    path = prefix + root + "other" + sep;
                    readFilesInFolder(path, folderName);
                } catch (SecurityException ex) {
                } catch (IllegalArgumentException ex) {
                }
            }
    }
    
    boolean readFilesInFolder(String path, String folderName) {
        if (path != null) {
            path += folderName + sep;
            Main.log(path);
            try {
                FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
                if (fc.exists() & fc.isDirectory()) {
                    Enumeration list =  fc.list();
                    while (list.hasMoreElements()) {
                        //readFile(path + list.nextElement());
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
}
