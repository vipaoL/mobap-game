/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor;

import java.io.IOException;

import mobileapplication3.editor.elements.Element;
import mobileapplication3.editor.elements.StartPoint;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.Utils;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.AbstractButtonSet;
import mobileapplication3.ui.BackButton;
import mobileapplication3.ui.Button;
import mobileapplication3.ui.ButtonCol;
import mobileapplication3.ui.ButtonComponent;
import mobileapplication3.ui.ButtonPanelHorizontal;
import mobileapplication3.ui.ButtonRow;
import mobileapplication3.ui.Container;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.Keys;
import mobileapplication3.ui.TextComponent;

/**
 *
 * @author vipaol
 */
public class EditorUI extends Container {

    private final static int BUTTONS_IN_ROW = 4;
    public final static int FONT_H = Font.getDefaultFontHeight();
    public final static int BTN_H = FONT_H*2;
    public final static int MODE_STRUCTURE = 1, MODE_LEVEL = 2;
    private EditorCanvas editorCanvas = null;
    private ButtonRow bottomButtonPanel = null;
    private ButtonPanelHorizontal placementButtonPanel = null;
    private ButtonCol placedElementsList = null;
    private StartPointWarning startPointWarning = null;
    private final StructureBuilder elementsBuffer;

    private boolean isAutoSaveEnabled = false;
    private final int mode;
    private boolean viewMode = false;

    public EditorUI(int editorMode) {
    	mode = editorMode;
    	elementsBuffer = new StructureBuilder(mode) {
            public void onUpdate() {
                try {
                    initListPanel();
                    saveToAutoSave();
                } catch (NullPointerException ignored) { }
            }
        };
	}

    public EditorUI(int editorMode, Element[] elements, String path) {
		this(editorMode);
		elementsBuffer.setElements(elements);
		elementsBuffer.setFilePath(path);
	}

    public void init() {
        super.init();

    	isAutoSaveEnabled = EditorSettings.getAutoSaveEnabled(true);
        initEditorCanvas();
        initBottomPanel();
        initStartPointWarning();
        initPlacementPanel();
        initListPanel();

        setComponents(new IUIComponent[]{editorCanvas, startPointWarning, placementButtonPanel, placedElementsList, bottomButtonPanel});
    }

    public void onSetBounds(int x0, int y0, int w, int h) {
        bottomButtonPanel
        		.setButtonsBgPadding(BTN_H/16)
                .setSize(w, BTN_H)
                .setPos(x0, y0 + h, BOTTOM | LEFT);
        editorCanvas
                .setSize(w, h - bottomButtonPanel.h)
                .setPos(x0, y0, TOP | LEFT);
        placementButtonPanel
                .setSizes(w, ButtonPanelHorizontal.H_AUTO, BTN_H)
                .setPos(x0, y0 + h - bottomButtonPanel.h, BOTTOM | LEFT);
        placedElementsList
                .setSizes(w/3, bottomButtonPanel.getTopY() - y0 - BTN_H / 4, FONT_H * 3)
                .setPos(x0 + w, y0 + h - bottomButtonPanel.h, RIGHT | BOTTOM);
        if (startPointWarning != null) {
        	startPointWarning
	        		.setSize(startPointWarning.getOptimalW(w/3), startPointWarning.getOptimalH(bottomButtonPanel.getTopY() - y0))
	        		.setPos(bottomButtonPanel.getLeftX(), bottomButtonPanel.getTopY(), LEFT | BOTTOM);
        }
    }

