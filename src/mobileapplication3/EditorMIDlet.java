/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import mobileapplication3.editor.Editor;
import mobileapplication3.platform.MobappMIDlet;

/**
 *
 * @author vipaol
 */
public class EditorMIDlet extends MobappMIDlet {

    public void onStart() {
        Editor.startEditor();
    }
}
