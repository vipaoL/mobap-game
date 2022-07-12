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
    public void startBgMusic() {
        try {
            midiPlayer = Manager.createPlayer(Main.thiss.getClass().getResourceAsStream("/a.mid"), "audio/midi");
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (MediaException ex) {
            ex.printStackTrace();
        }
        if (midiPlayer != null & DebugMenu.music) {
            try {
                midiPlayer.start();
            } catch (MediaException ex) {
                ex.printStackTrace();
            }
        }
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
