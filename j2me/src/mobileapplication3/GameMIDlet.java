/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreNotFoundException;
import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.MobappMIDlet;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.Records;
import mobileapplication3.platform.ui.RootContainer;

/**
 * @author vipaol
 */
public class GameMIDlet extends MobappMIDlet {
    private boolean isRunning = false;

    public void onStart() {
        if (isRunning) {
            Logger.log("Main:startApp:already started");
            return;
        }

        Platform.init(this);
        isRunning = true;
        Logger.log("Main:constr");
        MenuCanvas menuCanvas = new MenuCanvas();
        RootContainer.setRootUIComponent(menuCanvas);

        // ------- migrate records to use a unified way to storing them
        try {
            try {
            	// try to read from the new storage
            	int[] records = Records.getRecords();
            	// if the new storage is empty...
            	if (records == null || records.length == 0) {
            		// old format and old storage
                	tryMigrateRecordsFrom("Records");
            	}
            } catch (NumberFormatException ex) {
            	// old format, but already moved to the new storage
            	tryMigrateRecordsFrom("records");
            }
        } catch (Exception ex) {
            Platform.showError("Can't migrate records!", ex);
        }
        // -------
    }

	private void tryMigrateRecordsFrom(String oldStoreName) {
		try {
			RecordStore oldStore = null;
			int numRecords;
			try {
				oldStore = RecordStore.openRecordStore(oldStoreName, false);
				numRecords = oldStore.getNumRecords();
			} catch (RecordStoreNotFoundException ex) {
				numRecords = 0;
			}

			if (numRecords > 0) {
				int[] records = new int[numRecords];
			    for (int i = 0; i < numRecords; i++) {
				    records[i] = byteArrayToInt(oldStore.getRecord(i + 1));
			    }
			    oldStore.closeRecordStore();
				RecordStore.deleteRecordStore(oldStoreName);

			    for (int i = 0; i < numRecords; i++) {
					 // save to the new place
					Records.saveRecord(records[i], numRecords);
			    }
			}
		} catch (Exception ex) {
	        Platform.showError("Can't migrate records!", ex);
	    }
	}

    public static int byteArrayToInt(byte[] bytes) {
        return (bytes[0] << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
    }
    
}
