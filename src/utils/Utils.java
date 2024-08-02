/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author vipaol
 */
public class Utils {
    
    public static String shortArrayToString(short [] arr) {
        try {
            if (arr == null) {
                return "null";
            }

            if (arr.length == 0) {
                return "[]";
            }

            StringBuffer sb = new StringBuffer(arr.length*6);
            sb.append("[");
            for (int i = 0; i < arr.length - 1; i++) {
                sb.append(arr[i]);
                sb.append(", ");
            }
            sb.append(arr[arr.length-1]);
            sb.append("]");
            return sb.toString();
        } catch(Exception ex) {
            ex.printStackTrace();
            return ex.toString();
        }
    }
    
    public static String[] split(String sb, String splitter){
        String[] strs = new String[sb.length()];
        int splitterLength = splitter.length();
        int initialIndex = 0;
        int indexOfSplitter = indexOf(sb, splitter, initialIndex);
        int count = 0;
        if(-1==indexOfSplitter) return new String[]{sb};
        while(-1!=indexOfSplitter){
            char[] chars = new char[indexOfSplitter-initialIndex];
            sb.getChars(initialIndex, indexOfSplitter, chars, 0);
            initialIndex = indexOfSplitter+splitterLength;
            indexOfSplitter = indexOf(sb, splitter, indexOfSplitter+1);
            strs[count] = new String(chars);
            count++;
        }
        // get the remaining chars.
        if(initialIndex+splitterLength<=sb.length()){
            char[] chars = new char[sb.length()-initialIndex];
            sb.getChars(initialIndex, sb.length(), chars, 0);
            strs[count] = new String(chars);
            count++;
        }
        String[] result = new String[count];
        for(int i = 0; i<count; i++){
            result[i] = strs[i];
        }
        return result;
    }

    public static int indexOf(String sb, String str, int start){
        int index = -1;
        if((start>=sb.length() || start<-1) || str.length()<=0) return index;
        char[] tofind = str.toCharArray();
        outer: for(;start<sb.length(); start++){
            char c = sb.charAt(start);
            if(c==tofind[0]){
                if(1==tofind.length) return start;
                inner: for(int i = 1; i<tofind.length;i++){ // start on the 2nd character
                    char find = tofind[i];
                    int currentSourceIndex = start+i;
                    if(currentSourceIndex<sb.length()){
                        char source = sb.charAt(start+i);
                        if(find==source){
                            if(i==tofind.length-1){
                                return start;
                            }
                            continue inner;
                        } else {
                            start++;
                            continue outer;
                        }
                    } else {
                        return -1;
                    }

                }
            }
        }
        return index;
    }

    public static String replace(String _text, String _searchStr, String _replacementStr) {
        // String buffer to store str
        StringBuffer sb = new StringBuffer();

        // Search for search
        int searchStringPos = _text.indexOf(_searchStr);
        int startPos = 0;
        int searchStringLength = _searchStr.length();

        // Iterate to add string
        while (searchStringPos != -1) {
            sb.append(_text.substring(startPos, searchStringPos)).append(_replacementStr);
            startPos = searchStringPos + searchStringLength;
            searchStringPos = _text.indexOf(_searchStr, startPos);
        }

        // Create string
        sb.append(_text.substring(startPos,_text.length()));

        return sb.toString();
    }
    
    public static void drawLine(Graphics g, int x1, int y1, int x2, int y2, int thickness, int zoomOut) {
        drawLine(g, x1, y1, x2, y2, thickness, zoomOut, true, true);
    }
    
    public static void drawLine(Graphics g, int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean rounding, boolean markSkeleton) {
        if (thickness > 2) {
            int t2 = thickness/2;
            int dx = x2 - x1;
            int dy = y2 - y1;
            int l = (int) Math.sqrt(dx*dx+dy*dy);
            
            if (l == 0 || zoomOut < 1000) {
                g.drawLine(x1, y1, x2, y2);
                return;
            }
            
            // normal vector
            int nx = dy*t2 * 1000 / zoomOut / l;
            int ny = dx*t2 * 1000 / zoomOut / l;
            
            if (nx == 0 && ny == 0) {
                g.drawLine(x1, y1, x2, y2);
                return;
            }
            
            // draw bold line with two triangles (splitting by diagonal)
            g.fillTriangle(x1-nx, y1+ny, x2-nx, y2+ny, x1+nx, y1-ny);
            g.fillTriangle(x2-nx, y2+ny, x2+nx, y2-ny, x1+nx, y1-ny);
            if (rounding) {
                int r = t2 * 1000 / zoomOut;
                int d = r * 2;
                g.fillArc(x1-r, y1-r, d, d, 0, 360);
                g.fillArc(x2-r, y2-r, d, d, 0, 360);
            }
            if (markSkeleton) {
                int prevCol = g.getColor();
                g.setColor(0xff0000);
                g.drawLine(x1, y1, x2, y2);
                g.setColor(prevCol);
            }
        } else {
            g.drawLine(x1, y1, x2, y2);
        }
    }
    
    public static int[][] getLineBounds(String text, Font font, int w, int padding) {
        Vector lineBoundsVector = new Vector(text.length() / 5);
        int charOffset = 0;
        if (font.stringWidth(text) <= w - padding * 2 && text.indexOf('\n') == -1) {
            lineBoundsVector.addElement(new int[]{0, text.length()});
        } else {
            while (charOffset < text.length()) {
                int maxSymsInCurrLine = 1;
                boolean maxLineLengthReached = false;
                boolean lineBreakSymFound = false;
                for (int lineLength = 1; lineLength <= text.length() - charOffset; lineLength++) {
                    if (font.substringWidth(text, charOffset, lineLength) > w - padding * 2) {
                        maxLineLengthReached = true;
                        break;
                    }
                    
                    maxSymsInCurrLine = lineLength;
                    
                    if (charOffset + lineLength < text.length()) {
                        if (text.charAt(charOffset+lineLength) == '\n') {
                            lineBoundsVector.addElement(new int[]{charOffset, lineLength});
                            charOffset = charOffset + lineLength + 1;
                            lineBreakSymFound = true;
                            break;
                        }
                    }
                }
                
                if (lineBreakSymFound) {
                    continue;
                }
                

                boolean spaceFound = false;

                int maxRightBorder = charOffset + maxSymsInCurrLine;
                
                if (maxRightBorder >= text.length()) {
                    lineBoundsVector.addElement(new int[]{charOffset, maxSymsInCurrLine});
                    break;
                }
                
                if (!maxLineLengthReached) {
                    lineBoundsVector.addElement(new int[]{charOffset, maxSymsInCurrLine}); //
                    charOffset = maxRightBorder;
                } else {
                    for (int i = maxRightBorder; i > charOffset; i--) {
                        if (text.charAt(i) == ' ') {
                            lineBoundsVector.addElement(new int[]{charOffset, i - charOffset});
                            charOffset = i + 1;
                            spaceFound = true;
                            break;
                        }
                    }

                    if (!spaceFound) {
                        lineBoundsVector.addElement(new int[]{charOffset, maxRightBorder - charOffset});
                        charOffset = maxRightBorder;
                    }
                }
            }
        }
        
        int[][] lineBounds = new int[lineBoundsVector.size()][];
        for (int i = 0; i < lineBoundsVector.size(); i++) {
            lineBounds[i] = (int[]) lineBoundsVector.elementAt(i);
        }
        return lineBounds;
    }

    static int count(String s, char c) {
        int ret = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                ret++;
            }
        }
        return ret;
    }

}
