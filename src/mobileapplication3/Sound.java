/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

/**
 *
 * @author vipaol
 */
public class Sound {
    Player midiPlayer = null;
    String guide = "repack the game with your .mid music named as \"a.mid\"";
    public void startBgMusic() {
        if (!load("/a.mid", true)) {
            load("resource://a.mid", false);
        }
        
        if (midiPlayer != null & DebugMenu.music) {
            try {
                midiPlayer.start();
            } catch (MediaException ex) {
                ex.printStackTrace();
                Main.showAlert("Can't play music (" + ex.toString() + ")");
            }
        }
    }
    public boolean load(String path_res, boolean supressAlert) {
        try {
            midiPlayer = Manager.createPlayer(Main.thiss.getClass().getResourceAsStream(path_res), "audio/midi");
            return true;
        } catch (IllegalArgumentException ex) {
            if (!supressAlert) {
                Main.showAlert("Can't load music (" + ex.toString() + "). No music found, " + guide, 10000);
            }
        } catch (Exception ex) {
            if (!supressAlert) {
                Main.showAlert("Can't load music (" + ex.toString() + "). Maybe your device doesn't support it. If it does, " + guide, 10000);
            }
            ex.printStackTrace();
        }
        return false;
    }
    public void stop() {
        if (midiPlayer != null) {
            try {
                midiPlayer.stop();
            } catch (MediaException ex) {
                ex.printStackTrace();
            }
        }
    }
}
