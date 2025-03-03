package com.vipaol;

import mobileapplication3.game.DebugMenu;
import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MobappGameDesktopMain extends Frame {

    public static void main(String[] args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
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
        new MobappGameDesktopMain();
    }

    public MobappGameDesktopMain() {
        setSize(1200, 900);
        Platform.init(this);
        RootContainer.getInst().setBgColor(0);
        setVisible(true);
        setLayout(new BorderLayout());
        add(RootContainer.getInst(), BorderLayout.CENTER);
        setMinimumSize(new Dimension(400, 300));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
        setLocationRelativeTo(null);
        RootContainer.setRootUIComponent(new MenuCanvas());
    }
}