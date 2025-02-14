package mobileapplication3.editor;

import java.io.IOException;
import java.util.Vector;

import mobileapplication3.editor.elements.Element;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.Button;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.UIComponent;

public class LevelsMenu extends AbstractEditorMenu {

	private String path = null;

	public LevelsMenu(final IPopupFeedback parent) {
		super(parent, "Levels");
	}

	private String getPath() {
		if (path == null) {
			path = EditorSettings.getLevelsFolderPath();
		}
		return path;
	}

	public UIComponent[] getGridContent() {
		Vector gridContentVector = new Vector();
		String[] files = { };
		try {
			files = listFiles(getPath());
		} catch (IOException e) {
			Platform.showError(e);
		}
		try {
			for (int i = 0; i < files.length; i++) {
				String path = getPath() + files[i];
				try {
					gridContentVector.addElement(new StructureViewerInteractive(MGStructs.readMGStruct(path), path));
				} catch (Exception ignored) { }
			}
		} catch (Exception e) {
			Platform.showError(e);
		}
		UIComponent[] gridContent = new UIComponent[gridContentVector.size()];
		for (int i = 0; i < gridContentVector.size(); i++) {
			gridContent[i] = (UIComponent) gridContentVector.elementAt(i);
		}
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

	private class StructureViewerInteractive extends StructureViewerComponent {
		private final String path;
		public StructureViewerInteractive(Element[] mgStruct, String path) {
			super(mgStruct);
			this.path = path;
		}

		public boolean canBeFocused() {
			return true;
		}

		protected boolean handlePointerClicked(int x, int y) {
			openInEditor();
			return true;
		}

		protected boolean handleKeyPressed(int keyCode, int count) {
			openInEditor();
			return true;
		}

		public void openInEditor() {
			LevelsMenu.this.openInEditor(elements, path);
		}
	}

	public void openInEditor(String path) {
		openInEditor(MGStructs.readMGStruct(path), path);
	}

	public void openInEditor(Element[] elements, String path) {
		RootContainer.setRootUIComponent(new EditorUI(EditorUI.MODE_LEVEL, elements, path));
	}

	protected void createNew() {
		RootContainer.setRootUIComponent(new EditorUI(EditorUI.MODE_LEVEL));
	}
}
