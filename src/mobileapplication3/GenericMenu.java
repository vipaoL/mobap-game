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
public class GenericMenu {
    private static final int PAUSE_DELAY = 5;
    private static final int KEY_PRESS_DELAY = 5;
    
    private int x0, y0, w, h, fontH, tick = 0, k, keyPressDelay = KEY_PRESS_DELAY,
            delayAfterShowing = 5, firstReachable, lastReachable,
            firstDrawable = 0, specialOption = -1, pauseDelay = PAUSE_DELAY;
    public int currKeyStates = 0, currKeyCode = 0;
    
    int selected;
    
    // colors
    private int normalColor = 0x00ffffff, selectedColor = 0x00ff4040,
            pressedColor = 0x00E03838, specialOptionActivatedColor = 0x00ffff00,
            colUnreachable = 0x00888888, colReachableEnabled = 0x00ccff00;
    String[] options;
    
    private boolean pressed, firstload = true,
            isSpecialOptionActivated = false, isSelectPressed = false,
            isSelectAlreadyPressed = false, isStatemapEnabled = false,
            dontLoadStateMap = false, fontFound = false;
    
    public boolean isKnownButton = true;
    private Font font;
    private int[] stateMap = null;
    public static final int OPTIONTYPE_UNREACHABLE = -1;
    public static final int OPTIONTYPE_NORMAL = 0;
    public static final int OPTIONTYPE_REACHABLE_ENABLED = 1;
    
    
    // key codes
    public static final int SIEMENS_KEY_FIRE = -26;
    public static final int SIEMENS_KEY_UP = -59;
    public static final int SIEMENS_KEY_DOWN = -60;
    public static final int SIEMENS_KEY_LEFT = -61;
    public static final int SIEMENS_KEY_RIGHT = -62;
    public static final int SIEMENS_KEY_LEFT_SOFT = -1;
    public static final int SIEMENS_KEY_RIGHT_SOFT = -4;
    
    Feedback feedback;
    
    public GenericMenu(Feedback feedback) {
        this.feedback = feedback;
    }
    
    public void paint(Graphics g) {
        for (int i = firstDrawable; i < options.length; i++) {
            g.setFont(font);
            g.setColor(normalColor);
            int offset = 0;
            
            if (i == selected) { // highlighting selected option
                offset = Mathh.sin(tick * 360 / 10); //waving
                g.setColor(selectedColor);
                if (pressed) {
                    g.setColor(pressedColor);
                    g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, font.getSize()));
                }
                
            }
            
            if (isStatemapEnabled) { // coloring other options depending on theirs state (if we have this info)
                if (stateMap[i] == OPTIONTYPE_REACHABLE_ENABLED) {
                    g.setColor(colReachableEnabled);
                } else if (stateMap[i] == OPTIONTYPE_UNREACHABLE) {
                    g.setColor(colUnreachable);
                }
            }
            
            
            if (i == specialOption & isSpecialOptionActivated) { // painting special option in a different color
                g.setColor(specialOptionActivatedColor);
            }
            
            int x = x0 + w / 2;
            int y = y0 + k * (i + 1 - firstDrawable) - fontH / 2 - h / (options.length + 1 - firstDrawable) / 2 + offset*Font.getDefaultFont().getHeight() / 8000;
            g.drawString(options[i], x, y, Graphics.HCENTER | Graphics.TOP); // draw option on (x, y) //
            
