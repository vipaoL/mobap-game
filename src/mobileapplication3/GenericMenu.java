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
    
    int x0, y0, w, h, selected, fontH, tick = 0, k, delay = 5, firstReachable, lastReachable, specialOption = -1;
    private int defaultColor = 0x00ffffff, selectedColor = 0x00ff4040, pressedColor = 0x00E03838, specialOptionActivatedColor = 0x00ffff00;
    String[] options;
    boolean pressed, firstload = true, isSpecialOptionActivated = false;
    Font font;
    
    public GenericMenu() {
        
    }
    public void loadParams(int w, int h, Vector options, int firstReachable, int lastReachable, int defaultSelected) {
        String[] optsArray = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            optsArray[i] = (String) options.elementAt(i);
        }
        loadParams(0, 0, w, h, optsArray, firstReachable, lastReachable, defaultSelected);
    }
    public void loadParams(int w, int h, String[] options, int firstReachable, int lastReachable, int defaultSelected) {
        loadParams(0, 0, w, h, options, firstReachable, lastReachable, defaultSelected);
    }
    public void loadParams(int x0, int y0, int w, int h, String[] options, int firstReachable, int lastReachable, int defaultSelected) {
        this.x0 = x0;
        this.y0 = y0;
        this.w = w;
        this.h = h;
        this.options = options;
        k = (h + h / (options.length + 1)) / (options.length + 1);
        this.firstReachable = firstReachable;
        this.lastReachable = lastReachable;
        findOptimalFont();
        if (firstload) {
            selected = defaultSelected;
            firstload = false;
        }
    }
    public void setDefaultColor(int color_hex) {
        defaultColor = color_hex;
    }
    public void setSelectedColor(int color_hex) {
        selectedColor = color_hex;
    }
    public void setPressedColor(int color_hex) {
        pressedColor = color_hex;
    }
    public void setIsPressedNow(boolean isPressed) {
        pressed = isPressed;
    }
    public void setSpecialOptnActColor(int colorActivated) {
        specialOptionActivatedColor = colorActivated;
    }
    public void setSpecialOption(int n) {
        specialOption = n;
    }
    public void tick() {
        if (tick > 9) {
            tick = 0;
        } else {
            tick++;
        }
    }
    public void paint(Graphics g) {
        for (int i = 0; i < options.length; i++) {
            int offset = 0;
            if (i == selected) {
                if (pressed) {
                    g.setColor(pressedColor);
                    g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, font.getSize()));
                } else {
                    g.setColor(selectedColor);
                }
                offset = Mathh.sin(tick * 360 / 10);
            } else {
                g.setFont(font);
                g.setColor(defaultColor);
            }
            if (i == specialOption & isSpecialOptionActivated) {
                g.setColor(specialOptionActivatedColor);
            }
            g.drawString(options[i], x0 + w / 2, y0 + k * (i + 1) - fontH / 2 - h / (options.length + 1) / 2 + offset*Font.getDefaultFont().getHeight() / 8000, Graphics.HCENTER | Graphics.TOP);
        }
    }
    
    public void findOptimalFont() {
        font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
        
        // height
        if (font.getHeight() * options.length > h) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        }
        if (font.getHeight() * options.length > h) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }
        
        // width
        if (font.getSize() != Font.SIZE_SMALL) {
            for (int i = 1; i < options.length - 1; i++) {
                if (font.stringWidth((String) options[i]) >= w) {
                    font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
                    if (font.stringWidth((String) options[i]) >= w) {
                        font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
                        break;
                    }
                }
            }
        }
        fontH = font.getHeight();
    }
    
    public boolean pointer(int x, int y) {
        selected = y / k;
        if (selected < firstReachable) {
            selected = firstReachable;
            return false;
        }
        if (selected > lastReachable) {
            selected = lastReachable;
            return false;
        }
        return true;
    }
    
    public boolean keyPressed(int key) {
        if (delay < 1) {
            delay = 5;
            if ((key & GameCanvas.UP_PRESSED) != 0) {
                if (selected > firstReachable) {
                    selected--;
                } else {
                    selected = lastReachable;
                }
            } else if ((key & GameCanvas.DOWN_PRESSED) != 0) {
                if (selected < lastReachable) {
                    selected++;
                } else {
                    selected = firstReachable;
                }
            }
        } else {
            delay--;
        }
        if (key == 0) {
            delay = 0;
        }
        return (key & (GameCanvas.RIGHT_PRESSED | GameCanvas.FIRE_PRESSED)) != 0;
    }
}
