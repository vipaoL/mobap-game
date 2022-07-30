/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import java.io.IOException;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

/**
 *
 * @author vipaol
 */
public class Sound {
    Player midiPlayer = null;
    String guide = "ou need to repack the game and place your .mid music named as \"a.mid\" into it";
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
        } catch (IOException ex) {
            if (!supressAlert) {
                Main.showAlert("Can't load music (" + ex.toString() + "). Y" + guide, 10000);
            }
            ex.printStackTrace();
        } catch (MediaException ex) {
            if (!supressAlert) {
                Main.showAlert("Can't load music (" + ex.toString() + "). Maybe your device doesn't support it. If it does, then y" + guide, 10000);
            }
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            if (!supressAlert) {
                Main.showAlert("Can't load music (" + ex.toString() + "). Maybe your device doesn't support it. If it does, then y" + guide, 10000);
            }
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
