package mobileapplication3.game;

import mobileapplication3.platform.Platform;
import mobileapplication3.platform.Records;
import mobileapplication3.platform.ui.RootContainer;

/**
 *
 * @author vipaol
 */
public class RecordsScreen extends GenericMenu implements Runnable {

    private String[] buttons;

    public RecordsScreen() {
        int[] records = new int[0];
		try {
			records = Records.getRecords();
		} catch (Exception ex) {
			Platform.showError("Can't get records:", ex);
		}
        buttons = new String[records.length + 2];
    	buttons[0] = "Best scores";
        buttons[buttons.length-1] = "Back";
        for (int i = 0; i < records.length; i++) {
        	buttons[i + 1] = records[i] + "";
        }
        loadParams(buttons, 1, buttons.length - 1, buttons.length - 1);
	}

    public void init() {
        isStopped = false;
        getFontSize();
        (new Thread(this, "records")).start();
    }

    public void selectPressed() {
        if (selected == buttons.length - 1) {
            isStopped = true;
            RootContainer.setRootUIComponent(new MenuCanvas());
        }
    }

    public void run() {
        long sleep = 0;
        long start = 0;

        isPaused = false;
        while (!isStopped) {
            if (!isPaused) {
                start = System.currentTimeMillis();

                repaint();
                tick();

                sleep = GameplayCanvas.TICK_DURATION - (System.currentTimeMillis() - start);
                sleep = Math.max(sleep, 0);
            } else {
                sleep = 100;
            }
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
