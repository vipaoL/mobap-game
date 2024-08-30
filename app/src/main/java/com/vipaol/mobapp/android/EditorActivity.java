package com.vipaol.mobapp.android;

import mobileapplication3.editor.EditorSettings;
import mobileapplication3.editor.MainScreenUI;
import mobileapplication3.platform.ui.MobappActivity;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.editor.setup.SetupWizard;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.UISettings;

public class EditorActivity extends MobappActivity {

    @Override
    protected IUIComponent getRootUIComponent() {
        if (EditorSettings.isSetupWizardCompleted()) {
            return new MainScreenUI();
        } else {
            return new SetupWizard(new SetupWizard.FinishSetup() {
                public void onFinish() {
                    RootContainer.setRootUIComponent(new MainScreenUI());
                }
            });
        }
    }

    @Override
    protected UISettings getUISettings() {
        return new UISettings() {
            public boolean getKeyRepeatedInListsEnabled() {
                return EditorSettings.getKeyRepeatedInListsEnabled(false);
            }

            public boolean getAnimsEnabled() {
                return EditorSettings.getAnimsEnabled(true);
            }

            public void onChange() {
                onUISettingsChange();
            }
        };
    }
}
