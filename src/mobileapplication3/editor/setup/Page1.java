/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor.setup;

import mobileapplication3.editor.About;
import mobileapplication3.ui.Button;
import mobileapplication3.ui.IUIComponent;

/**
 *
 * @author vipaol
 */
public class Page1 extends AbstractSetupWizardPage {

    public Page1(Button[] buttons, SetupWizard.Feedback feedback) {
        super("Welcome to the editor for mobapp-game", buttons, feedback);
    }

    public void initOnFirstShow() { }

    protected IUIComponent initAndGetPageContent() {
    	return About.getAppLogo();
    }

    public void setPageContentBounds(IUIComponent pageContent, int x0, int y0, int w, int h) {
        int freeSpaceH = h - margin*2;
        int logoSide = Math.min(freeSpaceH, w - margin * 2);

        pageContent.setSize(logoSide, logoSide)
                .setPos(x0 + w / 2, y0 + h / 2, IUIComponent.VCENTER | IUIComponent.HCENTER);
    }

}
