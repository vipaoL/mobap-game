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
    private static final int BUILTIN_STRUCTS_NUMBER = 6;
    private static final int FLOOR_RANDOM_WEIGHT = 4;

    public final Object lock = new Object();

    public static boolean isEnabled = false;
    
    private int prevStructRandomId;
    private int nextStructRandomId;
    private boolean isResettingPosition = false;
    private Vector deferredStructures = null;
    
    private int lastX;
    private int lastY;
    private final int POINTS_DIVIDER = 2000;
    private int nextPointsCounterTargetX;
    int tick = 0;
    public int mspt;
    
    private boolean paused = false;
    private boolean needSpeed = true;
    
    private final Random rand;
    private final GameplayCanvas game;
    private final GraphicsWorld w;
    private final Landscape landscape;
    private StructLog structLogger;
    private Thread wgThread = null;
    
    // wg activity indicator
    public int currStep;
    public static final int STEP_IDLE = 0;
    public static final int STEP_ADD = 1;
    public static final int STEP_RES_POS = 2;
    public static final int STEP_CLEAN_SGS = 3;
    
    
    public WorldGen(GameplayCanvas game, GraphicsWorld w) {
        w.lowestY = 2000;
        Logger.log("wg:starting");
        this.game = game;
        this.w = w;
        landscape = w.getLandscape();
        Logger.log("wg:start()");
        rand = new Random();
        Logger.log("wg:loading mgstruct");
        new MgStruct();
        reset();
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
                    game.shouldWait = true;
                    Logger.log("wg can't keep up, locking game thread...");
                }
                currStep = STEP_ADD;
                placeNext();
            } else {
                if (!structLogger.shouldRmFirstStruct()) {
                    needSpeed = false;
                    game.shouldWait = false;
                }
            }
            
            if (tick == 0) {
                currStep = STEP_RES_POS;
                /* World cycling
                * The larger the coordinates, the weirder the physics engine behaves.
                * So we need to move all structures and bodies to the left from time to time.
                */
                if (w.carX > 3000 && (game.timeFlying > -1 || game.uninterestingDebug)) {
                    resetPosition();
                }

                w.refreshCarPos();
                if (w.carX > nextPointsCounterTargetX) {
                    nextPointsCounterTargetX += POINTS_DIVIDER;
                    game.points++;
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
            idsCount = BUILTIN_STRUCTS_NUMBER + FLOOR_RANDOM_WEIGHT + MgStruct.loadedStructsNumber;
        }
        while (nextStructRandomId == prevStructRandomId
                || (DebugMenu.whatTheGame && (nextStructRandomId < 6 || nextStructRandomId > 9))) {
            nextStructRandomId = rand.nextInt(idsCount); // 10: 0-9
        }
        //nextStructRandomId = 21;
        prevStructRandomId = nextStructRandomId;
        if (DebugMenu.mgstructOnly) {
            nextStructRandomId+= BUILTIN_STRUCTS_NUMBER + FLOOR_RANDOM_WEIGHT;
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
                    case StructurePlacer.STRUCTURE_ID_ARC1:
                        structData = StructurePlacer.arc1(w, isResettingPosition, lastX, lastY, 200 + Math.abs(rand.nextInt()) % 400, 120);
                        break;
                    case StructurePlacer.STRUCTURE_ID_SIN:
                        int halfPeriods = 4 + rand.nextInt(8);
                        int l = halfPeriods * 180;
                        int amp = 15;
                        structData = StructurePlacer.sinStruct(w, isResettingPosition, lastX, lastY, l, halfPeriods, 0, amp);
                        break;
                    case StructurePlacer.STRUCTURE_ID_FLOOR_STAT:
                        structData = StructurePlacer.floorStat(w, isResettingPosition, lastX, lastY, 400 + rand.nextInt(10) * 100);
                        break;
                    case StructurePlacer.STRUCTURE_ID_ARC2:
                        structData = StructurePlacer.arc2(w, isResettingPosition, lastX, lastY, 500 + Math.abs(rand.nextInt()) % 500, 20);
                        break;
                    case StructurePlacer.STRUCTURE_ID_ABYSS:
                        structData = StructurePlacer.abyss(w, isResettingPosition, lastX, lastY, rand.nextInt(6) * 1000);
                        break;
                    case StructurePlacer.STRUCTURE_ID_SLANTED_DOTTED_LINE:
                        int n = rand.nextInt(6) + 5;
                        structData = StructurePlacer.slantedDottedLine(w, isResettingPosition, lastX, lastY, n);
                        break;
                    default:
                        if (Mathh.strictIneq(BUILTIN_STRUCTS_NUMBER - 1,/*<*/ nextStructRandomId,/*<*/ BUILTIN_STRUCTS_NUMBER + FLOOR_RANDOM_WEIGHT)) {
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
        structLogger.add(structData);

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
        int x1 = lastX - 600;
        int y1 = lastY - 100;
        elementPlacer.line(x1, y1, lastX, lastY);
        structLogger.add(concatArrays(new int[] {lastX, lastY, elementPlacer.getLineCount(), -1}, elementPlacer.getDrawingData()));
    }
    private void cleanWorld() {
        Constraint[] constraints = w.getConstraints();
        while (w.getConstraintCount() > 0) {
            w.removeConstraint(constraints[0]);
        }
        rmAllBodies();
        rmLandscapeSegments();
    }
    private void rmLandscapeSegments() {
        while (landscape.segmentCount() > 0) {
            landscape.removeSegment(0);
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

    public int[][] getStructures() {
        return structLogger.getStructures();
    }

    public int getStructuresCount() {
        return structLogger.getNumberOfLogged();
    }

    public int getStructuresRingBufferOffset() {
        return structLogger.getRingBufferOffset();
    }
    
    private void resetPosition() { // world cycling
        isResettingPosition = true;

        synchronized (lock) {
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
        }
        game.onPosReset();
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
        int id = relID - FLOOR_RANDOM_WEIGHT - BUILTIN_STRUCTS_NUMBER;
        return placeMGStructByID(id);
    }

    private int[] placeMGStructByID(int id) {
        short[][] data = MgStruct.structStorage[id];
        if (data.length < 1) {
            Logger.log("mgs" + id + " is broken: data.length=", data.length);
            return null;
        }
        Logger.log("+mgs", id);
        int[] structureData = StructurePlacer.place(w, isResettingPosition, data, lastX, lastY);
        structureData[3] = BUILTIN_STRUCTS_NUMBER + id;
        return structureData;
    }

    public static int[] concatArrays(int[] arr1, int[] arr2) { // TODO move to Utils
        if (arr1 == null) {
            return arr2;
        }
        if (arr2 == null) {
            return arr1;
        }
        int[] structData = new int[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, structData, 0, arr1.length);
        System.arraycopy(arr2, 0, structData, arr1.length, arr2.length);
        return structData;
    }
    
    private class StructLog {
        public static final int MAX_DIST_TO_RM_STRUCT = 4000;
        public static final int MAX_DIST_TO_RM_STRUCT_IN_SIMULATION = 300;
        private int[][] structLog;
        private int numberOfLoggedStructs = 0;
        private int ringLogStart = 0;
        private boolean isLeftBarrierAdded = false;
        
        public StructLog(int structLogSize) {
            structLog = new int[structLogSize][];
        }
        
        public void add(int[] structureData) {
            if (numberOfLoggedStructs >= structLog.length) {
                int ns = structLog.length+1;
                Logger.log("structLog len => " + ns);
                increase(ns);
            }

            int nextID = (ringLogStart + numberOfLoggedStructs) % structLog.length;
            Logger.log("logging struct to " + nextID);
            structLog[nextID] = structureData;
            
            numberOfLoggedStructs++;
        }
        
        public void increase(int newSize) {
            if (newSize < structLog.length) {
                throw new IllegalArgumentException("newSize can't be less than current size");
            }
            int[][] tmp = structLog;
            structLog = new int[newSize][];
            // copying from start of ring log to end of array
            System.arraycopy(tmp, ringLogStart, structLog, 0, tmp.length - ringLogStart);
            // copying from start of array to tail of ring log
            System.arraycopy(tmp, 0, structLog, tmp.length - ringLogStart, ringLogStart);
            ringLogStart = 0;
        }

        public int[] getElementAt(int i) {
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

        public int getRingBufferOffset() {
            return ringLogStart;
        }

        public int[][] getStructures() {
            return structLog;
        }
        
        public void rmFirstElement() {
            structLog[ringLogStart] = null;
            ringLogStart = (ringLogStart + 1) % structLog.length;
            numberOfLoggedStructs--;
        }
        
        public void moveXAllElements(int dx) {
            for (int i = 0; i < getSize(); i++) {
                int[] structureData = getElementAt(i);
                if (structureData == null) continue;
                structureData[0] = structureData[0] + dx;

                if (structureData.length > 4) {
                    int c = 4;
                    while (c < structureData.length - 1) {
                        int id = structureData[c++];
                        switch (id) {
                            case ElementPlacer.DRAWING_DATA_ID_LINE:
                                structureData[c++] += dx; // x1
                                c++; // y1
                                structureData[c++] += dx; // x2
                                c++; // y2
                                break;
                            case ElementPlacer.DRAWING_DATA_ID_PATH:
                                int pointsCount = structureData[c++];
                                for (int j = 0; j < pointsCount; j++) {
                                    structureData[c++] += dx; // x[i]
                                    c++; // y[i]
                                }
                                break;
                            case ElementPlacer.DRAWING_DATA_ID_CIRCLE: {
                                structureData[c++] += dx; // x
                                c++; // y
                                c++; // r
                                break;
                            }
                            case ElementPlacer.DRAWING_DATA_ID_ARC: {
                                structureData[c++] += dx; // x
                                c++; // y
                                c++; // r
                                c++; // startAngle
                                c++; // arcAngle
                                c++; // kx
                                c++; // ky
                                break;
                            }
                        }
                    }
                }
            }
        }
        public void rmFarStructures() {
            if (shouldRmFirstStruct()) {
                
                // add a barrier to the left world border
                if (!isLeftBarrierAdded) {
                    w.barrierX = structLog[getElementID(0)][0];
                    Logger.log("+barrier at " + w.barrierX);
                    landscape.addSegment(FXVector.newVector(w.barrierX, -10000), FXVector.newVector(w.barrierX, 10000), (short) 1);
                    structLog[getElementID(1)][2] += 1;
                    isLeftBarrierAdded = true;
                }
                
                int deletedSegments = 0;
                for (int i = 0; i < Math.min(getElementAt(0)[2], 3); i++) {
                    landscape.removeSegment(0);
                    deletedSegments++;
                }
                int id = getElementID(0);
                structLog[id][2] -= deletedSegments;

                // if 0 segments left, then the structure was deleted completely.
                // Deleting it from log
                if (getElementAt(0)[2] == 0) {
                    isLeftBarrierAdded = false;
                    rmFirstElement();
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