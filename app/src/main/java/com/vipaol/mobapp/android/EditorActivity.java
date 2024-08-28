package com.vipaol.mobapp.android;

import android.app.Activity;
import android.os.Bundle;

import mobileapplication3.editor.EditorSettings;
import mobileapplication3.editor.MainScreenUI;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.editor.setup.SetupWizard;
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
            if (EditorSettings.isSetupWizardCompleted()) {
                setRootComponent(new RootContainer(inst, new MainScreenUI(), uiSettings));
            } else {
                setRootComponent(new RootContainer(this, new SetupWizard(new SetupWizard.FinishSetup() {
                    public void onFinish() {
                        inst.setRootComponent(new RootContainer(inst, new MainScreenUI(), uiSettings));
                    }
                }), uiSettings));
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            Platform.showError(ex);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
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
