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
public class Circle extends AbstractCurve {
    
    private short x, y, r = 1, arcAngle = 360, startAngle, kx = 100, ky = 100;
    
    public PlacementStep[] getPlacementSteps() {
        return new PlacementStep[] {
            new PlacementStep() {
                public void place(short pointX, short pointY) {
                    setCenter(pointX, pointY);
                }

                public String getName() {
                    return "Move";
                }

				public String getCurrentStepInfo() {
					return "x=" + x + " y=" + y;
				}

            },
            new PlacementStep() {
                public void place(short pointX, short pointY) {
                    short dx = (short) (pointX - x);
                    short dy = (short) (pointY - y);
                    setRadius(calcDistance(dx, dy));
                }

                public String getName() {
                    return "Change radius";
                }
                
                public String getCurrentStepInfo() {
					return "r=" + r;
				}
            }
        };
    }

    public PlacementStep[] getExtraEditingSteps() {
    	final short centerX = x;
    	final short centerY = y;
    	final short startAngle = this.startAngle;
        return new PlacementStep[] {
            new PlacementStep() {
                public void place(short x, short y) {
                	short ang = (short) (Mathh.arctg(x - centerX, y - centerY) - startAngle);
                	ang %= 360;
                	while (ang < 1) {
                		ang += 360;
                	}
                	setArcAngle(ang);
                }

                public String getName() {
                    return "Change angle";
                }
                
                public String getCurrentStepInfo() {
					return "ang=" + arcAngle;
				}
            },
            new PlacementStep() {
                public void place(short x, short y) {
                	short ang = (short) Mathh.arctg(x - centerX, y - centerY);
                	setStartAngle(ang);
                }

                public String getName() {
                    return "Change start angle";
                }
                
                public String getCurrentStepInfo() {
					return "startAng=" + startAngle;
				}
            },
            new PlacementStep() {
                public void place(short pointX, short pointY) {
                    short dx = (short) (pointX - x);
                    short dy = (short) (pointY - y);
                    setScale((short) (Math.abs(dx) * 100 / r), (short) (Math.abs(dy) * 100 / r));
                }

                public String getName() {
                    return "Scale";
                }
                
                public String getCurrentStepInfo() {
					return "kx=" + kx + ", ky=" + ky;
				}
            }
        };
    }
    
    public Element setCenter(short x, short y) {
        if (this.x == x && this.y == y) {
            return this;
        }
        if (pointsCache != null) {
        	pointsCache.movePoints((short) (x - this.x), (short) (y - this.y));
        }
        this.x = x;
        this.y = y;
        return this;
    }
    
    public Element setRadius(short r) {
        r = (short) Math.max(Math.abs(r), 1);
        if (this.r == r) {
            return this;
        }
        this.r = r;
        pointsCache = null;
        return this;
    }
    
    public Element setArcAngle(short arcAngle) {
        if (this.arcAngle == arcAngle) {
            return this;
        }
        this.arcAngle = arcAngle;
        pointsCache = null;
        return this;
    }
    
    public Element setStartAngle(short startAngle) {
        while (startAngle < 0) {
            startAngle += 360;
        }
        startAngle%=360;

        if (this.startAngle == startAngle) {
            return this;
        }
        this.startAngle = startAngle;
        pointsCache = null;
        return this;
    }

    public Element setScale(short scaleX, short scaleY) {
        if (this.kx == scaleX && this.ky == scaleY) {
            return this;
        }
        this.kx = (short) Math.max(Math.abs(scaleX), 1);
        this.ky = (short) Math.max(Math.abs(scaleY), 1);
        pointsCache = null;
        return this;
    }

    public Element setArgs(short[] args) {
        x = args[0];
        y = args[1];
        r = args[2];
        arcAngle = args[3];
        startAngle = args[4];
        kx = args[5];
        ky = args[6];
        pointsCache = null;
        return this;
    }

    public short[] getArgsValues() {
        return new short[]{x, y, r, arcAngle, startAngle, kx, ky};
    }

