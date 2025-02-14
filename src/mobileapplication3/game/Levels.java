/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.game;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import at.emini.physics2D.World;
import at.emini.physics2D.util.PhysicsFileReader;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;
import utils.GameFileUtils;
import utils.MgStruct;

/**
 *
 * @author vipaol
 */
public class Levels extends GenericMenu implements Runnable {

    private static final String LEVELS_FOLDER_NAME = "MobappGame/Levels";

    private static int defaultSelected = 1; // currently selected option in menu

    private String[] levelPaths = new String[0];
    private String[] buttons;

    private int builtinLevelsCount = 0;

    public Levels() {
        Logger.log("Levels:constr");
        buttons = new String[2];
        builtinLevelsCount = seekForLevelsInRes();
        if (builtinLevelsCount > 0) {
            buttons = new String[builtinLevelsCount + 3];
            buttons[0] = "Levels";
            for (int i = 1; i <= builtinLevelsCount; i++) {
                buttons[i] = "Level " + i;
            }
            buttons[buttons.length - 2] = "Load custom level";
        } else {
            seekForLevelsInFS();
        }
        // TODO: separate with pages -----------------------!
        refreshButtons();
    }

    private void refreshButtons() {
        buttons[buttons.length-1] = "Back";
        defaultSelected = Math.min(defaultSelected, buttons.length - 1);
        loadParams(buttons, 1, buttons.length - 1, defaultSelected);
        selected = defaultSelected;
        if (w != 0 && h != 0) {
            loadCanvasParams(x0, y0, w, h);
        }
    }

    private void seekForLevelsInFS() {
        builtinLevelsCount = 0;
        try {
            levelPaths = getLevels();
            buttons = new String[levelPaths.length + 2];
            System.arraycopy(levelPaths, 0, buttons, 1, levelPaths.length);
        } catch (SecurityException e) {
            e.printStackTrace();
            buttons[0] = "no read permission";
        } catch (Exception e) {
            e.printStackTrace();
            buttons[0] = e.toString();
        }
        buttons[0] = "Load emini \".phy\" world";
        refreshButtons();
    }

    private int seekForLevelsInRes() {
        int c = 0;
        for (int i = 1; tryRes(getLevelResPath(i)); i++) {
            Logger.log(getLevelResPath(i));
            c++;
        }
        return c;
    }

    private String getLevelResPath(int i) {
        return "/l" + i + ".mglvl";
    }

    public boolean tryRes(String path) {
        InputStream is = null;
        try {
            is = Platform.getResource(path);
            DataInputStream dis = new DataInputStream(is);
            dis.readShort();
            return true;
        } catch (Exception ex) {
            Logger.log(path + " " + ex);
            return false;
        } finally {
            try {
                is.close();
            } catch (Exception ex) { }
        }
    }

    private void openFromRes(String path) {
        InputStream is = null;
        try {
            is = Platform.getResource(path);
            RootContainer.setRootUIComponent(openLevel(new DataInputStream(is)));
            isStopped = true;
        } catch (Exception ex) {
            Platform.showError("Can't open level!", ex);
        } finally {
            try {
                is.close();
            } catch (Exception ex) { }
        }
    }

    public void init() {
        isStopped = false;
        getFontSize();
        (new Thread(this, "levels")).start();
    }
    
    public String[] getLevels() {
        Logger.log("Levels:getLevels()");
        return GameFileUtils.listFilesInAllPlaces(LEVELS_FOLDER_NAME);
    }
    
    public void startLevel(final String path) {
        (new Thread(new Runnable() {
            public void run() {
                GameplayCanvas gameCanvas = null;
                if (path.endsWith(".phy")) {
                    gameCanvas = new GameplayCanvas(readWorldFile(path));
                } else if (path.endsWith(".mglvl")) {
                    gameCanvas = openLevel(path);
                }
                if (gameCanvas != null) {
                    RootContainer.setRootUIComponent(gameCanvas);
                    isStopped = true;
                }
            }
        })).start();
    }

    private static GameplayCanvas openLevel(String path) {
        return openLevel(FileUtils.fileToDataInputStream(path));
    }

    private static GameplayCanvas openLevel(DataInputStream dis) {
        try {
            short[][] level = MgStruct.readFromDataInputStream(dis);
            if (level != null) {
                return new GameplayCanvas(new GraphicsWorld()).loadLevel(level);
            }
        } catch (IOException ex) {
            Platform.showError(ex);
        }
        return null;
    }

    public GraphicsWorld readWorldFile(String path) {
        PhysicsFileReader reader;
        InputStream is = FileUtils.fileToDataInputStream(path);
        reader = new PhysicsFileReader(is);
        GraphicsWorld w = new GraphicsWorld(World.loadWorld(reader));
        reader.close();
        return w;
    }
    
    public void selectPressed() {
        defaultSelected = selected;
        if (selected == buttons.length - 1) {
            isStopped = true;
            RootContainer.setRootUIComponent(new MenuCanvas());
        } else {
            if (builtinLevelsCount > 0) {
                if (selected == buttons.length - 2) {
                    seekForLevelsInFS();
                } else {
                    openFromRes(getLevelResPath(selected));
                }
            } else {
                try {
                    startLevel(levelPaths[selected - 1]);
                } catch (Exception ex) {
                    Platform.showError(ex);
                }
            }
        }
    }

    public void run() {
        Logger.log("Levels:run()");
        long sleep = 0;
        long start = 0;
        
        isPaused = false;
        while (!isStopped) {
            if (!isPaused) {
                start = System.currentTimeMillis();
                
                repaint();
                tick();

                sleep = GameplayCanvas.TICK_DURATION - (System.currentTimeMillis() - start);
                sleep = Math.max(sleep, 0);
            } else {
                sleep = 100;
            }
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
