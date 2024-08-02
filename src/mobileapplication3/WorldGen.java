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
import utils.Mathh;
import utils.MgStruct;

import java.util.Random;

/**
 *
 * @author vipaol
 */
public class WorldGen implements Runnable {
    
    public static boolean isEnabled = false;
    
    int stdStructsNumber = 6;
    int floorWeightInRandom = 4;
    
    private int prevStructRandomId;
    private int nextStructRandomId;
    public boolean isResettingPosition = false;
    
    private int lastX;
    private int lastY;
    private int lowestY;
    
    public static int barrierX = Short.MIN_VALUE;
    public static int bgZeroPoint = 0;
    private final int POINTS_DIVIDER = 2000;
    private int nextPointsCounterTargetX;
    int tick = 0;
    public static int mspt;
    
    private final int SEGMENTS_IN_CIRCLE = 36;       // how many lines will draw up a circle
    private final int CIRCLE_SEGMENT_LEN = 360 / SEGMENTS_IN_CIRCLE;
    
    
    private boolean paused = false;
    private boolean needSpeed = true;
    private int lock = 0;
    private boolean gameTrLockedByAdding = false;
    
    private Random rand;
    private GraphicsWorld w;
    private Landscape lndscp;
    private MgStruct mgStruct;
    private StructLog structlogger;
    
    // counter
    private int linesInStructure = 0;
    
    // wg activity indicator
    public static int currStep;
    public static final int STEP_IDLE = 0;
    public static final int STEP_ADD = 1;
    public static final int STEP_RES_POS = 2;
    public static final int STEP_CLEAN_SGS = 3;
    
    
    public WorldGen(GraphicsWorld w) {
        lock = 0;
        Logger.log("wg:starting");
        lockGameThread("init");
        this.w = w;
        lndscp = w.getLandscape();
        Logger.log("wg:start()");
        rand = new Random();
        Logger.log("wg:loading mgstruct");
        mgStruct = new MgStruct();
        reset();
        unlockGameThread("init");
        (new Thread(this, "world generator")).start();
    }
    
