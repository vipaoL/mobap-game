package mobileapplication3.game;

import at.emini.physics2D.Body;
import at.emini.physics2D.Landscape;
import at.emini.physics2D.Shape;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Mathh;
import utils.MobappGameSettings;

public class ElementPlacer {
    public static final short EOF = 0;
    public static final short END_POINT = 1;
    public static final short LINE = 2;
    public static final short CIRCLE = 3;
    public static final short BROKEN_LINE = 4;
    public static final short BROKEN_CIRCLE = 5;
    public static final short SINE = 6;
    public static final short ACCELERATOR = 7;
    public static final short TRAMPOLINE = 8;
    public static final short LEVEL_START = 9;
    public static final short LEVEL_FINISH = 10;
    public static final short LAVA = 11;

    public static final int DRAWING_DATA_ID_LINE = 1, DRAWING_DATA_ID_PATH = 2, DRAWING_DATA_ID_CIRCLE = 3, DRAWING_DATA_ID_ARC = 4;

    private final static int DETAIL_LEVEL = readDetailLevelSetting();

    private int lineCount;
    private final GraphicsWorld w;
    private final Landscape landscape;
    private final boolean dontPlaceBodies;
    private int[] drawingData;

    public ElementPlacer(GraphicsWorld world, boolean dontPlaceBodies) {
        lineCount = 0;
        w = world;
        landscape = world.getLandscape();
        this.dontPlaceBodies = dontPlaceBodies;
        drawingData = null;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void place(short[] data, int originX, int originY) {
        int prevLowestY = w.lowestY;
        short id = data[0];
        switch (id) {
            case LINE:
                line(originX + data[1], originY + data[2], originX + data[3], originY + data[4]);
                break;
            case CIRCLE:
                arc(originX + data[1], originY + data[2], data[3], data[4], data[5], data[6] / 10, data[7] / 10);
                break;
            case BROKEN_LINE:
                if (!dontPlaceBodies) {
                    int x1 = data[1];
                    int y1 = data[2];
                    int x2 = data[3];
                    int y2 = data[4];
                    int dx = x2 - x1;
                    int dy = y2 - y1;
                    int platfH = data[5];
                    int platfL = data[6];
                    int spacing = data[7];
                    int l = data[8];
                    int ang = data[9];
                    int n = (l + spacing) / (platfL + spacing);

                    Shape rect = Shape.createRectangle(platfL, platfH);
                    rect.setMass(1);
                    rect.setFriction(10);
                    rect.setElasticity(0);
                    dx /= (l / platfL);
                    int spX = spacing * dx / l; // TODO fix this mess (the editor should be fixed too, it will be a new mgstruct format version)
                    dy /= (l / platfL);
                    int spY = spacing * dy / l;
                    int offsetX = platfL / 2 * Mathh.cos(ang) / 1000;
                    int offsetY = platfL / 2 * Mathh.sin(ang) / 1000;

                    for (int i = 0; i < n; i++) {
                        Body fallinPlatf = new Body(originX + x1 + i * (dx + spX) + offsetX, originY + y1 + i * (dy + spY) + offsetY, rect, false);
                        fallinPlatf.setRotation2FX(FXUtil.TWO_PI_2FX / 360 * ang);
                        fallinPlatf.setUserData(new MUserData(MUserData.TYPE_FALLING_PLATFORM, new short[]{20}));
                        w.addBody(fallinPlatf);
                    }
                    updateLowestY(Math.max(y1, y2) + platfH);
                }
                break;
            case BROKEN_CIRCLE:
                // not implemented yet
                arc(originX + data[1], originY + data[2], data[3], 360, 0);
                break;
            case SINE:
                sin(originX + data[1], originY + data[2], data[3], data[4], data[5], data[6]);
                break;
            case ACCELERATOR: {
                int x = originX + data[1];
                int y = originY + data[2];
                int l = data[3];
                int thickness = data[4];
                int ang = data[5];

                short effectID = GameplayCanvas.EFFECT_SPEED;
                short effectDuration = data[8];
                short directionOffset = data[6];
                short speedMultiplier = data[7];

                int centerX = x + l * Mathh.cos(ang) / 2000;
                int centerY = y + l * Mathh.sin(ang) / 2000;

                int colorModifier = (speedMultiplier - 100) * 3;
                int red = Math.min(255, Math.max(0, colorModifier));
                int blue = Math.min(255, Math.max(0, -colorModifier));
                int green = blue;
                if (red < 50 && blue < 50) {
                    red = 50;
                    blue = 50;
                }

                int color = ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
                Shape plate = Shape.createRectangle(l, thickness);
                Body pressurePlate = new Body(centerX, centerY, plate, false);
                MUserData mUserData = new MUserData(MUserData.TYPE_ACCELERATOR, new short[]{effectID, effectDuration, directionOffset, speedMultiplier});
                mUserData.color = color;
                pressurePlate.setUserData(mUserData);
                //Main.log(pressurePlate.getUserData().bodyType);
                pressurePlate.setRotation2FX(FXUtil.TWO_PI_2FX / 360 * ang);
                w.addBody(pressurePlate);
                updateLowestY(y + Math.max(l, thickness));
                break;
            }
            case TRAMPOLINE: {
                int x = originX + data[1];
                int y = originY + data[2];
                int l = data[3];
                int thickness = data[4];
                int ang = data[5];
                int elasticity = data[6];

                Shape plate = Shape.createRectangle(l, thickness);
                plate.setElasticity(elasticity);
                Body pressurePlate = new Body(x, y, plate, false);
                MUserData mUserData = new MUserData(MUserData.TYPE_TRAMPOLINE, null);
                pressurePlate.setUserData(mUserData);
                pressurePlate.setRotation2FX(FXUtil.TWO_PI_2FX / 360 * ang);
                w.addBody(pressurePlate);
                updateLowestY(y + Math.max(l, thickness));
                break;
            }
            case LEVEL_FINISH: {
                int x = originX + data[1];
                int y = originY + data[2];
                int l = data[3];
                int thickness = data[4];
                int ang = data[5];

                Shape plate = Shape.createRectangle(l, thickness);
                Body pressurePlate = new Body(x, y, plate, false);
                MUserData mUserData = new MUserData(MUserData.TYPE_LEVEL_FINISH, null);
                pressurePlate.setUserData(mUserData);
                //Main.log(pressurePlate.getUserData().bodyType);
                pressurePlate.setRotation2FX(FXUtil.TWO_PI_2FX / 360 * ang);
                w.addBody(pressurePlate);
                updateLowestY(y + Math.max(l, thickness));
                break;
            }
            case LAVA: {
                int x = originX + data[1];
                int y = originY + data[2];
                int l = data[3];
                int thickness = data[4];
                int ang = data[5];

                Shape plate = Shape.createRectangle(l, thickness);
                Body pressurePlate = new Body(x, y, plate, false);
                MUserData mUserData = new MUserData(MUserData.TYPE_LAVA, null);
                pressurePlate.setUserData(mUserData);
                pressurePlate.setRotation2FX(FXUtil.TWO_PI_2FX / 360 * ang);
                w.addBody(pressurePlate);
                updateLowestY(y + Math.max(l, thickness));
                break;
            }
        }
        if (w.lowestY != prevLowestY) {
            Logger.log("lowestY=", w.lowestY);
        }
    }

    public void sin(int x, int y, int l, int halfPeriods, int startAngle, int amp) {    //3
        int pointCount = 0;
        if (amp == 0) {
            line(x, y, x + l, y);
        } else {
            int step = 30 / DETAIL_LEVEL;
            int endAngle = startAngle + halfPeriods * 180;
            int a = endAngle - startAngle;

            int prevPointX = x;
            int prevPointY = y + amp * Mathh.sin(startAngle) / 1000;

            int nextPointX;
            int nextPointY;

            int[] points = new int[2 * (endAngle - startAngle) / step + 10]; // IDK how much exactly it will take, but 10 extra reserved cells should be enough
            points[0] = prevPointX;
            points[1] = prevPointY;
            pointCount++;

            for (int i = startAngle; i <= endAngle; i+=step) {
                nextPointX = x + (i - startAngle)*l/a;
                nextPointY = y + amp*Mathh.sin(i)/1000;
                line(prevPointX, prevPointY, nextPointX, nextPointY, 1, false);
                prevPointX = nextPointX;
                prevPointY = nextPointY;
                points[pointCount * 2] = prevPointX;
                points[pointCount++ * 2 + 1] = prevPointY;
            }

            if (a % step != 0) {
                nextPointX = x + l;
                nextPointY = y + amp*Mathh.sin(endAngle)/1000;
                line(prevPointX, prevPointY, nextPointX, nextPointY, 1, false);
                prevPointX = nextPointX;
                prevPointY = nextPointY;
                points[2 + pointCount * 2] = prevPointX;
                points[2 + pointCount++ * 2 + 1] = prevPointY;
            }

            // ID, point count, points
            int[] drawingData = new int[2 + pointCount * 2];
            drawingData[0] = DRAWING_DATA_ID_PATH;
            drawingData[1] = pointCount;
            System.arraycopy(points, 0, drawingData, 2, pointCount * 2);
            appendDrawingData(drawingData);
        }
        updateLowestY(y + amp);
    }
    public void arc(int x, int y, int r, int angle, int startAngle) {
        arc(x, y, r, angle, startAngle, 10, 10);
    }
    public void arc(int x, int y, int r, int angle, int startAngle, int kx, int ky) { //k: 10 = 1.0
        // calculated formula. r=20: sn=5,step=72; r=1000: sn=36,step=10
        int step = 10000/(140+r);
        if ((step = step / DETAIL_LEVEL) <= 2) {
            arcSmooth(x, y, r, angle, startAngle, kx, ky);
        }
        step = Mathh.constrain( 10 / DETAIL_LEVEL, step, 72 / DETAIL_LEVEL);

        while (startAngle < 0) {
            startAngle += 360;
        }

        int linesFacing = 0;
        if (angle == 360) {
            linesFacing = 1; // these lines push bodies only in one direction
        }

        int lastAng = 0;
        for(int i = 0; i <= angle - step; i+=step) {
            line(x+Mathh.cos(i+startAngle)*kx*r/10000, y+Mathh.sin(i+startAngle)*ky*r/10000, x+Mathh.cos(i+step+startAngle)*kx*r/10000,y+Mathh.sin(i+step+startAngle)*ky*r/10000, linesFacing, false);
            lastAng = i + step;
        }

        // close the circle if the angle is not multiple of the step (step)
        if (angle % step != 0) {
            line(x+Mathh.cos(lastAng+startAngle)*kx*r/10000, y+Mathh.sin(lastAng+startAngle)*ky*r/10000, x+Mathh.cos(angle+startAngle)*kx*r/10000,y+Mathh.sin(angle+startAngle)*ky*r/10000, linesFacing, false);
        }

        updateLowestY(y + r);

        if (angle == 360 && startAngle == 0 && kx == 10 && ky == 10) {
            appendDrawingData(new int[] {DRAWING_DATA_ID_CIRCLE, x, y, r});
        } else {
            appendDrawingData(new int[] {DRAWING_DATA_ID_ARC, x, y, r, startAngle, angle, kx, ky});
        }
    }

    public void arcSmooth(int x, int y, int r, int angle, int startAngle, int kx, int ky) { //k: 100 = 1.0
        double angleD = Math.PI * angle / 180;
        double startAngleD = Math.PI * startAngle / 180;
        // calculated formula. r=20: sn=5,step=72; r=1000: sn=36,step=10
        double step = Math.PI * 10f/(140+r) / 180 / DETAIL_LEVEL;
        step = Mathh.constrain( Math.PI * 10f / 180 / DETAIL_LEVEL, step, Math.PI * 72f / 180 / DETAIL_LEVEL);

        while (startAngleD < 0) {
            startAngleD += Math.PI;
        }

        int linesFacing = 0;
        if (angle == 360) {
            linesFacing = 1; // these lines push bodies only in one direction
        }

        double lastAng = 0;
        for(double i = 0; i <= angleD - step; i+=step) {
            line((int) (x+Math.cos(i+startAngleD)*kx*r/10), (int) (y+Math.sin(i+startAngleD)*ky*r/10), (int) (x+Math.cos(i+step+startAngleD)*kx*r/10), (int) (y+Math.sin(i+step+startAngleD)*ky*r/10), linesFacing, false);
            lastAng = i + step;
        }

        // close the circle if the angle is not multiple of the step (step)
        if (angleD % step != 0) {
            line((int) (x+Math.cos(lastAng+startAngleD)*kx*r/10), (int) (y+Math.sin(lastAng+startAngleD)*ky*r/10), (int) (x+Math.cos(angleD+startAngleD)*kx*r/10), (int) (y+Math.sin(angleD+startAngleD)*ky*r/10), linesFacing, false);
        }

        updateLowestY(y + r);

        if (angle == 360 && startAngle == 0 && kx == 10 && ky == 10) {
            appendDrawingData(new int[] {DRAWING_DATA_ID_CIRCLE, x, y, r});
        } else {
            appendDrawingData(new int[] {DRAWING_DATA_ID_ARC, x, y, r, startAngle, angle, kx, ky});
        }
    }

    public void line(int x1, int y1, int x2, int y2) {
        line(x1, y1, x2, y2, 0, true);
    }

    public void line1(int x1, int y1, int x2, int y2) {
        line(x1, y1, x2, y2, 1, true);
    }

    public void line(int x1, int y1, int x2, int y2, int facing) {
        line(x1, y1, x2, y2, facing, true);
    }

    public void line(int x1, int y1, int x2, int y2, int facing, boolean saveDrawingData) {
        int dx = x2-x1;
        int dy = y2-y1;
        if (dx == 0 && dy == 0) {
            return;
        }
        landscape.addSegment(FXVector.newVector(x1, y1), FXVector.newVector(x2, y2), (short) facing);
        lineCount++;
        updateLowestY(Math.max(y1, y2));
        if (saveDrawingData) {
            appendDrawingData(new int[] {DRAWING_DATA_ID_LINE, x1, y1, x2, y2});
        }
    }

    private void appendDrawingData(int[] newData) {
        drawingData = WorldGen.concatArrays(drawingData, newData);
    }

    private void updateLowestY(int y) {
        w.lowestY = Math.max(y, w.lowestY);
    }

    public int[] getDrawingData() {
        return drawingData;
    }

    private static int readDetailLevelSetting() {
        try {
            Logger.log("placer: reading settings");
            return MobappGameSettings.getDetailLevel();
        } catch (Throwable ex) {
            ex.printStackTrace();
            return MobappGameSettings.DEFAULT_DETAIL_LEVEL;
        }
    }
}
