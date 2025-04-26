/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.game;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.CanvasComponent;
import mobileapplication3.ui.Keys;

/**
 *
 * @author vipaol
 */
public abstract class GenericMenu extends CanvasComponent {
    private static final int PAUSE_DELAY = 5;
    public int x0, y0, w, h;
    private int fontH, tick = 0, k = 10, keyPressDelay = 0,
            keyPressDelayAfterShowing = 5, firstReachable = 0, lastReachable = NOT_SET,
            firstDrawable = 0, specialOption = -1, pauseDelay = PAUSE_DELAY, lastKeyCode = 0;
    
    public int selected;
    
    // colors
    protected int normalColor = 0xffffff, selectedColor = 0xff4040,
            pressedColor = 0xE03838, specialOptionActivatedColor = 0xffff00,
            colUnreachable = 0x888888, colReachableEnabled = 0x88ff00, bgColor = 0x000000;
    private String[] options;
    
    private boolean isPressedByPointerNow, firstload = true,
            isSpecialOptionActivated = false, isSelectPressed = false,
            fontFound = false;
    
    private boolean isKnownButton = true, isInited = false;
    public boolean isPaused = false;
    public boolean isStopped = false;
    private Font font;
    private int[] stateMap = null;
    public static final int STATE_INACTIVE = -1;
    public static final int STATE_NORMAL = 0;
    public static final int STATE_NORMAL_ENABLED = 1;

    public static final int SIEMENS_KEY_FIRE = -26;
    public static final int SIEMENS_KEY_UP = -59;
    public static final int SIEMENS_KEY_DOWN = -60;
    public static final int SIEMENS_KEY_LEFT = -61;
    public static final int SIEMENS_KEY_RIGHT = -62;
    
    protected void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
    	if (bgColor >= 0) {
    		g.setColor(bgColor);
    		g.fillRect(x0, y0, Math.max(w, h), Math.max(w, h));
    	}
        if (isInited && options != null) {
            for (int i = firstDrawable; i < options.length; i++) {
            	if (font != null) {
            		g.setFont(font);
            	}
                g.setColor(normalColor);
                int offset = 0;

                if (i == selected) { // highlighting selected option
                    offset = Mathh.sin(tick * 360 / 10); // waving animation
                    g.setColor(selectedColor);
                    if (isPressedByPointerNow) {
                        g.setColor(pressedColor);
                        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, font.getSize()));
                    }

                }

                if (stateMap != null) { // coloring other options depending on theirs state (if we have this info)
                    if (stateMap[i] == STATE_NORMAL_ENABLED) {
                        g.setColor(colReachableEnabled);
                    } else if (stateMap[i] == STATE_INACTIVE) {
                        g.setColor(colUnreachable);
                    }
                }

                if (i == specialOption && isSpecialOptionActivated) { // painting special option in a different color
                    g.setColor(specialOptionActivatedColor);
                }

                int x = x0 + w / 2;
                int y = y0 + k * (i + 1 - firstDrawable) - fontH / 2 - h / (options.length + 1 - firstDrawable) / 2 + offset*Font.getDefaultFont().getHeight() / 8000;
                if (options[i] != null) {
                	g.drawString(options[i], x, y, Graphics.HCENTER | Graphics.TOP); // draw option on (x, y) //
                }

