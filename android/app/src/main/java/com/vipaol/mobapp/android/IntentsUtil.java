package com.vipaol.mobapp.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.DataInputStream;
import java.io.FileNotFoundException;

import mobileapplication3.editor.EditorSettings;
import mobileapplication3.editor.EditorUI;
import mobileapplication3.editor.MGStructs;
import mobileapplication3.editor.elements.Element;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.ui.IUIComponent;

public class IntentsUtil {
    public static IUIComponent handleFileOpenIntent(Intent intent, Context context) throws FileNotFoundException {
        Uri uri = intent.getData();
        Element[] elements = MGStructs.readMGStruct(new DataInputStream(context.getContentResolver().openInputStream(uri)));
        if (elements == null) {
            elements = new Element[0];
        }
        String name = getFileName(uri, context);
        int mode = name.endsWith(".mgstruct") ? EditorUI.MODE_STRUCTURE : EditorUI.MODE_LEVEL;
        String path = (mode == EditorUI.MODE_STRUCTURE ?
                EditorSettings.getStructsFolderPath() :
                EditorSettings.getLevelsFolderPath())
                + FileUtils.SEP + name;
        return new EditorUI(mode, elements, path).setViewMode(true);
    }

    @SuppressLint("Range")
    public static String getFileName(Uri uri, Context context) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
