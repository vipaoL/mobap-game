/*
 * To change this license header, choose License Headers in Project Properties. // where???????
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mobileapplication3;

import at.emini.physics2D.Body;
import at.emini.physics2D.Constraint;
import at.emini.physics2D.Landscape;
import at.emini.physics2D.Shape;
import at.emini.physics2D.UserData;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import java.util.Random;

/**
 *
 * @author vipaol
 */
public class WorldGen implements Runnable {
    
    int stdStructsNumber = 6;
    int floorWeightInRandom = 4;
    int structLogSize = 7;
    
    private int prevStructRandomId;
    private int nextStructRandomId;
    public boolean isResettingPosition = false;
    private int[][] structLog = new int[structLogSize][];
    private int numberOfLoggedStructs = 0;
    private int ringLogTail = 0;
    
    private int lastX = -8000;
    private int lastY = 0;
    private int lowestY = 0;
    
    public static int zeroPoint = 0;
    private final int POINTS_DIVIDER = 2000;
    private int nextPointsCounterTargetX = lastX + POINTS_DIVIDER;
    
    private int segmentsNum = 36;       // how many lines will draw up a circle
    private int segmentLen = 360 / segmentsNum;
    
    
    private boolean paused = false;
    private boolean needSpeed = true;
    private boolean isReady = true;
    private int tick = 0;
    
    private Random rand;
    private GraphicsWorld w;
    private Landscape lndscp;
    private MgStruct mgStruct;
    
    public WorldGen(GraphicsWorld w) {
        Main.log("wg:constructor");
        this.w = w;
        lndscp = w.getLandscape();
    }
    
