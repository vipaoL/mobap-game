package mobileapplication3.editor.setup;

import mobileapplication3.ui.Button;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.TextComponent;

/**
 *
 * @author vipaol
 */
public class Page3 extends AbstractSetupWizardPage {
    
    private final TextComponent text = new TextComponent();
    
    public Page3(Button[] buttons, SetupWizard.Feedback feedback) {
        super("What are levels?", buttons, feedback);
    }
    
    public void init() {
    	super.init();
    	actionButtons.setSelected(actionButtons.getButtonCount() - 1);
    }

    public void initOnFirstShow() {
        this.text.setText(
                "Levels are completely predefined tracks."
                + " To complete a level you should touch a green finish plate."
                + " Levels are an experimental feature yet.");
    }

    protected IUIComponent initAndGetPageContent() {
        return text;
    }
    
}
