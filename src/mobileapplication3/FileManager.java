/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mobileapplication3;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

/**
 *
 * @author junaed
 *
public class FileManager implements CommandListener
{

    public List directoryList;
    private Command openCommand;
    private Command upCommand;
    private Command useCommand;
    private Command cancelCommand;
    private String[] roots = null;
    private int selectionMode;
    private Main application;
    private Displayable backDisplay;
    private String currentDirectory;
    private Image folderIcon;
    private Image songIcon;
    private Image imageIcon;
    public final static int DIRECTORY_SELECTION_ONLY = 0;
    public final static int AUDIO_FILE_SELECTION_ONLY = 1;
    public final static int IMAGE_FILE_SELECTION_ONLY = 2;
    private final static String SEP_STR = (DeviceManager.getSystemFileSeparator() != null) ? System.getProperty("file.separator") : "/";
    private final static char SEP_CHAR = SEP_STR.charAt(0);
    private final static String ROOT_PREFIX = "file:///";
    private final static String FILE_BROWSER_TITLE = "File Browser";
    private String selectedFileName;

    public FileManager(int selectionMode, Main midlet, Displayable backDisplay)
    {
        this.selectionMode = selectionMode;
        this.application = midlet;
        this.backDisplay = backDisplay;

        currentDirectory = "";
        openCommand = new Command("Open", Command.OK, 1);
        upCommand = new Command("Up", Command.BACK, 2);
        useCommand = new Command("Use", Command.ITEM, 3);
        cancelCommand = new Command("Cancel", Command.CANCEL, 4);
        if (selectionMode == DIRECTORY_SELECTION_ONLY)
        {
            try
            {
                folderIcon = ImageUtil.getFolderIcon();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        else if (selectionMode == AUDIO_FILE_SELECTION_ONLY)
        {
            try
            {
                songIcon = ImageUtil.getSongIcon();
            }
            catch (Exception e)
            {
            }
        }
        else if(selectionMode == IMAGE_FILE_SELECTION_ONLY)
        {
            try
            {
                imageIcon = ImageUtil.getImageIcon();
            }
            catch (Exception e)
            {
            }
        }

    }

    private void addCommands()
    {
        if (this.selectionMode == DIRECTORY_SELECTION_ONLY)
        {
        }
        else
        {
        }
        directoryList.addCommand(openCommand);
        directoryList.addCommand(upCommand);
        directoryList.addCommand(cancelCommand);
        directoryList.setSelectCommand(useCommand);
        directoryList.setCommandListener(this);

    }

    private final String[] getRoots()
    {
        try
        {
            if (!DeviceManager.isFileManagerSupported())
            {
                throw new Exception("File Browsing is not permitted in your hand set.");
            }
            Enumeration enumeration = FileSystemRegistry.listRoots();
            Vector temp = new Vector();
            String root = "";
            while (enumeration.hasMoreElements())
            {
                root = (String) enumeration.nextElement();
                if (true)
                {
                    temp.addElement(root);
                }
                else
                {
                    temp.addElement(root);
                }
            }
            String[] ret = new String[temp.size()];
            temp.copyInto(ret);
            return ret;

        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return null;
        }

    }

    private void printDir(String title, String dir)
    {
        System.out.println(title + " = " + ROOT_PREFIX + dir);
    }

    private Vector getDirectoryListing(String directory) throws Exception
    {
        try
        {
            Vector tempVector;
            String tempString;

            try
            {
                if (directory.equalsIgnoreCase(""))
                {
                    if (roots == null)
                    {
                        roots = getRoots();
                        if (roots == null)
                        {
                            return null;
                        }
                    }

                    tempVector = new Vector();
                    for (int i = 0; i < roots.length; i++)
                    {
                        tempVector.addElement(roots[i]);
                    }
                    directoryList.removeCommand(upCommand);

                }
                else
                {
                    FileConnection fileConnection = (FileConnection) Connector.open(ROOT_PREFIX + directory, Connector.READ);
                    if (!fileConnection.exists())
                    {
                        throw new Exception("Cannot open file: " + ROOT_PREFIX + directory);
                    }
                    tempVector = new Vector();
                    for (Enumeration enumer = fileConnection.list(); enumer.hasMoreElements();)
                    {
                        tempString = (String) enumer.nextElement();
                        tempVector.addElement(tempString);
                    }
                    fileConnection.close();
                }
            }
            catch (Exception exception)
            {
                throw new Exception(exception.toString());
            }
            return tempVector;
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    public void show() throws Exception
    {
        try
        {
            directoryList = new List(FILE_BROWSER_TITLE, List.IMPLICIT);
            addCommands();
            Vector dir = getDirectoryListing(currentDirectory);

            for (int i = 0; i < dir.size(); i++)
            {
                String fname = (String) dir.elementAt(i);
                if (isDirectory(fname))
                {
                    directoryList.append(fname, folderIcon);
                }

                if (selectionMode == AUDIO_FILE_SELECTION_ONLY)
                {
                    if (isFileTypeSupported(fname))
                    {
                        directoryList.append(fname, songIcon);
                    }
                }
                else if (selectionMode == IMAGE_FILE_SELECTION_ONLY)
                {
                    if (isFileTypeSupported(fname))
                    {
                        directoryList.append(fname, imageIcon);
                    }
                }            }
            application.getDisplay().setCurrent(directoryList);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    private boolean isDirectory(String directory)
    {
        if (directory.charAt(directory.length() - 1) == SEP_CHAR)
        {
            return true;
        }
        else
        {
            return false;
        }

    }

    private String getFileType(String fileName)
    {
        char ch = '.';
        int index = fileName.lastIndexOf((int) ch);
        return fileName.substring(index + 1);
    }

    private boolean isFileTypeSupported(String fileName)
    {
        String extension = getFileType(fileName);
        if (selectionMode == AUDIO_FILE_SELECTION_ONLY)
        {
            if (extension.equalsIgnoreCase("mp3"))
            {
                return true;
            }
        }
        else if (selectionMode == IMAGE_FILE_SELECTION_ONLY)
        {
            if (extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg"))
            {
                return true;
            }
        }
        return false;
    }

    public void commandAction(Command c, Displayable d)
    {
        if (d == directoryList)
        {
            if (c == openCommand || c.getCommandType() == Command.OK)
            {
                String selected = directoryList.getString(directoryList.getSelectedIndex());
                currentDirectory += selected;
                if (isDirectory(selected))
                {

                    Thread t = new Thread(new Runnable()
                    {

                        public void run()
                        {
                            try
                            {
                                show();
                            }
                            catch (Exception ex)
                            {
                                ex.printStackTrace();
                            }
                        }
                    });
                    t.start();

                }
                else
                {
                    if (isFileTypeSupported(selected))
                    {
                        selectedFileName = selected;
                        ((Form) backDisplay).append(currentDirectory);
                        application.getDisplay().setCurrent(backDisplay);
                    }
                }
            }
            else if (c == upCommand || c.getCommandType() == Command.BACK)
            {

                Thread t = new Thread(new Runnable()
                {

                    public void run()
                    {
                        int index = currentDirectory.lastIndexOf(SEP_CHAR);
                        currentDirectory = currentDirectory.substring(0, index);
                        index = currentDirectory.lastIndexOf(SEP_CHAR);
                        currentDirectory = currentDirectory.substring(0, index + 1);
                        try
                        {
                            show();
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                });
                t.start();

            }
            else if (c == useCommand)
            {
                Thread t = new Thread(new Runnable()
                {

                    public void run()
                    {
                        selectedFileName = directoryList.getString(directoryList.getSelectedIndex());
                        currentDirectory += selectedFileName;
                        ((Form) backDisplay).append(currentDirectory);
                        application.getDisplay().setCurrent(backDisplay);
                    }
                });
                t.start();

            }
            else if (c == cancelCommand)
            {
                application.getDisplay().setCurrent(backDisplay);
            }

        }
    }

    /**
     * @return the selectedFileName
     *
    public String getSelectedFileName()
    {
        return selectedFileName;
    }
}*/