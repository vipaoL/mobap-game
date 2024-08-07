/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

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
public class FileUtils {
    String prefix = "file:///";
    String sep = "/";
    public String path;
    String root;
    String prefixToDisable = null;
    String workingFolderName;
    String[] folders = {"", "other" + sep};
    int counter = folders.length;
    
    Enumeration list;
    Enumeration roots;
    
    boolean photosChecked = false;
    boolean graphicsChecked = false;
    
    public FileUtils(String workingFolderName) {
        this.workingFolderName = workingFolderName;
    }
    
    private void refreshRoots() {
        try {
            roots = FileSystemRegistry.listRoots();
        } catch (SecurityException ex) {
            ex.printStackTrace();
            Logger.log("No permission to get roots");
        }
    }
    
    private boolean isNextRootAvaliable() {
        try {
            return roots.hasMoreElements();
        } catch (NullPointerException ex) {
            return false;
        }
    }
    
    /**
     * Sets the prefix, files with it will be ignored. Disabled by default
     * @param prefix e.g: if prefix set to "-" then "-test.txt" will be ignored
     */
    public void setPrefixToDisable(String prefix) {
        prefixToDisable = prefix;
    }
    
    public DataInputStream fileToDataInputStream(String path) {
        Logger.log(path);
        try {
            FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
            return fc.openDataInputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public InputStream fileToInputStream(String path) throws IOException {
        FileConnection fc = (FileConnection) Connector.open(path);
        return fc.openInputStream();
    }
    
    /**
     * Load next file in the folder selected in the constructor
     * 
     * @return DataInputStream of next file if is's available.
     *          null if no more files available
     * @throws SecurityException if cancelled by user or no read permission
     */
    public DataInputStream loadNext() {
        if (list == null) {
            list = getNextList();
        }
        
        while (true) {
            if (list == null) {
                break;
            }
            if (!list.hasMoreElements()) {
                list = getNextList();
                continue;
            }
            String name = list.nextElement().toString();
            // don't load files with prefix (if set)
            if (prefixToDisable != null) {
                if (name.startsWith(prefixToDisable) & !prefixToDisable.equals("")) {
                    Logger.log("file \"" + name + "\" was ignored due to name prefix \"-\"");
                    continue;
                }
            }
            Logger.log("loading " + name);
            return fileToDataInputStream(path + name);
        }
        return null;
    }
    boolean canRead = false;
    boolean checkingRoot = true;
    public Enumeration getNextList() {
        if (roots == null) {
            refreshRoots();
        }
        while (isNextRootAvaliable() | checkingRoot | !photosChecked | !graphicsChecked) {
            try {
                if (isNextRootAvaliable() | counter < folders.length) {
                    if (counter < folders.length) {
                        counter++;
                    } else {
                        root = (String) roots.nextElement();
                        counter = 1;
                    }
                    path = prefix + root + folders[counter - 1];
                } else {
                    checkingRoot = false;
                    if (!photosChecked) {
                        path = System.getProperty("fileconn.dir.photos");
                        photosChecked = true;
                    } else if (!graphicsChecked) {
                        path = System.getProperty("fileconn.dir.graphics");
                        graphicsChecked = true;
                    }
                }
                //Main.log(path);
                if (path == null) {
                    continue;
                }
                path += workingFolderName + sep;
                Logger.log(path);
                FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
                canRead = true;
                Logger.log("reachable");
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
            } catch (SecurityException sex) {
                sex.printStackTrace();
            }
        }
        if (!canRead) {
            throw new SecurityException("No read permission");
        }
        return null;
    }
}