                if (DebugMenu.isDebugEnabled && DebugMenu.showFontSize) {
                    g.drawString(String.valueOf(font.getSize()), x0, y0, 0); // display text size (for debug)
                }
            }
            
            if (!isKnownButton) {
                g.setColor(0x808080);
                g.drawString(lastKeyCode + " - unknown keyCode", w, h, Graphics.BOTTOM | Graphics.RIGHT);
            }
        } else {
            g.setColor(0x808080);
            g.drawString("Loading the menu...", w / 2, h, Graphics.BOTTOM | Graphics.HCENTER);
        }
    }
    
    public int findOptimalFont(int canvW, int canvH, String[] options) {
        font = new Font(Font.SIZE_LARGE);
        
        // height
        if (font.getHeight() * (options.length - firstDrawable) >= canvH - canvH/16) {
            font = new Font(Font.SIZE_MEDIUM);
        }
        if (font.getHeight() * (options.length - firstDrawable) >= canvH - canvH/16) {
            font = new Font(Font.SIZE_SMALL);
        }
        
        // width
        if (font.getSize() != Font.SIZE_SMALL) {
            for (int i = firstDrawable; i < options.length - 1; i++) {
                if (font.stringWidth(options[i]) >= canvW - canvW/16) {
                    font = new Font(Font.SIZE_MEDIUM);
                    if (font.stringWidth(options[i]) >= canvW - canvW/16) {
                        font = new Font(Font.SIZE_SMALL);
                        break;
                    }
                }
            }
        }
        return font.getHeight();
    }
    
    private boolean isOptionAvailable(int n) {
        if (stateMap != null) {
            if (n >= stateMap.length) {
                return false;
            }
            if (stateMap[n] == STATE_INACTIVE) {
                return false;
            }
        }

        if (n < firstReachable || n > getLastReachable()) {
            return false;
        }
        
        return true;
    }
    
    public boolean isMenuInited() {
        return isInited;
    }
    
    public boolean handlePointerPressed(int x, int y) {
        handlePointer(x, y);
        return true;
    }

    public boolean handlePointerDragged(int x, int y) {
        handlePointer(x, y);
        return true;
    }

    public boolean handlePointerClicked(int x, int y) {
        if (handlePointer(x, y)) {
            selectPressed();
        }
        return true;
    }
    
    public boolean handlePointer(int x, int y) {
        x -= x0;
        y -= y0;
        isPaused = false;
        int selected = firstDrawable + y / k;
        if (selected < firstReachable && firstReachable < firstDrawable) {
            selected = firstReachable;
        }
        if (!isOptionAvailable(selected)) {
            isPressedByPointerNow = false;
            return false;
        }
        this.selected = selected;
        isPressedByPointerNow = true;
        return stateMap == null || stateMap[selected] != STATE_INACTIVE;
    }
    
    private boolean handleKeyStates(int keyStates) {
        if (keyStates == 0) {
        	return false;
        }

        int lastReachable = getLastReachable();

        isPaused = false;
        switch (keyStates) {
        	case Keys.LEFT:
        		selected = lastReachable; // back
			case Keys.RIGHT:
			case Keys.FIRE:
				isSelectPressed = true;
            	isKnownButton = true;
				return stateMap == null || stateMap[selected] != STATE_INACTIVE;
		}
        
        boolean needRepeat;
        do {
            switch (keyStates) {
            	case Keys.UP:
            		isKnownButton = true;
                    isPaused = false;
                    if (selected > firstReachable) {
                        selected--;
                    } else {
                        selected = lastReachable;
                    }
                    break;
            	case Keys.DOWN:
                    isKnownButton = true;
                    isPaused = false;
                    if (selected < lastReachable) {
                        selected++;
                    } else {
                        selected = firstReachable;
                    }
                    break;
            }
            
            needRepeat = !isSelectPressed && stateMap != null && stateMap[selected] == STATE_INACTIVE;
        } while (needRepeat);

        return isSelectPressed;
    }

    public boolean handleKeyRepeated(int keyCode, int pressedCount) {
    	handleKeyPressed(keyCode);
    	return true;
    }
    
    public boolean handleKeyPressed(int keyCode, int count) {
        if(handleKeyPressed(keyCode)) {
            selectPressed();
            isSelectPressed = false;
        }
        return true;
    }
    
    public boolean handleKeyPressed(int keyCode) {
        lastKeyCode = keyCode;
        isKnownButton = false;
        isPaused = false;
        boolean pressed = false;
        int selected = -1;
        switch (keyCode) {
            case Keys.KEY_NUM1:
                selected = 0;
                pressed = true;
                break;
            case Keys.KEY_NUM2:
                selected = 1;
                pressed = true;
                break;
            case Keys.KEY_NUM3:
                selected = 2;
                pressed = true;
                break;
            case Keys.KEY_NUM4:
                selected = 3;
                pressed = true;
                break;
            case Keys.KEY_NUM5:
                selected = 4;
                pressed = true;
                break;
            case Keys.KEY_NUM6:
                selected = 5;
                pressed = true;
                break;
            case Keys.KEY_NUM7:
                selected = 6;
                pressed = true;
                break;
            case Keys.KEY_NUM8:
                selected = 7;
                pressed = true;
                break;
            case Keys.KEY_NUM9:
                selected = 8;
                pressed = true;
                break;
            case Keys.KEY_STAR:
                selected = 9;
                pressed = true;
                break;
            case SIEMENS_KEY_UP:
                isKnownButton = true;
                handleKeyStates(Keys.UP);
                break;
            case SIEMENS_KEY_DOWN:
                isKnownButton = true;
                handleKeyStates(Keys.DOWN);
                break;
            case Keys.KEY_NUM0: // back
            case Keys.KEY_SOFT_RIGHT:
            case SIEMENS_KEY_RIGHT:
            	return handleKeyStates(Keys.LEFT);
            case Keys.KEY_POUND:
            case Keys.KEY_SOFT_LEFT: // select
            case SIEMENS_KEY_LEFT:
            case SIEMENS_KEY_FIRE:
                return handleKeyStates(Keys.FIRE);
            default:
                return handleKeyStates(RootContainer.getAction(keyCode));
        }
        selected += firstReachable;
        
        if (pressed) {
            isKnownButton = true;
            if (isOptionAvailable(selected)) {
                this.selected = selected;
                return true;
            }
        }

        return false;
    }

    public boolean handleKeyReleased(int keyCode, int count) {
        keyPressDelay = 0;
        isSelectPressed = false;
        return true;
    }
    
    protected void loadCanvasParams(int x0, int y0, int w, int h) {
        this.x0 = x0;
        this.y0 = y0;
        if (w <= 0 || h <= 0) {
        	return;
        }
        
        this.w = w;
        this.h = h;
        
        if (options != null) {
            k = (h + h / (options.length + 1 - firstDrawable)) / (options.length + 1 - firstDrawable);
            fontH = findOptimalFont(w, h, options);
            fontFound = true;
        }
    }
    public void reloadCanvasParameters(int scW, int scH) {
        reloadCanvasParameters(x0, y0, scW, scH);
    }
    public void reloadCanvasParameters(int x0, int y0, int w, int h) {
        loadCanvasParams(x0, y0, w, h);
    }

    public void onShow() {
        Logger.log("menu:showNotify");
        
        isPaused = false;
        pauseDelay = PAUSE_DELAY;
    }
    
    public void onHide() {
        Logger.log("menu:hideNotify");
        // It prevents a bug on siemens that calls hideNotify right after calling showNotify.
        if (pauseDelay <= 0) {
            isPaused = true;
        }
    }
    
    protected void onSetBounds(int x0, int y0, int w, int h) {
        this.w = w;
        this.h = h;
        reloadCanvasParameters(w, h);
    }

    public boolean canBeFocused() {
    	return true;
    }

    public void loadParams(String[] options) {
        loadParams(options, options.length - 1);
    }

    public void loadParams(String[] options, int defaultSelected) {
        this.options = options;
        stateMap = new int[options.length];
        if (firstload) {
            selected = defaultSelected;
            firstload = false;
        }
        isInited = true;
    }

    public void setFirstReachable(int firstReachable) {
        this.firstReachable = firstReachable;
    }

    private int getLastReachable() {
        return lastReachable != NOT_SET ? lastReachable : options.length - 1;
    }

    public void setLastReachable(int lastReachable) {
        this.lastReachable = lastReachable;
    }

    public void loadStatemap(int[] stateMap) {
        if (stateMap != null) {
            if (stateMap.length == options.length) {
                this.stateMap = stateMap;
                Logger.log("stateMap loaded");
            } else {
                Platform.showError("incorrect stateMap length: " + stateMap.length + " " + getClass().getName());
            }
        }
    }
    public void setDefaultColor(int color_hex) {
        normalColor = color_hex;
    }
    public void setSelectedColor(int color_hex) {
        selectedColor = color_hex;
    }
    public void setPressedColor(int color_hex) {
        pressedColor = color_hex;
    }
    public void setColorEnabledOption(int color_hex) {
        colReachableEnabled = color_hex;
    }
    public void setColorUnreachableOption(int color_hex) {
        colUnreachable = color_hex;
    }
    public void setSpecialOptnActColor(int colorActivated) {
        specialOptionActivatedColor = colorActivated;
    }
    public void setSpecialOption(int n) {
        specialOption = n;
    }
    public void setIsSpecialOptnActivated (boolean isActivated) {
        isSpecialOptionActivated = isActivated;
    }
    public void setFirstDrawable(int n) {
        firstDrawable = n;
        k = (h + h / (options.length + 1 - firstDrawable)) / (options.length + 1 - firstDrawable);
    }
    public void setEnabledFor(boolean enabled, int i) {
        if (enabled) {
            setStateFor(1, i);
        } else {
            setStateFor(0, i);
        }
    }
    public void setStateFor(int state, int i) {
        if (stateMap == null) {
            return;
        }
        stateMap[i] = state;
    }
    public int getFontSize() {
        if (fontFound) {
            return font.getSize();
        } else {
            return -1;
        }
    }
    public void tick() {
        if (tick > 9) {
            tick = 0;
        } else {
            tick++;
        }
        if (pauseDelay > 0) {
            pauseDelay--;
        }
        if (keyPressDelayAfterShowing > 0) {
            keyPressDelayAfterShowing--;
        }
        if (keyPressDelay > 0) {
            keyPressDelay--;
        }
    }
    
    abstract void selectPressed();
}
