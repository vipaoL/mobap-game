/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

import at.emini.physics2D.*;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.FXUtil;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author steamuser
 */
public class GraphicsWorld extends World {

    public GraphicsWorld(World w) {
        super(w);
    }
    static String text = "";
    
    int scWidth = Main.sWidth;
    int scHeight = Main.sHeight;
    int zoomBase = 6000 / (1 + scWidth / 240);

    public void draw(Graphics g) {
        Body[] bodies = getBodies();
        int bodyCount = getBodyCount();

        int constraintCount = getConstraintCount();
        Constraint[] constraints = getConstraints();

        g.setColor(0, 0, 0);
        g.fillRect(0, 0, scWidth, scHeight);

        int carY = mCanvas.carbody.positionFX().yAsInt();
        int zoomOut = 0;
        zoomOut = (carY/2 - scHeight / 2) * 1000 / (scHeight / 2);
        if (zoomOut == 0) {
            zoomOut = 1;
        } else if (zoomOut < 0) {
            zoomOut = -zoomOut;
        }
        zoomOut += zoomBase;
        if (carY < scHeight / 2) {
            //zoomOut = -zoomOut;
        }
        //int offsetX = 0;
        int offsetX = -(mCanvas.carbody.positionFX().xAsInt() - scWidth / 2) * 1000/zoomOut + scWidth/2 - scWidth*1000/(zoomOut);
        int offsetY = scHeight / 2 - scHeight * 1000 / (2 * zoomOut) - carY * 1000 / zoomOut / 2;

        g.setColor(255, 255, 255);
        for (int i = 0; i < bodyCount; i++) {
            
            g.setColor(255, 255, 255);

            drawBody(g, bodies[i], offsetX, offsetY, zoomOut);
        }

        g.setColor(255, 255, 255);
        for (int i = 0; i < constraintCount; i++) {
            if (constraints[i] instanceof Spring) {
                Spring spring = (Spring) constraints[i];
                g.drawLine(spring.getPoint1().xAsInt(),
                        spring.getPoint1().yAsInt(),
                        spring.getPoint2().xAsInt(),
                        spring.getPoint2().yAsInt());
            }
        }
        drawLandscape(g, offsetX, offsetY, zoomOut);
    }

    public void drawBody(Graphics g, Body b, int offsetX, int offsetY, int zoomOut) {

        FXVector[] positions = b.getVertices();
        if (positions.length == 1) {
            int radius = FXUtil.fromFX(b.shape().getBoundingRadiusFX());
            //int radius = 15;
            g.drawString("" + text, 0, 0, 20);
            g.drawArc((b.positionFX().xAsInt() - radius) * 1000 / zoomOut + offsetX, (b.positionFX().yAsInt() - radius) * 1000 / zoomOut + offsetY, radius * 2 * 1000 / zoomOut, radius * 2 * 1000 / zoomOut, 0, 360);
        } else {
            for (int i = 0; i < positions.length - 1; i++) {
                g.drawLine(positions[i].xAsInt() * 1000 / zoomOut + offsetX,
                        positions[i].yAsInt() * 1000 / zoomOut + offsetY,
                        positions[i + 1].xAsInt() * 1000 / zoomOut + offsetX,
                        positions[i + 1].yAsInt() * 1000 / zoomOut + offsetY);
            }
            g.drawLine(positions[positions.length - 1].xAsInt() * 1000 / zoomOut + offsetX, positions[positions.length - 1].yAsInt() * 1000 / zoomOut + offsetY, positions[0].xAsInt() * 1000 / zoomOut + offsetX, positions[0].yAsInt() * 1000 / zoomOut + offsetY);
        }
    }

    private void drawLandscape(Graphics g, int offsetX, int offsetY, int zoomOut) {
        Landscape landscape = getLandscape();
        g.setColor(0x4444ff);
        for (int i = 0; i < landscape.segmentCount(); i++) {
            int stPoint = landscape.startPoint(i).xAsInt() * 1000 / zoomOut + offsetX;
            int endPoint = landscape.endPoint(i).xAsInt() * 1000 / zoomOut + offsetX;
            if (stPoint < scWidth | endPoint > 0) {
                g.drawLine(
                        stPoint,
                        landscape.startPoint(i).yAsInt() * 1000 / zoomOut + offsetY,
                        (landscape.endPoint(i).xAsInt()) * 1000 / zoomOut + offsetX,
                        landscape.endPoint(i).yAsInt() * 1000 / zoomOut + offsetY);
            }
        }
    }

    public static void setText(String t) {
        text = t;
    }
}
