/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.*;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.FXUtil;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author vipaol
 */
public class GraphicsWorld extends World {

    int scWidth = 64;
    int halfScWidth = 32;
    int scHeight = 320;
    int halfScHeight = 160;
    int scMinSide = 64;
    int zoomBase = 0;
    int zoomOut = 100;
    int offsetX = 0;
    int offsetY = 0;
    public static int carX = 0;
    public static int carY = 0;
    public static int viewField = 10;
    public static int points = 0;
    int fontH = 50;

    public GraphicsWorld(World w) {
        super(w);
        refreshScreenParameters();
        Main.print(getAreaStartFX());
    }

    public void addCar() {
        int x = 0;
        if (mnCanvas.wg) {
            x = -8000;
        }
        addCar(x, -400, FXUtil.TWO_PI_2FX / 360 * 30, null);
    }

    public Body carbody;
    public Body leftwheel;
    public Body rightwheel;

    public void addCar(int spawnX, int spawnY, int ang2FX, Object[] vel) {
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
        wheelShape.setElasticity(0);
        wheelShape.setFriction(0);
        wheelShape.setMass(2);
        wheelShape.correctCentroid();
        int lwX = spawnX - (carbodyLength / 2 - wheelRadius) * Mathh.cos(ang) / 1000;
        int lwY = spawnY + wheelRadius / 2 - (carbodyLength / 2 - wheelRadius) * Mathh.sin(ang) / 1000;
        int rwX = spawnX + (carbodyLength / 2 - wheelRadius) * Mathh.cos(ang) / 1000;
        int rwY = spawnY + wheelRadius / 2 + (carbodyLength / 2 - wheelRadius) * Mathh.sin(ang) / 1000;
        leftwheel = new Body(lwX, lwY, wheelShape, true);
        rightwheel = new Body(rwX, rwY, wheelShape, true);

        removeBody(carbody);
        removeBody(leftwheel);
        removeBody(leftwheel);

        addBody(carbody);
        carbody.addCollisionLayer(1);

        addBody(leftwheel);
        addBody(rightwheel);
        leftwheel.addCollisionLayer(1);
        rightwheel.addCollisionLayer(1);

        Joint leftjoint = new Joint(carbody, leftwheel, FXVector.newVector(-carbodyLength / 2 + wheelRadius, wheelRadius * 2 / 3), FXVector.newVector(0, 0), false);
        Joint rightjoint = new Joint(carbody, rightwheel, FXVector.newVector(carbodyLength / 2 - wheelRadius, wheelRadius * 2 / 3), FXVector.newVector(0, 0), false);
        addConstraint(leftjoint);
        addConstraint(rightjoint);

        if (vel != null) {
            FXVector velFX = (FXVector) vel[0];
            int rVel2FX = ((Integer) vel[1]).intValue();
            carbody.angularVelocity2FX(rVel2FX);
        }
    }

    public void draw(Graphics g) {
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, scWidth, scHeight);
        try {
            carX = carbody.positionFX().xAsInt();
            carY = carbody.positionFX().yAsInt();

            calculateZoomOut();
            offsetX = -carX * 1000 / zoomOut + scWidth / 3;
            offsetY = -carY * 1000 / zoomOut + scHeight * 2 / 3;

            viewField = scWidth * zoomOut / 1000;
            if (mnCanvas.debug & DebugMenu.closerWorldgen) {
                viewField /= 4;
            }

            g.setColor(0x4444ff);
            if (mnCanvas.debug) {
                g.setColor(255, 255, 255);
            }
            drawLandscape(g);
            g.setColor(255, 255, 255);
            drawBodies(g); //bodies, exclude car wheels
            drawCar(g); //car wheels
            drawConstraints(g);
        } catch (NullPointerException ex) {
            int l = scWidth * 2 / 3;
            int h = scHeight / 24;
            g.drawRect(scWidth / 2 - l / 2, scHeight * 2 / 3, l, h);
        }
    }

    public void drawBodies(Graphics g) {
        Body[] bodies = getBodies();
        int bodyCount = getBodyCount();
        for (int i = 0; i < bodyCount; i++) {
            if (bodies[i] != leftwheel & bodies[i] != rightwheel) {
                drawBody(g, bodies[i]);
            }
        }
    }

    public void drawBody(Graphics g, Body b) {
        FXVector[] positions = b.getVertices();
        if (positions.length == 1) {
            int radius = FXUtil.fromFX(b.shape().getBoundingRadiusFX());
            g.drawArc(xToPX(b.positionFX().xAsInt() - radius), yToPX(b.positionFX().yAsInt() - radius), radius * 2000 / zoomOut, radius * 2000 / zoomOut, 0, 360);
        } else {
            for (int i = 0; i < positions.length - 1; i++) {
                g.drawLine(xToPX(positions[i].xAsInt()),
                        yToPX(positions[i].yAsInt()),
                        xToPX(positions[i + 1].xAsInt()),
                        yToPX(positions[i + 1].yAsInt()));
            }
            g.drawLine(xToPX(positions[positions.length - 1].xAsInt()), yToPX(positions[positions.length - 1].yAsInt()), xToPX(positions[0].xAsInt()), yToPX(positions[0].yAsInt()));
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
            int stPoint = xToPX(landscape.startPoint(i).xAsInt());
            int endPoint = xToPX(landscape.endPoint(i).xAsInt());
            if (stPoint < scWidth | endPoint > 0) {
                g.drawLine(
                        stPoint,
                        yToPX(landscape.startPoint(i).yAsInt()),
                        endPoint,
                        yToPX(landscape.endPoint(i).yAsInt()));
            }
        }
    }

    private void drawConstraints(Graphics g) {
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
        //int radius = 15;
        //.drawString(text, 0, 0, 20);
        g.setColor(0, 0, 0);
        g.fillArc(xToPX(b.positionFX().xAsInt() - radius), yToPX(b.positionFX().yAsInt() - radius), radius * 2000 / zoomOut, radius * 2000 / zoomOut, 0, 360);
        g.setColor(255, 255, 255);
        g.drawArc(xToPX(b.positionFX().xAsInt() - radius), yToPX(b.positionFX().yAsInt() - radius), radius * 2000 / zoomOut, radius * 2000 / zoomOut, 0, 360);
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
        fontH = Font.getDefaultFont().getHeight();
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
        if (zoomOut < 1) {
            zoomOut = -zoomOut;
            zoomOut += 1;
        }
        zoomOut += zoomBase;
    }
}
