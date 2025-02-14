/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor;

import java.io.IOException;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.Image;
import mobileapplication3.ui.AbstractPopupPage;
import mobileapplication3.ui.BackButton;
import mobileapplication3.ui.Button;
import mobileapplication3.ui.ButtonCol;
import mobileapplication3.ui.Container;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.ImageComponent;
import mobileapplication3.ui.TextComponent;
import mobileapplication3.ui.UIComponent;

/**
 *
 * @author vipaol
 */
public class About extends AbstractPopupPage {
    private static final String LINK = "https://github.com/vipaoL/mobapp-editor";
    private static final String LINK_PREVIEW = "vipaoL/mobapp-editor";
    private static final String LINK2 = "https://t.me/mobapp_game";
    private static final String LINK2_PREVIEW = "@mobapp_game";

    public About(IPopupFeedback parent) {
        super("About", parent);
    }

    protected Button[] getActionButtons() {
        return new Button[] {
            new BackButton(feedback)
        };
    }

    public static UIComponent getAppLogo() {
    	Image logoImage = null;
        String errorMessage = null;

        try {
            logoImage = Image.createImage("/editorlogo.png");
        } catch (IOException ex) {
            Logger.log(ex);
            errorMessage = ex + " ";
            try {
                logoImage = Image.createImage("/editoricon.png");
            } catch (IOException e) {
                Logger.log(e);
                errorMessage += e;
            }
        }

        UIComponent logo;
        if (logoImage != null) {
            logo = new ImageComponent(logoImage);
            logo.setBgColor(COLOR_ACCENT_MUTED);
        } else {
            logo = new TextComponent("Here could be your ad. " + errorMessage);
            logo.setBgColor(COLOR_ACCENT_MUTED);
        }
        return logo;
    }

    protected IUIComponent initAndGetPageContent() {
        final UIComponent logo = getAppLogo();
        final ButtonCol buttonsList = new ButtonCol(new Button[]{
                new Button("Open GitHub " + LINK_PREVIEW) {
                    public void buttonPressed() {
                        Platform.platformRequest(LINK);
                    }
                }.setBgColor(BG_COLOR_HIGHLIGHTED),
                new Button("Open TG channel " + LINK2_PREVIEW) {
                    public void buttonPressed() {
                        Platform.platformRequest(LINK2);
                    }
                }.setBgColor(BG_COLOR_HIGHLIGHTED)
        });

        return new Container() {
            public void init() {
                setComponents(new IUIComponent[] {logo, buttonsList});
                setBgColor(COLOR_TRANSPARENT);
            }

            protected void onSetBounds(int x0, int y0, int w, int h) {
                buttonsList.setButtonsBgPadding(margin/8).setSize(w, ButtonCol.H_AUTO).setPos(x0, y0 + h, BOTTOM | LEFT);
                int logoSide = Math.min(w, h - buttonsList.getHeight() - margin);
                logo.setSize(logoSide, logoSide).setPos(x0 + w/2, (y0 + buttonsList.getTopY() - margin) / 2, HCENTER | VCENTER);
            }
        };
    }
}
