package com.vipaol.mobapp.android;

import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.ui.MobappActivity;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.UISettings;

public class GameActivity extends MobappActivity {

    @Override
    protected IUIComponent getRootUIComponent() {
        return new MenuCanvas();
    }

    @Override
    protected UISettings getUISettings() {
        return null;
    }
}
