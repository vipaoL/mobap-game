package mobileapplication3.editor;

import mobileapplication3.editor.elements.Element;
import mobileapplication3.editor.elements.EndPoint;
import mobileapplication3.editor.elements.Element.PlacementStep;
import mobileapplication3.editor.elements.LevelStart;
import mobileapplication3.ui.AbstractPopupPage;
import mobileapplication3.ui.BackButton;
import mobileapplication3.ui.Button;
import mobileapplication3.ui.ButtonCol;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.IUIComponent;

public class ElementEditUI extends AbstractPopupPage {

	private final Element element;
	private Button[] rows;
	private final StructureBuilder sb;

	public ElementEditUI(Element element, StructureBuilder sb, IPopupFeedback parent) {
		super("Edit " + element.getName(), parent);
		this.element = element;
		this.sb = sb;
	}

	protected Button[] getActionButtons() {
        return new Button[] {
            new BackButton(feedback)
        };
    }

	protected IUIComponent initAndGetPageContent() {
		PlacementStep[] editSteps = element.getPlacementSteps();
		PlacementStep[] extraEditSteps = element.getExtraEditingSteps();
		rows = new Button[editSteps.length + extraEditSteps.length + 3 /*clone, advanced edit and delete*/];
		for (int i = 0; i < editSteps.length; i++) {
			final int o = i;
			rows[o] = new Button(editSteps[i].getName()) {
				public void buttonPressed() {
					sb.edit(element, o);
					close();
				}
			};
		}

		for (int i = 0; i < extraEditSteps.length; i++) {
			final int o = i + editSteps.length;
			rows[o] = new Button(extraEditSteps[i].getName()) {
				public void buttonPressed() {
					sb.edit(element, o);
					close();
				}
			}.setBgColor(0x201010);
		}

		Button cloneButton = new Button("Clone") {
			public void buttonPressed() {
				Element clone = element.clone();
				sb.add(clone);
				sb.edit(clone, 0);
				close();
			}
		};

		final IPopupFeedback fb = this;
		Button advancedEditButton = new Button("AdvancedEdit") {
			public void buttonPressed() {
				showPopup(new AdvancedElementEditUI(element, sb, fb));
			}
		}.setBgColor(BG_COLOR_WARN);

		Button deleteButton = new Button("Delete element") {
			public void buttonPressed() {
				sb.remove(element);
				close();
			}
		}.setBgColor(BG_COLOR_DANGER);

		if (element instanceof EndPoint || element instanceof LevelStart) {
			cloneButton.setIsActive(false);
			deleteButton.setIsActive(false);
		}

		rows[rows.length - 3] = (cloneButton);
		rows[rows.length - 2] = (advancedEditButton);
		rows[rows.length - 1] = (deleteButton);

		return new ButtonCol(rows) {
			public void onSetBounds(int x0, int y0, int w, int h) {
				setButtonsBgPadding(getBtnH()/16);
				super.onSetBounds(x0, y0, w, h);
			}
		};
	}
}