    public void run() {
        tick = 1;
        Main.log("wg:run()");
        //placeMGStructByID(10);
        while(MenuCanvas.isWorldgenEnabled) {
            if (!paused) {
                if ((GraphicsWorld.carX + GraphicsWorld.viewField > lastX)) { // 
                    placeNext();
                }
                
                if (numberOfLoggedStructs >= structLog.length) {
                    if (GraphicsWorld.carX > 8000 & (GameplayCanvas.timeFlying > 1 | GameplayCanvas.uninterestingDebug)) {
                        if (!GameplayCanvas.isDrawingNow) {
                            resetPosition();
                        }
                    }
                }
                
                w.refreshPos();
                if (GraphicsWorld.carX > nextPointsCounterTargetX) {
                    nextPointsCounterTargetX += POINTS_DIVIDER;
                    GraphicsWorld.points++;
                }
                
                if (tick <= 10) {
                    tick++;
                } else {
                    tick = 1;
                }
            }
            try {
                if (!needSpeed) {
                    Thread.sleep(200);
                } else {
                    if (tick > 9 & !isResettingPosition) {
                        needSpeed = false;
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        Main.log("wg stopped.");
    }
    
    private void placeNext() {
        int idsCount;
        if (DebugMenu.mgstructOnly) {
            idsCount = mgStruct.structsInBufferNumber;
        } else {
            idsCount = stdStructsNumber + floorWeightInRandom + mgStruct.structsInBufferNumber;;
        }
        while (nextStructRandomId == prevStructRandomId) {
            nextStructRandomId = rand.nextInt(idsCount); // 10: 0-9
        }
        prevStructRandomId = nextStructRandomId;
        if (DebugMenu.mgstructOnly) {
            nextStructRandomId+=stdStructsNumber + floorWeightInRandom;
        }
        
        Main.log("placing: id=", nextStructRandomId);
        if (lastY > 1000 | lastY < -1000) { // will correct height if it is too high or too low
            Main.log("correcting height because lastY=", lastY);
            floor(lastX, lastY, 1000 + rand.nextInt(4) * 100, (rand.nextInt(7) - 3) * 100);
        } else {
            /*
            * 0 - circ1, 1 - sin, 2 - floorStat, 3 - circ2, 4 - abyss,
            * 5 - dotline, 6..9 - floor, 10 - mgstruct0, 11 - mgstruct1,
            * ...
            */
            switch(nextStructRandomId) {
                case 0:
                    circ1(lastX, lastY, 400, 15, 120);
                    break;
                case 1:
                    int l = 720 + rand.nextInt(8) * 180;
                    int amp = 15 + rand.nextInt(15);
                    sin(lastX, lastY, l, l / 180, 0, amp);
                    break;
                case 2:
                    floorStat(lastX, lastY, 400 + rand.nextInt(10) * 100);
                    break;
                case 3:
                    circ2(lastX, lastY, 1000, 20);
                    break;
                case 4:
                    abyss(lastX, lastY, rand.nextInt(6) * 1000);
                    break;
                case 5:
                    int n = rand.nextInt(6) + 5;
                    dotline(lastX, lastY, n);
                    break;
                default:
                    if (Mathh.strictIneq(stdStructsNumber - 1,/*<*/ nextStructRandomId,/*<*/ stdStructsNumber + floorWeightInRandom)) {
                        floor(lastX, lastY, 400 + rand.nextInt(10) * 100, (rand.nextInt(7) - 3) * 100);
                    } else {
                        placeMGStructByRelativeID(nextStructRandomId);
                    }
                    break;
            }
        }
        Main.log("lastX=", lastX);
        lowestY = Math.max(lastY, lowestY);
    }
    
    public void start() {
        isReady = false;
        Main.log("wg:start()");
        rand = new Random();
        Main.log("wg:loading mgstruct");
        mgStruct = new MgStruct();
        restart();
        resume();
        (new Thread(this, "world generator")).start();
    }
    public void pause() {
        Main.log("wg paused");
        paused = true;
    }
    public void resume() {
        Main.log("wg resumed");
        paused = false;
    }
    
    private void restart() {
        isReady = false;
        needSpeed = true;
        Main.log("wg:restart()");
        prevStructRandomId = 1;
        nextStructRandomId = 2;
        lastX = -7900;
        lastY = 0;
        try {
            Main.log("wg:cleaning world");
            cleanWorld();
        } catch (NullPointerException e) {
            
        }
        Main.log("wg:adding start platform");
        line(lastX - 600, lastY - 100, lastX, lastY);
        tick = 2;
        GraphicsWorld.points = 0;
        Main.log("wg:adding car");
        w.addCar();
        Main.log("wg ready.");
        isReady = true;
    }
    private void cleanWorld() {
        GameplayCanvas.paused = true;
        Constraint[] constraints = w.getConstraints();
        while (w.getConstraintCount() > 0) {
            w.removeConstraint(constraints[0]);
        }
        rmAllBodies();
        rmSegs();
        constraints = null;
        GameplayCanvas.paused = false;
    }
    private void rmSegs() {
        while (lndscp.segmentCount() > 0) {
            lndscp.removeSegment(0);
        }
    }
    /*private void rmBodies() {
        Body[] bodies = w.getBodies();
        int to = w.getBodyCount();
        for (int i = to - 1; i >= 0; i--) {
            if (bodies[i] != w.carbody & bodies[i] != w.leftwheel & bodies[i] != w.rightwheel)
            w.removeBody(bodies[i]);
        }
        bodies = null;
    }*/
    private void rmAllBodies() {
        Body[] bodies = w.getBodies();
        while (w.getBodyCount() > 0) {
            w.removeBody(bodies[0]);
        }
        bodies = null;
    }
    public boolean isReady() {
        return isReady;
    }
    public int getLowestY() {
        return lowestY;
    }
    
    private void structlogger(int[] a) {
        if (!isResettingPosition) {
            if (numberOfLoggedStructs < structLog.length) {
                numberOfLoggedStructs++;
            }
            
            int index = (ringLogTail) % structLog.length;
            structLog[index] = a;
            //Main.log(structLog[index][0]);
            //Main.log(index);
            if (ringLogTail >= structLog.length - 1) {
                ringLogTail = 0;
            } else {
                ringLogTail++;
            }
        }
    }
    
    private int[] structlog_getElementAt(int i) {
        return structLog[(i+ringLogTail)%structLog.length];
    }
    
    private void resetPosition() { // world cycling
        isReady = false;
        isResettingPosition = true;
        needSpeed = true;
        int prevLastX = lastX;
        lastX = -14000;
        
        Main.log("resetting pos");
        
        rmSegs();
        reproduce();
        
        moveBodies(lastX - prevLastX);
        zeroPoint = w.carbody.positionFX().xAsInt();
        
        nextPointsCounterTargetX -= (prevLastX - lastX);

        isResettingPosition = false;
        needSpeed = false;
        isReady = true;
    }
    
    private void reproduce() {
        Main.log("to reproduce: ", numberOfLoggedStructs);
        for (int i = 0; i < numberOfLoggedStructs; i++) {
            int[] struct = structlog_getElementAt(i);
            int structID = struct[0];

            int y = struct[2];
            lastY = y;
            if (structID == 0) {
                int r = struct[3];
                int sn = struct[4];
                int va = struct[5];
                circ1(lastX, y, r, sn, va);
                Main.log("placing circ1");
            }
            if (structID == 1) {
                int l = struct[3];
                floorStat(lastX, y, l);
                Main.log("placing flStat");
            }
            if (structID == 2) { // {2, l, y}
                int l = struct[3];
                abyss(lastX, y, l);
                Main.log("placing abyss");
            }
            if (structID == 3) {
                int l = struct[3];
                int halfperiods = struct[4];
                int offset = struct[5];
                int amp = struct[6];
                sin(lastX, y, l, halfperiods, offset, amp);
                Main.log("placing sin");
            }
            if (structID == 4) {
                int r = struct[3];
                int sn = struct[4];
                circ2(lastX, y, r, sn);
                Main.log("placing circ2");
            }
            if (structID == 5) {
                int n = struct[3];
                dotline(lastX, y, n);
                Main.log("placing dotline");
            }
            if (structID >= stdStructsNumber + floorWeightInRandom) {
                placeMGStructByRelativeID(structID);
            }
        }
    }
    
    private void moveBodies(int dx) {
        Body[] bodies = w.getBodies();
        for (int i = 0; i < w.getBodyCount(); i++) {
            bodies[i].translate(FXVector.newVector(dx, 0), 0);
        }
    }
    
    
    
    
    
    void placeMGStructByRelativeID(int relID) {
        int id = relID - floorWeightInRandom - stdStructsNumber;
        if (!isResettingPosition) {
            int[] log = {relID, lastX, lastY};
            structlogger(log);
        }
        placeMGStructByID(id);
    }
    
    void placeMGStructByID(int id) {
        short[][] data = mgStruct.structBuffer[id];
        if (mgStruct.structSizes[id] < 1) {
            Main.log("placing mgstruct cancelled, length=", mgStruct.structSizes[id]);
            return;
        }
        Main.log("placing mgstruct, id=", id);
        for (int i = 1; i < mgStruct.structSizes[id]; i++) {
            placePrimitive(data[i]);
        }
        lastX+=data[0][1];
        lastY+=data[0][2];
    }
    
    void placePrimitive(short[] data) {
        short id = data[0];
        //Main.print("placing element, id=", id);
        if (id == 2) {
            line(data[1] + lastX, data[2] + lastY, data[3] + lastX, data[4] + lastY);
        } else if (id == 3) {
            arc(data[1]+lastX, data[2]+lastY, data[3], data[4], data[5], data[6] / 10, data[7] / 10);
        } else if (id == 4 & !isResettingPosition) {
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
            int n = (l + spacing) / (platfL+spacing);
            
            Shape rect = Shape.createRectangle(platfL, platfH);
            rect.setMass(1);
            rect.setFriction(0);
            rect.setElasticity(50);
            dx/=(l/platfL);
            int spX = spacing * dx / l;
            dy/=(l/platfL);
            int spY = spacing * dy / l;
            int offsetX = platfL/2 * Mathh.cos(ang) / 1000;
            int offsetY = platfL/2 * Mathh.sin(ang) / 1000;
            
            for (int i = 0; i < n; i++) {
                Body fallinPlatf = new Body(lastX + x1 + i*(dx+spX) + offsetX, lastY + y1 + i*(dy+spY) + offsetY, rect, false);
                fallinPlatf.setRotation2FX(FXUtil.TWO_PI_2FX / 360 * ang);
                //UserData mUserData = new MUserData(MUserData.TYPE_ACCELERATOR, new short[] {/*GameplayCanvas.EFFECT_SPEED, 10, */105, 100});
                //fallinPlatf.setUserData(mUserData);
                fallinPlatf.setUserData(new MUserData(MUserData.TYPE_FALLING_PLATFORM, new short[] {20}));
                w.addBody(fallinPlatf);
                //Main.log(((MUserData) fallinPlatf.getUserData()).bodyType);
            }
            
        } else if (id == 5) {
            arc(data[1]+lastX, data[2]+lastY, data[3], 360, 0);
        } else if (id == 6) {
            sin(lastX + data[1], lastY + data[2], data[3], data[4], data[5], data[6]);
        } else if (id == 7) {
            int x = lastX + data[1];
            int y = lastY + data[2];
            int l = data[3];
            int h = data[4];
            int ang = data[5];
            
            short effectID = GameplayCanvas.EFFECT_SPEED;
            short effectDuration = data[8];
            short directionOffset = data[6];
            short speedMultipiler = data[7];
            
            int centerX = x + l * Mathh.cos(ang) / 2000;
            int centerY = y + l * Mathh.sin(ang) / 2000;
            
            int colorModifier = (speedMultipiler - 100) * 3;
            int red = Math.min(255, Math.max(0, colorModifier));
            int blue = Math.min(255, Math.max(0, -colorModifier));
            if (red < 50 & blue < 50) {
                red = 50;
                blue = 50;
            }
            
            String redStr = Integer.toHexString(red);
            while (redStr.length() < 2) {                
                redStr = "0" + redStr;
            }
            
            String blueStr = Integer.toHexString(blue);
            while (blueStr.length() < 2) {                
                blueStr = "0" + blueStr;
            }
            
            String greenStr = blueStr;
            
            String colorStr = redStr + greenStr + blueStr;
            int color = Integer.parseInt(colorStr, 16);
            
            Shape plate = Shape.createRectangle(l, h);
            Body pressurePlate = new Body(centerX, centerY, plate, false);
            UserData mUserData = new MUserData(MUserData.TYPE_ACCELERATOR, new short[] {effectID, effectDuration, directionOffset, speedMultipiler});
            ((MUserData) mUserData).color = color;
            pressurePlate.setUserData(mUserData);
            //Main.log(((MUserData) pressurePlate.getUserData()).bodyType);
            pressurePlate.setRotation2FX(FXUtil.TWO_PI_2FX / 360 * ang);
            w.addBody(pressurePlate);
        }
        
        
        
        /*for (int i = f0off+sl/2; i < 60; i+=sl) {
            Body fallinPlatf = new Body(x+r+Mathh.cos(i+f0off)*(r+platfHeight/2)/1000, y-r+Mathh.sin(i+f0off)*(r+platfHeight/2)/1000, rect, true);
            fallinPlatf.setDynamic(false);
            fallinPlatf.setRotationDeg(i+f0off-90);
            w.addBody(fallinPlatf);
        }
        rect = Shape.createRectangle(l2, platfHeight);
        rect.setMass(1);
        rect.setFriction(0);
        rect.setElasticity(50);
        */
        
        
    }
    
        
    /*******************************************
     * 
     * 
     * 
     * STRUCTURES AND PRIMITIVES               *
     * 
     *******************************************/
    
    
    
    
    private void circ1(int x, int y, int r, int sn, int va) { // 0
        int[] h = {0, x, y, r, sn, va};
        
        x+=r;
        int r2 = r*3/2;
        
        arc(x-r, y-r2, r2, 60, 30);
        arc(x+r/2, y-r*2, r, 300, va);
        int ofs = (1000 - Mathh.cos(30))*2*r2/1000;
        arc(x+r*2-ofs, y-r2, r2, 60, 90);
        
        int l = r2+r2-ofs;
        lastX += l;
        if (!isResettingPosition) {
            h[1] = l;
            structlogger(h);
        }
    }
    
    private void circ2(int x, int y, int r, int sn) { // 4
        int[] h = {4, x, y, r, sn};
        int sl = 360 / sn;
        
        int f0off = 30;
        int va = 60 + f0off;
        int ang = 360 - va;
        
        int offset = va + 90 - f0off;
        
        arc(x+r, y-r, r, ang, offset);
        int r2 = r/10*8;
        line(x, y, x+r-r2, y);
        arc(x+r, y, r2, 90, 90, 10, 5);
        line(x+r, y+r2/2, x+2*r, y+r2/2);
        lastY += r2/2;
        
        if (!isResettingPosition) {
            int l2 = (r2) / 2;

            int platfLength = 2*sl*r*3141/1000/360;
            int platfHeight = r/100;

            Shape rect = Shape.createRectangle(platfLength, platfHeight);
            rect.setMass(1);
            rect.setFriction(0);
            rect.setElasticity(50);

            for (int i = f0off+sl/2; i < 60; i+=sl) {
                Body fallinPlatf = new Body(x+r+Mathh.cos(i+f0off)*(r+platfHeight/2)/1000, y-r+Mathh.sin(i+f0off)*(r+platfHeight/2)/1000, rect, true);
                fallinPlatf.setDynamic(false);
                fallinPlatf.setRotationDeg(i+f0off-90);
                w.addBody(fallinPlatf);
            }
            rect = Shape.createRectangle(l2, platfHeight);
            rect.setMass(1);
            rect.setFriction(0);
            rect.setElasticity(50);
            for (int i = 0; i < 2; i++) {
                Body fallinPlatf = new Body(x+r-r2+i*l2+l2/2, y+platfHeight/2, rect, true);
                fallinPlatf.setUserData(new MUserData(MUserData.TYPE_FALLING_PLATFORM, new short[] {20}));
                fallinPlatf.setDynamic(false);
                w.addBody(fallinPlatf);
            }
        }
        
        int l = r+r;
        lastX += l;
        if (!isResettingPosition) {
            h[1] = l;
            structlogger(h);
        }
    }
    
    private void floor(int x, int y, int l, int y2) {
        int amp = (y2 - y) / 2;
        sin(x, y + amp, l, 1, 270, amp);
    }
    private void floorStat(int x, int y, int l) {      // 1
        if (!isResettingPosition) {
            int[] h = {1, l, y, l};
            structlogger(h);
        }
        line1(x, y, x + l, y);
        lastX += l;
    }
    private void abyss(int x, int y, int l) {
        if (!isResettingPosition) {
            int[] h = {2, x, y, l};
            structlogger(h);
        }
        int prLength = 1000;
        line(x, y, x + prLength, y);
        lastX += prLength;
        x += prLength;
        int ang = 60; // springboard angle
        int r = l / 8;
        arc(x, y-r, r, ang, 90 - ang, 15, 10);
        line(x+l - l / 5, y - r * Mathh.cos(ang) / 1000, x+l, y - r * Mathh.cos(ang) / 1000);
        lastX += l;
        lastY -= r * Mathh.cos(ang) / 1000;
    }
    private void dotline(int x, int y, int n) {
        if (!isResettingPosition) {
            int[] h = {5, x, y, n};
            structlogger(h);
        }
        int offsetL = 600;
        for (int i = 0; i < n; i++) {
            line(x + i*offsetL, y + i * 300/n, x + i*offsetL + 300, y + i * 300/n - 300);
        }
        lastX += n * offsetL;
    }
    
    
    
    
    private void sin(int x, int y, int l, int halfperiods, int offset, int amp) {    //3
        if (!isResettingPosition) {
            int[] h = {3, l, y, l, halfperiods, offset, amp};
            structlogger(h);
        }
        if (amp == 0) {
            line(x, y, x + l, y);
        } else {
            int sn = l/20;
            int sl = l/sn;
            for(int i = sl; i <= l; i+=sl) {
                line1(x + (i-sl), y + amp * Mathh.sin(180*(i-sl)/l*halfperiods+offset) / 1000, x + i, y + amp*Mathh.sin(180*i/l*halfperiods+offset)/1000);
            }
            lastY = y + Mathh.sin(offset+180*halfperiods) * amp / 1000;
        }
        lastX += l;
    }
    private void arc(int x, int y, int r, int ang, int of) {
        while (of < 0) {
            of += 360;
        }
        
        int lastAng = 0;
        for(int i = 0; i <= ang - segmentLen; i+=segmentLen) {
            line(x+Mathh.cos(i+of)*r/1000, y+Mathh.sin(i+of)*r/1000, x+Mathh.cos(i+segmentLen+of)*r/1000,y+Mathh.sin(i+segmentLen+of)*r/1000);
            lastAng = i + segmentLen;
        }
        
        if (ang % segmentLen != 0) {
            line(x+Mathh.cos(lastAng+of)*r/1000, y+Mathh.sin(lastAng+of)*r/1000, x+Mathh.cos(ang+of)*r/1000,y+Mathh.sin(ang+of)*r/1000);
        }
    }
    private void arc(int x, int y, int r, int ang, int of, int kx, int ky) { //k: 10 = 1.0
        while (of < 0) {
            of += 360;
        }
        
        int lastAng = 0;
        for(int i = 0; i <= ang - segmentLen; i+=segmentLen) {
            line(x+Mathh.cos(i+of)*kx*r/10000, y+Mathh.sin(i+of)*ky*r/10000, x+Mathh.cos(i+segmentLen+of)*kx*r/10000,y+Mathh.sin(i+segmentLen+of)*ky*r/10000);
            lastAng = i + segmentLen;
        }
        
        if (ang % segmentLen != 0) {
            line(x+Mathh.cos(lastAng+of)*kx*r/10000, y+Mathh.sin(lastAng+of)*ky*r/10000, x+Mathh.cos(ang+of)*kx*r/10000,y+Mathh.sin(ang+of)*ky*r/10000);
        }
    }
    /*int*/void line(int x1, int y1, int x2, int y2) {
        lndscp.addSegment(FXVector.newVector(x1, y1), FXVector.newVector(x2, y2), (short) 0);
        //return x2 - x1;
        //waitinForDel.addElement(new Integer(1));
    }
    private void line1(int x1, int y1, int x2, int y2) {
        lndscp.addSegment(FXVector.newVector(x1, y1), FXVector.newVector(x2, y2), (short) 0);
    }
}