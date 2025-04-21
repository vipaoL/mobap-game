package com.vipaol.mobapp.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import java.util.Objects;

import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.MobappActivity;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.UISettings;
import utils.MobappGameSettings;

public class GameActivity extends MobappActivity {
    private IUIComponent root = new MenuCanvas();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // handle file open intents
        try {
            Intent intent = getIntent();
            if (Objects.equals(intent.getAction(), Intent.ACTION_VIEW)) {
                root = IntentsUtil.handleFileOpenIntent(intent, this);
                RootContainer.setRootUIComponent(root);
            }
        } catch (Throwable ex) {
            Platform.showError(ex);
        }

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

        // Older Android versions don't support drawing arcs
        if (Platform.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Enable legacy drawing method by default
            MobappGameSettings.isLegacyDrawingMethodEnabled(true);
        }
    }

    @Override
    protected IUIComponent getRootUIComponent() {
        return root;
    }

    @Override
    protected UISettings getUISettings() {
        return null;
    }
}
