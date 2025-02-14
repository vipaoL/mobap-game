/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor.setup;

import mobileapplication3.editor.EditorSettings;
import mobileapplication3.platform.Logger;
import mobileapplication3.ui.Button;
import mobileapplication3.ui.Container;
import mobileapplication3.ui.IUIComponent;

/**
 *
 * @author vipaol
 */
public class SetupWizard extends Container {

    private int currentPageI = 0;
    private final FinishSetup finishSetup;
    private final Feedback pageSwitcher = new Feedback() {
        public void nextPage() {
            Logger.log(currentPageI + " - current. switching to next page");
            setCurrentPage(currentPageI + 1);
        }

        public void prevPage() {
            Logger.log(currentPageI + " - current. switching to prev page");
            setCurrentPage(currentPageI - 1);
        }

        public void needRepaint() {
            repaint();
        }
    };

    private AbstractSetupWizardPage[] pages;

    public SetupWizard(FinishSetup finishSetup) {
        this.finishSetup = finishSetup;

        pages = new AbstractSetupWizardPage[]{
            new Page1(new Button[]{getNewNextButton()}, pageSwitcher),
            new Page2(new Button[]{getNewPrevButton(), getNewNextButton()}, pageSwitcher),
            new Page3(new Button[]{getNewPrevButton(), getNewNextButton()}, pageSwitcher),
            new Page4(new Button[]{getNewPrevButton(), getNewNextButton()}, pageSwitcher),
            new Page5(new Button[]{getNewPrevButton(), getNewNextButton()}, pageSwitcher)
        };
    }

    public void init() {
    	for (int i = 0; i < pages.length; i++) {
        	pages[i].setParent(this);
		}
    	setCurrentPage(currentPageI);
    }
    
    private Button getNewNextButton() {
        return new Button("Next") {
            public void buttonPressed() {
                pageSwitcher.nextPage();
            }
        };
    }
    
    private Button getNewPrevButton() {
        return new Button("Back") {
            public void buttonPressed() {
                pageSwitcher.prevPage();
            }
        };
    }
    
    private void setCurrentPage(int i) {
        Logger.log("setting page i=" + i);
        pages[currentPageI].setParent(this);
        pages[currentPageI].setVisible(false);
        if (i < 0) {
            return;
        } else if (i >= pages.length) {
            finishSetup();
            return;
        }
        
        currentPageI = i;
        pages[currentPageI].onShow();
        pages[currentPageI].setVisible(true);
        setComponents(new IUIComponent[]{pages[currentPageI]});
        if (w != 0 && h != 0) {
            setSize(w, h);
        }
    }
    
    private void finishSetup() {
        pages = null;
        finishSetup.onFinish();
        EditorSettings.setIsSetupWizardCompleted(true);
    }

    public void onSetBounds(int x0, int y0, int w, int h) {
        pages[currentPageI].setSize(w, h);
    }
    
    public interface Feedback {
        public void nextPage();
        public void prevPage();
        public void needRepaint();
    }

//    protected IUIComponent[] getComponents() {
//        return new IUIComponent[]{currentPage};
//    }
    
    public interface FinishSetup {
        void onFinish();
    }
}
