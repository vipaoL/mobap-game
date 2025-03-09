package com.vipaol;

import mobileapplication3.game.DebugMenu;
import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.MobappDesktopMain;
import mobileapplication3.platform.ui.RootContainer;

public class MobappGameDesktopMain extends MobappDesktopMain {
    public static void main(String[] args) {
        if (args != null) {
            for (String arg : args) {
                if (arg != null) {
                    if (arg.startsWith("--")) {
                        arg = arg.substring(2);
                    }
                    if (arg.startsWith("/")) {
                        arg = arg.substring(1);
                    }
                    switch (arg) {
                        case "debug":
                            DebugMenu.isDebugEnabled = true;
                            break;
                    }
                }
            }
        }
        new MobappGameDesktopMain(args);
    }

    public MobappGameDesktopMain(String[] args) {
        super(args);
        RootContainer.setRootUIComponent(new MenuCanvas());
    }
}