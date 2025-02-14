package mobileapplication3.game;

import at.emini.physics2D.Body;
import at.emini.physics2D.Shape;
import mobileapplication3.platform.Mathh;

public class StructurePlacer {
    public static int[] place(GraphicsWorld world, boolean skipPlacingBodies, short[][] data, int x, int y) {
        ElementPlacer elementPlacer = new ElementPlacer(world, skipPlacingBodies);
        for (int i = 1; i < data.length; i++) {
            elementPlacer.place(data[i], x, y);
        }
        return new int[] {x + data[0][1], y + data[0][2], elementPlacer.getLineCount()}; //
    }

    public static int[] arc1(GraphicsWorld world, boolean skipPlacingBodies, int x, int y, int r, int va) { // 0
        ElementPlacer elementPlacer = new ElementPlacer(world, skipPlacingBodies);
        int endX = x;

        x+=r;
        int r2 = r*3/2;

        elementPlacer.arc(x-r, y-r2, r2, 60, 30);
        elementPlacer.arc(x+r/2, y-r*2, r, 300, va);
        int ofs = (1000 - Mathh.cos(30))*2*r2/1000;
        elementPlacer.arc(x+r*2-ofs, y-r2, r2, 60, 90);

        int l = r2+r2-ofs;
        endX += l;
        return new int[] {endX, y, elementPlacer.getLineCount()};
    }

    public static int[] arc2(GraphicsWorld world, boolean skipPlacingBodies, int x, int y, int r, int sn) { // 4
        ElementPlacer elementPlacer = new ElementPlacer(world, skipPlacingBodies);
        int endY = y;

        int sl = 360 / sn;

        int f0off = 30;
        int va = 60 + f0off;
        int ang = 360 - va;

        int offset = va + 90 - f0off;
        elementPlacer.arc(x+r, y-r, r, ang, offset);

        int r2 = r/10*8;
        elementPlacer.line(x, y, x+r-r2, y);
        elementPlacer.arc(x+r, y, r2, 90, 90, 10, 5);
        elementPlacer.line(x+r, y+r2/2, x+2*r, y+r2/2);
        endY += r2/2;

        if (!skipPlacingBodies) {
            int l2 = (r2) / 2 - r*(1000 - Mathh.cos(f0off)) / 2000;

            l2 += 10; // hack. I don't understand this code anymore. This method should be rewritten entirely

            int platformLength = 2*sl*r*3141/1000/360;
            int platformHeight = r/100;

            Shape rect = Shape.createRectangle(platformLength, platformHeight);
            rect.setMass(1);
            rect.setFriction(0);
            rect.setElasticity(50);

            for (int i = f0off+sl/2; i < 60; i+=sl) {
                Body fallingPlatform = new Body(x+r+Mathh.cos(i+f0off)*(r+platformHeight/2)/1000, y-r+Mathh.sin(i+f0off)*(r+platformHeight/2)/1000, rect, true);
                fallingPlatform.setDynamic(false);
                fallingPlatform.setUserData(new MUserData(MUserData.TYPE_FALLING_PLATFORM, new short[] {20}));
                fallingPlatform.setRotationDeg(i+f0off-90);
                world.addBody(fallingPlatform);
            }
            rect = Shape.createRectangle(l2, platformHeight);
            rect.setMass(1);
            rect.setFriction(10);
            rect.setElasticity(0);
            for (int i = 0; i < 2; i++) {
                Body fallingPlatform = new Body(x+r-r2+i*l2+l2/2, y+platformHeight/2, rect, true);
                fallingPlatform.setDynamic(false);
                fallingPlatform.setUserData(new MUserData(MUserData.TYPE_FALLING_PLATFORM, new short[] {20}));
                world.addBody(fallingPlatform);
            }
        }

        int l = r+r;
        x += l;
        return new int[] {x, endY, elementPlacer.getLineCount()};
    }

    public static int[] floor(GraphicsWorld world, boolean skipPlacingBodies, int x, int y, int l, int y2) {
        int amp = (y2 - y) / 2;
        return sinStruct(world, skipPlacingBodies, x, y + amp, l, 1, 270, amp);
    }
    public static int[] sinStruct(GraphicsWorld world, boolean skipPlacingBodies, int x, int y, int l, int halfPeriods, int offset, int amp) {    //3
        ElementPlacer elementPlacer = new ElementPlacer(world, skipPlacingBodies);

        elementPlacer.sin(x, y, l, halfPeriods, offset, amp);
        x += l;
        if (amp != 0) {
            y = y + amp*Mathh.sin(180*halfPeriods+offset)/1000;
        }
        return new int[] {x, y, elementPlacer.getLineCount()};
    }
    public static int[] floorStat(GraphicsWorld world, boolean skipPlacingBodies, int x, int y, int l) {      // 1
        ElementPlacer elementPlacer = new ElementPlacer(world, skipPlacingBodies);

        elementPlacer.line1(x, y, x + l, y);
        x += l;
        return new int[] {x, y, elementPlacer.getLineCount()};
    }
    public static int[] abyss(GraphicsWorld world, boolean skipPlacingBodies, int x, int y, int l) {
        ElementPlacer elementPlacer = new ElementPlacer(world, skipPlacingBodies);
        int endX = x;

        int prLength = 1000;
        elementPlacer.line(x, y, x + prLength, y);
        x += prLength;
        endX += prLength;
        int ang = 60; // springboard angle
        int r = l / 8;
        elementPlacer.arc(x, y-r, r, ang, 90 - ang, 15, 10);
        elementPlacer.line(x+l - l / 5, y - r * Mathh.cos(ang) / 1000, x+l, y - r * Mathh.cos(ang) / 1000);
        endX += l;
        y -= r * Mathh.cos(ang) / 1000;
        return new int[] {endX, y, elementPlacer.getLineCount()};
    }
    public static int[] slantedDottedLine(GraphicsWorld world, boolean skipPlacingBodies, int x, int y, int n) {
        ElementPlacer elementPlacer = new ElementPlacer(world, skipPlacingBodies);

        int offsetL = 600;
        for (int i = 0; i < n; i++) {
            elementPlacer.line(x + i*offsetL, y + i * 300/n, x + i*offsetL + 300, y + i * 300/n - 300);
        }
        x += n * offsetL;

        return new int[] {x, y, elementPlacer.getLineCount()};
    }
}
