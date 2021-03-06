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
    
    private int x0, y0, w, h, fontH, tick = 0, k, delay = 5, delayAfterShowing = 5, firstReachable, lastReachable, specialOption = -1;
    int selected;
    private int normalColor = 0x00ffffff, selectedColor = 0x00ff4040, pressedColor = 0x00E03838, specialOptionActivatedColor = 0x00ffff00, colUnreachable = 0x00888888, colReachableEnabled = 0x00ccff00;
    String[] options;
    private boolean pressed, firstload = true, isSpecialOptionActivated = false, isSelectPressed = false, isSelectAlreadyPressed = false, isStatemapEnabled = false, dontLoadStateMap = false, fontFound = false;
    private Font font;
    private int[] stateMap = null;
    public static final int OPTIONTYPE_UNREACHABLE = -1;
    public static final int OPTIONTYPE_NORMAL = 0;
    public static final int OPTIONTYPE_REACHABLE_ENABLED = 1;
    
    public static final int SIEMENS_KEYCODE_FIRE = -28;
    public static final int SIEMENS_KEYCODE_UP = -59;
    public static final int SIEMENS_KEYCODE_DOWN = -60;
    public static final int SIEMENS_KEYCODE_LEFT = -61;
    public static final int SIEMENS_KEYCODE_RIGHT = -62;
    public static final int SIEMENS_KEYCODE_LEFT_SOFT = -1;
    public static final int SIEMENS_KEYCODE_RIGHT_SOFT = -4;
    
    public void paint(Graphics g) {
        
        for (int i = 0; i < options.length; i++) {
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
            int y = y0 + k * (i + 1) - fontH / 2 - h / (options.length + 1) / 2 + offset*Font.getDefaultFont().getHeight() / 8000;
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
        if (font.getHeight() * options.length >= h - h/16) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        }
        if (font.getHeight() * options.length >= h - h/16) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }
        
        // width
        if (font.getSize() != Font.SIZE_SMALL) {
            for (int i = 0; i < options.length - 1; i++) {
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
        int selected = y / k;
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
                delay = 0;
                isSelectAlreadyPressed = false;
            }
        }
        if (delay < 1) {
            isSelectPressed = ((keyStates & (GameCanvas.RIGHT_PRESSED | GameCanvas.FIRE_PRESSED)) != 0);
            delay = 5;
            boolean needRepeat = true;
            while (needRepeat) {
                needRepeat = false;
                if ((keyStates & GameCanvas.UP_PRESSED) != 0) {
                    if (selected > firstReachable) {
                        selected--;
                    } else {
                        selected = lastReachable;
                    }
                } else if ((keyStates & GameCanvas.DOWN_PRESSED) != 0) {
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
            if (delay > 0)
                delay--;
        }
        if (keyStates == 0) {
            delay = 0;
            isSelectPressed = false;
            isSelectAlreadyPressed = false;
        }
        return isSelectPressed & !isSelectAlreadyPressed;
    }
    
    public boolean handleKeyPressed(int keyCode) {
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
                return true;
            case SIEMENS_KEYCODE_UP:
                handleKeyStates(GameCanvas.UP_PRESSED);
                break;
            case SIEMENS_KEYCODE_DOWN:
                handleKeyStates(GameCanvas.DOWN_PRESSED);
                break;
            case SIEMENS_KEYCODE_RIGHT:
                return true;
            case SIEMENS_KEYCODE_FIRE:
                return true;
            case -6: // left soft button
                return true;
            default:
                break;
        }
        selected += firstReachable;
        if (keyCode == GameCanvas.KEY_NUM0 | keyCode == -7/*right soft button*/) {
            selected = lastReachable; // back
            pressed = true;
        }
        
        if (pressed) {
            if (isOptionAvailable(selected)) {
                this.selected = selected;
                return true;
            }
        }
        return false;
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
        k = (h + h / (options.length + 1)) / (options.length + 1);
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
    }
}
