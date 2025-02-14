package mobileapplication3.editor;

import mobileapplication3.editor.elements.Element;
import mobileapplication3.platform.Platform;
import mobileapplication3.ui.AbstractPopupPage;
import mobileapplication3.ui.Button;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.IUIComponent;

public abstract class AutoSaveUI extends AbstractPopupPage {

	private final static String
			STORE_NAME_STRUCTURE_AUTOSAVE = "StructureAutoSave",
			STORE_NAME_LEVEL_AUTOSAVE = "LevelAutoSave",
			STORE_NAME_SUFFIX_FILE_PATH = "FilePath";

	public final static int STRUCTURE = EditorUI.MODE_STRUCTURE;
	public final static int LEVEL = EditorUI.MODE_LEVEL;

	private final Element[] elements;

	public AutoSaveUI(IPopupFeedback parent, Element[] elements) {
		super("Some unsaved data can be recovered", parent);
		this.elements = elements;
	}

	public void init() {
		super.init();
        actionButtons.setIsSelectionEnabled(true);
        actionButtons.setIsSelectionVisible(false);
	}

	protected Button[] getActionButtons() {
		return new Button[] {
			new Button("Restore") {
				public void buttonPressed() {
					onRestore();
				}
			},
			new Button("Delete") {
				public void buttonPressed() {
					onDelete();
				}
			}.setBgColor(0x550000)
		};
	}

	protected IUIComponent initAndGetPageContent() {
		return new StructureViewerComponent(elements);
	}

	public abstract void onRestore();
	public abstract void onDelete();

	public static void autoSaveWrite(StructureBuilder data, String filePath, int storeID) throws Exception {
		String storeName;
		switch (storeID) {
			case STRUCTURE:
				storeName = STORE_NAME_STRUCTURE_AUTOSAVE;
				Platform.storeShorts(data.asShortArray(), storeName);
				Platform.storeString(filePath, storeName + STORE_NAME_SUFFIX_FILE_PATH);
				break;
			case LEVEL:
				storeName = STORE_NAME_LEVEL_AUTOSAVE;
				Platform.storeShorts(data.asShortArray(), storeName);
				Platform.storeString(filePath, storeName + STORE_NAME_SUFFIX_FILE_PATH);
				break;
		}
	}

	public static AutoSaveData autoSaveRead(int autoSaveID) {
		String storeName = null, path = null;
        switch (autoSaveID) {
            case STRUCTURE:
            	storeName = STORE_NAME_STRUCTURE_AUTOSAVE;
            	path = Platform.readStoreAsString(storeName + STORE_NAME_SUFFIX_FILE_PATH);
            	break;
			case LEVEL:
				storeName = STORE_NAME_LEVEL_AUTOSAVE;
				path = Platform.readStoreAsString(storeName + STORE_NAME_SUFFIX_FILE_PATH);
				break;
        }

		Element[] elements = MGStructs.readMGStruct(Platform.readStore(storeName));
        if (elements != null) {
        	return new AutoSaveData(elements, path);
        } else {
        	return null;
        }
	}

	public static void deleteAutoSave(int autoSaveID) {
        switch (autoSaveID) {
            case STRUCTURE:
                Platform.clearStore(STORE_NAME_STRUCTURE_AUTOSAVE);
                break;
			case LEVEL:
                Platform.clearStore(STORE_NAME_LEVEL_AUTOSAVE);
                break;
        }
	}

	public static class AutoSaveData {
		private final Element[] elements;
		private final String filePath;

		public AutoSaveData(Element[] data, String filePath) {
			this.elements = data;
			if ("".equals(filePath)) {
				filePath = null;
			}
			this.filePath = filePath;
		}

		public Element[] getElements() {
			return elements;
		}

		public String getPath() {
			return filePath;
		}
	}

}
