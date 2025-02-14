/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor.setup;

import mobileapplication3.ui.Button;
import mobileapplication3.ui.Page;

/**
 *
 * @author vipaol
 */
public abstract class AbstractSetupWizardPage extends Page {
    private boolean isInited = false;
    private final Button[] buttons;
    protected SetupWizard.Feedback feedback;

    public AbstractSetupWizardPage(String title, Button[] actionButtons, SetupWizard.Feedback feedback) {
        super(title);
        buttons = actionButtons;
        if (buttons == null) {
            try {
                throw new IllegalArgumentException("buttons is null " + getClass().getName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        this.feedback = feedback;
    }

    public void init() {
    	super.init();
        actionButtons.setIsSelectionEnabled(true);
        actionButtons.setIsSelectionVisible(true);
        actionButtons.setFocused(false);
    }

    protected Button[] getActionButtons() {
        return buttons;
    }

    public final void onShow() {
        if (isInited) {
            return;
        }

        initOnFirstShow();
        isInited = true;
    }

    public abstract void initOnFirstShow();

}
