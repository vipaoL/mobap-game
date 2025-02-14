/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor;

import mobileapplication3.editor.setup.SetupWizard;
import mobileapplication3.ui.AbstractPopupPage;
import mobileapplication3.ui.BackButton;
import mobileapplication3.ui.Button;
import mobileapplication3.ui.ButtonCol;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.Switch;

/**
 *
 * @author vipaol
 */
public class SettingsUI extends AbstractPopupPage {

    public SettingsUI(IPopupFeedback parent) {
        super("Settings", parent);
    }

    protected Button[] getActionButtons() {
        return new Button[] {
            new BackButton(feedback)
        };
    }

    protected IUIComponent initAndGetPageContent() {
        Button[] settingsButtons = new Button[]{
    		new Button("Current game folder: " + EditorSettings.getGameFolderPath()) {
                public void buttonPressed() { }
            }.setIsActive(false),//.setBgColorInactive(0x223322).setFontColorInactive(0xaaaaaa),
            new Switch("Smooth keyboard scrolling") {
				public boolean getValue() {
					return EditorSettings.getKbSmoothScrollingEnabled();
				}

				public void setValue(boolean value) {
					EditorSettings.setKbSmoothScrollingEnabled(value);
					getUISettings().onChange();
				}
            },
            new Switch("Kinetic touch scrolling") {
				public boolean getValue() {
					return EditorSettings.getKineticScrollingEnabled();
				}

				public void setValue(boolean value) {
					EditorSettings.setKineticScrollingEnabled(value);
					getUISettings().onChange();
				}
            },
            new Switch("Transparent background of popups") {
                public boolean getValue() {
                    return EditorSettings.getTransparencyEnabled();
                }

                public void setValue(boolean value) {
                    EditorSettings.setTransparencyEnabled(value);
                    getUISettings().onChange();
                }
            },
            new Switch("Key repeats in lists") {
				public boolean getValue() {
					return EditorSettings.getKeyRepeatedInListsEnabled();
				}

				public void setValue(boolean value) {
					EditorSettings.setKeyRepeatedInListsEnabled(value);
					getUISettings().onChange();
				}
            },
            new Switch("Auto-save") {
            	public boolean getValue() {
					return EditorSettings.getAutoSaveEnabled();
				}

				public void setValue(boolean value) {
					EditorSettings.setAutoSaveEnabled(value);
					getUISettings().onChange();
				}
            },
            new Switch("Show log") {
            	public boolean getValue() {
					return EditorSettings.getOnScreenLogEnabled();
				}

				public void setValue(boolean value) {
					EditorSettings.setOnScreenLogEnabled(value);
					getUISettings().onChange();
				}
            },
            new Button("Open setup wizard") {
                public void buttonPressed() {
                    showPopup(new SetupWizard(new SetupWizard.FinishSetup() {
						public void onFinish() {
							closePopup();
							isInited = false;
		                    init();
						}
					}));
                }
            },
            new Button("Reset settings") {
                public void buttonPressed() {
                    EditorSettings.resetSettings();
                    isInited = false;
                    init();
                }
            }.setBgColor(0x550000)
        };

        return new ButtonCol(settingsButtons);
    }
    
    public void setPageContentBounds(IUIComponent pageContent, int x0, int y0, int w, int h) {
        if (pageContent != null) {
            ((ButtonCol) pageContent)
                    .setButtonsBgPadding(margin/8)
                    .setSize(w - margin*2, h - margin*2)
                    .setPos(x0 + w/2, y0 + h - margin, BOTTOM | HCENTER);
        }
    }
}
