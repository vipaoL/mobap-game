/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor.elements;

import mobileapplication3.platform.ui.Graphics;

/**
 *
 * @author vipaol
 */
public class BrokenCircle extends Circle {
    
    public void placePoint(int i, short x, short y) {
        
    }

    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY) {
        g.drawString("not implemented", offsetX, offsetY, Graphics.HCENTER | Graphics.BOTTOM);
        g.drawString("yet", offsetX, offsetY, Graphics.HCENTER | Graphics.TOP);
    }
    
    public Element setArgs(short[] args) {
        return this;
    }

    public short[] getArgsValues() {
        return null;
    }
    
//    public String[] getArgsNames() {
//		TODO return ARGS_NAMES;
//	}
    
    public short getID() {
        return Element.BROKEN_CIRCLE;
    }

    public int getStepsToPlace() {
        return 2;
    }
    
    public String getName() {
        return "Broken Circle";
    }

    public short[] getEndPoint() {
        return super.getEndPoint(); //To change body of generated methods, choose Tools | Templates.
    }
    
    public boolean isBody() {
		return true;
	}
    
}
