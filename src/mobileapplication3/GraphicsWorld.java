/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.*;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
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
    int currColBg = colBg;
    public static int currColWheel = 0;
    int currColLandscape = colLandscape;
    int currColBodies = colBodies;
    
    static boolean bg = false;
    public static final int BIGSCREEN_SIDE = 480;
    
    // test values, will be replaced
    int scWidth = 64;
    int halfScWidth = 32;
    int scHeight = 320;
    int halfScHeight = 160;
    int scMinSide = 64;
    
    int zoomBase = 0;
    int zoomOut = 100;
    int offsetX = 0;
    int offsetY = 0;
    public static int viewField = 10;
    public static int points = 0;
    
    public static int carX = 0;
    public static int carY = 0;
    int prevAng = 0;
    int ang = 0;
    public Body carbody;
    public Body leftwheel;
    public Body rightwheel;
    
    public GraphicsWorld(World w) {
        super(w);
    }

    public void addCar() {
        int x = 0;
        if (MenuCanvas.isWorldgenEnabled) {
            x = -8000;
        }
        addCar(x, -400, FXUtil.TWO_PI_2FX / 360 * 30, null);
    }

    public void addCar(int spawnX, int spawnY, int ang2FX, Object[] vel) {
        int carbodyLength = 240;
        int carbodyHeight = 40;
        int wheelRadius = 40;
        Shape carbodyShape;
        Shape wheelShape;

        carbodyShape = Shape.createRectangle(carbodyLength, carbodyHeight);
        carbodyShape.setMass(1*GameplayCanvas.gameSpeedMultiplier);
        carbodyShape.setFriction(0);
        carbodyShape.setElasticity(0);
        carbodyShape.correctCentroid();
        carbody = new Body(spawnX, spawnY, carbodyShape, true);
        carbody.setRotation2FX(ang2FX);

        long longAng2FX = ang2FX;
        int ang = (int) (longAng2FX * 360 / FXUtil.TWO_PI_2FX) + 1;

        wheelShape = Shape.createCircle(wheelRadius);
        wheelShape.setElasticity(0);
        wheelShape.setFriction(0);
        wheelShape.setMass(2*GameplayCanvas.gameSpeedMultiplier);
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
        addBody(rightwheel);
        leftwheel.addCollisionLayer(1);
        rightwheel.addCollisionLayer(1);

        Joint leftjoint = new Joint(carbody, leftwheel, FXVector.newVector(-carbodyLength / 2 + wheelRadius - 2, wheelRadius * 2 / 3), FXVector.newVector(0, 0), false);
        Joint rightjoint = new Joint(carbody, rightwheel, FXVector.newVector(carbodyLength / 2 - wheelRadius + 2, wheelRadius * 2 / 3), FXVector.newVector(0, 0), false);
        addConstraint(leftjoint);
        addConstraint(rightjoint);

        if (vel != null) {
            FXVector velFX = (FXVector) vel[0];
            int rVel2FX = ((Integer) vel[1]).intValue();
            carbody.angularVelocity2FX(rVel2FX);
        }
        WorldGen.zeroPoint = spawnX;
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
            offsetX = -carX * 1000 / zoomOut + scWidth / 3;
            offsetY = -carY * 1000 / zoomOut + scHeight * 2 / 3;

            // for timely track generation and deleting waste objects
            viewField = scWidth * zoomOut / 1000;
            if (DebugMenu.isDebugEnabled & DebugMenu.closerWorldgen) {
                viewField /= 4;
            }

            // some very boring code
            if (points > 291 & points < 293) {
                currColBg = 0x2f92cd;
                currColLandscape = 0xffffff;
            } else if (points > 293 & points < 300) {
                currColBg = colBg;
                currColLandscape = colLandscape;
            }
            
            if (bg) {
                currColLandscape = 0x0000ff;
                /*for (int i = 0; i < scHeight / 8; i++) {
                    g.setColor(0, Math.abs(255 * (i+1 - scHeight / 4) / (scHeight / 4)), 0);
                    int y1 = Math.abs(i * 8 + carY) % scHeight;
                    int y2 = Math.abs((scHeight - i) * 8 + carY) % scHeight;
                    g.drawLine(0, Math.abs(scHeight - y1 * 2), scWidth, Math.abs(scHeight - y2 * 2));
                */
                //g.setColor(0, 63, 0);
                g.setColor(63, 0, 31);
                int offset = (carX - WorldGen.zeroPoint) / 16;
                int l = (scWidth * 16);
                int step = scWidth / 3;
                for (int i = 0; i < l; i+=step) {
                    int x2 = -((i + offset) % l - l/2)/*  *64/8  */;
                    int x1 = x2 / 32;
                    g.drawLine(x1 + scWidth / 2, scHeight * 2 / 3, x2 + scWidth / 2, scHeight);
                }
                g.setColor(63, 31, 0);
                //g.setColor(0, 63, 0);
                int lines = 6;
                int r = Math.min(scWidth, scHeight) / 4;
                g.fillArc(scWidth / 2 - r, scHeight * 2 / 5 - r, r * 2, r * 2, 0, 360);
                g.setColor(0, 0, 0);
                //g.setColor(63, 0, 31);
                for (int i = 0; i < lines; i++) {
                    int y = i * r / lines + scHeight * 2 / 5 - r / 12;
                    g.drawLine(0, y-1, scWidth, y-1);
                    g.drawLine(0, y, scWidth, y);
                    g.drawLine(0, y+1, scWidth, y+1);
                }
            }
            
            // draw landscape
            if (!DebugMenu.isDebugEnabled) {
                g.setColor(currColLandscape);
            } else {
                g.setColor(255, 255, 255);
            }
            drawLandscape(g);
            
            drawBodies(g); // draw all bodies, excluding car wheels
            drawCar(g); // draw car wheels
            drawConstraints(g); // disabled
            
            countFlips();
        } catch (NullPointerException ex) {
            int l = scWidth * 2 / 3;
            int h = scHeight / 24;
            g.drawRect(scWidth / 2 - l / 2, scHeight * 2 / 3, l, h);
            g.fillRect(scWidth / 2 - l / 2, scHeight * 2 / 3, l/5, h);
        }
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
                    bodyType = bodyUserData.bodyType;
                } catch (ClassCastException ex) {

                } catch (NullPointerException ex) {

                }
                
                g.setColor(currColBodies);
                if (bodyType == MUserData.TYPE_ACCELERATOR) {
                    g.setColor(bodyUserData.color);
                }
                drawBody(g, bodies[i]);
            }
        }
    }

    public void drawBody(Graphics g, Body b) {
        FXVector[] positions = b.getVertices();
        
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
        //g.setColor(0x00ff00);
        for (int i = 0; i < landscape.segmentCount(); i++) {
            int stPointX = xToPX(landscape.startPoint(i).xAsInt());
            int stPointY = yToPX(landscape.startPoint(i).yAsInt());
            int endPointX = xToPX(landscape.endPoint(i).xAsInt());
            int endPointY = yToPX(landscape.endPoint(i).yAsInt());
            if (stPointX < scWidth | endPointX > 0) {
                drawLine(
                        g,
                        stPointX,
                        stPointY,
                        endPointX,
                        endPointY,
                        24);
                if (DebugMenu.showLinePoints) {
                    g.fillArc(stPointX - 2, stPointY - 2, 4, 4, 0, 360);
                    g.fillArc(endPointX - 2, endPointY - 2, 4, 4, 0, 360);
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
            currColWheel = colBg;
        }
        g.setColor(currColWheel);
        g.fillArc(xToPX(b.positionFX().xAsInt() - radius), yToPX(b.positionFX().yAsInt() - radius), radius * 2000 / zoomOut, radius * 2000 / zoomOut, 0, 360);
        g.setColor(currColBodies);
        g.drawArc(xToPX(b.positionFX().xAsInt() - radius), yToPX(b.positionFX().yAsInt() - radius), radius * 2000 / zoomOut, radius * 2000 / zoomOut, 0, 360);
    }
    
    void drawLine(Graphics g, int x1, int y1, int x2, int y2, int thickness) {
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
        try {
            carX = carbody.positionFX().xAsInt();
            carY = carbody.positionFX().yAsInt();
        } catch (NullPointerException ex) {
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
        zoomOut = zoomBase;
    }

    void calculateZoomOut() {
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
    
    boolean flipWaiting = false;
    boolean backFlipWaiting = false;
    boolean step1Done = false;
    boolean step2Done = false;
    
    int backFlipsCount = 0;
    int upperAng = 0;
    
    void countFlips() {
        if (DebugMenu.dontCountFlips) {
            return;
        }
        if (carbody.rotationVelocity2FX() >= 0) {
            if (flipWaiting) {
                flipWaiting = false;
                step1Done = false;
                step2Done = false;
            }
            backFlipWaiting = true;
        } else {
            if (backFlipWaiting) {
                backFlipsCount = 0;
                backFlipWaiting = false;
                step1Done = false;
                step2Done = false;
            }
            flipWaiting = true;
        }
        int ang = carbody.rotation2FX();
        if (!step1Done) {
            if (ang < 13176794 | ang > 92237561) {
                step1Done = true;
            }
        } else {
            if (GameplayCanvas.timeFlying < 1 & !GameplayCanvas.uninterestingDebug) { // cancel when touched the ground
                step2Done = false;
                backFlipsCount = 0;
                return;
            }
            if (!step2Done) {
                if (!(ang < 13176794 | ang > 92237561))
                    step2Done = true;
            } else {
                if ((ang < 13176794 | ang > 92237561)) {
                    if (carbody.rotationVelocity2FX() >= 0) {
                        if (backFlipWaiting) {
                            backFlipsCount++;
                            //System.out.println("backFlipsCount" + backFlipsCount);
                            if (backFlipsCount > 1) {
                                points += 1;
                                backFlipsCount = 0;
                                GameplayCanvas.indicateFlip();
                            }
                        }
                    } else {
                        if (flipWaiting) {
                            points += 1;
                            GameplayCanvas.indicateFlip();
                        }
                        backFlipsCount = 0;
                        //FXUtil.
                    }
                    step1Done = false;
                    step2Done = false;
                }
            }
        }
    }
}
