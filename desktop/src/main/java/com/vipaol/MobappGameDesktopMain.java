package com.vipaol;

import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MobappGameDesktopMain extends Frame {

    public static void main(String[] args) {
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