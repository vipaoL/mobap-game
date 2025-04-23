/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.AbstractPopupPage;
import mobileapplication3.ui.BackButton;
import mobileapplication3.ui.Button;
import mobileapplication3.ui.ButtonCol;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.Keys;

/**
 *
 * @author vipaol
 */
public class EditorQuickMenu extends AbstractPopupPage {
    private final EditorUI parent;

    public EditorQuickMenu(EditorUI parent) {
        super(parent.getFileName(), parent);
        this.parent = parent;
    }

    protected Button[] getActionButtons() {
        return new Button[] {
            new BackButton(parent)
        };
    }

    protected IUIComponent initAndGetPageContent() {
        return new ButtonCol(getButtons());
    }

    private Button[] getButtons() {
        boolean gameIncluded = false;
        try {
            Class.forName("mobileapplication3.game.GameplayCanvas");
            gameIncluded = true;
        } catch (ClassNotFoundException ignored) { }

        Button levelTestButton = new Button("Open this level in the game") {
            public void buttonPressed() {
                RootContainer.setRootUIComponent(new mobileapplication3.game.GameplayCanvas(parent).loadLevel(parent.getData()).disablePointCounter());
            }
        }.setBgColor(0x112211).setSelectedColor(0x115511).setIsActive(gameIncluded).setBindedKeyCode(Keys.KEY_NUM1);

        Button structureTestButton = new Button("Test this structure in the game") {
            public void buttonPressed() {
                RootContainer.setRootUIComponent(new mobileapplication3.game.GameplayCanvas(parent).addDeferredStructure(parent.getData()).disablePointCounter());
            }
        }.setBgColor(0x112211).setSelectedColor(0x115511).setIsActive(gameIncluded).setBindedKeyCode(Keys.KEY_NUM1);

        Button saveButton = new Button("Save \"" + parent.getFileName() + "\"") {
            public void buttonPressed() {
            	close();
            	try {
            		parent.saveToFile(parent.getFilePath());
            		AutoSaveUI.deleteAutoSave(parent.getMode());
            	} catch (Exception ex) {
                    Logger.log(ex);
                    Platform.showError(ex);
                }
            }
        }.setBindedKeyCode(Keys.KEY_NUM8).setIsActive(parent.getFilePath() != null);

        Button saveAsButton = new Button("Save as...") {
            public void buttonPressed() {
            	close();
            	final int mode = parent.getMode();
            	String path;
            	if (mode == EditorUI.MODE_STRUCTURE) {
            		path = EditorSettings.getStructsFolderPath();
            	} else {
            		path = EditorSettings.getLevelsFolderPath();
            	}
                parent.showPopup(new PathPicker(mode, parent).pickFolder(path, "Save as \"" + PathPicker.QUESTION_REPLACE_WITH_PATH + "\" ?", new PathPicker.Feedback() {
                    public void onComplete(final String path) {
                        (new Thread(new Runnable() {
                            public void run() {
                                try {
                                    Logger.log("Saving to " + path);
                                    parent.saveToFile(path);
                                    Logger.log("Saved!");
                                    AutoSaveUI.deleteAutoSave(mode);
                                    parent.setFilePath(path);
                                    parent.closePopup();
                                } catch (Exception ex) {
                                    Logger.log(ex);
                                    Platform.showError(ex);
                                }
                                repaint();
                            }
                        })).start();
                    }

                    public void onCancel() {
                    	parent.closePopup();
                    }
                }));
            }
        }.setBindedKeyCode(Keys.KEY_NUM9);

        Button menuButton = new Button("Open Menu") {
            public void buttonPressed() {
                RootContainer.setRootUIComponent(new MainMenu(RootContainer.getInst()));
            }
        }.setBindedKeyCode(Keys.KEY_NUM5);

        Button[] buttons = null;
        if (parent.getMode() == EditorUI.MODE_STRUCTURE) {
            buttons = new Button[]{structureTestButton, saveButton, saveAsButton, menuButton};
        } else if (parent.getMode() == EditorUI.MODE_LEVEL) {
            buttons = new Button[]{levelTestButton, saveButton, saveAsButton, menuButton};
        }
        return buttons;
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
