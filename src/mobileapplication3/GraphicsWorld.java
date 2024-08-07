/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.*;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import utils.Logger;
import utils.Mathh;
import utils.MobappGameSettings;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author vipaol
 */
public class GraphicsWorld extends World {
	
	private static final int BIGSCREEN_SIDE = 480;

    int colBg = 0x000000;
    int colLandscape = 0x4444ff;
    int colBodies = 0xffffff;
    int colFunctionalObjects = 0xff5500;
    int currColBg;
    int currColWheel;
    int currColLandscape = colLandscape;
    int currColBodies;
    
    private int scWidth = Main.sWidth;
    private int halfScWidth = scWidth/2;
    private int scHeight = Main.sHeight;
    private int halfScHeight = scHeight/2;
    private int scMinSide = Math.min(scWidth, scHeight);
    
    private boolean betterGraphics;
    private boolean bg;
    public static boolean bgOverride = false;
    private int bgLineStep = scMinSide / 3;
    private int bgLineThickness = Math.max(Main.sWidth, Main.sHeight)/250;
    
    int zoomBase = 0;
    int zoomOut = 100;
    int offsetX = 0;
    int offsetY = 0;
    public static int viewField;
    
    public static int carX = 0;
    public static int carY = 0;
    public Body carbody;
    public Body leftwheel;
    public Body rightwheel;
    Random random = new Random();
    
    // list of all bodies car touched (for falling platforms)
    Vector waitingForDynamic = new Vector();
    Vector waitingTime = new Vector();
    long prevBodyTickTime = System.currentTimeMillis();
    
    public GraphicsWorld(World w) {
        super(w);
        
        betterGraphics = MobappGameSettings.isBetterGraphicsEnabled(Math.max(Main.sWidth, Main.sHeight) >= BIGSCREEN_SIDE);
    	bg = bgOverride || MobappGameSettings.isBGEnabled(false);
    	if (bg) {
    		colBg = 0x150031;
    	}
        
        if (DebugMenu.whatTheGame) {
            currColWheel = 0x888888;
            colBg = 0x001155;
            colBodies = 0x555555;
        }
        currColWheel = colBg;
        currColBg = colBg;
        currColBodies = colBodies;
    }

    public void addCar() {
        int x = 0;
        if (WorldGen.isEnabled) {
            x = -3000;
        }
        addCar(x, -400, FXUtil.TWO_PI_2FX / 360 * 30);
    }

    public void addCar(int spawnX, int spawnY, int ang2FX) {
        carX = spawnX;
        carY = spawnY;
        int carbodyLength = 240;
        int carbodyHeight = 40;
        int wheelRadius = 40;
        Shape carbodyShape;
        Shape wheelShape;

        carbodyShape = Shape.createRectangle(carbodyLength, carbodyHeight);
        carbodyShape.setMass(1);
        carbodyShape.setFriction(0);
        carbodyShape.setElasticity(0);
        carbodyShape.correctCentroid();
        carbody = new Body(spawnX, spawnY, carbodyShape, true);
        carbody.setRotation2FX(ang2FX);

        long longAng2FX = ang2FX;
        int ang = (int) (longAng2FX * 360 / FXUtil.TWO_PI_2FX) + 1;

        wheelShape = Shape.createCircle(wheelRadius);
        wheelShape.setElasticity(100);
        wheelShape.setFriction(0);
        wheelShape.setMass(2);
        wheelShape.correctCentroid();
        int lwX = spawnX - (carbodyLength / 2 - wheelRadius - 2) * Mathh.cos(ang) / 1000;
        int lwY = spawnY + wheelRadius / 2 - (carbodyLength / 2 - wheelRadius) * Mathh.sin(ang) / 1000;
        int rwX = spawnX + (carbodyLength / 2 - wheelRadius + 2) * Mathh.cos(ang) / 1000;
        int rwY = spawnY + wheelRadius / 2 + (carbodyLength / 2 - wheelRadius) * Mathh.sin(ang) / 1000;
        leftwheel = new Body(lwX, lwY, wheelShape, true);
        rightwheel = new Body(rwX, rwY, wheelShape, true);

        removeBody(carbody);
        removeBody(leftwheel);
        removeBody(rightwheel);

        addBody(carbody);
        carbody.addCollisionLayer(1);
        addBody(leftwheel);
        leftwheel.addCollisionLayer(1);
        addBody(rightwheel);
        rightwheel.addCollisionLayer(1);

        Joint leftjoint = new Joint(carbody, leftwheel, FXVector.newVector(-carbodyLength / 2 + wheelRadius - 2, wheelRadius * 2 / 3), FXVector.newVector(0, 0), false);
        Joint rightjoint = new Joint(carbody, rightwheel, FXVector.newVector(carbodyLength / 2 - wheelRadius + 2, wheelRadius * 2 / 3), FXVector.newVector(0, 0), false);
        addConstraint(leftjoint);
        addConstraint(rightjoint);
        
        WorldGen.bgZeroPoint = spawnX;
    }
    
