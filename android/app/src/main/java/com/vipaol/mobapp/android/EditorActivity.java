package com.vipaol.mobapp.android;

import mobileapplication3.editor.EditorSettings;
import mobileapplication3.editor.MainMenu;
import mobileapplication3.platform.ui.MobappActivity;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.editor.setup.SetupWizard;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.UISettings;

public class EditorActivity extends MobappActivity {

    @Override
    protected IUIComponent getRootUIComponent() {
        if (EditorSettings.isSetupWizardCompleted()) {
            return new MainMenu(RootContainer.getInst());
        } else {
            return new SetupWizard(new SetupWizard.FinishSetup() {
                public void onFinish() {
                    RootContainer.setRootUIComponent(new MainMenu(RootContainer.getInst()));
                }
            });
        }
    }

    @Override
    protected UISettings getUISettings() {
        return new UISettings() {
            @Override
            public boolean getKbSmoothScrollingEnabled() {
                return EditorSettings.getKbSmoothScrollingEnabled(true);
            }

            @Override
            public boolean getKineticTouchScrollingEnabled() {
                return EditorSettings.getKineticScrollingEnabled(true);
            }

            @Override
            public boolean getTransparencyEnabled() {
                return EditorSettings.getTransparencyEnabled(false);
            }

            @Override
            public boolean getKeyRepeatedInListsEnabled() {
                return EditorSettings.getKeyRepeatedInListsEnabled(false);
            }

            @Override
            public boolean showKbHints() {
                return RootContainer.displayKbHints;
            }

            @Override
            public boolean enableOnScreenLog() {
                return EditorSettings.getOnScreenLogEnabled(false);
            }

            public void onChange() {
                onUISettingsChange();
            }
        };
    }
}
