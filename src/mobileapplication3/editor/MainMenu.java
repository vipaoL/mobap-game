package mobileapplication3.editor;

import mobileapplication3.editor.AutoSaveUI.AutoSaveData;
import mobileapplication3.editor.elements.Element;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.*;

public class MainMenu extends Container {
	private final TextComponent title;
	private final UIComponent logo;
	private final ButtonCol buttons;
	private boolean isAutoSaveEnabled = true;
	private static boolean autoSaveCheckDone = false;

	public MainMenu(IPopupFeedback parent) {
		boolean gameIncluded = false;
        try {
            Class.forName("mobileapplication3.game.MenuCanvas");
            gameIncluded = true;
        } catch (ClassNotFoundException ignored) { }

		final MainMenu inst = this;
		title = new TextComponent("Mobapp Editor");
		logo = About.getAppLogo();
		int c = Keys.KEY_NUM1;
		buttons = new ButtonCol(new Button[] {
				new Button("Structures") {
					public void buttonPressed() {
						showPopup(new StructuresMenu(inst));
					}
				}.setBgColor(BG_COLOR_HIGHLIGHTED).setBindedKeyCode(c++),
				new Button("Levels") {
					public void buttonPressed() {
						showPopup(new LevelsMenu(inst));
					}
				}.setBgColor(BG_COLOR_HIGHLIGHTED).setBindedKeyCode(c++),
				new Button("Open Game") {
					public void buttonPressed() {
						RootContainer.setRootUIComponent(new mobileapplication3.game.MenuCanvas());
					}
				}.setBgColor(BG_COLOR_HIGHLIGHTED).setIsActive(gameIncluded).setBindedKeyCode(c++),
				new Button("Settings") {
					public void buttonPressed() {
						showPopup(new SettingsUI(inst));
					}
				}.setBindedKeyCode(c++),
				new Button("About") {
		            public void buttonPressed() {
		                showPopup(new About(inst));
		            }
		        }.setBindedKeyCode(c++),
				new BackButton(parent)
		});
		setComponents(new IUIComponent[]{title, logo, buttons});
	}

	public void init() {
		isAutoSaveEnabled = EditorSettings.getAutoSaveEnabled(true);
		super.init();
		checkAutoSaveStorage();
	}

	protected void onSetBounds(int x0, int y0, int w, int h) {
		int margin = h/16;
		if (w <= h) { // vertical layout
	        title
	                .setSize(w, TextComponent.HEIGHT_AUTO)
	                .setPos(x0, y0, TOP | LEFT);
	        buttons
			        .setButtonsBgPadding(w/128)
			        .setSize(w/2, h/2)
			        .setPos(x0 + w/2, y0 + h - margin, BOTTOM | HCENTER);
	        
	        int logoSide = Math.min(w * 3 / 4, buttons.getTopY() - title.getBottomY() - margin);
	        if (logoSide < 1) {
	        	logoSide = 1;
	        }
	        logo
	        		.setSize(logoSide, logoSide)
	        		.setPos(x0 + w/2, (buttons.getTopY() + title.getBottomY()) / 2, VCENTER | HCENTER);
	        buttons.setSize(Math.max(w/2, logoSide * 32 / 31), h/2);
		} else { // horizontal layout
			title
		            .setSize(w, TextComponent.HEIGHT_AUTO)
		            .setPos(x0, y0, TOP | LEFT);
		    buttons
					.setIsSelectionEnabled(true)
			        .setButtonsBgPadding(w/128)
			        .setSize(w/2 - margin, h - title.h - margin * 2)
			        .setPos(x0 + w - margin, (title.getBottomY() + y0 + h) / 2, VCENTER | RIGHT);
		    
		    int logoSide = Math.min(buttons.getWidth(), y0 + h - title.getBottomY() - margin * 2);
		    if (logoSide < 1) {
		    	logoSide = 1;
		    }
		    logo
		    		.setSize(logoSide, logoSide)
		    		.setPos(x0 + w/4, (y0 + h + title.getBottomY()) / 2, VCENTER | HCENTER);
		}
	}

	private void checkAutoSaveStorage() {
		if (!isAutoSaveEnabled || autoSaveCheckDone) {
			return;
		}

		final int mode;
		try {
			AutoSaveData data = AutoSaveUI.autoSaveRead(AutoSaveUI.STRUCTURE);
			if (data != null) {
				mode = AutoSaveUI.STRUCTURE;
			} else {
				data = AutoSaveUI.autoSaveRead(AutoSaveUI.LEVEL);
				mode = AutoSaveUI.LEVEL;
			}
			if (data != null) {
				final Element[] elements = data.getElements();
				if (elements.length > 2) {
					final String path = data.getPath();
					showPopup(new AutoSaveUI(this, elements) {
						public void onRestore() {
							close();
							RootContainer.setRootUIComponent(new EditorUI(mode, elements, path));
						};
	
						public void onDelete() {
							AutoSaveUI.deleteAutoSave(mode);
							close();
						};
					});
				}
			}
		} catch (Exception ex) {
			Platform.showError("Can't restore auto-saved data:", ex);
			closePopup();
		}

		autoSaveCheckDone = true;
	}
}