    public void paint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        if (startPointWarning != null) {
            startPointWarning.setVisible(!StartPoint.checkStartPoint(elementsBuffer.getElementsAsArray()));
        }
    	super.paint(g, x0, y0, w, h, forceInactive);
    }

    public String getFilePath() {
    	return elementsBuffer.getFilePath();
    }

    public void setFilePath(String path) {
    	elementsBuffer.setFilePath(path);
    }

    public String getFileName() {
    	String path = getFilePath();
    	String name = "Unnamed file";
    	try {
	    	if (path != null) {
	    		String[] tmp = Utils.split(path, "/");
	    		name = tmp[tmp.length - 1];
	    	}
    	} catch (Exception ignored) { }
    	return name;
    }

    public short[][] getData() {
        return elementsBuffer.asShortArrays();
    }

    public int getMode() {
        return mode;
    }

    public EditorUI setViewMode(boolean viewMode) {
        this.viewMode = viewMode;
        return this;
    }

    public void saveToFile(String path) throws SecurityException, IOException {
    	elementsBuffer.saveToFile(path);
    }

    private void saveToAutoSave() {
    	if (isAutoSaveEnabled && elementsBuffer != null) {
    		new Thread(new Runnable() {
				public void run() {
					try {
						AutoSaveUI.autoSaveWrite(elementsBuffer, elementsBuffer.getFilePath(), mode);
					} catch (Exception ex) {
						Logger.log(ex);
						Platform.showError(ex);
					}
				}
			}).start();
    	}
	}

    private void initEditorCanvas() {
        editorCanvas = new EditorCanvas(elementsBuffer);
    }

    private void initBottomPanel() {
    	Button placeButton = new Button("Place") {
            public void buttonPressed() {
                placedElementsList.setVisible(false);
                placementButtonPanel.toggleIsVisible();
                placementButtonPanel.setFocused(placementButtonPanel.getIsVisible());
            }
        };

        Button menuButton = new Button("Menu") {
            public void buttonPressed() {
                showPopup(new EditorQuickMenu(EditorUI.this));
            }
        }.setBindedKeyCode(Keys.KEY_NUM0);

        Button zoomInButton = new Button("+") {
            public void buttonPressed() {
                editorCanvas.zoomIn();
            }
        }.setBindedKeyCode(Keys.KEY_STAR);

        Button zoomOutButton = new Button("-") {
            public void buttonPressed() {
                editorCanvas.zoomOut();
            }
        }.setBindedKeyCode(Keys.KEY_POUND);

        Button editButton = new Button("Edit") {
            public void buttonPressed() {
                placementButtonPanel.setVisible(false);
                placedElementsList.toggleIsVisible();
                placedElementsList.setFocused(placedElementsList.getIsVisible());
            }
        };

        BackButton backButton = new BackButton(RootContainer.getInst());
        backButton.setTitle("Back");

        Button[] bottomButtons = viewMode ?
                new Button[] {menuButton, backButton} :
                new Button[] {placeButton, menuButton, zoomInButton, zoomOutButton, editButton};
        bottomButtonPanel = (ButtonRow) new ButtonRow()
                .setButtons(bottomButtons)
                .setButtonsBgColor(BG_COLOR_HIGHLIGHTED);
        bottomButtonPanel.bindToSoftButtons();
    }

    private void initStartPointWarning() {
    	if (mode == MODE_STRUCTURE) {
    		startPointWarning = (StartPointWarning) new StartPointWarning().setVisible(false);
    	}
    }

    private void initPlacementPanel() {
        Button btnLine = new Button("Line") {
            public void buttonPressed() {
                place(Element.LINE, editorCanvas.getCursorX(), editorCanvas.getCursorY());
                placementButtonPanel.setVisible(false);
            }
        };

        Button btnCircle = new Button("Circle") {
            public void buttonPressed() {
                place(Element.CIRCLE, editorCanvas.getCursorX(), editorCanvas.getCursorY());
                placementButtonPanel.setVisible(false);
            }
        };

        Button btnSine = new Button("Sine") {
            public void buttonPressed() {
                place(Element.SINE, editorCanvas.getCursorX(), editorCanvas.getCursorY());
                placementButtonPanel.setVisible(false);
            }
        };

        Button btnBrLine = new Button("Broken\nline") {
            public void buttonPressed() {
                place(Element.BROKEN_LINE, editorCanvas.getCursorX(), editorCanvas.getCursorY());
                placementButtonPanel.setVisible(false);
            }
        };

        Button btnBrCircle = new Button("Broken\ncircle") {
            public void buttonPressed() {
                place(Element.BROKEN_CIRCLE, editorCanvas.getCursorX(), editorCanvas.getCursorY());
                placementButtonPanel.setVisible(false);
            }
        };

        Button btnAccel = new Button("Accele-\nrator") {
            public void buttonPressed() {
                place(Element.ACCELERATOR, editorCanvas.getCursorX(), editorCanvas.getCursorY());
                placementButtonPanel.setVisible(false);
            }
        };

        Button btnFinish = new Button("Level-\nFinish") {
            public void buttonPressed() {
            	place(Element.LEVEL_FINISH, editorCanvas.getCursorX(), editorCanvas.getCursorY());
                placementButtonPanel.setVisible(false);
            }
        };

        Button btnTrampoline = new Button("Trampo-\nline") {
            public void buttonPressed() {
            	place(Element.TRAMPOLINE, editorCanvas.getCursorX(), editorCanvas.getCursorY());
                placementButtonPanel.setVisible(false);
            }
        };

        Button btnLava = new Button("Lava") {
            public void buttonPressed() {
                place(Element.LAVA, editorCanvas.getCursorX(), editorCanvas.getCursorY());
                placementButtonPanel.setVisible(false);
            }
        };

        Button[] placementButtons;
        if (mode == MODE_STRUCTURE) {
        	placementButtons = new Button[] {btnLine, btnCircle, btnSine, btnBrLine, btnBrCircle.setIsActive(false), btnAccel, btnTrampoline, btnLava};
        } else {
        	placementButtons = new Button[] {btnLine, btnCircle, btnSine, btnBrLine, btnBrCircle.setIsActive(false), btnAccel, btnTrampoline, btnLava, btnFinish};
        }
        placementButtonPanel = (ButtonPanelHorizontal) new ButtonPanelHorizontal(placementButtons)
                .setBtnsInRowCount(BUTTONS_IN_ROW)
                .setIsSelectionEnabled(true)
                .setVisible(false);
    }

    private void place(int id, int x, int y) {
    	elementsBuffer.place((short) id, (short) x, (short) y);
    	placedElementsList.setSelected(placedElementsList.getButtonCount() - 1);
    }

    private void initListPanel() {
        Element[] elements = elementsBuffer.getElementsAsArray();
        Logger.log("updating, " + elements.length + " elements");
        Button[] listButtons = new Button[elements.length];
        for (int i = 0; i < elements.length; i++) {
            final Element element = elements[i];
            listButtons[i] = new Button(elements[i].getName()) {
                public void buttonPressed() { }
                public void buttonPressedSelected() {
                	placedElementsList.setVisible(false);
                	placementButtonPanel.setVisible(false);
                    showPopup(new ElementEditUI(element, elementsBuffer, EditorUI.this));
                }
            };
        }

        if (placedElementsList == null) {
            placedElementsList = new ButtonCol() {
            	public AbstractButtonSet setSelected(int selected) {
            		editorCanvas.selectedElement = selected;
            		return super.setSelected(selected);
            	}
            };
        }

        placedElementsList
        		.setButtons(listButtons)
        		.setSelected(Mathh.constrain(0, placedElementsList.getSelected(), listButtons.length - 1))
		        .setIsSelectionVisible(true)
		        .setVisible(false);
    }

    private void moveToZeros() {
    	StartPoint.moveToZeros(elementsBuffer.getElementsAsArray());
    }

    class StartPointWarning extends Container {
    	private final TextComponent message;
    	private final ButtonComponent button;

    	public StartPointWarning() {
    		setBgColor(COLOR_TRANSPARENT);
    		message = new TextComponent("Warn: start point of the structure should be on (x,y) 0 0");
    		message.setBgColor(COLOR_TRANSPARENT);
    		message.setFontColor(0xffff00);
    		Button button = new Button("Move to 0 0") {
                public void buttonPressed() {
					moveToZeros();
				}
			}.setBgColor(0x002200);
    		this.button = new ButtonComponent(button).setBindedKeyCode(Keys.KEY_NUM7);
		}

    	public void init() {
    		setComponents(new IUIComponent[] {message, this.button});
    	}

		protected void onSetBounds(int x0, int y0, int w, int h) {
			button.setSize(w, ButtonComponent.H_AUTO).setPos(x0, y0 + h, LEFT | BOTTOM);
			message.setSize(w, h - button.getHeight()).setPos(x0, y0, LEFT | TOP);
		}

		public int getOptimalW(int freeSpace) {
			return Math.min(freeSpace, Font.defaultFontStringWidth(message.getText()) / 2);
		}

		public int getOptimalH(int freeSpace) {
			return Math.min(freeSpace, FONT_H * 10);
		}

    }
}
