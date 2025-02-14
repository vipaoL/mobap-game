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
public class Line extends Element {
    
    protected short x1, y1, x2, y2;
    
    public PlacementStep[] getPlacementSteps() {
        return new PlacementStep[] {
            new PlacementStep() {
                public void place(short pointX, short pointY) {
                	int dx = pointX - x1;
                	int dy = pointY - y1;
                    setStartPoint(pointX, pointY);
                    setEndPoint((short) (x2 + dx), (short) (y2 + dy));
                }

                public String getName() {
                    return "Move";
                }
                
                public String getCurrentStepInfo() {
					return "x1=" + x1 + " y1=" + y1;
				}
            },
            new PlacementStep() {
                public void place(short pointX, short pointY) {
                    setEndPoint(pointX, pointY);
                }

                public String getName() {
                    return "Move end point";
                }
                
                public String getCurrentStepInfo() {
					return "x1=" + x1 + " y1=" + y1 + "; x2=" + x2 + " y2=" + y2;
				}
            }
        };
    }

    public PlacementStep[] getExtraEditingSteps() {
        return new PlacementStep[] {
        		new PlacementStep() {
                    public void place(short pointX, short pointY) {
                        setStartPoint(pointX, pointY);
                    }

                    public String getName() {
                        return "Move start point";
                    }
                    
                    public String getCurrentStepInfo() {
    					return "x1=" + x1 + " y1=" + y1;
    				}
                }
        };
    }
    
    public void setStartPoint(short x, short y) {
        x1 = x;
        y1 = y;
    }
    
    public void setEndPoint(short x, short y) {
        x2 = x;
        y2 = y;
    }
    
    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
    	g.setColor(getSuitableColor(drawAsSelected));
        g.drawLine(
        		xToPX(x1, zoomOut, offsetX),
        		yToPX(y1, zoomOut, offsetY),
        		xToPX(x2, zoomOut, offsetX),
        		yToPX(y2, zoomOut, offsetY),
        		LINE_THICKNESS,
        		zoomOut, drawThickness, true, true, true
        		);
    }
    
    public Element setArgs(short[] args) {
        x1 = args[0];
        y1 = args[1];
        x2 = args[2];
        y2 = args[3];
        return this;
    }

    public short[] getArgsValues() {
        short[] args = {x1, y1, x2, y2};
        return args;
    }
    
    public Property[] getArgs() {
    	return new Property[] {
    			new Property("X1") {
					public void setValue(short value) {
						x1 = value;
					}

					public short getValue() {
						return x1;
					}
    			},
    			new Property("Y1") {
					public void setValue(short value) {
						y1 = value;
					}

					public short getValue() {
						return y1;
					}
    			},
    			new Property("X2") {
					public void setValue(short value) {
						x2 = value;
					}

					public short getValue() {
						return x2;
					}
    			},
    			new Property("Y2") {
					public void setValue(short value) {
						y2 = value;
					}

					public short getValue() {
						return y2;
					}
    			}
    	};
    }
    
    public short getID() {
        return Element.LINE;
    }
    
    public int getStepsToPlace() {
        return 2;
    }

    public String getName() {
        return "Line";
    }
    
    public void move(short dx, short dy) {
    	x1 += dx;
    	y1 += dy;
    	x2 += dx;
    	y2 += dy;
    }
    
    private short[][] getEnds() {
    	return new short[][] {
    			new short[]{x1, y1},
    			new short[]{x2, y2}
		};
    }
    
    public short[] getStartPoint() {
    	short[][] ends = getEnds();
        return StartPoint.compareAsStartPoints(ends[0], ends[1]);
    }

    public short[] getEndPoint() {
    	short[][] ends = getEnds();
    	return EndPoint.compareAsEndPoints(ends[0], ends[1]);
    }

	public boolean isBody() {
		return false;
	}
	
	public void recalcCalculatedArgs() { }
    
}
