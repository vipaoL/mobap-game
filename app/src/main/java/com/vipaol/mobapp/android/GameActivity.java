package com.vipaol.mobapp.android;

import android.app.Activity;
import android.os.Bundle;

import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.UISettings;

public class GameActivity extends Activity {
    private RootContainer currentRoot;
    private GameActivity inst;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Platform.init(this);
        inst = this;
        try {
            final UISettings uiSettings = new UISettings() {
                public boolean getKeyRepeatedInListsEnabled() {
                    return true;
                }

                public boolean getAnimsEnabled() {
                    return false;
                }

                public void onChange() {
                    try {
                        currentRoot.init();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            setRootComponent(new RootContainer(inst, new MenuCanvas(), uiSettings));
        } catch(Exception ex) {
            ex.printStackTrace();
            Platform.showError(ex);
        }
    }

    public void setRootComponent(RootContainer currentRoot) {
        this.currentRoot = currentRoot;
        Platform.getActivityInst().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(currentRoot);
            }
        });
    }
}
