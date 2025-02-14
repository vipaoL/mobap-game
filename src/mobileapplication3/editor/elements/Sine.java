/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor.elements;

import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.Property;

/**
 *
 * @author vipaol
 */
public class Sine extends AbstractCurve {
	
	//	      #
	//	    #
	//	.  #	"." - (x0;y0)
	//	  #
	//	@		"@" - (anchorX;anchorY)
    
	private final static int STEP = 30;
    private short x0, y0, l, halfperiods = 1, offset = 270, amp;
    private short anchorX, anchorY;
    
    public PlacementStep[] getPlacementSteps() {
        return new PlacementStep[] {
            new PlacementStep() {
                public void place(short pointX, short pointY) {
                    setAnchorPoint(pointX, pointY);
                }

                public String getName() {
                    return "Move start point";
                }
                
                public String getCurrentStepInfo() {
					return "x0=" + x0 + " y0=" + y0;
				}
            },
            new PlacementStep() {
                public void place(short pointX, short pointY) {
                    setLength((short) (pointX - x0));
                    int sinoffset = Mathh.sin(-offset);
                    int amp = (anchorY - pointY);
                    if (sinoffset != 0) {
                    	amp = amp * 1000 / sinoffset;
                    }
                    setAmplitude((short) (amp/2));
                    calcZeroPoint();
                }

                public String getName() {
                    return "Change length and amplitude";
                }
                
                public String getCurrentStepInfo() {
					return "l=" + l;
				}
            },
            new PlacementStep() {
                public void place(short pointX, short pointY) {
                	int dx = pointX - anchorX;
                	if (dx * l > 0) {
                		setHalfperiodsNumber((short) Math.max(1, l/dx));
                	}
                }

                public String getName() {
                    return "Change number of halfperiods";
                }
                
                public String getCurrentStepInfo() {
					return "halfperiods=" + halfperiods;
				}
            }
        };
    }

    public PlacementStep[] getExtraEditingSteps() {
        return new PlacementStep[] {
        		new PlacementStep() {
                    public void place(short pointX, short pointY) {
                    	int dx = pointX - anchorX;
                    	setOffset((short) ((dx)*halfperiods*180/l + 90));
                    }

                    public String getName() {
                        return "Change phase shift";
                    }
                    
                    public String getCurrentStepInfo() {
    					return "offset=" + offset;
    				}
                }
        };
    }
    
    public void setAnchorPoint(short x, short y) {
        if (anchorX == x && anchorY == y) {
            return;
        }
        pointsCache = null;
        anchorX = x;
        anchorY = y;
        calcZeroPoint();
    }
    
    public void calcZeroPoint() {
        setZeroPoint(anchorX, (short) (anchorY - amp*Mathh.sin(-offset)/1000));
    }
    
    public void calcAnchorPoint() {
    	setAnchorPoint(x0, (short) (y0 + amp*Mathh.sin(-offset)/1000));
    }
    
    public void setZeroPoint(short x, short y) {
        if (x0 == x && y0 == y) {
            return;
        }
        pointsCache = null;
        x0 = x;
        y0 = y;
        calcAnchorPoint();
    }
    
    public void setLength(short l) {
        if (this.l == l) {
            return;
        }
        pointsCache = null;
        this.l = l;
    }
    
    public void setHalfperiodsNumber(short n) {
        if (halfperiods == n) {
            return;
        }
        pointsCache = null;
        halfperiods = n;
    }

    public void setOffset(short offset) throws IllegalArgumentException {
        if (this.offset == offset) {
            return;
        }
        pointsCache = null;
        offset = (short) Mathh.normalizeAngle(offset);
        this.offset = offset;
        calcZeroPoint();
    }
    
    public void setAmplitude(short a) {
        if (amp == a) {
            return;
        }
        pointsCache = null;
        amp = a;
    }

    public Element setArgs(short[] args) {
        setZeroPoint(args[0], args[1]);
        setLength(args[2]);
        setHalfperiodsNumber(args[3]);
        setOffset((short) -args[4]);
        setAmplitude(args[5]);
        pointsCache = null;
        recalcCalculatedArgs();
        return this;
    }
    
    public short[] getArgsValues() {
        return new short[]{x0, y0, l, halfperiods, (short) Mathh.normalizeAngle(-offset), amp};
    }
    