    public Property[] getArgs() {
    	return new Property[] {
    			new Property("X") {
					public void setValue(short value) {
						if (x != value) {
							pointsCache = null;
						}
						x = value;
					}

					public short getValue() {
						return x;
					}
    			},
    			new Property("Y") {
					public void setValue(short value) {
						if (y != value) {
							pointsCache = null;
						}
						y = value;
					}

					public short getValue() {
						return y;
					}
    			},
    			new Property("R") {
					public void setValue(short value) {
						if (r != value) {
							pointsCache = null;
						}
						r = value;
					}

					public short getValue() {
						return r;
					}
					
					public short getMinValue() {
						return 1;
					}
    			},
    			new Property("Arc angle") {
					public void setValue(short value) {
						if (arcAngle != value) {
							pointsCache = null;
						}
						arcAngle = value;
					}

					public short getValue() {
						return arcAngle;
					}
					
					public short getMinValue() {
						return 0;
					}
					
					public short getMaxValue() {
						return 360;
					}
    			},
    			new Property("Start angle") {
					public void setValue(short value) {
						if (startAngle != value) {
							pointsCache = null;
						}
						startAngle = value;
					}

					public short getValue() {
						return startAngle;
					}
					
					public short getMinValue() {
						return 0;
					}
					
					public short getMaxValue() {
						return 360;
					}
    			},
    			new Property("X-axis scale") {
					public void setValue(short value) {
						if (kx != value) {
							pointsCache = null;
						}
						kx = value;
					}

					public short getValue() {
						return kx;
					}
					
					public short getMinValue() {
						return 1;
					}
					
					public short getMaxValue() {
						return 2048;
					}
    			},
    			new Property("Y-axis scale") {
					public void setValue(short value) {
						if (ky != value) {
							pointsCache = null;
						}
						ky = value;
					}

					public short getValue() {
						return ky;
					}
					
					public short getMinValue() {
						return 1;
					}
					
					public short getMaxValue() {
						return 2048;
					}
    			}
    	};
    }
    
    public short getID() {
        return Element.CIRCLE;
    }
    
    public int getStepsToPlace() {
        return 2;
    }

    public String getName() {
        return "Circle";
    }
    
    public void move(short dx, short dy) {
    	x += dx;
    	y += dy;
    	if (pointsCache != null) {
    		pointsCache.movePoints(dx, dy);
    	}
    }

    public short[] getStartPoint() {
    	if (Mathh.isPointOnArc(180, startAngle, arcAngle)) {
    		return new short[]{(short) (x - r), y};
    	} else {
    		return StartPoint.compareAsStartPoints(getPointOnCircleByAngle(startAngle), getPointOnCircleByAngle(startAngle + arcAngle));
    	}
    }
    
    public short[] getEndPoint() {
        if (Mathh.isPointOnArc(0, startAngle, arcAngle)) {
        	return new short[]{(short) (x + r * kx/100), y};
    	} else {
    		return EndPoint.compareAsEndPoints(getPointOnCircleByAngle(startAngle), getPointOnCircleByAngle(startAngle + arcAngle));
    	}
    }
    
    private short[] getPointOnCircleByAngle(int a) {
    	return new short[] {(short) (x+Mathh.cos(a)*kx*r/100000), (short) (y+Mathh.sin(a)*ky*r/100000)};
    }
    
    protected void genPoints() { //k: 100 = 1.0
        // calculated formula. r=20: sn=5,sl=72; r=1000: sn=36,sl=10
        int circleSegmentLen=10000/(140+r);
        circleSegmentLen = Math.min(72, Math.max(10, circleSegmentLen));
        int pointsNumber = arcAngle/circleSegmentLen + 1;
        if (arcAngle % circleSegmentLen != 0) {
            pointsNumber += 1;
        }
        pointsCache = new PointsCache(pointsNumber);
        
        int startAngle = this.startAngle;
        
        for(int i = 0; i <= arcAngle; i+=circleSegmentLen) {
            pointsCache.writePointToCache(getPointOnCircleByAngle(startAngle+i));
        }
        
        if (arcAngle % circleSegmentLen != 0) {
            pointsCache.writePointToCache(getPointOnCircleByAngle(startAngle+arcAngle));
        }
    }
    
    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
    	super.paint(g, zoomOut, offsetX, offsetY, drawThickness, drawAsSelected);
        int centerMarkR = 4;
        int leftX = xToPX(x - centerMarkR, zoomOut, offsetX);
        int rightX = xToPX(x + centerMarkR, zoomOut, offsetX);
        int topY = yToPX(y - centerMarkR, zoomOut, offsetY);
        int bottomY = yToPX(y + centerMarkR, zoomOut, offsetY);
        g.drawLine(leftX, topY, rightX, bottomY);
        g.drawLine(rightX, topY, leftX, bottomY);
    }
    
    public void recalcCalculatedArgs() { }
    
}
