package mobileapplication3.editor;

import java.io.IOException;
import java.util.Vector;

import mobileapplication3.editor.elements.Element;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.Button;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.IUIComponent;

public class StructuresMenu extends AbstractEditorMenu {

	private String path = null;

	public StructuresMenu(final IPopupFeedback parent) {
		super(parent, "Structures");
	}

	private String getPath() {
		if (path == null) {
			path = EditorSettings.getStructsFolderPath();
		}
		return path;
	}

	public IUIComponent[] getGridContent() {
		Vector gridContentVector = new Vector();
		String[] files = { };
		try {
			files = listFiles(getPath());
		} catch (IOException e) {
			Platform.showError(e);
		}
		Logger.log("getting grid content: " + files.length + " files");
		try {
			for (int i = 0; i < files.length; i++) {
				final String filePath = getPath() + files[i];
				try {
					gridContentVector.addElement(new EditorFileListCell(filePath) {
						public void openInEditor() {
							StructuresMenu.this.openInEditor(filePath);
						}
					});
				} catch (Exception ex) {
					Logger.log("Can't create StructureViewer:");
					Logger.log(ex);
				}
			}
		} catch (Exception e) {
			Platform.showError(e);
		}
		IUIComponent[] gridContent = new IUIComponent[gridContentVector.size()];
		for (int i = 0; i < gridContentVector.size(); i++) {
			gridContent[i] = (IUIComponent) gridContentVector.elementAt(i);
		}
		Logger.log("Grid: " + gridContent.length + " cells");
		return gridContent;
	}

	public Button[] getList() {
		String[] files = null;
		try {
			files = listFiles(getPath());
		} catch (IOException e) {
			Platform.showError(e);
		}
		if (files == null) {
			files = new String[0];
		}
		Button[] buttons = new Button[files.length];
		for (int i = 0; i < files.length; i++) {
			final String name = files[i];
			buttons[i] = new Button(name) {
				public void buttonPressed() {
					openInEditor(getPath() + name);
				}
			};
		}
		return buttons;
	}

	public void openInEditor(String path) {
		openInEditor(MGStructs.readMGStruct(path), path);
	}

	public void openInEditor(Element[] elements, String path) {
		RootContainer.setRootUIComponent(new EditorUI(EditorUI.MODE_STRUCTURE, elements, path));
	}

	protected void createNew() {
		RootContainer.setRootUIComponent(new EditorUI(EditorUI.MODE_STRUCTURE));
	}
}