    public void run() {
        Logger.log("wg:run()");
        while(isEnabled) {
            try {
                long startTime = System.currentTimeMillis();
                tick();
                mspt = (int) (System.currentTimeMillis() - startTime);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }
        Logger.log("wg stopped.");
    }
    
    public void tick() {
        if (!paused || needSpeed) {
            w.refreshPos();
            
            if ((GraphicsWorld.carX + GraphicsWorld.viewField*2 > lastX)) {
                if ((GraphicsWorld.carX + GraphicsWorld.viewField > lastX)) {
                    needSpeed = true;
                    if (!gameTrLockedByAdding) {
                        lockGameThread("addSt");
                    }
                    gameTrLockedByAdding = true;
                    Logger.log("wg can't keep up, waiting;");
                }
                currStep = STEP_ADD;
                placeNext();
            } else {
                if (gameTrLockedByAdding) {
                    gameTrLockedByAdding = false;
                    unlockGameThread("addSt");
                }
                if (!structlogger.shouldRmFirstStruct()) {
                    needSpeed = false;
                }
            }
            
            if (tick == 0) {
                currStep = STEP_RES_POS;
                // World cycling
                //(the physics engine is working weird when the coordinate reaches around 10000
                //  then we need to move all structures and bodies to the left when the car is to the right of 3000)
                if (GraphicsWorld.carX > 3000 && (GameplayCanvas.timeFlying > -1 || GameplayCanvas.uninterestingDebug)) {
                    resetPosition();
                }

                w.refreshPos();
                if (GraphicsWorld.carX > nextPointsCounterTargetX) {
                    nextPointsCounterTargetX += POINTS_DIVIDER;
                    GameplayCanvas.points++;
                }
            }

            currStep = STEP_CLEAN_SGS;
            structlogger.rmFarStructures();
        }
        currStep = STEP_IDLE;
        tick++;
        if (tick >= 10) {
            tick = 0;
        }
        try {
            if (!needSpeed) {
                Thread.sleep(20);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    private void placeNext() {
        int idsCount;
        if (DebugMenu.mgstructOnly) {
            idsCount = mgStruct.loadedStructsNumber;
        } else {
            idsCount = stdStructsNumber + floorWeightInRandom + mgStruct.loadedStructsNumber;
        }
        while (nextStructRandomId == prevStructRandomId
                || (DebugMenu.whatTheGame && (nextStructRandomId < 6 || nextStructRandomId > 9))) {
            nextStructRandomId = rand.nextInt(idsCount); // 10: 0-9
        }
        //nextStructRandomId = 1;
        prevStructRandomId = nextStructRandomId;
        if (DebugMenu.mgstructOnly) {
            nextStructRandomId+=stdStructsNumber + floorWeightInRandom;
        }
        
        if (lastY > 1000 | lastY < -1000) { // will correct height if it is too high or too low
            Logger.log("correcting height because lastY=", lastY);
            floor(lastX, lastY, 1000 + rand.nextInt(4) * 100, (rand.nextInt(7) - 3) * 100);
        } else {
            Logger.log("placing: id=", nextStructRandomId);
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
                    int amp = 15;
                    sinStruct(lastX, lastY, l, l / 180, 0, amp);
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
        Logger.log("lastX=", lastX);
        lowestY = Math.max(lastY, lowestY);
    }
    
    public void pause() {
        Logger.log("wg pause");
        needSpeed = true;
        paused = true;
    }
    
    public void resume() {
        Logger.log("wg resume");
        paused = false;
    }
    
    private void reset() {
        needSpeed = true;
        Logger.log("wg:restart()");
        prevStructRandomId = 1;
        nextStructRandomId = 2;
        lastX = -2900;
        nextPointsCounterTargetX = lastX + POINTS_DIVIDER;
        lastY = 0;
        try {
            Logger.log("wg:cleaning world");
            cleanWorld();
        } catch (NullPointerException e) {
            
        }
        
        structlogger = new StructLog(10);
        
        // start platform
        line(lastX - 600, lastY - 100, lastX, lastY);
        structlogger.add(lastX, linesInStructure);
    }
    private void cleanWorld() {
        lockGameThread("clnW");
        Constraint[] constraints = w.getConstraints();
        while (w.getConstraintCount() > 0) {
            w.removeConstraint(constraints[0]);
        }
        rmAllBodies();
        rmSegs();
        constraints = null;
        unlockGameThread("clnW");
    }
    private void rmSegs() {
        while (lndscp.segmentCount() > 0) {
            lndscp.removeSegment(0);
        }
    }
    
    public void lockGameThread(String where) {
        String msg = "locking by " + where;
        Logger.log(msg);
        lock++;
        GameplayCanvas.shouldWait = true;
        if (GameplayCanvas.isBusy) {
            while (!GameplayCanvas.isWaiting && GameplayCanvas.isBusy) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        Logger.logReplaceLast(msg, "locked by " + where);
    }
    
    public void unlockGameThread(String where) {
        Logger.log("unlocking by " + where);
        lock--;
        if (lock <= 0) {
            GameplayCanvas.shouldWait = false;
        } else {
            Logger.log("not unlocking: " + lock);
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
    public int getLowestY() {
        return lowestY;
    }
    public int getSegmentCount() {
        try {
            return lndscp.segmentCount();
        } catch(NullPointerException ex) {
            return 0;
        }
    }
    
    private void resetPosition() { // world cycling
        lockGameThread("rsPos");
        isResettingPosition = true;
        
        int dx = -3000 - w.carbody.positionFX().xAsInt();
        lastX = lastX + dx;
        
        Logger.log("resetting pos");
        
        moveLandscape(dx);
        moveBodies(dx);
        structlogger.moveXAllElements(dx);
        barrierX += dx;
        bgZeroPoint += dx;
        
        nextPointsCounterTargetX += dx;

        isResettingPosition = false;
        unlockGameThread("rsPos");
    }
    
    private void moveLandscape(int dx) {
        movePoints(lndscp.elementStartPoints(), dx);
        movePoints(lndscp.elementEndPoints(), dx);
    }
    
    private void movePoints(FXVector[] points, int dx) {
        try {
            for (int i = 0; i < lndscp.segmentCount(); i++) {
                FXVector point = points[i];
                points[i] = new FXVector(point.xFX + FXUtil.toFX(dx), point.yFX);
            }
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
    }
    
    private void moveBodies(int dx) {
        Body[] bodies = w.getBodies();
        for (int i = 0; i < w.getBodyCount(); i++) {
            bodies[i].translate(FXVector.newVector(dx, 0), 0);
        }
    }
    
    private class StructLog {
        public static final int MAX_DIST_TO_RM_STRUCT = 4000;
        public static final int MAX_DIST_TO_RM_STRUCT_SIMUL = 300;
        private short[][] structLog;
        private int numberOfLoggedStructs = 0;
        private int ringLogStart = 0;
        private boolean isLeftBarrierAdded = false;
        
        public StructLog(int structLogSize) {
            structLog = new short[structLogSize][];
        }
        
        public void add(int endX, int segsNumber) {
            //Main.log("strL:add "+endX+" "+segsNumber);
            linesInStructure = 0;
            
            if (numberOfLoggedStructs >= structLog.length) {
                int ns = structLog.length+1;
                Logger.log("strcLog is too small. to " + ns);
                increase(ns);
            }
            
            short[] a = {(short) endX, (short) segsNumber};
            int nextID = (ringLogStart + numberOfLoggedStructs) % structLog.length;
            Logger.log("logging struct, to " + nextID);
            structLog[nextID] = a;
            
            numberOfLoggedStructs++;
        }
        
        public void increase(int newSize) {
            if (newSize < structLog.length) {
                throw new IllegalArgumentException("newSize can't be less than current size");
            }
            short[][] tmp = structLog;
            structLog = new short[newSize][];
            // copying from start of ringlog to end of array
            System.arraycopy(tmp, ringLogStart, structLog, 0, tmp.length - ringLogStart);
            // copying from start of array to tail of ringlog
            System.arraycopy(tmp, 0, structLog, tmp.length - ringLogStart, ringLogStart);
            ringLogStart = 0;
        }

        public short[] getElementAt(int i) {
            int id = getElementID(i);
            return structLog[id];
        }
        
        public int getElementID(int i) {
            return (ringLogStart+i)%structLog.length;
        }
        
        public int getNumberOfLogged() {
            return numberOfLoggedStructs;
        }
        
        public int getSize() {
            return structLog.length;
        }
        
        public boolean isFull() {
            return getNumberOfLogged() >= getSize();
        }
        
        public void rmFirstElement() {
            ringLogStart = (ringLogStart + 1) % structLog.length;
            numberOfLoggedStructs--;
        }
        
        public void moveXAllElements(int dx) {
            for (int i = 0; i < getSize(); i++) {
                if (structLog[i] == null) return;
                structLog[i][0] = (short) (structLog[i][0] + ((short) dx));
            }
        }
        public void rmFarStructures() {
            if (shouldRmFirstStruct()) {
                
                // add a barrier to the left world border
                if (!isLeftBarrierAdded) {
                    barrierX = structLog[getElementID(0)][0];
                    Logger.log("adding a barrier at " + barrierX);
                    lndscp.addSegment(FXVector.newVector(barrierX, -10000), FXVector.newVector(barrierX, 10000), (short) 1);
                    structLog[getElementID(1)][1] += 1;
                    isLeftBarrierAdded = true;
                }
                
                int deletedSegs = 0;
                for (int i = 0; i < Math.min(getElementAt(0)[1], 3); i++) {
                    lndscp.removeSegment(0);
                    deletedSegs++;
                }
                int id = getElementID(0);
                structLog[id][1] = (short) (structLog[id][1] - ((short) deletedSegs));

                // if 0 segments left, then the structure was deleted completely.
                // Deleting it from log
                if (getElementAt(0)[1] == 0) {
                    isLeftBarrierAdded = false;
                    rmFirstElement();
                    
                    /* broken code, unused
                    // check if there are a concatenated line partially behind the barrier
                    FXVector prevLineStartPoint = lndscp.elementStartPoints()[0];
                    if (prevLineStartPoint.xAsInt() < barrierX) {
                        lndscp.elementStartPoints()[0] = FXVector.newVector(barrierX, prevLineStartPoint.yAsInt());
                    }*/
                }
            }
        }
    
        public boolean shouldRmFirstStruct() {
            int maxDistToRemove = MAX_DIST_TO_RM_STRUCT;
            if (DebugMenu.simulationMode) {
                maxDistToRemove = MAX_DIST_TO_RM_STRUCT_SIMUL;
            }
            try {
                if (getNumberOfLogged() > 0) {
                    return GraphicsWorld.carX - getElementAt(0)[0] > maxDistToRemove;
                } else
                    return false;
            } catch(NullPointerException ex) {
                Logger.enableOnScreenLog(Main.sHeight);
                Logger.log(ex.toString());
                Logger.log("structLog:critical err");
                ex.printStackTrace();
                return false;
            }
        }
    }
    
    
    void placeMGStructByRelativeID(int relID) {
        int id = relID - floorWeightInRandom - stdStructsNumber;
        placeMGStructByID(id);
    }
    
    void placeMGStructByID(int id) {
        short[][] data = mgStruct.structStorage[id];
        if (data.length < 1) {
            Logger.log("placing mgstruct cancelled, length=", data.length);
            return;
        }
        Logger.log("placing mgstruct, id=", id);
        for (int i = 1; i < data.length; i++) {
            placePrimitive(data[i]);
        }
        lastX+=data[0][1];
        lastY+=data[0][2];
        structlogger.add(lastX, linesInStructure);
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
            rect.setFriction(10);
            rect.setElasticity(0);
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
    }
    
        
    /*******************************************
     * 
     * 
     * 
     * STRUCTURES AND PRIMITIVES               *
     * 
     *******************************************/
    
    
    
    
    private void circ1(int x, int y, int r, int sn, int va) { // 0
        x+=r;
        int r2 = r*3/2;
        
        arc(x-r, y-r2, r2, 60, 30);
        arc(x+r/2, y-r*2, r, 300, va);
        int ofs = (1000 - Mathh.cos(30))*2*r2/1000;
        arc(x+r*2-ofs, y-r2, r2, 60, 90);
        
        int l = r2+r2-ofs;
        lastX += l;
        structlogger.add(lastX, linesInStructure);
    }
    
    private void circ2(int x, int y, int r, int sn) { // 4
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
            rect.setFriction(10);
            rect.setElasticity(0);
            for (int i = 0; i < 2; i++) {
                Body fallinPlatf = new Body(x+r-r2+i*l2+l2/2, y+platfHeight/2, rect, true);
                fallinPlatf.setUserData(new MUserData(MUserData.TYPE_FALLING_PLATFORM, new short[] {20}));
                fallinPlatf.setDynamic(false);
                w.addBody(fallinPlatf);
            }
        }
        
        int l = r+r;
        lastX += l;
        structlogger.add(lastX, linesInStructure);
    }
    
    private void floor(int x, int y, int l, int y2) {
        int amp = (y2 - y) / 2;
        sinStruct(x, y + amp, l, 1, 270, amp);
    }
    private void sinStruct(int x, int y, int l, int halfperiods, int offset, int amp) {    //3
        sin(x, y, l, halfperiods, offset, amp);
        lastX += l;
        if (amp != 0) {
            lastY = y + amp*Mathh.sin(180*halfperiods+offset)/1000;
        }
        structlogger.add(lastX, linesInStructure);
    }
    private void floorStat(int x, int y, int l) {      // 1
        line1(x, y, x + l, y);
        lastX += l;
        structlogger.add(lastX, linesInStructure);
    }
    private void abyss(int x, int y, int l) {
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
        structlogger.add(lastX, linesInStructure);
    }
    private void dotline(int x, int y, int n) {
        int offsetL = 600;
        for (int i = 0; i < n; i++) {
            line(x + i*offsetL, y + i * 300/n, x + i*offsetL + 300, y + i * 300/n - 300);
        }
        lastX += n * offsetL;
        structlogger.add(lastX, linesInStructure);
    }
    
    private void sin(int x, int y, int l, int halfperiods, int offset, int amp) {    //3
        if (amp == 0) {
            line(x, y, x + l, y);
        } else {
            int step = 30;
            int startA = offset;
            int endA = offset + halfperiods * 180;
            int a = endA - startA;
            
            int prevPointX = x;
            int prevPointY = y + amp * Mathh.sin(offset) / 1000;
            int nextPointX;
            int nextPointY;
            
            for (int i = startA; i <= endA; i+=30) {
                nextPointX = x + (i - startA)*l/a;
                nextPointY = y + amp*Mathh.sin(i)/1000;
                line1(prevPointX, prevPointY, nextPointX, nextPointY);
                prevPointX = nextPointX;
                prevPointY = nextPointY;
            }
            
            if (a % step != 0) {
                nextPointX = x + l;
                nextPointY = y + amp*Mathh.sin(endA)/1000;
                line1(prevPointX, prevPointY, nextPointX, nextPointY);
            }
        }
    }
    private void arc(int x, int y, int r, int ang, int of) {
        arc(x, y, r, ang, of, 10, 10);
    }
    private void arc(int x, int y, int r, int ang, int of, int kx, int ky) { //k: 100 = 1.0
        // calculated formula. r=20: sn=5,sl=72; r=1000: sn=36,sl=10
        int sl=10000/(140+r);
        sl = Math.min(72, Math.max(10, sl));
        
        while (of < 0) {
            of += 360;
        }
        
        int linesFacing = 0;
        if (ang == 360) {
            linesFacing = 1;
        }
        
        int lastAng = 0;
        for(int i = 0; i <= ang - sl; i+=sl) {
            line(x+Mathh.cos(i+of)*kx*r/10000, y+Mathh.sin(i+of)*ky*r/10000, x+Mathh.cos(i+sl+of)*kx*r/10000,y+Mathh.sin(i+sl+of)*ky*r/10000, linesFacing);
            lastAng = i + sl;
        }
        
        if (ang % sl != 0) {
            line(x+Mathh.cos(lastAng+of)*kx*r/10000, y+Mathh.sin(lastAng+of)*ky*r/10000, x+Mathh.cos(ang+of)*kx*r/10000,y+Mathh.sin(ang+of)*ky*r/10000, linesFacing);
        }
    }
    private /*int*/void line(int x1, int y1, int x2, int y2) {
        line(x1, y1, x2, y2, 0);
    }
    private void line1(int x1, int y1, int x2, int y2) {
        line(x1, y1, x2, y2, 1);
    }
    //int prevLineK = Integer.MIN_VALUE;
    private void line(int x1, int y1, int x2, int y2, int facing) {
        //x1 += 1;
        //System.out.println(x1 + " " + x2);
        int dx = x2-x1;
        int dy = y2-y1;
        if (dx == 0 && dy == 0) {
            return;
        }
        
        /*
        * experimental optimization. instead of adding a new line with same
        * tilt angle, move end point of previous line.
        * It is buggy when it concatenates a line with line from
        * another (previous) structure. That's why I disabled it
        *
        int lineK;
        if (dx != 0) {
            lineK = 1000*dy/dx; // TODO: experiment with "1000"
        } else {
            lineK = Integer.MIN_VALUE;
        }
        if (lineK == prevLineK) {
            int prevLineEndPointID = lndscp.segmentCount()-1;
            int prevLineEndPointX = lndscp.elementEndPoints()[prevLineEndPointID].xAsInt();
            int prevLineEndPointY = lndscp.elementEndPoints()[prevLineEndPointID].yAsInt();
            if (x1 == prevLineEndPointX && y1 == prevLineEndPointY) {
                lndscp.elementEndPoints()[prevLineEndPointID] = FXVector.newVector(x2, y2);
                int prevStructID = structlogger.getElementID(structlogger.getNumberOfLogged() - 1);
                structlogger.structLog[prevStructID][1] -= 1;
            }
        } else {*/
            lndscp.addSegment(FXVector.newVector(x1, y1), FXVector.newVector(x2, y2), (short) facing);
            /*prevLineK = lineK;
        }*/
        linesInStructure++;
    }
    
}