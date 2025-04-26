/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor;

import java.io.IOException;
import java.util.Calendar;

import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Utils;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.ui.AbstractPopupPage;
import mobileapplication3.ui.Button;
import mobileapplication3.ui.ButtonCol;
import mobileapplication3.ui.Container;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.TextComponent;

/**
 *
 * @author vipaol
 */


// TODO: move list to FileManager


public class PathPicker extends AbstractPopupPage {

    public static final String STRUCTURE_FILE_EXTENSION = ".mgstruct";
    public static final String LEVEL_FILE_EXTENSION = ".mglvl";
	public static final String QUESTION_REPLACE_WITH_PATH = ".curr_folder.";
    private static final int TARGET_SAVE = 0, TARGET_OPEN = 1;
    public static final int MODE_STRUCTURE = EditorUI.MODE_STRUCTURE, MODE_LEVEL = EditorUI.MODE_LEVEL;

    private final TextComponent question = new TextComponent();
    private final ButtonCol list = new ButtonCol();
    private Feedback feedback;

    private final int mode;
    private int currentTarget = TARGET_SAVE;
    private String currentFolder = null, pickedPath, fileName = "";
    private String questionTemplate = "";

    public PathPicker(int mode, IPopupFeedback parent) {
    	super("File picker", parent);
    	this.mode = mode;
    	initUI();
    }

    public PathPicker pickFolder(String question, Feedback onComplete) {
        return pickFolder(null, question, onComplete);
    }

    public PathPicker pickFolder(String initialPath, String question, Feedback onComplete) {
        questionTemplate = question;
        currentTarget = TARGET_SAVE;
        currentFolder = initialPath;
        feedback = onComplete;
        initFM();
        return this;
    }

    public PathPicker pickFile(String question, Feedback onComplete) {
        return pickFile(null, question, onComplete);
    }

    public PathPicker pickFile(String initialPath, String question, Feedback onComplete) {
        questionTemplate = question;
        currentTarget = TARGET_OPEN;
        this.currentFolder = initialPath;
        feedback = onComplete;
        initFM();
        return this;
    }

    private void initFM() {
        question.setText("");
        if (currentTarget == TARGET_SAVE) {
            title.setText("Choose a folder");
        } else if (currentTarget == TARGET_OPEN) {
            title.setText("Choose a file");
        }

        if (currentTarget == TARGET_SAVE) {
            fileName = generateNewFileName();
            Logger.log(fileName);
        }

        (new Thread(new Runnable() {
            public void run() {
                if (currentFolder == null) {
                    currentFolder = FileUtils.PREFIX;
                    String[] roots = FileUtils.getRoots();
                    setPaths(roots);
                } else {
                    if (currentTarget == TARGET_SAVE) {
                        pickPath(currentFolder + fileName);
                    }
                    getNewList();
                }
            }
        })).start();
    }

    private void getNewList() {
        title.setText(currentFolder);

        (new Thread(new Runnable() {
            public void run() {
                try {
                    setPaths(FileUtils.list(currentFolder));
                } catch (IOException ex) {
                    Logger.log(ex);
                }
            }
        })).start();
    }

    private void pickPath(String path) {
        pickedPath = path;
        if (!fileName.equals("")) {
            question.setText(Utils.replace(questionTemplate, QUESTION_REPLACE_WITH_PATH, pickedPath));
        } else {
            question.setText("");
        }
    }

    private void setPaths(String[] paths) {
        Button[] listButtons = new Button[paths.length];
        for (int i = 0; i < paths.length; i++) {
            final String name = paths[i];
            listButtons[i] = new Button(name) {
                public void buttonPressed() {
                    // folder or file
                    if (name.endsWith(String.valueOf(FileUtils.SEP))) {
                        if (currentTarget == TARGET_OPEN) {
                            fileName = "";
                        }
                        currentFolder = currentFolder + name;
                        pickPath(currentFolder + fileName);
                        getNewList();
                    } else {
                        if (currentTarget == TARGET_OPEN) {
                            fileName = name;
                        }

                        pickPath(currentFolder + name);
                    }
                }
            };
        }
        setListButtons(listButtons);
    }

    private void setListButtons(Button[] buttons) {
        list.setButtons(buttons);
        setSize(w, h);
        refreshFocusedComponents();
        repaint();
    }

    private void initUI() {
        question.enableHorizontalScrolling(true).setBgColor(COLOR_TRANSPARENT);
    }

    protected Button[] getActionButtons() {
    	Button okBtn = new Button("OK") {
            public void buttonPressed() {
                if (currentTarget == TARGET_OPEN && pickedPath.endsWith(String.valueOf(FileUtils.SEP))) {
                    return;
                }
                feedback.onComplete(pickedPath);
            }
        };

        Button cancelBtn = new Button("Cancel") {
            public void buttonPressed() {
                setVisible(false);
                feedback.onCancel();
            }
        };
    	return new Button[]{okBtn, cancelBtn};
    }

    protected IUIComponent initAndGetPageContent() {
    	return new Container() {
    		public void init() {
    			setComponents(new IUIComponent[] {list, question});
    			super.init();
    		}

    		protected void onSetBounds(int x0, int y0, int w, int h) {
    	        question
    			        .setSize(w, Font.getDefaultFontHeight())
    			        .setPos(x0 + w/2, y0 + h, TextComponent.HCENTER | TextComponent.BOTTOM);
    	        list
    	        		.setButtonsBgPadding(margin/4)
    	                .setSize(w, question.getTopY() - margin - y0)
    	                .setPos(x0 + w/2, question.getTopY() - margin, ButtonCol.HCENTER | ButtonCol.BOTTOM);
    	    }
		};
    }

    private String generateNewFileName() {
        Calendar calendar = Calendar.getInstance();
        String fileExtension;
        switch (mode) {
		case MODE_STRUCTURE:
			fileExtension = STRUCTURE_FILE_EXTENSION;
			break;
		case MODE_LEVEL:
			fileExtension = LEVEL_FILE_EXTENSION;
			break;
		default:
			fileExtension = ".unknown";
			break;
		}
        return calendar.get(Calendar.YEAR)
                // it counts months from 0 while everything else from 1.
                + "-" + format((calendar.get(Calendar.MONTH) + 1)) // why? who knows...
                + "-" + format(calendar.get(Calendar.DAY_OF_MONTH))
                + "_" + format(calendar.get(Calendar.HOUR_OF_DAY))
                + "-" + format(calendar.get(Calendar.MINUTE))
                + "-" + format(calendar.get(Calendar.SECOND))
                + fileExtension;
    }

    private String format(int date) {
    	String dateStr = String.valueOf(date);
    	if (dateStr.length() < 2) {
    		dateStr = "0" + dateStr;
    	}
    	return dateStr;
    }

    public interface Feedback {
        void onComplete(final String path);
        void onCancel();
    }

}
