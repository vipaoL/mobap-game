package com.vipaol.mobapp.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.MobappActivity;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.UISettings;

public class GameActivity extends MobappActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ------- migrate records to use a unified way to storing them
        try {
            // try to read from the new storage
            String records = Platform.readStoreAsString("records");

            // if the new storage is empty...
            if (records == null || records.equals("")) {
                // read from the old storage
                SharedPreferences oldPrefs = getSharedPreferences("Records", Context.MODE_PRIVATE);
                records = oldPrefs.getString("records", "");
                // save to the new place
                Platform.storeString(records, "records");
            }
        } catch (Exception ex) {
            Platform.showError("Can't migrate records!", ex);
        }
        // -------
    }

    @Override
    protected IUIComponent getRootUIComponent() {
        return new MenuCanvas();
    }

    @Override
    protected UISettings getUISettings() {
        return null;
    }
}
