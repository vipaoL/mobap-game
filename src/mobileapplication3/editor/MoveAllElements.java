package mobileapplication3.editor;

import mobileapplication3.editor.elements.Element;
import mobileapplication3.ui.*;

public class MoveAllElements extends AbstractPopupPage {
    private final IPopupFeedback parent;
    private final Element[] elements;
    private final Property dx, dy;

    public MoveAllElements(IPopupFeedback parent, Element[] elements) {
        super(getName(), parent);
        this.parent = parent;
        this.elements = elements;
        dx = new Property("dx");
        dy = new Property("dy");
    }

    protected Button[] getActionButtons() {
        return new Button[] {
                new Button("OK") {
                    public void buttonPressed() {
                        Element.move(elements, dx.getValue(), dy.getValue());
                        close();
                    }
                },
                new BackButton(parent)
        };
    }

    protected IUIComponent initAndGetPageContent() {
        return new List(new IUIComponent[] {
                new Slider(dx),
                new Slider(dy)
        });
    }

    public static String getName() {
        return "Move all elements";
    }
}
