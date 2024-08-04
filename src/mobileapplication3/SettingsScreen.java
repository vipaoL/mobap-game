package mobileapplication3;

import javax.microedition.lcdui.Graphics;

import utils.MobappGameSettings;

public class SettingsScreen extends GenericMenu implements Runnable {
    private static final String[] MENU_OPTS = {
            "Better graphics",
            "Unlock FPS",
            "Show FPS",
            "Debug settings",
            "back"
        };
        
        // array with states of all buttons (active/inactive/enabled)
        private final int[] statemap = new int[MENU_OPTS.length];
        private static int fontSizeCache = -1;
        
        public SettingsScreen() {
            setFullScreenMode(true);
            (new Thread(this, "debug menu")).start();
        }
        
        private void init() {
            sizeChanged(getWidth(), getHeight());
            loadParams(Main.sWidth, Main.sHeight, MENU_OPTS, statemap, fontSizeCache);
            fontSizeCache = getFontSize();
            
            setSpecialOption(MENU_OPTS.length - 2); // highlight "Debug settings" if enabled
            setIsSpecialOptnActivated(DebugMenu.isDebugEnabled);
            
            refreshStates();
        }
        
        protected void sizeChanged(int w, int h) {
            fontSizeCache = -1;
            super.sizeChanged(w, h);
        }

        public void run() {
            long sleep;
            long start;
            
            paint();
            
            if (!isMenuInited()) {
                init();
            }

            while (!isStopped) {
                if (!isPaused) {
                    start = System.currentTimeMillis();
                    paint();

                    sleep = Main.TICK_DURATION - (System.currentTimeMillis() - start);
                    sleep = Math.max(sleep, 0);
                } else {
                    sleep = 200;
                }
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
        public void paint() {
            Graphics g = getGraphics();
            g.setColor(0, 0, 0);
            g.fillRect(0, 0, Math.max(w, h), Math.max(w, h));
            
            if (isMenuInited()) {
                super.paint(g);
                tick();
            }
            
            flushGraphics();
        }
        void selectPressed() {
            int selected = this.selected;
            switch (selected) {
                case 0:
                    MobappGameSettings.toggleBetterGraphics();
                    break;
                case 1:
                    MobappGameSettings.toggleFPSUnlocked();
                    break;
                case 2:
                	MobappGameSettings.toggleFPSShown();
                	break;
                default:
                    break;
            }
            if (selected == MENU_OPTS.length - 2) {
                isStopped = true;
                Main.set(new DebugMenu());
            } else if (selected == MENU_OPTS.length - 1) {
                isStopped = true;
                Main.set(new MenuCanvas());
            } else {
                refreshStates();
            }
        }
        void refreshStates() {
        	setEnabledFor(MobappGameSettings.isBetterGraphicsEnabled(), 0);
        	setEnabledFor(MobappGameSettings.isFPSUnlocked(), 1);
        	setEnabledFor(MobappGameSettings.isFPSShown(), 2);
        }
    }