    public Property[] getArgs() {
    	return new Property[] {
    			new Property("X0") {
					public void setValue(short value) {
						if (x0 != value) {
							pointsCache = null;
						}
						x0 = value;
						calcAnchorPoint();
					}

					public short getValue() {
						return x0;
					}
    			},
    			new Property("Y0") {
					public void setValue(short value) {
						if (y0 != value) {
							pointsCache = null;
						}
						y0 = value;
					}

					public short getValue() {
						return y0;
					}
    			},
    			new Property("Length") {
					public void setValue(short value) {
						setLength(value);
					}

					public short getValue() {
						return l;
					}
					
					public short getMinValue() {
						return (short) -x0;
					}
					
					public short getMaxValue() {
						return (short) (Short.MAX_VALUE - x0);
					}
    			},
    			new Property("Halfperiods") {
					public void setValue(short value) {
						setHalfperiodsNumber(value);
					}

					public short getValue() {
						return halfperiods;
					}
					
					public short getMinValue() {
						return 1;
					}
					
					public short getMaxValue() {
						return (short) (l / 64);
					}
    			},
    			new Property("Phase shift") {
					public void setValue(short value) {
						setOffset(value);
					}

					public short getValue() {
						return offset;
					}
					
					public short getMaxValue() {
						return 360;
					}
					
					public short getMinValue() {
						return 0;
					}
    			},
    			new Property("Amplitude") {
					public void setValue(short value) {
						setAmplitude(value);
					}

					public short getValue() {
						return amp;
					}
    			}
    	};
    }
    
    public short getID() {
        return Element.SINE;
    }

    public int getStepsToPlace() {
        return 3;
    }

    public String getName() {
        return "Sine";
    }
    
    public void move(short dx, short dy) {
    	anchorX += dx;
    	anchorY += dy;
    	x0 += dx;
    	y0 += dy;
    	
    	if (pointsCache != null) {
    		pointsCache.movePoints(dx, dy);
    	}
    }
    
    private short[][] getEnds() {
    	return new short[][] {
	    		new short[]{anchorX, anchorY},
	    		new short[]{(short) (x0 + l), (short) (y0 + amp*Mathh.sin(180*halfperiods - offset)/1000)}
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
    
    protected void genPoints() {
        if (amp == 0) {
            pointsCache = new PointsCache(2);
            pointsCache.writePointToCache(x0, y0);
            pointsCache.writePointToCache(x0 + l, y0);
        } else {
            int startA = 360 - offset;
            int endA = 360 + halfperiods * 180 - offset;
            int a = endA - startA;

            int nextPointX;
            int nextPointY;
            pointsCache = new PointsCache(1 + halfperiods*6);
            for (int i = startA; i <= endA; i+=STEP) {
                nextPointX = x0 + (i - startA)*l/a;
                nextPointY = y0 + amp*Mathh.sin(i)/1000;
                pointsCache.writePointToCache(nextPointX, nextPointY);
            }
            
            if (a % STEP != 0) {
                nextPointX = x0 + l;
                nextPointY = y0 + amp*Mathh.sin(endA)/1000;
                pointsCache.writePointToCache(nextPointX, nextPointY);
            }
        }
    }
    
    public void recalcCalculatedArgs() {
    	calcAnchorPoint();
    }
    
    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        if (pointsCache == null) {
            genPoints();
        }
        
        if (pointsCache.getSize() == 0) {
        	return;
        }

        g.setColor(getSuitableColor(drawAsSelected));

        short[] startPoint = pointsCache.getPoint(0);
        for (int i = 0; i < pointsCache.getSize() - 1; i++) {
            short[] endPoint = pointsCache.getPoint(i+1);
            int x1 = xToPX(startPoint[0], zoomOut, offsetX);
            int y1 = yToPX(startPoint[1], zoomOut, offsetY);
            int x2 = xToPX(endPoint[0], zoomOut, offsetX);
            int y2 = yToPX(endPoint[1], zoomOut, offsetY);
            g.drawLine(xToPX(startPoint[0], zoomOut, offsetX), yToPX(startPoint[1], zoomOut, offsetY), xToPX(endPoint[0], zoomOut, offsetX), yToPX(endPoint[1], zoomOut, offsetY), LINE_THICKNESS, zoomOut, drawThickness, true, true, true);
            if (i % 2 == 0) {
	            int dx = x2 - x1;
	            int dy = y2 - y1;
	            int l = Mathh.calcDistance(dx, dy);
	            int centerX = (x1 + x2) / 2;
	            int centerY = (y1 + y2) / 2;
	            int lzoomout = l * zoomOut;
	            g.drawArrow(centerX, centerY, centerX + dy * 50000 / lzoomout, centerY - dx * 50000 / lzoomout, LINE_THICKNESS/6, zoomOut, drawThickness);
            }
            startPoint = endPoint;
        }
    }
    
}