            if (DebugMenu.isDebugEnabled & DebugMenu.fontSize) {
                g.drawString(String.valueOf(font.getSize()), x0, y0, 0); // display text size (for debug)
            }
            if (Main.isScreenLogEnabled) {
                g.setColor(150, 255, 150);
                Font font = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL);
                g.setFont(font);
                for (int j = 0; j < Main.onScreenLog.length; j++) {
                    try {
                        g.drawString(Main.onScreenLog[j], 0, font.getHeight() * j, Graphics.TOP | Graphics.LEFT);
                    } catch (NullPointerException ex) {

                    } catch (IllegalArgumentException ex) {

                    }
                }
            }
        }
    }
    
    public void findOptimalFont() {
        font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
        
        // height
        if (font.getHeight() * options.length - firstDrawable >= h - h/16) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        }
        if (font.getHeight() * options.length - firstDrawable >= h - h/16) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }
        
        // width
        if (font.getSize() != Font.SIZE_SMALL) {
            for (int i = firstDrawable; i < options.length - 1; i++) {
                if (font.stringWidth((String) options[i]) >= w - w/16) {
                    font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
                    if (font.stringWidth((String) options[i]) >= w - w/16) {
                        font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
                        break;
                    }
                }
            }
        }
        fontH = font.getHeight();
        fontFound = true;
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
        if (n < firstReachable | n > lastReachable) {
            return false;
        }
        return true;
    }
    
    public boolean handlePointer(int x, int y) {
        feedback.setIsPaused(false);
        int selected = firstDrawable + y / k;
        if (!isOptionAvailable(selected)) {
            pressed = false;
            return false;
        }
        this.selected = selected;
        pressed = true;
        return true;
    }
    
    boolean inited = false;
    public boolean handleKeyStates(int keyStates) {
        if (keyStates != 0) {
            //Main.log("states", keyStates);
        }
        isSelectAlreadyPressed = isSelectPressed;
        if (delayAfterShowing > 0) {
            delayAfterShowing--;
            inited = false;
            return false;
        } else {
            if (!inited) {
                inited = true;
                keyPressDelay = 0;
                isSelectAlreadyPressed = false;
            }
        }
        
        if (keyPressDelay < 1) {
            isSelectPressed = ((keyStates & (GameCanvas.RIGHT_PRESSED | GameCanvas.FIRE_PRESSED)) != 0);
            keyPressDelay = KEY_PRESS_DELAY;
            boolean needRepeat = true;
            while (needRepeat) {
                needRepeat = false;
                if ((keyStates & GameCanvas.UP_PRESSED) != 0) {
                    isKnownButton = true;
                    feedback.setIsPaused(false);
                    if (selected > firstReachable) {
                        selected--;
                    } else {
                        selected = lastReachable;
                    }
                } else if ((keyStates & GameCanvas.DOWN_PRESSED) != 0) {
                    isKnownButton = true;
                    feedback.setIsPaused(false);
                    if (selected < lastReachable) {
                        selected++;
                    } else {
                        selected = firstReachable;
                    }
                }
                if (isStatemapEnabled & !isSelectPressed) {
                    needRepeat = stateMap[selected] == OPTIONTYPE_UNREACHABLE;
                }
            }
        } else {
            keyPressDelay--;
        }
        if (keyStates == 0) {
            keyPressDelay = 0;
            isSelectPressed = false;
            isSelectAlreadyPressed = false;
        } else {
            currKeyStates = keyStates;
        }
        return isSelectPressed & !isSelectAlreadyPressed;
    }
    
    public boolean handleKeyPressed(int keyCode) {
        if (feedback.getIsPaused()) {
            feedback.recheckInput();
        }
        feedback.setIsPaused(false);
        Main.log("pressed", keyCode);
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
                handleKeyStates(GameCanvas.UP_PRESSED);
                break;
            case SIEMENS_KEY_DOWN:
                isKnownButton = true;
                handleKeyStates(GameCanvas.DOWN_PRESSED);
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
                isKnownButton = true;
                if (keyPressDelay < 1) {
                    keyPressDelay = KEY_PRESS_DELAY;
                    return true;
                } else {
                    return false;
                }
            case -6: // left soft button
                isKnownButton = true;
                if (keyPressDelay < 1) {
                    keyPressDelay = KEY_PRESS_DELAY;
                    return true;
                } else {
                    return false;
                }
            default:
                isKnownButton = false;
                break;
        }
        selected += firstReachable;
        if (keyCode == GameCanvas.KEY_NUM0 | keyCode == /**/ -7/*right soft button*/ | keyCode == SIEMENS_KEY_LEFT /*| keyCode == GameCanvas.LEFT_PRESSED*/) {
            isKnownButton = true;
            selected = lastReachable; // back
            if (keyPressDelay < 1) {
                pressed = true;
                keyPressDelay = KEY_PRESS_DELAY;
            }
        }
        
        if (!isKnownButton & !pressed) {
            currKeyCode = keyCode;
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
    
    /**
     * Should be placed to showNotify.
     * <p>handleHideNotify() in its right place is also needed.
     * <p>
     * It prevents siemens' bug that calls hideNotify right after
     * calling showNotify.
     */
    public void handleShowNotify() {
        pauseDelay = PAUSE_DELAY;
    }
    
    /**
     * Should be places in the END of hideNotify() (after "isPaused = true").
     * <p>handleShowNotify() in its right place is also needed.
     * <p>
     * It prevents siemens' bug that calls hideNotify right after
     * calling showNotify.
     */
    public void handleHideNotify() {
        if (pauseDelay > 0) {
            feedback.setIsPaused(false);
        }
    }
    
    
    
    
    
    
    
    public void loadParams(int w, int h, String[] options) {
        loadParams(0, 0, w, h, options, 0, options.length - 1, options.length - 1);
    }
    public void loadParams(int w, int h, String[] options, int[] statemap) {
        dontLoadStateMap = true;
        loadParams(0, 0, w, h, options, 0, options.length - 1, options.length - 1);
        loadStatemap(statemap);
    }
    public void loadParams(int w, int h, String[] options, int[] statemap, int fontSize) {
        dontLoadStateMap = true;
        loadParams(0, 0, w, h, options, 0, options.length - 1, options.length - 1, null, fontSize);
        loadStatemap(statemap);
    }
    public void loadParams(int w, int h, Vector options, int firstReachable, int lastReachable, int defaultSelected) {
        loadParams(0, 0, w, h, this.options, firstReachable, lastReachable, defaultSelected, -1);
    }
    public void loadParams(int w, int h, String[] options, int firstReachable, int lastReachable, int defaultSelected) {
        loadParams(0, 0, w, h, options, firstReachable, lastReachable, defaultSelected);
    }
    public void loadParams(int w, int h, String[] options, int firstReachable, int lastReachable, int defaultSelected, int fontSize) {
        loadParams(0, 0, w, h, options, firstReachable, lastReachable, defaultSelected);
    }
    public void loadParams(int w, int h, Vector options, int firstReachable, int lastReachable, int defaultSelected, int fontSize) {
        String[] optsArray = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            optsArray[i] = (String) options.elementAt(i);
        }
        loadParams(0, 0, w, h, optsArray, firstReachable, lastReachable, defaultSelected);
    }
    public void loadParams(int x0, int y0, int w, int h, String[] options, int firstReachable, int lastReachable, int defaultSelected) {
        loadParams(x0, y0, w, h, options, firstReachable, lastReachable, defaultSelected, null);
    }
    public void loadParams(int x0, int y0, int w, int h, String[] options, int firstReachable, int lastReachable, int defaultSelected, int fontSize) {
        loadParams(x0, y0, w, h, options, firstReachable, lastReachable, defaultSelected, null, fontSize);
    }
    public void loadParams(int x0, int y0, int w, int h, String[] options, int firstReachable, int lastReachable, int defaultSelected, int[] optionStateMap) {
        loadParams(x0, y0, w, h, options, firstReachable, lastReachable, defaultSelected, optionStateMap, -1);
    }
    public void loadParams(int x0, int y0, int w, int h, String[] options, int firstReachable, int lastReachable, int defaultSelected, int[] optionStateMap, int fontSize) {
        this.x0 = x0;
        this.y0 = y0;
        this.w = w;
        this.h = h;
        this.options = options;
        if (!dontLoadStateMap & optionStateMap != null) {
            loadStatemap(optionStateMap);
        }
        k = (h + h / (options.length + 1 - firstDrawable)) / (options.length + 1 - firstDrawable);
        this.firstReachable = firstReachable;
        this.lastReachable = lastReachable;
        if (fontSize == -1) {
            findOptimalFont();
        } else {
            Main.log(fontSize);
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSize);
            fontH = font.getHeight();
            fontFound = true;
        }
        if (firstload) {
            selected = defaultSelected;
            firstload = false;
        }
        dontLoadStateMap = false;
    }
    public void loadStatemap(int[] stateMap) {
        isStatemapEnabled = false;
        if (stateMap != null) {
            if (stateMap.length == options.length) {
                this.stateMap = stateMap;
                isStatemapEnabled = true;
                Main.log("stateMap loaded");
            } else {
                Main.log("GenericMenu.loadParams: optionTypeMap.length must be == options.length", Main.printCategory_err);
            }
        } else {
            Main.log("null stateMap", Main.printCategory_err);
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
    }
    
    interface Feedback {
        boolean getIsPaused();
        void setIsPaused(boolean isPaused);
        /*
        * if this method was called, then you need to re-check keyboard input
        * <p> e.g.: <p>
        * void recheckInput() { <p>
        *   input(); <p>
        * }
        */
        void recheckInput();
    }
}
