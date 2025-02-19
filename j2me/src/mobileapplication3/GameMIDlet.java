/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.MobappMIDlet;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;

/**
 * @author vipaol
 */
public class GameMIDlet extends MobappMIDlet {
    private boolean isRunning = false;

    public void onStart() {
        if (isRunning) {
            Logger.log("Main:startApp:already started");
            return;
        }

        Platform.init(this);
        isRunning = true;
        Logger.log("Main:constr");
        MenuCanvas menuCanvas = new MenuCanvas();
        RootContainer.setRootUIComponent(menuCanvas);
    }
    
}
