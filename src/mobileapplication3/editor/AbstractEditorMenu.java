package mobileapplication3.editor;

import java.io.IOException;

import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.Utils;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.ui.*;

public abstract class AbstractEditorMenu extends AbstractPopupWindow {

	private final static int LAYOUT_MINIMIZED = 1, LAYOUT_LIST_OF_NAMES = 2, LAYOUT_GRID = 3;

	private final TextComponent title;
	private final ButtonCol buttons;
	private Grid grid = null;
	private ButtonRow backButtonComponent;
	private String[] files = null;
	private final IPopupFeedback parent;

	private int layout = LAYOUT_MINIMIZED;

	public AbstractEditorMenu(final IPopupFeedback parent, String titleStr) {
		super(parent);
        this.parent = parent;

		title = new TextComponent(titleStr);
		buttons = new ButtonCol();

		switch (EditorSettings.getWhatToLoadAutomatically()) {
			case EditorSettings.OPTION_ALWAYS_LOAD_NONE:
				setLayout(LAYOUT_MINIMIZED);
				break;
			case EditorSettings.OPTION_ALWAYS_LOAD_LIST:
				setLayout(LAYOUT_LIST_OF_NAMES);
				break;
			case EditorSettings.OPTION_ALWAYS_LOAD_THUMBNAILS:
				setLayout(LAYOUT_GRID);
				break;
		}
	}

	public void setLayout(int layout) {
		this.layout = layout;

		final Button createButton = new Button("Create new") {
			public void buttonPressed() {
				createNew();
			}
		}.setBindedKeyCode(Keys.KEY_NUM1);

		final Switch alwaysShowListSwitch = new Switch("Always show list") {
			public void setValue(boolean value) {
				EditorSettings.setWhatToLoadAutomatically(value ? EditorSettings.OPTION_ALWAYS_LOAD_LIST : EditorSettings.OPTION_ALWAYS_LOAD_NONE);
			}

			public boolean getValue() {
				return EditorSettings.getWhatToLoadAutomatically() >= EditorSettings.OPTION_ALWAYS_LOAD_LIST;
			}
		};

		final Switch alwaysShowGridSwitch = new Switch("Always show thumbnails") {
			public void setValue(boolean value) {
				EditorSettings.setWhatToLoadAutomatically(value ? EditorSettings.OPTION_ALWAYS_LOAD_THUMBNAILS : EditorSettings.OPTION_ALWAYS_LOAD_LIST);
			}

			public boolean getValue() {
				return EditorSettings.getWhatToLoadAutomatically() >= EditorSettings.OPTION_ALWAYS_LOAD_THUMBNAILS;
			}
		};

		final Button showGridButton = new Button("Show thumbnails") {
			public void buttonPressed() {
				setLayout(LAYOUT_GRID);
			}
		};

		final BackButton backButton = new BackButton(parent);
		backButtonComponent = new ButtonRow(new Button[]{backButton}).bindToSoftButtons();

		switch (layout) {
			case LAYOUT_MINIMIZED:
				buttons.setButtons(new Button[] {
						createButton,
						new Button("Open") {
							public void buttonPressed() {
								setLayout(LAYOUT_LIST_OF_NAMES);
							}
						},
						new ButtonStub(),
						new BackButton(parent)
				});
				setComponents(new IUIComponent[]{title, buttons});
				break;
			case LAYOUT_LIST_OF_NAMES:
				Button[] fileButtons = getList();
				int topExtraButtons = 3;
				int bottomExtraButtons = 0;
				Button[] btns = new Button[topExtraButtons + fileButtons.length + bottomExtraButtons];
				btns[0] = createButton;
				btns[1] = alwaysShowListSwitch;
				btns[2] = showGridButton;
				System.arraycopy(fileButtons, 0, btns, topExtraButtons, fileButtons.length);
				buttons.setButtons(btns);
				setComponents(new IUIComponent[]{title, buttons, backButtonComponent});
				break;
			case LAYOUT_GRID:
				IUIComponent[] thumbnails = getGridContent();
				int topExtraCells = 2;
				IUIComponent[] cells = new IUIComponent[topExtraCells + thumbnails.length];
				cells[0] = new ButtonComponent(createButton);
				cells[1] = new ButtonComponent(alwaysShowGridSwitch);
				for (int i = 0; i < thumbnails.length; i++) {
					cells[topExtraCells + i] = thumbnails[i];
				}
				grid = new Grid(cells);
				setComponents(new IUIComponent[]{title, grid, backButtonComponent});
				break;
		}
	}

	protected void onSetBounds(int x0, int y0, int w, int h) {
		title
		        .setSize(w, TextComponent.HEIGHT_AUTO)
		        .setPos(x0, y0, TOP | LEFT);

		switch (layout) {
			case LAYOUT_MINIMIZED:
			    buttons
			            .setButtonsBgPadding(w/128)
			            .setSize(w/2, (y0 + h - title.getBottomY()))
			            .setPos(x0 + w/2, y0 + h, BOTTOM | HCENTER);
			    break;
			case LAYOUT_LIST_OF_NAMES:
				backButtonComponent
						.setSize(w, ButtonRow.H_AUTO)
						.setPos(x0 + w/2, y0 + h, HCENTER | BOTTOM);
			    buttons
			            .setButtonsBgPadding(w/128)
			            .setSize(w, backButtonComponent.getTopY() - title.getBottomY())
			            .setPos(x0 + w/2, backButtonComponent.getTopY(), BOTTOM | HCENTER);
				break;
			case LAYOUT_GRID:
				backButtonComponent
						.setSize(w, ButtonRow.H_AUTO)
						.setPos(x0 + w/2, y0 + h, HCENTER | BOTTOM);
				grid
						.setCols(Mathh.constrain(1, w/Font.getDefaultFontHeight()/5, 3))//.setCols(3)
						.setElementsPadding(w/128)
						.setSize(w, backButtonComponent.getTopY() - title.getBottomY())
						.setPos(x0 + w/2, backButtonComponent.getTopY(), BOTTOM | HCENTER);
				break;
		}
	}

	protected String[] listFiles(String path) throws IOException {
		if (files == null) {
			files = FileUtils.list(path);
		}
		return files;
	}

	protected abstract Button[] getList();
	protected abstract IUIComponent[] getGridContent();
	protected abstract void createNew();

	protected abstract class EditorFileListCell extends Container {
		protected String path;
		private StructureViewerComponent structureViewer;
		private TextComponent fileNameLabel;

		public EditorFileListCell(String path) {
			this.path = path;
			structureViewer = new StructureViewerComponent(MGStructs.readMGStruct(path));
			String[] tmp = Utils.split(path, String.valueOf(FileUtils.SEP));
			fileNameLabel = new TextComponent(tmp[tmp.length - 1]);
			setComponents(new IUIComponent[]{structureViewer, fileNameLabel});
		}

		protected void onSetBounds(int x0, int y0, int w, int h) {
			fileNameLabel.setSize(w, TextComponent.HEIGHT_AUTO).setPos(x0, y0 + h, TextComponent.BOTTOM | TextComponent.LEFT);
			structureViewer.setSize(w, fileNameLabel.getTopY() - y0).setPos(x0, y0, TextComponent.TOP | TextComponent.LEFT);
		}

		public boolean canBeFocused() {
			return true;
		}

		public boolean pointerClicked(int x, int y) {
			openInEditor();
			return true;
		}

		public boolean keyPressed(int keyCode, int count) {
			openInEditor();
			return true;
		}

		protected abstract void openInEditor();
	}
}
