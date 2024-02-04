/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author vipaol
 */
public abstract class GenericMenu extends GameCanvas {
    private static final int PAUSE_DELAY = 5;
    private static final int KEY_PRESS_DELAY = 5;
    
    public int w, h;
    private int x0, y0, fontH, tick = 0, k = 10, keyPressDelay = 0,
            keyPressDelayAfterShowing = 5, firstReachable, lastReachable,
            firstDrawable = 0, specialOption = -1, pauseDelay = PAUSE_DELAY, lastKeyCode = 0;
    
    public int selected;
    
    // colors
    private int normalColor = 0x00ffffff, selectedColor = 0x00ff4040,
            pressedColor = 0x00E03838, specialOptionActivatedColor = 0x00ffff00,
            colUnreachable = 0x00888888, colReachableEnabled = 0x00ccff00;
    private String[] options;
    
    private boolean isPressedByPointerNow, firstload = true,
            isSpecialOptionActivated = false, isSelectPressed = false,
            isSelectAlreadyPressed = false, isStatemapEnabled = false,
            fontFound = false;
    
    private boolean isKnownButton = true, isInited = false;
    public boolean isPaused = false;
    public boolean isStopped = false;
    private Font font;
    private int[] stateMap = null;
    public static final int STATE_INACTIVE = -1;
    public static final int STATE_NORMAL = 0;
    public static final int STATE_NORMAL_ENABLED = 1;
    
    
    // key codes
    public static final int SIEMENS_KEY_FIRE = -26;
    public static final int SIEMENS_KEY_UP = -59;
    public static final int SIEMENS_KEY_DOWN = -60;
    public static final int SIEMENS_KEY_LEFT = -61;
    public static final int SIEMENS_KEY_RIGHT = -62;
    public static final int SIEMENS_KEY_LEFT_SOFT = -1;
    public static final int SIEMENS_KEY_RIGHT_SOFT = -4;
    public static final int SE_KEY_BACK = -11;
    public static final int KEY_SOFT_RIGHT = -7;
    
    public GenericMenu() {
        super(false);
    }
    
