/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor.elements;

import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;

/**
 *
 * @author vipaol
 */
public abstract class AbstractCurve extends Element {
    protected static final int NO_ARROWS = 0, ARROWS_NORMAL = 1, ARROWS_INVERTED = -1;
    
    protected PointsCache pointsCache;

    public AbstractCurve() {
        pointsCache = null;
    }
    
    abstract void genPoints();
    
    public void paint(Graphics g, int zoomOut, int offsetX, int offsetY, boolean drawThickness, boolean drawAsSelected) {
        if (pointsCache == null) {
            genPoints();
        }

        if (pointsCache.getSize() == 0) {
            return;
        }

        g.setColor(getSuitableColor(drawAsSelected));

        short[] startPoint = pointsCache.getPoint(0);
        int arrowsDirection = getArrowsDirection();
        for (int i = 0; i < pointsCache.getSize() - 1; i++) {
            short[] endPoint = pointsCache.getPoint(i+1);
            int x1 = xToPX(startPoint[0], zoomOut, offsetX);
            int y1 = yToPX(startPoint[1], zoomOut, offsetY);
            int x2 = xToPX(endPoint[0], zoomOut, offsetX);
            int y2 = yToPX(endPoint[1], zoomOut, offsetY);
            g.drawLine(x1, y1, x2, y2, LINE_THICKNESS, zoomOut, drawThickness, true, true, true);
            if (arrowsDirection != NO_ARROWS && i % 2 == 0) {
                int dx = x2 - x1;
                int dy = y2 - y1;
                if (arrowsDirection == ARROWS_INVERTED) {
                    dx = -dx;
                    dy = -dy;
                }
                int l = Mathh.calcDistance(dx, dy);
                int centerX = (x1 + x2) / 2;
                int centerY = (y1 + y2) / 2;
                int lzoomout = l * zoomOut;
                g.drawArrow(centerX, centerY, centerX + dy * 50000 / lzoomout, centerY - dx * 50000 / lzoomout, LINE_THICKNESS/6, zoomOut, drawThickness);
            }
            startPoint = endPoint;
        }
    }
    
    public boolean isBody() {
		return false;
	}

    protected int getArrowsDirection() {
        return NO_ARROWS;
    }
    
    protected class PointsCache {
        short[][] pointsCache;
        int cacheCarriage = 0;

        PointsCache(int length) {
            pointsCache = new short[length][2];
        }
        
        public void writePointToCache(short[] point) {
        	writePointToCache(point[0], point[1]);
        }

        public void writePointToCache(int x, int y) {
            pointsCache[cacheCarriage][0] = (short) x;
            pointsCache[cacheCarriage][1] = (short) y;
            cacheCarriage += 1;
        }
        
        public void movePoints(short dx, short dy) {
        	for (int i = 0; i < pointsCache.length; i++) {
				pointsCache[i][0] += dx;
				pointsCache[i][1] += dy;
			}
        }

        public int getSize() {
            return cacheCarriage;
        }
        
        public short[] getPoint(int i) {
            return pointsCache[i];
        }
    
    }
    
}
