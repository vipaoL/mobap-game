/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import java.io.DataInputStream;
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
    String sep = "/";
    String path;
    String root;
    String[] folders = {"", "other" + sep};
    int counter = folders.length;
    
    Enumeration list;
    Enumeration roots;
    
    boolean photosChecked = false;
    boolean graphicsChecked = false;
    
    public DataInputStream fileToDataInputStream(String path) {
        Main.log(path);
        try {
            FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
            return fc.openDataInputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public DataInputStream loadNextMGStuct() throws SecurityException {
        if (roots == null) {
            roots = FileSystemRegistry.listRoots();
        }
        if (list == null) {
            list = getNextList();
        }
        while (list != null) {
            if (!list.hasMoreElements()) {
                list = getNextList();
            }
            if (list == null) {
                return null;
            }
            String name = list.nextElement().toString();
            if (!name.startsWith("-")) {
                return fileToDataInputStream(path + name);
            } else {
                Main.log("struct file \"" + name + "\" is disabled by name prefix \"-\"");
            }
        }
        return null;
    }
    
    private Enumeration getNextList() throws SecurityException {
        while (roots.hasMoreElements() | !photosChecked | !graphicsChecked) {
            if (roots.hasMoreElements()) {
                if (counter >= folders.length) {
                    root = (String) roots.nextElement();
                    counter = 1;
                } else {
                    counter++;
                }
                path = prefix + root + folders[counter - 1];
            } else if (!photosChecked) {
                path = System.getProperty("fileconn.dir.photos");
                photosChecked = true;
            } else if (!graphicsChecked) {
                path = System.getProperty("fileconn.dir.graphics");
                graphicsChecked = true;
            }
            if (path == null) {
                continue;
            }
            path += "MGStructs" + sep;
            try {
                FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
                if (fc.exists() & fc.isDirectory()) {
                    Enumeration list = fc.list();
                    if (list.hasMoreElements()) {
                        return list;
                    }
                }
            } catch (IllegalArgumentException iaex) {
                iaex.printStackTrace();
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
        return null;
    }
}
