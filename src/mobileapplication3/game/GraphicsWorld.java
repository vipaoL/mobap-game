/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.game;

import java.util.Random;
import java.util.Vector;

import at.emini.physics2D.Body;
import at.emini.physics2D.Joint;
import at.emini.physics2D.Landscape;
import at.emini.physics2D.Shape;
import at.emini.physics2D.UserData;
import at.emini.physics2D.World;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;
import utils.MobappGameSettings;

/**
 *
 * @author vipaol
 */
public class GraphicsWorld extends World {
	
	private static final int BIGSCREEN_SIDE = 480;
	private static final int CAR_COLLISION_LAYER = 1;

    public int colBg = 0x000000;
    public int colLandscape = 0x4444ff;
    int colBodies = 0xffffff;
    int colFunctionalObjects = 0xff5500;
    int currColBg;
    int currColWheel;
    int currColLandscape = colLandscape;
    int currColBodies;
    
    public static int scWidth = 200;
    private int halfScWidth = scWidth/2;
    public static int scHeight = 200;
    private int scMinSide = Math.min(scWidth, scHeight);

    public boolean removeBodies = true;
    private boolean betterGraphics;
    private boolean bg;
    public static boolean bgOverride = false;
    private int bgLineStep = scMinSide / 3;
    private int bgLineThickness;
    public int bgXOffset = 0;
    
    int zoomBase = 0;
    int zoomOut = 100;
    int offsetX = 0;
    int offsetY = 0;
    public int viewField;
    
    public int carX = 0;
    public int carY = 0;
    public Body carbody;
    public Body leftwheel;
    public Body rightwheel;
    private Joint leftjoint;
	private Joint rightjoint;
    Random random = new Random();
    
    // list of all bodies car touched (for falling platforms)
    Vector waitingForDynamic = new Vector();
    Vector waitingTime = new Vector();
    long prevBodyTickTime = System.currentTimeMillis();
    public int barrierX = Integer.MIN_VALUE;
    public int lowestY;

    public GraphicsWorld() {
        init();
    }

    public GraphicsWorld(World w) {
        super(w);
        init();
    }

