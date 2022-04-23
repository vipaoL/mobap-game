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

    public GraphicsWorld(World w) {
        super(w);
        this.fontH = Font.getDefaultFont().getHeight();
        this.scWidth = Main.sWidth;
        this.halfScWidth = scWidth / 2;
        this.scHeight = Main.sHeight;
        this.halfScHeight = scHeight / 2;
        this.scMinSide = Math.min(scWidth, scHeight);
        this.zoomBase = 6000 * 240 / scMinSide;
        this.zoomOut = zoomBase;
    }
    String text = "";
    
    int scWidth;
    int halfScWidth;
    int scHeight;
    int halfScHeight;
    int scMinSide;
    int zoomBase;
    int zoomOut;
    int offsetX = 0;
    int offsetY = 0;
    public static int carX = 0;
    public static int carY = 0;
    static int carAng2FX = 0;
    static int rVel2FX = 0;
    static FXVector velFX = new FXVector(FXVector.newVector(0, 0));
    public static int viewField = 10;
    public static int points = 0;
    int fontH;
    

    public void draw(Graphics g) {
        Body[] bodies = getBodies();
        int bodyCount = getBodyCount();

        int constraintCount = getConstraintCount();
        Constraint[] constraints = getConstraints();

        g.setColor(0, 0, 0);
        g.fillRect(0, 0, scWidth, scHeight);

        try {
            carX = gCanvas.carbody.positionFX().xAsInt();
            carY = gCanvas.carbody.positionFX().yAsInt();
        } catch (NullPointerException ex) {
            Main.print("ждём автомобиль");
            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException ex1) {
                ex1.printStackTrace();
            }*/
        }
        //zoomOut = (carY/2 - scMinSide / 2) * 1000 / (scMinSide / 2);
        zoomOut = 2*(500 * carY / scMinSide - 500); // same, optimized
        if (zoomOut < 1) {
            zoomOut = -zoomOut;
            zoomOut+=1;
        }
        //zoomOut = 0;
        zoomOut += zoomBase;
        viewField = scWidth * zoomOut / 1000;
        if (mnCanvas.debug) {
            viewField /= 4;
        }
        //offsetX = -(carX + halfScWidth)*1000/zoomOut  + halfScWidth;
        offsetX = -carX*1000/zoomOut + scWidth / 3;
        //offsetX = (halfScWidth - carY - scWidth)*1000/(zoomOut) + halfScWidth;
        offsetY = -carY*1000/zoomOut + scHeight * 3 / 4;

        g.setColor(255, 255, 255);
        //g.drawString("" + carX/2000, halfScWidth, scHeight-fontH, Graphics.HCENTER|Graphics.TOP);
        g.drawString("" + points, halfScWidth, scHeight-fontH, Graphics.HCENTER|Graphics.TOP);
        for (int i = 0; i < bodyCount; i++) {
            if (bodies[i] != gCanvas.leftwheel & bodies[i] != gCanvas.rightwheel)
            drawBody(g, bodies[i]);
        }
        
        drawCar(g);

        //g.setColor(255, 255, 255);
        for (int i = 0; i < constraintCount; i++) {
            if (constraints[i] instanceof Spring) {
                Spring spring = (Spring) constraints[i];
                g.drawLine(xToPX(spring.getPoint1().xAsInt()),
                        yToPX(spring.getPoint1().yAsInt()),
                        xToPX(spring.getPoint2().xAsInt()),
                        yToPX(spring.getPoint2().yAsInt()));
            }
        }
        drawLandscape(g);
    }

    public void drawBody(Graphics g, Body b) {

        FXVector[] positions = b.getVertices();
        if (positions.length == 1) {
            int radius = FXUtil.fromFX(b.shape().getBoundingRadiusFX());
            //int radius = 15;
            //.drawString(text, 0, 0, 20);
            g.setColor(255, 255, 255);
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

    private void drawLandscape(Graphics g) {
        Landscape landscape = getLandscape();
        g.setColor(0x4444ff);
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
    
    public void drawCar(Graphics g) {
        drawWheel(g, gCanvas.leftwheel);
        drawWheel(g, gCanvas.rightwheel);
        
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

    public void setText(String t) {
        text = t;
    }
    
    public int xToPX(int c) {
        return c*1000/zoomOut+offsetX;
    }
    public int yToPX(int c) {
        return c*1000/zoomOut+offsetY;
    }
    static void refreshPos() {
        carX = gCanvas.carbody.positionFX().xAsInt();
        carY = gCanvas.carbody.positionFX().yAsInt();
    }
    static void getRot() {
        carAng2FX = gCanvas.carbody.rotation2FX();
    }
    static void getVel() {
        rVel2FX = gCanvas.carbody.rotationVelocity2FX();
        velFX = gCanvas.carbody.velocityFX();
    }
}
