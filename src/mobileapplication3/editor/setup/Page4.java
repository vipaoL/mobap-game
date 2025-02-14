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
public class Page4 extends AbstractSetupWizardPage {
    
    private final TextComponent text = new TextComponent();
    
    public Page4(Button[] buttons, SetupWizard.Feedback feedback) {
        super("Let's get started", buttons, feedback);
    }
    
    public void init() {
    	super.init();
    	actionButtons.setSelected(actionButtons.getButtonCount() - 1);
    }

    public void initOnFirstShow() {
        this.text.setText(
                "This app lets you to create structures and levels for mobapp-game."
                        + " You can load structures into the game"
                        + " by pressing \"Load Structures\" in the main menu."
                        + " Let's choose a folder to save structures"
                        + " and levels you'll create into.");
    }

    protected IUIComponent initAndGetPageContent() {
        return text;
    }
    
}