    public void tickBodies() {
        int diffTime = (int) (System.currentTimeMillis() - prevBodyTickTime);
        // ticking timers on each body car touched and set it as dynamic
        // for falling platforms
        for (int i = 0; i < waitingForDynamic.size(); i++) {
            try {
                waitingTime.setElementAt(new Integer(((Integer) waitingTime.elementAt(i)).intValue() - diffTime), i);
                if (Integer.parseInt(String.valueOf(waitingTime.elementAt(i))) <= 0) {
                    ((Body) waitingForDynamic.elementAt(i)).setDynamic(true);
                    waitingForDynamic.removeElementAt(i);
                    waitingTime.removeElementAt(i);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {

            }
        }
        // removing all that fell out the world or got too left
        for (int i = 0; i < getBodyCount(); i++) {
            if (GraphicsWorld.viewField < 100) {
                break;
            }
            Body[] bodies = getBodies();
            Body body = bodies[i];
            if (body.positionFX().xAsInt() < WorldGen.barrierX || body.positionFX().yAsInt() > 20000) {
                if (body != carbody && body != leftwheel && body != rightwheel) {
                    removeBody(body);
                }
            }
        }
        prevBodyTickTime = System.currentTimeMillis();
    }
    
    public void setWheelColor(int color) {
        currColWheel = color;
    }
    
    public void drawWorld(Graphics g) {
        // fill background
        g.setColor(currColBg);
        g.fillRect(0, 0, scWidth, scHeight);
        try {
            carX = carbody.positionFX().xAsInt();
            carY = carbody.positionFX().yAsInt();

            // zooming and moving virtual camera
            calcZoomOut();
            calcOffset();

            drawBg(g);
            drawLandscape(g);
            drawBodies(g); // draw all bodies, excluding car wheels
            drawCar(g); // draw car wheels
            drawConstraints(g); // disabled
        } catch (NullPointerException ex) {
            int l = scWidth * 2 / 3;
            int h = scHeight / 24;
            g.drawRect(scWidth / 2 - l / 2, scHeight * 2 / 3, l, h);
            g.fillRect(scWidth / 2 - l / 2, scHeight * 2 / 3, l/5, h);
            ex.printStackTrace();
        }
        //g.fillTriangle(xToPX(carX+viewField/2-10), 0, xToPX(carX+viewField/2), scHeight, xToPX(carX+viewField/2+10), 0);
    }
    
    private void drawBg(Graphics g) {
    	// some very boring code
        if (GameplayCanvas.points > 291 && GameplayCanvas.points < 293) {
            currColBg = 0x2f92ff;
            currColLandscape = 0xffffff;
        } else if (GameplayCanvas.points > 293 && GameplayCanvas.points < 300) {
            currColBg = colBg;
            currColLandscape = colLandscape;
        }
        
    	if (bg) {
    		int sunR = Math.min(scWidth, scHeight) / 4;
            int sunCenterY = scHeight - scHeight * 3 / 5;
            
            g.setColor(191, 0, 127);
            int offset = (carX - WorldGen.bgZeroPoint) / 16;
            int l = (scWidth * 4);
            int y1 = sunCenterY + sunR;
            int y2 = scHeight;
            int ii = 0;
            int n = l/bgLineStep;
            // horizontal lines
            for (int i = 0; i < n; i++) {
                int x2 = -(ii + (offset) % bgLineStep - l/2)/*  *64/8  */;
                ii += bgLineStep;
                int x1 = x2 / 4;
                int thickness = bgLineThickness;
                if (Math.abs(i*8 - n*4) > n) {
                	thickness -= 1;
                }
                drawLine(g, x1 + halfScWidth, y1, x2 + halfScWidth, y2, thickness, false);
            }
            // vertical lines
            n = scHeight*2/bgLineStep;
            for (int i = 0; i < n; i++) {
            	int y = y1 + (y2 - y1) * i * i / n / n;
            	drawLine(g, 0, y, scWidth, y, 1, false);
            }
            g.setColor(255, 170, 0);
            
            // sun
            int lines = 6;
            g.fillArc(halfScWidth - sunR, sunCenterY - sunR, sunR * 2, sunR * 2, 0, 360);
            g.setColor(currColBg);
            for (int i = 0; i < lines; i++) {
                int y = i * sunR / lines + sunCenterY - sunR / 12;
                drawLine(g, 0, y, scWidth, y, bgLineThickness*2*(i+1)/lines, false);
            }
        }
    }

    private void drawBodies(Graphics g) {
        Body[] bodies = getBodies();
        int bodyCount = getBodyCount();
        for (int i = 0; i < bodyCount; i++) {
            if (bodies[i] != leftwheel & bodies[i] != rightwheel) {
                // default value, will be overwritten if it is an other type of body
                int bodyType = MUserData.TYPE_FALLING_PLATFORM;
                MUserData bodyUserData = null;
                try {
                    bodyUserData = (MUserData) bodies[i].getUserData();
                    if (bodyUserData != null) {
                        bodyType = bodyUserData.bodyType;
                    }
                } catch (ClassCastException ex) {

                } catch (NullPointerException ex) {
                    
                }
                
                g.setColor(currColBodies);
                if (bodyType == MUserData.TYPE_ACCELERATOR) {
                    g.setColor(bodyUserData.color);
                }
                try {
                    drawBody(g, bodies[i]);
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                    Logger.log("can't draw" + bodyType);
                }
            }
        }
    }

    private void drawBody(Graphics g, Body b) {
        FXVector[] positions = b.getVertices();
        
        if (positions.length == 1) { // if shape of the body is circle
            int radius = FXUtil.fromFX(b.shape().getBoundingRadiusFX());
            drawArc(g,
            		xToPX(b.positionFX().xAsInt() - radius),
            		yToPX(b.positionFX().yAsInt() - radius),
            		radius * 2000 / zoomOut,
            		radius * 2000 / zoomOut,
            		0, 360, 10, currColBg);
        }
        else { // if not a circle, then a polygon
        	if (b == carbody) {
        		int prevColor = g.getColor();
        		g.setColor(currColBg);
        		if (DebugMenu.whatTheGame) {
        			g.setColor(0xff0000);
        		}
        		
                g.fillTriangle(xToPX(positions[0].xAsInt()),
                        yToPX(positions[0].yAsInt()),
                        xToPX(positions[1].xAsInt()),
                        yToPX(positions[1].yAsInt()),
                        xToPX(positions[2].xAsInt()),
                        yToPX(positions[2].yAsInt()));
                g.fillTriangle(xToPX(positions[0].xAsInt()),
                        yToPX(positions[0].yAsInt()),
                        xToPX(positions[3].xAsInt()),
                        yToPX(positions[3].yAsInt()),
                        xToPX(positions[2].xAsInt()),
                        yToPX(positions[2].yAsInt()));
                g.setColor(prevColor);
            }
        	
            for (int i = 0; i < positions.length - 1; i++) {
                drawLine(g,
                        xToPX(positions[i].xAsInt()),
                        yToPX(positions[i].yAsInt()),
                        xToPX(positions[i + 1].xAsInt()),
                        yToPX(positions[i + 1].yAsInt()),
                        10);
                if (b != carbody) {
	                g.fillTriangle(
	                		xToPX(positions[0].xAsInt()),
	                        yToPX(positions[0].yAsInt()),
	                        xToPX(positions[i].xAsInt()),
	                        yToPX(positions[i].yAsInt()),
	                        xToPX(positions[i + 1].xAsInt()),
	                        yToPX(positions[i + 1].yAsInt()));
                }
            }
            drawLine(g,
                    xToPX(positions[positions.length - 1].xAsInt()),
                    yToPX(positions[positions.length - 1].yAsInt()),
                    xToPX(positions[0].xAsInt()),
                    yToPX(positions[0].yAsInt()),
                    10);
        }
    }

    private void drawCar(Graphics g) {
        drawWheel(g, leftwheel);
        drawWheel(g, rightwheel);
    }

    private void drawLandscape(Graphics g) {
        Landscape landscape = getLandscape();
        for (int i = 0; i < landscape.segmentCount(); i++) {
            int stPointX = xToPX(landscape.startPoint(i).xAsInt());
            int stPointY = yToPX(landscape.startPoint(i).yAsInt());
            int endPointX = xToPX(landscape.endPoint(i).xAsInt());
            int endPointY = yToPX(landscape.endPoint(i).yAsInt());
            if (stPointX < scWidth | endPointX > 0) {
                if (!DebugMenu.isDebugEnabled) {
                    g.setColor(currColLandscape);
                } else {
                    g.setColor(255, 255, 255);
                }
                if (DebugMenu.whatTheGame) {
                    drawGroundLine(
                            g,
                            stPointX,
                            stPointY,
                            endPointX,
                            endPointY,
                            24);
                } else {
                    drawLine(
                            g,
                            stPointX,
                            stPointY,
                            endPointX,
                            endPointY,
                            24);
                }
                g.setColor(0xff0000);
                if (DebugMenu.showLinePoints) {
                    g.fillArc(stPointX-1, stPointY-1, 2, 2, 0, 360);
                    g.fillArc(endPointX-1, endPointY-1, 2, 2, 0, 360);
                }
            }
        }
    }

    private void drawConstraints(Graphics g) {
        
        // disable drawing constraints
        if (true) return;
        
        int constraintCount = getConstraintCount();
        Constraint[] constraints = getConstraints();
        for (int i = 0; i < constraintCount; i++) {
            if (constraints[i] instanceof Spring) {
                Spring spring = (Spring) constraints[i];
                g.drawLine(xToPX(spring.getPoint1().xAsInt()),
                        yToPX(spring.getPoint1().yAsInt()),
                        xToPX(spring.getPoint2().xAsInt()),
                        yToPX(spring.getPoint2().yAsInt()));
            }
        }
    }

    private void drawWheel(Graphics g, Body b) {
        int radius = FXUtil.fromFX(b.shape().getBoundingRadiusFX());
        if (GameplayCanvas.currentEffects[GameplayCanvas.EFFECT_SPEED] == null) {
            currColWheel = currColBg;
            if (DebugMenu.discoMode) {
                currColWheel = random.nextInt(16777216);
                currColBodies = random.nextInt(16777216);
            }
        }
        
        g.setColor(currColWheel);
        g.fillArc(
                xToPX(b.positionFX().xAsInt() - radius),
                yToPX(b.positionFX().yAsInt() - radius),
                radius * 2000 / zoomOut,
                radius * 2000 / zoomOut,
                0, 360);
        
        g.setColor(currColBodies);
        drawArc(g,
                xToPX(b.positionFX().xAsInt() - radius),
                yToPX(b.positionFX().yAsInt() - radius),
                radius * 2000 / zoomOut,
                radius * 2000 / zoomOut,
                0, 360, 10, currColWheel);
    }
    
    private void drawLine(Graphics g, int x1, int y1, int x2, int y2, int thickness) {
    	drawLine(g, x1, y1, x2, y2, thickness, true);
    }
    
    private void drawLine(Graphics g, int x1, int y1, int x2, int y2, int thickness, boolean zoomThickness) {
        if (DebugMenu.discoMode) {
            g.setColor(random.nextInt(16777216));
        }
        if (thickness > 1 && betterGraphics) {
            int t2 = thickness/2;
            int dx = x2 - x1;
            int dy = y2 - y1;
            int l = (int) Math.sqrt(dx*dx+dy*dy);
            
            if (l == 0) {
                g.drawLine(x1, y1, x2, y2);
                return;
            }
            
            // normal vector
            int nx = dy*t2 * 1000 / (zoomThickness ? zoomOut : 1000) / l;
            int ny = dx*t2 * 1000 / (zoomThickness ? zoomOut : 1000) / l;
            
            if (nx == 0 && ny == 0) {
                g.drawLine(x1, y1, x2, y2);
                return;
            }
            
            // draw bold line with two triangles (splitting by diagonal)
            g.fillTriangle(x1-nx, y1+ny, x2-nx, y2+ny, x1+nx, y1-ny);
            g.fillTriangle(x2-nx, y2+ny, x2+nx, y2-ny, x1+nx, y1-ny);
            int r = t2;
            if (zoomThickness) {
            	r = r * 1000 / zoomOut;
            }
            int d = r * 2;
            g.fillArc(x1-r, y1-r, d, d, 0, 360);
            g.fillArc(x2-r, y2-r, d, d, 0, 360);
        } else {
            g.drawLine(x1, y1, x2, y2);
        }
    }
    
    private void drawGroundLine(Graphics g, int x1, int y1, int x2, int y2, int thickness) {
        g.setColor(0x333300);
        if (DebugMenu.discoMode) {
            g.setColor(random.nextInt(16777216));
        }
        int y3 = Math.max(y1, y2);
        int x3 = x1;
        if (y3 == y1) {
            x3 = x2;
        }
        g.fillTriangle(x1, y1, x2, y2, x3, y3);
        g.fillRect(x1, y3, x2 - x1, scHeight - y3);
        
        g.setColor(0x00ff00);
        
        if (thickness > 1 && betterGraphics) {
            int t2 = thickness/2;
            int dx = x2 - x1;
            int dy = y2 - y1;
            int l = (int) Math.sqrt(dx*dx+dy*dy);
            
            if (l == 0) {
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
            int r = t2 * 1000 / zoomOut;
            int d = r * 2;
            g.fillArc(x1-r, y1-r, d, d, 0, 360);
            g.fillArc(x2-r, y2-r, d, d, 0, 360);
        } else {
            g.drawLine(x1, y1, x2, y2);
        }
    }
    
    private void drawArc(Graphics g, int x, int y, int w, int h, int startAngle, int arcAngle, int thickness, int fillColor) {
    	int prevColor = g.getColor();
    	thickness = thickness * 500 / zoomOut * 2;
    	
    	if (thickness > 1 && betterGraphics) {
	    	g.fillArc(x - thickness / 2, y - thickness / 2, w + thickness, h + thickness, startAngle, arcAngle);
	    	g.setColor(fillColor);
	    	g.fillArc(x + thickness / 2, y + thickness / 2, w - thickness, h - thickness, startAngle, arcAngle);
	    	g.setColor(prevColor);
    	} else {
    		g.drawArc(x, y, w, h, startAngle, arcAngle);
    	}
    }


    private int xToPX(int c) {
        return c * 1000 / zoomOut + offsetX;
    }

    private int yToPX(int c) {
        return c * 1000 / zoomOut + offsetY;
    }

    public void refreshPos() {
        if (carbody != null) {
            FXVector posFX = carbody.positionFX();
            carX = posFX.xAsInt();
            carY = posFX.yAsInt();
        } else {
            carX = -8000;
            carY = 0;
        }
    }

    public void refreshScreenParameters() {
        Logger.log("world:refreshing screen params:");
        Logger.log(Main.sWidth + " " + Main.sHeight);
        scWidth = Main.sWidth;
        halfScWidth = scWidth / 2;
        scHeight = Main.sHeight;
        halfScHeight = scHeight / 2;
        scMinSide = Math.min(scWidth, scHeight);
        zoomBase = 6000 * 240 / scMinSide;
        calcZoomOut();
    }

    private void calcZoomOut() {
        if (DebugMenu.simulationMode) {
            zoomOut = 50000;
        } else {
            zoomOut = (1000 * carY / scMinSide - 1000);
            int zoomBase = this.zoomBase;
            if (GameplayCanvas.currentEffects[GameplayCanvas.EFFECT_SPEED] != null) {
                if (GameplayCanvas.currentEffects[GameplayCanvas.EFFECT_SPEED][0] > 0) {
                    zoomOut = zoomOut * GameplayCanvas.currentEffects[GameplayCanvas.EFFECT_SPEED][2] / 100;
                    zoomBase = zoomBase * GameplayCanvas.currentEffects[GameplayCanvas.EFFECT_SPEED][2] / 100;
                }
            }
            if (zoomOut < 1) {
                zoomOut = -zoomOut;
                zoomOut += 1;
            }
            zoomOut += zoomBase;
        }
        
        // for timely track generation and deleting waste objects
        viewField = scWidth * zoomOut / 1000;
        if (DebugMenu.isDebugEnabled && DebugMenu.closerWorldgen || DebugMenu.simulationMode) {
            viewField /= 4;
        }
    }
    
    private void calcOffset() {
    	offsetX = -carX * 1000 / zoomOut + scWidth / 3;
        offsetY = -carY * 1000 / zoomOut + scHeight * 2 / 3;
        offsetY += carY / 20;
        offsetY = Mathh.constrain(-carY * 1000 / zoomOut + scHeight/16, offsetY, -carY * 1000 / zoomOut + scHeight*4/5);
    }
}
