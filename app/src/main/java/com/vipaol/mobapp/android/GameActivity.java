package com.vipaol.mobapp.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.UISettings;

public class GameActivity extends Activity {
    private RootContainer currentRoot = null;
    private GameActivity inst;
    private static boolean activityVisible;

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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (currentRoot != null) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                currentRoot.keyPressed(event.getKeyCode());
                return true;
            }
            if (event.getAction() == KeyEvent.ACTION_UP) {
                currentRoot.keyReleased(event.getKeyCode());
                return true;
            }
        }
        return false;
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
