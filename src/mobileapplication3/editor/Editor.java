package mobileapplication3.editor;

import mobileapplication3.editor.setup.SetupWizard;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.UISettings;

public class Editor {
	public Editor() {
		startEditor();
	}

	public static void startEditor() {
    	try {
        	RootContainer.setUISettings(new UISettings() {
				public boolean getKeyRepeatedInListsEnabled() {
					return EditorSettings.getKeyRepeatedInListsEnabled(false);
				}

				public boolean getKbSmoothScrollingEnabled() {
					return EditorSettings.getKbSmoothScrollingEnabled(true);
				}

				public boolean getKineticTouchScrollingEnabled() {
					return EditorSettings.getKineticScrollingEnabled(true);
				}

				public boolean getTransparencyEnabled() {
					return EditorSettings.getTransparencyEnabled(false);
				}

				public boolean showKbHints() {
					return RootContainer.displayKbHints;
				}

				public boolean enableOnScreenLog() {
					return EditorSettings.getOnScreenLogEnabled(false);
				}

				public void onChange() {
					try {
						RootContainer.init();
					} catch (Exception ex) {
						Logger.log(ex);
					}
				}
			});

            if (EditorSettings.isSetupWizardCompleted()) {
                RootContainer.setRootUIComponent(new MainMenu(RootContainer.getInst()));
            } else {
                RootContainer.setRootUIComponent(new SetupWizard(new SetupWizard.FinishSetup() {
                    public void onFinish() {
                        RootContainer.setRootUIComponent(new MainMenu(RootContainer.getInst()));
                    }
                }));
            }
        } catch(Exception ex) {
            Platform.showError(ex);
        }
    }
}