    private void init() {
    	bg = bgOverride;
        
        try {
        	bg = bg || MobappGameSettings.isBGEnabled(false);
        } catch (Throwable ex) {
        	ex.printStackTrace();
        }
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

    public void removeBody(Body body) {
        if (body != carbody && body != leftwheel && body != rightwheel) {
            super.removeBody(body);
        } else {
            try {
                throw new IllegalArgumentException("Trying to remove a part of the car. Please report this bug");
            } catch (IllegalArgumentException ex) {
                Logger.log(ex.getMessage());
                ex.printStackTrace();
            }
        }
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
        carbodyShape.setElasticity(100);
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

        super.removeBody(carbody);
        super.removeBody(leftwheel);
        super.removeBody(rightwheel);

        addBody(carbody);
        carbody.addCollisionLayer(CAR_COLLISION_LAYER);
        addBody(leftwheel);
        leftwheel.addCollisionLayer(CAR_COLLISION_LAYER);
        addBody(rightwheel);
        rightwheel.addCollisionLayer(CAR_COLLISION_LAYER);

        leftjoint = new Joint(carbody, leftwheel, FXVector.newVector(-carbodyLength / 2 + wheelRadius - 2, wheelRadius * 2 / 3), FXVector.newVector(0, 0), false);
        rightjoint = new Joint(carbody, rightwheel, FXVector.newVector(carbodyLength / 2 - wheelRadius + 2, wheelRadius * 2 / 3), FXVector.newVector(0, 0), false);
        addConstraint(leftjoint);
        addConstraint(rightjoint);
        
        bgXOffset = spawnX;
    }
    
    public void destroyCar() {
    	removeConstraint(leftjoint);
    	removeConstraint(rightjoint);
    	leftwheel.removeCollisionLayer(CAR_COLLISION_LAYER);
    	carbody.removeCollisionLayer(CAR_COLLISION_LAYER);
    	rightwheel.removeCollisionLayer(CAR_COLLISION_LAYER);
    	int forceFX = -FXUtil.ONE_FX * 500;
    	leftwheel.applyMomentum(new FXVector(-forceFX, forceFX));
    	rightwheel.applyMomentum(new FXVector(forceFX, forceFX));
    	leftwheel.shape().setElasticity(100);
    	carbody.shape().setElasticity(100);
    	getLandscape().getShape().setElasticity(200);
    }
    
    public void tickCustomBodies() {
        int diffTime = (int) (System.currentTimeMillis() - prevBodyTickTime);
        // ticking timers on each body car touched and set it as dynamic
        // for falling platforms
        for (int i = 0; i < waitingForDynamic.size(); i++) {
            try {
                int intValue = ((Integer) waitingTime.elementAt(i)).intValue();
                intValue -= diffTime;
                // The constructor Integer(int) was deprecated in Java 9
                // Integer.valueOf() accepts only String in Java 1.3
                // So this is the only way?
                waitingTime.setElementAt(Integer.valueOf(String.valueOf(intValue)), i);
				if (intValue <= 0) {
                    ((Body) waitingForDynamic.elementAt(i)).setDynamic(true);
                    waitingForDynamic.removeElementAt(i);
                    waitingTime.removeElementAt(i);
                }
            } catch (ArrayIndexOutOfBoundsException ignored) { }
        }
        // removing all that fell out the world or got too left
        if (removeBodies) {
            for (int i = 0; i < getBodyCount(); i++) {
                if (viewField < 100) {
                    break; // Hack to not remove bodies until the correct screen size is set. Needs a proper fix
                }
                Body[] bodies = getBodies();
                Body body = bodies[i];
                if (body.positionFX().xAsInt() < barrierX || body.positionFX().yAsInt() > lowestY + 2000) {
                    if (body != carbody && body != leftwheel && body != rightwheel) {
                        removeBody(body);
                    }
                }
            }
        }
        prevBodyTickTime = System.currentTimeMillis();
    }
    
    public void setWheelColor(int color) {
        currColWheel = color;
    }
    
    public void drawWorld(Graphics g, int[][] structuresData, int structureRingBufferOffset, int structureCount) {
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
            if (structuresData != null) {
                try {
                    drawLandscape(g, structuresData, structureRingBufferOffset, structureCount);
                } catch (Exception ex) { }
            } else {
                drawLandscape(g);
            }
            drawBodies(g); // draw all bodies, excluding car wheels
            drawCar(g); // draw car wheels
            //drawConstraints(g); // don't draw constraints
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
        if (GameplayCanvas.points == 292) {
            currColBg = 0x2f92ff;
            currColLandscape = 0xffffff;
        } else if (GameplayCanvas.points == 293) {
            currColBg = colBg;
            currColLandscape = colLandscape;
        }
        
    	if (bg) {
    		int sunR = Math.min(scWidth, scHeight) / 4;
            int sunCenterY = scHeight - scHeight * 3 / 5;
            
            g.setColor(191, 0, 127);
            int offset = (carX - bgXOffset) / 16;
            int l = (scWidth * 4);
            int y1 = sunCenterY + sunR;
            int y2 = scHeight;
            int ii = 0;
            int n = l/bgLineStep;
            // vertical lines
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
            // horizontal lines
            n = scHeight*2/bgLineStep;
            for (int i = 0; i < n; i++) {
                if (i == 1) {
                    continue;
                }
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
            if (bodies[i] != leftwheel && bodies[i] != rightwheel) {
                UserData userData = bodies[i].getUserData();
                if (userData instanceof MUserData) {
                    MUserData mUserData = (MUserData) userData;
                    int bodyType = mUserData.bodyType;
                    switch (bodyType) {
                        case MUserData.TYPE_ACCELERATOR:
                            g.setColor(mUserData.color);
                            break;
                        case MUserData.TYPE_TRAMPOLINE:
                            g.setColor(0xffaa00);
                            break;
                        case MUserData.TYPE_LEVEL_FINISH:
                            g.setColor(0x00ff00);
                            break;
                        case MUserData.TYPE_LAVA:
                            g.setColor(0xff5500);
                            break;
                        default:
                            g.setColor(currColBodies);
                            break;
                    }
                } else {
                    g.setColor(currColBodies);
                }
                drawBody(g, bodies[i]);
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

    private void drawLandscape(Graphics g, int[][] structuresData, int structureRingBufferOffset, int structureCount) {
        int prevStructureEndX = 0;
        int prevStructureEndY = 0;
        for (int i = structureRingBufferOffset; i < structureRingBufferOffset + structureCount; i++) {
            int[] structureData = structuresData[i % structuresData.length];
            int c = 0;
            int endX = structureData[c++];
            int endY = structureData[c++];
            int lineCount = structureData[c++];
            int structureID = structureData[c++];
            int color = currColLandscape;
            if (DebugMenu.isDebugEnabled) {
                Random random = new Random(structureID);
                g.setColor(128 + random.nextInt() % 128, 128 + random.nextInt() % 128, 128 + random.nextInt() % 128);
                color = g.getColor();
            } else {
                g.setColor(currColLandscape);
            }

            if (xToPX(endX) < 0) {
                prevStructureEndX = endX;
                prevStructureEndY = endY;
                continue;
            }

            while (c < structureData.length - 1) {
                int id = structureData[c++];
                switch (id) {
                    case ElementPlacer.DRAWING_DATA_ID_LINE:
                        int x1 = xToPX(structureData[c++]);
                        int y1 = yToPX(structureData[c++]);
                        drawLine(g, x1, y1, xToPX(structureData[c++]), yToPX(structureData[c++]), 24);
                        break;
                    case ElementPlacer.DRAWING_DATA_ID_PATH:
                        int pointsCount = structureData[c++];
                        int prevX = xToPX(structureData[c++]);
                        int prevY = yToPX(structureData[c++]);
                        for (int j = 1; j < pointsCount; j++) {
                            drawLine(g, prevX, prevY, prevX = xToPX(structureData[c++]), prevY = yToPX(structureData[c++]), 24);
                        }
                        break;
                    case ElementPlacer.DRAWING_DATA_ID_CIRCLE: {
                        int x = structureData[c++];
                        int y = structureData[c++];
                        int r = structureData[c++];
                        g.drawArc(xToPX(x - r), yToPX(y - r), r * 2 * 1000 / zoomOut, r * 2 * 1000 / zoomOut, 0, 360, 24, zoomOut, true, true, true);
                        break;
                    }
                    case ElementPlacer.DRAWING_DATA_ID_ARC: {
                        int x = structureData[c++];
                        int y = structureData[c++];
                        int r = structureData[c++];
                        int startAngle = structureData[c++];
                        int arcAngle = structureData[c++];
                        if (arcAngle == 0) {
                            arcAngle = 360;
                        }
                        int kx = structureData[c++];
                        int ky = structureData[c++];
                        if (DebugMenu.isDebugEnabled) {
                            if (!DebugMenu.simulationMode) {
                                g.drawString("startAngle=" + startAngle, xToPX(x), yToPX(y), Graphics.BOTTOM | Graphics.HCENTER);
                                g.drawString("arcAngle=" + arcAngle, xToPX(x), yToPX(y), Graphics.TOP | Graphics.HCENTER);
                            }
                        }
                        g.drawArc(xToPX(x - r * kx / 10), yToPX(y - r * ky / 10), r*2 * kx * 100 / zoomOut, r*2 * ky * 100 / zoomOut, startAngle, arcAngle, 24, zoomOut, true, true, true);
                        break;
                    }
                }
            }

            if (DebugMenu.isDebugEnabled) {
                if (prevStructureEndX == 0) {
                    prevStructureEndX = endX - 1000;
                    prevStructureEndY = endY - 100;
                }
                g.setColor(0x000033);
                String str = String.valueOf(lineCount);
                int x = xToPX((endX + prevStructureEndX) / 2);
                int y = yToPX((endY + prevStructureEndY) / 2);
                int w = g.stringWidth(str);
                int h = g.getFontHeight();
                g.fillRect(x - w/2, y - h/2, w, h);
                g.setColor(color);
                g.drawLine(xToPX(endX), 0, xToPX(endX), scHeight);
                g.drawString(str, x, y, Graphics.VCENTER | Graphics.HCENTER);
            }

            prevStructureEndX = endX;
            prevStructureEndY = endY;

            if (xToPX(endX) >= scWidth) {
                break;
            }
        }
    }

//    private void drawConstraints(Graphics g) {
//        int constraintCount = getConstraintCount();
//        Constraint[] constraints = getConstraints();
//        for (int i = 0; i < constraintCount; i++) {
//            if (constraints[i] instanceof Spring) {
//                Spring spring = (Spring) constraints[i];
//                g.drawLine(xToPX(spring.getPoint1().xAsInt()),
//                        yToPX(spring.getPoint1().yAsInt()),
//                        xToPX(spring.getPoint2().xAsInt()),
//                        yToPX(spring.getPoint2().yAsInt()));
//            }
//        }
//    }

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
        g.drawLine(x1, y1, x2, y2, thickness, zoomOut, betterGraphics, zoomThickness);
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

        g.drawLine(x1, y1, x2, y2, thickness, zoomOut, betterGraphics);
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

    public void refreshCarPos() {
        if (carbody != null) {
            FXVector posFX = carbody.positionFX();
            carX = posFX.xAsInt();
            carY = posFX.yAsInt();
        } else {
            carX = -8000;
            carY = 0;
        }
    }

    public void refreshScreenParameters(int w, int h) {
        Logger.log("world:refreshing screen params:");
        Logger.log(w + " " + h);
        if (w <= 0 || h <= 0) {
        	return;
        }
        
        scWidth = w;
        halfScWidth = scWidth / 2;
        scHeight = h;
        scMinSide = Math.min(scWidth, scHeight);
        bgLineStep = scMinSide / 3;
        zoomBase = 6000 * 240 / scMinSide;
        calcZoomOut();
        bgLineThickness = Math.max(w, h)/250;
        try {
        	betterGraphics = MobappGameSettings.isBetterGraphicsEnabled(Math.max(scWidth, scHeight) >= BIGSCREEN_SIDE);
        	bg = bg || MobappGameSettings.isBGEnabled(false);
        } catch (Throwable ex) {
        	ex.printStackTrace();
        }
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

    public void moveBg(int dx) {
        bgXOffset = bgXOffset + dx;
    }
}
