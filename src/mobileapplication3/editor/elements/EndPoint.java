/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor.elements;

import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.Property;

/**
 *
 * @author vipaol
 */
public class EndPoint extends Element {
    
    private short x, y;
    
    public PlacementStep[] getPlacementSteps() {
        return new PlacementStep[] {
            new PlacementStep() {
                public void place(short pointX, short pointY) {
                    x = pointX;
                    y = pointY;
                }

                public String getName() {
                    return "Move";
                }
                
                public String getCurrentStepInfo() {
					return "x=" + x + " y=" + y;
				}
            }
        };
    }

    public PlacementStep[] getExtraEditingSteps() {
        return new PlacementStep[0];
    }
    
    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        int r = 3;
        int prevColor = g.getColor();
        g.setColor(0xff0000);
        g.fillArc(xToPX(x, zoomOut, offsetX) - r, yToPX(y, zoomOut, offsetY) - r, r*2, r*2, 0, 360);
        g.setColor(prevColor);
    }

    public Element setArgs(short[] args) {
        x = args[0];
        y = args[1];
        return this;
    }
    
    public short[] getArgsValues() {
        short[] args = {x, y};
        return args;
    }
    
    public Property[] getArgs() {
    	return new Property[] {
    			new Property("X") {
					public void setValue(short value) {
						x = value;
					}

					public short getValue() {
						return x;
					}
    			},
    			new Property("Y") {
					public void setValue(short value) {
						y = value;
					}

					public short getValue() {
						return y;
					}
    			}
    	};
    }

    public short getID() {
        return Element.END_POINT;
    }

    public int getStepsToPlace() {
        return 1;
    }
    
    public String getName() {
        return "End point";
    }
    
    public void move(short dx, short dy) {
    	x += dx;
    	y += dy;
    }
    
    public short[] getStartPoint() {
    	return new short[] {x, y};
    }
    
    public short[] getEndPoint() throws Exception {
        throw new Exception("Never ask end point its end point");
    }
    
    public static boolean compare(short[] oldEndPoint, short[] newEndPoint) {
        short oldX = oldEndPoint[0];
        short oldY = oldEndPoint[1];
        short newX = newEndPoint[0];
        short newY = newEndPoint[1];
        if (newX >= oldX) {
            if (newX > oldX || (newY > oldY)) {
                return true;
            }
        }
        return false;
    }
    
    public static short[] compareAsEndPoints(short[] a, short[] b) {
		if (compare(a, b)) {
			return b;
		} else {
			return a;
		}
	}
    
    public static short[] findEndPoint(Element[] elements) {
    	short[] endPoint = {0, 0};
        short[] mayBeEndPoint = endPoint;
        for (int i = 1; i < elements.length; i++) {
            try {
                mayBeEndPoint = elements[i].getEndPoint();
                if (EndPoint.compare(endPoint, mayBeEndPoint)) {
                    endPoint = mayBeEndPoint;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return endPoint;
    }

	public boolean isBody() {
		return false;
	}

	public void recalcCalculatedArgs() { }
    
}
