package com.vipaol.mobapp.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import mobileapplication3.editor.EditorSettings;
import mobileapplication3.editor.MainScreenUI;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.editor.setup.SetupWizard;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.UISettings;
import mobileapplication3.platform.Platform;

public class EditorActivity extends Activity {
    private RootContainer currentRoot;
    private EditorActivity inst;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Platform.init(this);
        inst = this;
        try {
            final UISettings uiSettings = new UISettings() {
                public boolean getKeyRepeatedInListsEnabled() {
                    return EditorSettings.getKeyRepeatedInListsEnabled(false);
                }

                public boolean getAnimsEnabled() {
                    return EditorSettings.getAnimsEnabled(true);
                }

                public void onChange() {
                    try {
                        currentRoot.init();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            setRootContainer(new RootContainer(this, null, uiSettings));
            if (EditorSettings.isSetupWizardCompleted()) {
                RootContainer.setRootUIComponent(new MainScreenUI());
            } else {
                RootContainer.setRootUIComponent(new SetupWizard(new SetupWizard.FinishSetup() {
                    public void onFinish() {
                        RootContainer.setRootUIComponent(new MainScreenUI());
                    }
                }));
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            Platform.showError(ex);
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_HOME:
                return false;
        }
        if (currentRoot != null) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                currentRoot.keyPressed(keyCode);
                return true;
            }
            if (event.getAction() == KeyEvent.ACTION_UP) {
                currentRoot.keyReleased(keyCode);
                return true;
            }
        }
        return false;
    }

    public void setRootContainer(RootContainer newRootContainer) {
        this.currentRoot = newRootContainer;
        Platform.getActivityInst().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(newRootContainer);
            }
        });
    }

    public void setRootComponent(RootContainer currentRoot) {
        this.currentRoot = currentRoot;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(currentRoot);
            }
        });
    }
}