    public void paint(Graphics g) {
        if (isInited) {
            for (int i = firstDrawable; i < options.length; i++) {
                g.setFont(font);
                g.setColor(normalColor);
                int offset = 0;

                if (i == selected) { // highlighting selected option
                    offset = Mathh.sin(tick * 360 / 10); //waving
                    g.setColor(selectedColor);
                    if (isPressedByPointerNow) {
                        g.setColor(pressedColor);
                        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, font.getSize()));
                    }

                }

                if (isStatemapEnabled) { // coloring other options depending on theirs state (if we have this info)
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
                g.drawString(options[i], x, y, Graphics.HCENTER | Graphics.TOP); // draw option on (x, y) //

                if (DebugMenu.isDebugEnabled && DebugMenu.showFontSize) {
                    g.drawString(String.valueOf(font.getSize()), x0, y0, 0); // display text size (for debug)
                }
            }
            
            if (!isKnownButton) {
                g.setColor(127, 127, 127);
                g.drawString("Unknown keyCode=" + lastKeyCode, Main.sWidth, Main.sHeight, Graphics.BOTTOM | Graphics.RIGHT);
            }
        } else {
            g.setColor(128, 128, 128);
            g.drawString("Loading the menu...", Main.sWidth / 2, Main.sHeight, Graphics.BOTTOM | Graphics.HCENTER);
        }
        Logger.paint(g);
    }
    
    public int findOptimalFont(int canvW, int canvH, String[] options) {
        font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
        
        // height
        if (font.getHeight() * options.length - firstDrawable >= canvH - canvH/16) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        }
        if (font.getHeight() * options.length - firstDrawable >= canvH - canvH/16) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }
        
        // width
        if (font.getSize() != Font.SIZE_SMALL) {
            for (int i = firstDrawable; i < options.length - 1; i++) {
                if (font.stringWidth((String) options[i]) >= canvW - canvW/16) {
                    font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
                    if (font.stringWidth((String) options[i]) >= canvW - canvW/16) {
                        font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
                        break;
                    }
                }
            }
        }
        return font.getHeight();
    }
    
    private boolean isOptionAvailable(int n) {
        if (isStatemapEnabled) {
            if (n >= stateMap.length) {
                return false;
            }
            if (stateMap[n] == -1) {
                return false;
            }
        }
        
        if (n < firstReachable || n > lastReachable) {
            return false;
        }
        
        return true;
    }
    
    public boolean isMenuInited() {
        return isInited;
    }
    
    protected void pointerPressed(int x, int y) {
        handlePointer(x, y);
    }

    protected void pointerDragged(int x, int y) {
        handlePointer(x, y);
    }

    protected void pointerReleased(int x, int y) {
        if (handlePointer(x, y)) {
            selectPressed();
        }
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
        return true;
    }
    
    private boolean handleKeyStates(int keyStates) {
        if (keyStates != 0) {
            //Main.log("states", keyStates);
            if (keyStates == GameCanvas.RIGHT || keyStates == GameCanvas.FIRE ||
                    keyStates == GameCanvas.UP || keyStates == GameCanvas.DOWN) {
                isKnownButton = true;
            }
        }
        isPaused = false;
        isSelectAlreadyPressed = isSelectPressed;
        
        if (keyPressDelay < 1) {
            keyPressDelay = KEY_PRESS_DELAY;
            isSelectPressed = (keyStates == GameCanvas.RIGHT || keyStates == GameCanvas.FIRE);
            boolean needRepeat = true;
            while (needRepeat) {
                needRepeat = false;
                if (keyStates == GameCanvas.UP) {
                    isKnownButton = true;
                    isPaused = false;
                    if (selected > firstReachable) {
                        selected--;
                    } else {
                        selected = lastReachable;
                    }
                    //Main.log("up");
                } else if (keyStates == GameCanvas.DOWN) {
                    //Main.log("down");
                    isKnownButton = true;
                    isPaused = false;
                    if (selected < lastReachable) {
                        selected++;
                    } else {
                        selected = firstReachable;
                    }
                }
                if (isStatemapEnabled && !isSelectPressed) {
                    needRepeat = stateMap[selected] == STATE_INACTIVE;
                }
            }
        }
        return isSelectPressed && !isSelectAlreadyPressed;
    }
    
    public void keyPressed(int keyCode) {
        if(handleKeyPressed(keyCode)) {
            selectPressed();
        }
    }
    
    public boolean handleKeyPressed(int keyCode) {
        lastKeyCode = keyCode;
        isKnownButton = false;
        isPaused = false;
        Logger.log("pressed:", keyCode);
        boolean pressed = false;
        int selected = -1;
        switch (keyCode) {
            case GameCanvas.KEY_NUM1:
                selected = 0;
                pressed = true;
                break;
            case GameCanvas.KEY_NUM2:
                selected = 1;
                pressed = true;
                break;
            case GameCanvas.KEY_NUM3:
                selected = 2;
                pressed = true;
                break;
            case GameCanvas.KEY_NUM4:
                selected = 3;
                pressed = true;
                break;
            case GameCanvas.KEY_NUM5:
                selected = 4;
                pressed = true;
                break;
            case GameCanvas.KEY_NUM6:
                selected = 5;
                pressed = true;
                break;
            case GameCanvas.KEY_NUM7:
                selected = 6;
                pressed = true;
                break;
            case GameCanvas.KEY_NUM8:
                selected = 7;
                pressed = true;
                break;
            case GameCanvas.KEY_NUM9:
                selected = 8;
                pressed = true;
                break;
            case GameCanvas.KEY_STAR:
                selected = 9;
                pressed = true;
                break;
            case GameCanvas.KEY_NUM0:
                break;
            case KEY_SOFT_RIGHT:
                break;
            case SE_KEY_BACK:
                break;
            case SIEMENS_KEY_LEFT:
                break;
            case GameCanvas.KEY_POUND:
                isKnownButton = true;
                if (keyPressDelay < 1) {
                    keyPressDelay = KEY_PRESS_DELAY;
                    return true;
                } else {
                    return false;
                }
            case SIEMENS_KEY_UP:
                isKnownButton = true;
                handleKeyStates(GameCanvas.UP);
                break;
            case SIEMENS_KEY_DOWN:
                isKnownButton = true;
                handleKeyStates(GameCanvas.DOWN);
                break;
            case SIEMENS_KEY_RIGHT:
                isKnownButton = true;
                if (keyPressDelay < 1) {
                    keyPressDelay = KEY_PRESS_DELAY;
                    return true;
                } else {
                    return false;
                }
            case SIEMENS_KEY_FIRE:
            case -6: // left soft button
                handleKeyStates(GameCanvas.FIRE_PRESSED);
            default:
                return handleKeyStates(getGameAction(keyCode));
        }
        selected += firstReachable;
        if (keyCode == GameCanvas.KEY_NUM0 || keyCode == KEY_SOFT_RIGHT || keyCode == SIEMENS_KEY_LEFT || keyCode == SE_KEY_BACK) {
            isKnownButton = true;
            selected = lastReachable; // back
            if (keyPressDelay < 1) {
                pressed = true;
                keyPressDelay = KEY_PRESS_DELAY;
            }
        }
        
        if (pressed) {
            isKnownButton = true;
            if (isOptionAvailable(selected)) {
                this.selected = selected;
                return true;
            }
        }
        return false;
    }
    
    public void keyReleased(int keyCode) {
        keyPressDelay = 0;
        isSelectPressed = false;
        isSelectAlreadyPressed = false;
    }
    
    private void loadCanvasParams(int x0, int y0, int w, int h, int fontSize) {
        this.x0 = x0;
        this.y0 = y0;
        this.w = w;
        this.h = h;
        if (options != null) {
            k = (h + h / (options.length + 1 - firstDrawable)) / (options.length + 1 - firstDrawable);
            if (fontSize == -1) {
                fontH = findOptimalFont(w, h, options);
            } else {
                Logger.log("got fontSize:", fontSize);
                font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSize);
                fontH = font.getHeight();
            }
            fontFound = true;
        }
    }
    public void reloadCanvasParameters(int scW, int scH) {
        reloadCanvasParameters(x0, y0, scW, scH);
    }
    public void reloadCanvasParameters(int x0, int y0, int w, int h) {
        int fontSize = -1;
        if (w - x0 == this.w - this.x0 && h - y0 == this.h - this.y0 && font != null) {
            fontSize = font.getSize();
        }
        loadCanvasParams(x0, y0, w, h, fontSize);
    }
    
    /**
     * Should be placed to showNotify.
     * <p>handleHideNotify() in its right place is also needed.
     * <p>
     * It prevents siemens' bug that calls hideNotify right after
     * calling showNotify.
     */
    public void showNotify() {
        Logger.log("menu:showNotify");
        sizeChanged(getWidth(), getHeight());
        
        isPaused = false;
        pauseDelay = PAUSE_DELAY;
    }
    
    public void hideNotify() {
        Logger.log("menu:hideNotify");
        // It prevents a bug on siemens that calls hideNotify right after calling showNotify.
        if (pauseDelay <= 0) {
            isPaused = true;
        }
    }
    
    protected void sizeChanged(int w, int h) {
        Main.sWidth = w;
        Main.sHeight = h;
        reloadCanvasParameters(w, h);
    }
    
    
    
    public void loadParams(int w, int h, String[] options, int[] statemap, int fontSize) {
        loadParams(0, 0, w, h, options, 0, options.length - 1, options.length - 1, statemap, fontSize);
    }
    public void loadParams(int w, int h, String[] options, int firstReachable, int lastReachable, int defaultSelected, int fontSize) {
        loadParams(0, 0, w, h, options, firstReachable, lastReachable, defaultSelected, fontSize);
    }
    public void loadParams(int w, int h, Vector options, int firstReachable, int lastReachable, int defaultSelected, int fontSize) {
        String[] optsArray = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            optsArray[i] = (String) options.elementAt(i);
        }
        loadParams(0, 0, w, h, optsArray, firstReachable, lastReachable, defaultSelected, fontSize);
    }
    public void loadParams(int x0, int y0, int w, int h, String[] options, int firstReachable, int lastReachable, int defaultSelected, int fontSize) {
        loadParams(x0, y0, w, h, options, firstReachable, lastReachable, defaultSelected, null, fontSize);
    }
    public void loadParams(int x0, int y0, int w, int h, String[] options, int firstReachable, int lastReachable, int defaultSelected, int[] optionStateMap, int fontSize) {
        this.options = options;
        this.firstReachable = firstReachable;
        this.lastReachable = lastReachable;
        loadCanvasParams(x0, y0, w, h, fontSize);
        if (firstload) {
            selected = defaultSelected;
            firstload = false;
        }
        if (optionStateMap != null) {
            loadStatemap(optionStateMap);
        }
        isInited = true;
    }
    public void loadStatemap(int[] stateMap) {
        isStatemapEnabled = false;
        if (stateMap != null) {
            if (stateMap.length == options.length) {
                this.stateMap = stateMap;
                isStatemapEnabled = true;
                Logger.log("stateMap loaded");
            } else {
                Main.showAlert("GenericMenu.loadStatemap:optionTypeMap.length must be == options.length");
            }
        } else {
            Main.showAlert("GenericMenu.loadStatemap:null stateMap");
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
