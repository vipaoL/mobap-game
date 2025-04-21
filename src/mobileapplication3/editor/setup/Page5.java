/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor.setup;

import mobileapplication3.editor.EditorSettings;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.AbstractPopupPage;
import mobileapplication3.ui.Button;
import mobileapplication3.ui.ButtonCol;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.UIComponent;

/**
 *
 * @author vipaol
 */
public class Page5 extends AbstractSetupWizardPage {

    private final ButtonCol list;
    private Button[] listButtons;

    public Page5(Button[] buttons, SetupWizard.Feedback feedback) {
        super("Let's choose the game folder", buttons, feedback);
        list = new ButtonCol() {
            public boolean handlePointerClicked(int x, int y) {
                super.handlePointerClicked(x, y);
                return false;
        	}
        };
    }

    public void init() {
    	super.init();
    	actionButtons.setSelected(actionButtons.getButtonCount() - 1);
    	actionButtons.buttons[1] = new Button("Finish") {
			public void buttonPressed() {
				saveFolderChoice(listButtons[list.getSelected()].getTitle());
			}
		};
		list.setIsSelectionVisible(true);
    }

    public void initOnFirstShow() {
    	fillList();
        list.setButtonsBgPadding(margin/4);
    }

    private void fillList() {
        (new Thread(new Runnable() {
            public void run() {
                String[] folders = FileUtils.getAllPlaces("MobappGame"); // TODO make constant
                listButtons = new Button[folders.length];
                for (int i = 0; i < folders.length; i++) {
                    listButtons[i] = new Button(folders[i]) {
                    	public void buttonPressed() { }
                    };
                }
                list.setButtons(listButtons);
                list.setSelected(list.buttons.length - 1);
                if (w != 0 && h != 0) {
	                onSetBounds(x0, y0, w, h/2);
	                onSetBounds(x0, y0, w, h);
                }
                feedback.needRepaint();
            }
        })).start();
    }

    private void saveFolderChoice(final String path) {
        showPopup(new LoadingPopup("Checking folder...", this));
        (new Thread(new Runnable() {
        	String folderPath;
            public void run() {
            	folderPath = null;
                try {
                	folderPath = EditorSettings.getStructsFolderPath(path);
                    FileUtils.createFolder(folderPath);
                } catch (Throwable ex) {
                    closePopup();
                    Platform.showError("Can't create folder \"" + folderPath + ": ", ex);
                }
                folderPath = null;
                try {
                	folderPath = EditorSettings.getLevelsFolderPath(path);
                    Logger.log("creating subfolder: " + folderPath);
                    FileUtils.createFolder(folderPath);
                } catch (Throwable ex) {
                    closePopup();
                    Platform.showError("Can't create folder \"" + folderPath + "\": ", ex);
                }
                try {
                    FileUtils.checkFolder(path);
                    EditorSettings.setGameFolderPath(path);
                    feedback.nextPage();
                } catch (Exception ex) {
                    closePopup();
                    Platform.showError("Can't create file in this folder: ", ex);
                }
            }
        })).start();
    }

    public void setPageContentBounds(IUIComponent pageContent, int x0, int y0, int w, int h) {
        if (pageContent != null) {
            ((ButtonCol) pageContent)
                    .setSizes(w - margin*2, h - margin*2, ButtonCol.H_AUTO, true)
                    .setPos(x0 + w/2, y0 + h - margin, BOTTOM | HCENTER);
        }
    }

    protected IUIComponent initAndGetPageContent() {
        return list;
    }
    
    protected void refreshFocusedComponents() {
    	super.refreshFocusedComponents();
    	list.setFocused(true);
    }
    
    private static class LoadingPopup extends AbstractPopupPage {
        int animOffset = 100;

        public LoadingPopup(String title, IPopupFeedback parent) {
            super(title, parent);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        while (hasParent()) {
                            animOffset += 10;
                            repaint();
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException ignored) { }
                }
            }, "loading anim").start();
        }

        public boolean canBeFocused() {
            return true;
        }

        protected Button[] getActionButtons() {
            return new Button[] {
                new Button("Cancel") { public void buttonPressed() { } }.setIsActive(false)
            };
        }

        protected IUIComponent initAndGetPageContent() {
            return new UIComponent() {
                protected boolean handlePointerClicked(int x, int y) {
                    return false;
                }

                protected boolean handleKeyPressed(int keyCode, int count) {
                    return false;
                }

                public void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
                    g.setColor(0xffffff);
                    int side = Math.min(w, h);
                    g.drawArc(x0 + (w - side)/2, y0 + (h - side)/2, side, side, animOffset % 360, (animOffset + 250) % 360);
                    g.drawString("Please", x0 + w/2, y0 + h/2, Graphics.BOTTOM | Graphics.HCENTER);
                    g.drawString("wait....", x0 + w/2, y0 + h/2, Graphics.TOP | Graphics.HCENTER);
                }

                public boolean canBeFocused() {
                    return false;
                }
            };
        }
    }
}
