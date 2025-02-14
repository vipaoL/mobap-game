/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor.setup;

import mobileapplication3.ui.Button;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.TextComponent;

/**
 *
 * @author vipaol
 */
public class Page2 extends AbstractSetupWizardPage {

    private final TextComponent text = new TextComponent();

    public Page2(Button[] buttons, SetupWizard.Feedback feedback) {
        super("What are structures?", buttons, feedback);
    }

    public void init() {
    	super.init();
    	actionButtons.setSelected(actionButtons.getButtonCount() - 1);
    }

    public void initOnFirstShow() {
        this.text.setText(
                "The structures can be compared to parts of an endless puzzle,"
                        + " and the world of mobapp-game is just"
                        + " a random combination of them."
                        + " You can create your own structures,"
                        + " load them into the game and the game will use them"
                        + " to generate the world on a par with the built-in ones.");
    }

    protected IUIComponent initAndGetPageContent() {
        return text;
    }
    
}
