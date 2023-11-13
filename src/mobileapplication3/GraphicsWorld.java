/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.*;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author vipaol
 */
public class GraphicsWorld extends World {

    int colBg = 0x000000;
    int colLandscape = 0x4444ff;
    int colBodies = 0xffffff;
    int colFunctionalObjects = 0xff5500;
    int currColBg;
    int colWheel ;
    int currColWheel;
    int currColLandscape = colLandscape;
    int currColBodies;
    
    static boolean bg = false;
    public static int bgLineStep = Main.sWidth / 3;
    public static final int BIGSCREEN_SIDE = 480;
    
    int scWidth = Main.sWidth;
    int halfScWidth = scWidth/2;
    int scHeight = Main.sHeight;
    int halfScHeight = scHeight/2;
    int scMinSide = Math.min(scWidth, scHeight);
    
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
        if (DebugMenu.whatTheGame) {
            colWheel = 0x888888;
            colBg = 0x001155;
            colBodies = 0x555555;
        }
        currColWheel = colWheel;
        currColBg = colBg;
        currColBodies = colBodies;
    }

    public void addCar() {
        int x = 0;
        if (MenuCanvas.isWorldgenEnabled) {
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
        calculateZoomOut();
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
            calculateZoomOut();
            //zoomOut = 500;
            offsetX = -carX * 1000 / zoomOut + scWidth / 3;
            offsetY = -carY * 1000 / zoomOut + scHeight * 2 / 3;
            //offsetX-=200;
            //offsetY-=200;

            // some very boring code
            if (GameplayCanvas.points > 291 && GameplayCanvas.points < 293) {
                currColBg = 0x2f92ff;
                currColLandscape = 0xffffff;
            } else if (GameplayCanvas.points > 293 && GameplayCanvas.points < 300) {
                currColBg = colBg;
                currColLandscape = colLandscape;
            }
            
            if (bg) {
                currColLandscape = 0x0000ff;
                g.setColor(63, 0, 31);
                int offset = (carX - WorldGen.bgZeroPoint) / 16;
                int l = (scWidth * 16);
                for (int i = 0; i < l; i+=bgLineStep) {
                    int x2 = -(i + (offset) % bgLineStep - l/2)/*  *64/8  */;
                    int x1 = x2 / 32;
                    g.drawLine(x1 + scWidth / 2, scHeight * 2 / 3, x2 + scWidth / 2, scHeight);
                }
                g.setColor(63, 31, 0);
                int lines = 6;
                int r = Math.min(scWidth, scHeight) / 4;
                g.fillArc(scWidth / 2 - r, scHeight * 2 / 5 - r, r * 2, r * 2, 0, 360);
                g.setColor(currColBg);
                for (int i = 0; i < lines; i++) {
                    int y = i * r / lines + scHeight * 2 / 5 - r / 12;
                    g.drawLine(0, y-1, scWidth, y-1);
                    g.drawLine(0, y, scWidth, y);
                    g.drawLine(0, y+1, scWidth, y+1);
                }
            }
            
            // draw landscape
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

    public void drawBodies(Graphics g) {
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
                    Main.log("can't draw" + bodyType);
                }
            }
        }
    }

    public void drawBody(Graphics g, Body b) {
        FXVector[] positions = b.getVertices();
        
        if (b == carbody && DebugMenu.whatTheGame) {
            g.setColor(0xff0000);
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
        }
        
        if (positions.length == 1) { // if shape of the body is circle
            int radius = FXUtil.fromFX(b.shape().getBoundingRadiusFX());
            g.drawArc(xToPX(b.positionFX().xAsInt() - radius), yToPX(b.positionFX().yAsInt() - radius), radius * 2000 / zoomOut, radius * 2000 / zoomOut, 0, 360);
        }
        else { // if not a circle, then a polygon
            for (int i = 0; i < positions.length - 1; i++) {
                drawLine(g,
                        xToPX(positions[i].xAsInt()),
                        yToPX(positions[i].yAsInt()),
                        xToPX(positions[i + 1].xAsInt()),
                        yToPX(positions[i + 1].yAsInt()),
                        10);
            }
            drawLine(g,
                    xToPX(positions[positions.length - 1].xAsInt()),
                    yToPX(positions[positions.length - 1].yAsInt()),
                    xToPX(positions[0].xAsInt()),
                    yToPX(positions[0].yAsInt()),
                    10);
        }
    }

    public void drawCar(Graphics g) {
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

    void drawWheel(Graphics g, Body b) {
        int radius = FXUtil.fromFX(b.shape().getBoundingRadiusFX());
        if (GameplayCanvas.currentEffects[GameplayCanvas.EFFECT_SPEED] == null) {
            currColWheel = colWheel;
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
        g.drawArc(
                xToPX(b.positionFX().xAsInt() - radius),
                yToPX(b.positionFX().yAsInt() - radius),
                radius * 2000 / zoomOut,
                radius * 2000 / zoomOut,
                0, 360);
    }
    
    void drawLine(Graphics g, int x1, int y1, int x2, int y2, int thickness) {
        if (DebugMenu.discoMode) {
            g.setColor(random.nextInt(16777216));
        }
        if (thickness > 2 && Settings.bigScreen == Settings.TRUE) {
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
    
    void drawGroundLine(Graphics g, int x1, int y1, int x2, int y2, int thickness) {
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
        
        if (thickness > 2 && Settings.bigScreen == Settings.TRUE) {
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


    public int xToPX(int c) {
        return c * 1000 / zoomOut + offsetX;
    }

    public int yToPX(int c) {
        return c * 1000 / zoomOut + offsetY;
    }

    void refreshPos() {
        if (carbody != null) {
            FXVector posFX = carbody.positionFX();
            carX = posFX.xAsInt();
            carY = posFX.yAsInt();
        } else {
            carX = -8000;
            carY = 0;
        }
    }

    void refreshScreenParameters() {
        Main.log("world:refreshing screen params");
        scWidth = Main.sWidth;
        halfScWidth = scWidth / 2;
        scHeight = Main.sHeight;
        halfScHeight = scHeight / 2;
        scMinSide = Math.min(scWidth, scHeight);
        zoomBase = 6000 * 240 / scMinSide;
        calculateZoomOut();
    }

    void calculateZoomOut() {
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
}
