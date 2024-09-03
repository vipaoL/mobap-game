/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import javax.microedition.midlet.MIDlet;

import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;

/**
 * @author vipaol
 */
public class GameMIDlet extends MIDlet {
    
    // for numbering snapshots. e.g.: '1', '2', '3', ... .
    // '-1' if release
    // TODO: move to manifest
    public static int PRE_VERSION = -1;
    
    // time for one frame. 1000ms / 50ms = 20fps
    public static final int TICK_DURATION = 50;
    private static GameMIDlet thiss;
    
    
    private boolean isStartedAlready = false;

    public void startApp() {
        if (isStartedAlready) {
            Logger.log("Main:startApp:already started");
            return;
        }

        Platform.init(this);
        isStartedAlready = true;
        thiss = this;

        Logger.log("Main:constr");
        MenuCanvas menuCanvas = new MenuCanvas();
        Platform.setCurrent(new RootContainer(menuCanvas, null));
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        notifyDestroyed();
    }
    
    public static void exit() {
        thiss.destroyApp(true);
    }
    
}
