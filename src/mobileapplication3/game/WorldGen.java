/*
 * To change this license header, choose License Headers in Project Properties. // where???????
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mobileapplication3.game;

import java.util.Random;
import java.util.Vector;

import at.emini.physics2D.Body;
import at.emini.physics2D.Constraint;
import at.emini.physics2D.Landscape;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Mathh;
import utils.MgStruct;

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
    private boolean isResettingPosition = false;
    private Vector deferredStructures = null;
    
    private int lastX;
    private int lastY;
    private final int POINTS_DIVIDER = 2000;
    private int nextPointsCounterTargetX;
    int tick = 0;
    public static int mspt;
    
    private boolean paused = false;
    private boolean needSpeed = true;
    private int lock;
    private boolean gameTrLockedByAdding = false;
    
    private final Random rand;
    private final GraphicsWorld w;
    private final Landscape landscape;
    private StructLog structLogger;
    private Thread wgThread = null;
    
    // wg activity indicator
    public static int currStep;
    public static final int STEP_IDLE = 0;
    public static final int STEP_ADD = 1;
    public static final int STEP_RES_POS = 2;
    public static final int STEP_CLEAN_SGS = 3;
    
    
    public WorldGen(GraphicsWorld w) {
        lock = 0;
        w.lowestY = 2000;
        Logger.log("wg:starting");
        lockGameThread("init");
        this.w = w;
        landscape = w.getLandscape();
        Logger.log("wg:start()");
        rand = new Random();
        Logger.log("wg:loading mgstruct");
        new MgStruct();
        reset();
        unlockGameThread("init");
        wgThread = new Thread(this, "wg");
        wgThread.start();
    }

    public void addDeferredStructure(short[][] structureData) {
        if (deferredStructures == null) {
            deferredStructures = new Vector();
        }
        deferredStructures.addElement(structureData);
    }
    
    public void run() {
        Logger.log("wg:run()");
        while(isEnabled) {
            try {
                tick();
            } catch (NullPointerException ex) {
                Logger.log(ex);
            }
        }
        Logger.log("wg stopped.");
    }
    
    public void tick() {
    	long startTime = System.currentTimeMillis();
        if (!paused || needSpeed) {
            w.refreshCarPos();
            
            if ((w.carX + w.viewField*2 > lastX)) {
                if ((w.carX + w.viewField > lastX)) {
                    needSpeed = true;
                    if (!gameTrLockedByAdding) {
                        lockGameThread("addSt");
                    }
                    gameTrLockedByAdding = true;
                    Logger.log("wg can't keep up, locking game thread;");
                }
                currStep = STEP_ADD;
                placeNext();
            } else {
                if (gameTrLockedByAdding) {
                    gameTrLockedByAdding = false;
                    unlockGameThread("addSt");
                }
                if (!structLogger.shouldRmFirstStruct()) {
                    needSpeed = false;
                }
            }
            
            if (tick == 0) {
                currStep = STEP_RES_POS;
                /* World cycling
                * The larger the coordinates, the weirder the physics engine behaves.
                * So we need to move all structures and bodies to the left from time to time.
                */
                if (w.carX > 3000 && (GameplayCanvas.timeFlying > -1 || GameplayCanvas.uninterestingDebug)) {
                    resetPosition();
                }

                w.refreshCarPos();
                if (w.carX > nextPointsCounterTargetX) {
                    nextPointsCounterTargetX += POINTS_DIVIDER;
                    GameplayCanvas.points++;
                }
            }

            currStep = STEP_CLEAN_SGS;
            structLogger.rmFarStructures();
        }
        currStep = STEP_IDLE;
        tick++;
        if (tick >= 10) {
            tick = 0;
        }
        Thread.yield();
        try {
            if (!needSpeed) {
                Thread.sleep(20);
            }
        } catch (InterruptedException ignored) { }
        mspt = (int) (System.currentTimeMillis() - startTime);
    }
    
    private void placeNext() {
        int idsCount;
        if (DebugMenu.mgstructOnly) {
            idsCount = MgStruct.loadedStructsNumber;
        } else {
            idsCount = stdStructsNumber + floorWeightInRandom + MgStruct.loadedStructsNumber;
        }
        while (nextStructRandomId == prevStructRandomId
                || (DebugMenu.whatTheGame && (nextStructRandomId < 6 || nextStructRandomId > 9))) {
            nextStructRandomId = rand.nextInt(idsCount); // 10: 0-9
        }
        //nextStructRandomId = 21;
        prevStructRandomId = nextStructRandomId;
        if (DebugMenu.mgstructOnly) {
            nextStructRandomId+=stdStructsNumber + floorWeightInRandom;
        }

        int[] structData; // [endX, endY, lineCount]
        if (lastY > 1000 | lastY < -1000) { // will correct height if it is too high or too low
            Logger.log("correcting height. lastY=", lastY);
            structData = StructurePlacer.floor(w, isResettingPosition, lastX, lastY, 1000 + rand.nextInt(4) * 100, (rand.nextInt(7) - 3) * 100);
        } else {
            Logger.log("+id", nextStructRandomId);
            /*
            * 0 - arc1, 1 - sin, 2 - floorStat, 3 - arc2, 4 - abyss,
            * 5 - slantedDottedLine, 6..9 - floor, 10 - mgstruct0, 11 - mgstruct1,
            * ...
            */
            if (deferredStructures != null && !needSpeed && !deferredStructures.isEmpty()) {
                structData = StructurePlacer.place(w, isResettingPosition, (short[][]) deferredStructures.elementAt(0), lastX, lastY);
                deferredStructures.removeElementAt(0);
                if (deferredStructures.isEmpty()) {
                    deferredStructures = null;
                }
            } else {
                switch (nextStructRandomId) {
                    case 0:
                        structData = StructurePlacer.arc1(w, isResettingPosition, lastX, lastY, 200 + Math.abs(rand.nextInt()) % 400, 120);
                        break;
                    case 1:
                        int halfPeriods = 4 + rand.nextInt(8);
                        int l = halfPeriods * 180;
                        int amp = 15;
                        structData = StructurePlacer.sinStruct(w, isResettingPosition, lastX, lastY, l, halfPeriods, 0, amp);
                        break;
                    case 2:
                        structData = StructurePlacer.floorStat(w, isResettingPosition, lastX, lastY, 400 + rand.nextInt(10) * 100);
                        break;
                    case 3:
                        structData = StructurePlacer.arc2(w, isResettingPosition, lastX, lastY, 500 + Math.abs(rand.nextInt()) % 500, 20);
                        break;
                    case 4:
                        structData = StructurePlacer.abyss(w, isResettingPosition, lastX, lastY, rand.nextInt(6) * 1000);
                        break;
                    case 5:
                        int n = rand.nextInt(6) + 5;
                        structData = StructurePlacer.slantedDottedLine(w, isResettingPosition, lastX, lastY, n);
                        break;
                    default:
                        if (Mathh.strictIneq(stdStructsNumber - 1,/*<*/ nextStructRandomId,/*<*/ stdStructsNumber + floorWeightInRandom)) {
                            structData = StructurePlacer.floor(w, isResettingPosition, lastX, lastY, 400 + rand.nextInt(10) * 100, (rand.nextInt(7) - 3) * 100);
                        } else {
                            structData = placeMGStructByRelativeID(nextStructRandomId);
                        }
                        break;
                }
            }
        }
        lastX = structData[0];
        lastY = structData[1];
        structLogger.add(lastX, structData[2]);

        Logger.log("lastX=", lastX);
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
    
    public void stop() {
    	Logger.log("stopping wg thread...");
    	isEnabled = false;
    	boolean succeed = wgThread == null;
        while (!succeed) {
            try {
                wgThread.join();
                succeed = true;
            } catch (InterruptedException ex) {
                Logger.log(ex);
            }
        }
        Logger.log("wg: stopped");
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
        } catch (NullPointerException ignored) { }
        
        structLogger = new StructLog(10);
        
        // start platform
        ElementPlacer elementPlacer = new ElementPlacer(w, isResettingPosition);
        elementPlacer.line(lastX - 600, lastY - 100, lastX, lastY);
        structLogger.add(lastX, elementPlacer.getLineCount());
    }
    private void cleanWorld() {
        lockGameThread("clnW");
        Constraint[] constraints = w.getConstraints();
        while (w.getConstraintCount() > 0) {
            w.removeConstraint(constraints[0]);
        }
        rmAllBodies();
        rmLandscapeSegments();
        unlockGameThread("clnW");
    }
    private void rmLandscapeSegments() {
        while (landscape.segmentCount() > 0) {
            landscape.removeSegment(0);
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
                    Thread.yield();
                    Thread.sleep(0);
                } catch (InterruptedException ex) {
                    Logger.log(ex);
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
    
    private void rmAllBodies() {
        Body[] bodies = w.getBodies();
        while (w.getBodyCount() > 0) {
            w.removeBody(bodies[0]);
        }
    }

    public int getSegmentCount() {
        try {
            return landscape.segmentCount();
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
        structLogger.moveXAllElements(dx);
        w.barrierX += dx;
        w.moveBg(dx);
        
        nextPointsCounterTargetX += dx;

        isResettingPosition = false;
        unlockGameThread("rsPos");
    }
    
    private void moveLandscape(int dx) {
        movePoints(landscape.elementStartPoints(), dx);
        movePoints(landscape.elementEndPoints(), dx);
    }
    
    private void movePoints(FXVector[] points, int dx) {
        try {
            for (int i = 0; i < landscape.segmentCount(); i++) {
                FXVector point = points[i];
                points[i] = new FXVector(point.xFX + FXUtil.toFX(dx), point.yFX);
            }
        } catch(NullPointerException ex) {
            Logger.log(ex);
        }
    }
    
    private void moveBodies(int dx) {
        Body[] bodies = w.getBodies();
        for (int i = 0; i < w.getBodyCount(); i++) {
            bodies[i].translate(FXVector.newVector(dx, 0), 0);
        }
    }

    private int[] placeMGStructByRelativeID(int relID) {
        int id = relID - floorWeightInRandom - stdStructsNumber;
        return placeMGStructByID(id);
    }

    private int[] placeMGStructByID(int id) {
        short[][] data = MgStruct.structStorage[id];
        if (data.length < 1) {
            Logger.log("mgs" + id + " is broken: data.length=", data.length);
            return null;
        }
        Logger.log("+mgs", id);
        return StructurePlacer.place(w, isResettingPosition, data, lastX, lastY);
    }
    
    private class StructLog {
        public static final int MAX_DIST_TO_RM_STRUCT = 4000;
        public static final int MAX_DIST_TO_RM_STRUCT_IN_SIMULATION = 300;
        private short[][] structLog;
        private int numberOfLoggedStructs = 0;
        private int ringLogStart = 0;
        private boolean isLeftBarrierAdded = false;
        
        public StructLog(int structLogSize) {
            structLog = new short[structLogSize][];
        }
        
        public void add(int endX, int segmentCount) {
            //Logger.log("strL:add "+endX+" "+segmentCount);
            
            if (numberOfLoggedStructs >= structLog.length) {
                int ns = structLog.length+1;
                Logger.log("structLog len => " + ns);
                increase(ns);
            }
            
            short[] a = {(short) endX, (short) segmentCount};
            int nextID = (ringLogStart + numberOfLoggedStructs) % structLog.length;
            Logger.log("logging struct to " + nextID);
            structLog[nextID] = a;
            
            numberOfLoggedStructs++;
        }
        
        public void increase(int newSize) {
            if (newSize < structLog.length) {
                throw new IllegalArgumentException("newSize can't be less than current size");
            }
            short[][] tmp = structLog;
            structLog = new short[newSize][];
            // copying from start of ring log to end of array
            System.arraycopy(tmp, ringLogStart, structLog, 0, tmp.length - ringLogStart);
            // copying from start of array to tail of ring log
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
                    w.barrierX = structLog[getElementID(0)][0];
                    Logger.log("+barrier at " + w.barrierX);
                    landscape.addSegment(FXVector.newVector(w.barrierX, -10000), FXVector.newVector(w.barrierX, 10000), (short) 1);
                    structLog[getElementID(1)][1] += 1;
                    isLeftBarrierAdded = true;
                }
                
                int deletedSegments = 0;
                for (int i = 0; i < Math.min(getElementAt(0)[1], 3); i++) {
                    landscape.removeSegment(0);
                    deletedSegments++;
                }
                int id = getElementID(0);
                structLog[id][1] = (short) (structLog[id][1] - ((short) deletedSegments));

                // if 0 segments left, then the structure was deleted completely.
                // Deleting it from log
                if (getElementAt(0)[1] == 0) {
                    isLeftBarrierAdded = false;
                    rmFirstElement();
                    
                    /* broken code, unused
                    // check if there are a concatenated line partially behind the barrier
                    FXVector prevLineStartPoint = landscape.elementStartPoints()[0];
                    if (prevLineStartPoint.xAsInt() < barrierX) {
                        landscape.elementStartPoints()[0] = FXVector.newVector(barrierX, prevLineStartPoint.yAsInt());
                    }*/
                }
            }
        }
    
        public boolean shouldRmFirstStruct() {
            int maxDistToRemove = MAX_DIST_TO_RM_STRUCT;
            if (DebugMenu.simulationMode) {
                maxDistToRemove = MAX_DIST_TO_RM_STRUCT_IN_SIMULATION;
            }
            try {
                if (getNumberOfLogged() > 0) {
                    return w.carX - getElementAt(0)[0] > maxDistToRemove;
                } else
                    return false;
            } catch(NullPointerException ex) {
                Logger.enableOnScreenLog(GraphicsWorld.scHeight);
                Logger.log(ex);
                Logger.log("structLog:critical err");
                return false;
            }
        }
    }
}