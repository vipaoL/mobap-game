/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mobileapplication3;

import at.emini.physics2D.Body;
import at.emini.physics2D.Constraint;
import at.emini.physics2D.Landscape;
import at.emini.physics2D.Shape;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import java.util.Random;

/**
 *
 * @author vipaol
 */
public class WorldGen implements Runnable {
    
    int stdStructsNumber = 6;
    int floorStatWheightInRandom = 4;
    
    private int prevR;
    private int i;
    private int zeroPoint =  -8000;
    private int lastX = -8000;
    private int lastY = 0;
    private short u = 0;
    private short u1 = 1;
    private Random rand;
    private int sn = 36;
    private int sl = 360 / sn;
    //private Vector waitinForDel;
    private int toD = 0;
    private int t = 0;
    private boolean reseted = true;
    private boolean stopped = false;
    public boolean resettingPosition = false;
    private int[][] structLog = new int[7][];
    private int numberOfLoggedStructs = 0;
    private int ringLogTail = 0;
    private int nowLogging = 0;
    //static Vector l_log;
    private boolean waitinForRestart = false;
    private int savedPoints = 0;
    private final int POINTS_DIVIDER = 2000;
    private int nextPointsCounterTargetX = lastX + POINTS_DIVIDER;
    private int lowestY = 0;
    private boolean paused;
    //private Thread thread;
    boolean needSpeed = true;
    boolean ready = true;
    GraphicsWorld w;
    Landscape lndscp;
    MgStruct mgStruct = new MgStruct();
    
    public WorldGen(GraphicsWorld w) {
        this.w = w;
        lndscp = w.getLandscape();
        
        /*for (int i = 1; i < 0 & mgStruct.readRes("/" + i + ".mgstruct"); i++) {
            Main.print(i);
        }*/
        
        //Main.print("read completed");
    }
    
    public void run() {
        t = 1;
        Main.print("gen:run()");
        while(mnCanvas.wg) {
            if (!paused) {
                if (waitinForRestart) {
                    restart();
                }
                
                if ((GraphicsWorld.carX + GraphicsWorld.viewField > lastX)) {
                    ///System.out.println("ttt" + GraphicsWorld.carX + lastX);
                    random();
                    /*if (false & waitinForDel.size() > 16) {
                        toD = ((Integer)waitinForDel.elementAt(0)).intValue();
                        for(int i = 0; i < toD; i++) {
                            gCanvas.l.removeSegment(0);
                        }
                        waitinForDel.removeElementAt(0);
                        toD = 0;
                    }*/
                    if (numberOfLoggedStructs >= structLog.length) {
                        if (GraphicsWorld.carX > 8000 & gCanvas.flying > 1) {
                            resetPosition();
                        }
                    }
                }
                
                if (false & t <= 10) {
                    t++;
                } else {
                    w.refreshPos();
                    if (GraphicsWorld.carX > nextPointsCounterTargetX) {
                        nextPointsCounterTargetX += POINTS_DIVIDER;
                        GraphicsWorld.points++;
                    }
                    //GraphicsWorld.points = Math.max(GraphicsWorld.points, savedPoints + (GraphicsWorld.carX-zeroPoint)/POINTS_DIVIDER);
                    t = 1;
                }
            }
            try {
                if (!needSpeed) {
                    Thread.sleep(200);
                } else {
                    if (t > 9) {
                        needSpeed = false;
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void random() {
        Main.print("gen:random()");
        while (i == prevR) {
            int a = stdStructsNumber + floorStatWheightInRandom;
            if (DebugMenu.mgstructOnly) {
                a=0;
            }
            a += mgStruct.structBufSizeInCells;
            i = rand.nextInt(a/*10*/); //0-9
            if (DebugMenu.mgstructOnly) {
               i+=stdStructsNumber + floorStatWheightInRandom;
            }
        }
        prevR = i;
        Main.print("id"+i);
        if (i == 0) {
            circ1(lastX, lastY, 400, 15, 120);
        } else if (i == 1) {
            int l = 720 + rand.nextInt(8) * 180;
            int amp = 15 + rand.nextInt(15);
            sin(lastX, lastY, l, l / 180, 0, amp);
        } else if (i == 2) {
            floor(lastX, lastY, 400 + rand.nextInt(10) * 100, (rand.nextInt(6) - 3) * 100);
        } else if (i == 3) {
            circ2(lastX, lastY, 1000, 20);
        } else if (i == 4) {
            int l = 6000;
            l = rand.nextInt(6) * 1000;
            abyss(lastX, lastY, l);
        } else if (i == 5) {
            int n = rand.nextInt(6) + 5;
            dotline(lastX, lastY, n);
        } else if (i > stdStructsNumber - 1 & i < stdStructsNumber + floorStatWheightInRandom) {
            floorStat(lastX, lastY, 400 + rand.nextInt(10) * 100);
        } else {
            placeMGStructByRelativeID(i);
        }
        Main.print("lastX" + lastX);
        resettingPosition = false;
        lowestY = Math.max(lastY, lowestY);
    }
    
    public void start() {
        ready = false;
        rand = new Random();
        stopped = false;
        waitinForRestart = true;
        resume();
        Main.print("gen:start()");
        (new Thread(this, "world generator")).start();
    }
    public void pause() {
        paused = true;
    }
    public void resume() {
        paused = false;
    }
    public void stop() {
        stopped = true;
    }
    
    private void restart() {
        ready = false;
        needSpeed = true;
        waitinForRestart = false;
        Main.print("gen:reset()");
        prevR = 1;
        i = 2;
        lastX = -7900;
        lastY = 0;
        reseted = true;
        //waitinForDel = new Vector();
        try {
            cleanWorld();
        } catch (NullPointerException e) {
            
        }
        savedPoints = 0;
        toD = 0;
        line(lastX - 600, lastY - 100, lastX, lastY);
        t = 2;
        GraphicsWorld.points = 0;
        w.addCar();
        ready = true;
    }
    public void restartToQue() {
        waitinForRestart = true;
    }
    private void cleanWorld() {
        gCanvas.paused = true;
        Constraint[] constraints = w.getConstraints();
        toD = w.getConstraintCount();
        //Main.print("toD:" + toD);
        while (w.getConstraintCount() > 0) {
            w.removeConstraint(constraints[0]);
        }
        rmAllBodies();
        rmSegs();
        constraints = null;
        //waitinForDel.removeAllElements();
        toD = 0;
        gCanvas.paused = false;
    }
    private void rmSegs() {
        toD = lndscp.segmentCount();
        //Main.print("toD:" + toD);
        while (lndscp.segmentCount() > 0) {
            lndscp.removeSegment(0);
        }
    }
    private void rmBodies() {
        Body[] bodies = w.getBodies();
        toD = w.getBodyCount();
        //Main.print("toD:" + toD);
        int to = w.getBodyCount();
        for (int i = to - 1; i >= 0; i--) {
            if (bodies[i] != w.carbody & bodies[i] != w.leftwheel & bodies[i] != w.rightwheel)
            w.removeBody(bodies[i]);
        }
        bodies = null;
    }
    private void rmAllBodies() {
        Body[] bodies = w.getBodies();
        toD = w.getBodyCount();
        //Main.print("toD:" + toD);
        while (w.getBodyCount() > 0) {
            w.removeBody(bodies[0]);
        }
        bodies = null;
    }
    public boolean isReady() {
        return ready;
    }
    public int getLowestY() {
        return lowestY;
    }
    
    private void structlogger(int[] a) {
        if (!resettingPosition) {
            if (numberOfLoggedStructs >= structLog.length) {
                //structLog.removeElementAt(0);
                
            } else {
                numberOfLoggedStructs++;
            }
            //structLog.addElement(a);
            
            int index = (ringLogTail) % structLog.length;
            structLog[index] = a;
            Main.print(structLog[index][0]);
            Main.print(index);
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
    
    private void resetPosition() {
        
        // world cycling
        
        ready = false;
        resettingPosition = true;
        needSpeed = true;
        int prevLastX = lastX;
        lastX = -8000;
        
        Main.print("REGEN");
        
        rmSegs();
        //rmBodies();
        reproduce();
        
        //w.carbody.translate(FXVector.newVector(lastX - prevLastX, 0), 0);
        moveBodies(lastX - prevLastX);
        zeroPoint = w.carbody.positionFX().xAsInt();
        //w.leftwheel.translate(FXVector.newVector(lastX - prevLastX, 0), 0);
        //w.rightwheel.translate(FXVector.newVector(lastX - prevLastX, 0), 0);
        
        nextPointsCounterTargetX -= (prevLastX - lastX);

        resettingPosition = false;
        needSpeed = false;
        ready = true;
    }
    
    private void reproduce() {
        Main.print("REPRODUCE:" + numberOfLoggedStructs);
        //for (int i = 0; i < structLog.size(); i++) {
        for (int i = 0; i < numberOfLoggedStructs; i++) {
            //Main.print("REPR:0");
            int[] struct = structlog_getElementAt(i);
            //Main.print("REPR:1");
            int structID = struct[0];

            int y = struct[2];
            lastY = y;
            //Main.print("REPRODUCE:" + structID);
            if (structID == 0) {
                int r = struct[3];
                int sn = struct[4];
                int va = struct[5];
                circ1(lastX, y, r, sn, va);
                Main.print("circ1");
            }
            if (structID == 1) {
                int l = struct[3];
                floorStat(lastX, y, l);
                Main.print("flStat");
            }
            if (structID == 2) { // {2, l, y}
                int l = struct[3];
                abyss(lastX, y, l);
                Main.print("abyss");
            }
            if (structID == 3) {
                int l = struct[3];
                int halfperiods = struct[4];
                int offset = struct[5];
                int amp = struct[6];
                sin(lastX, y, l, halfperiods, offset, amp);
                Main.print("sin");
            }
            if (structID == 4) {
                int r = struct[3];
                int sn = struct[4];
                circ2(lastX, y, r, sn);
                Main.print("circ2");
            }
            if (structID == 5) {
                int n = struct[3];
                dotline(lastX, y, n);
                Main.print("dotline");
            }
            if (structID >= stdStructsNumber + floorStatWheightInRandom) {
                placeMGStructByRelativeID(structID);
            }
            //WorldGen.lastX = lastX;
            //structLog.removeElementAt(0);
        }
        //numberOfLoggedStructs = 0;
        //ringLogTail = 0;
    }
    
    private void moveBodies(int dx) {
        Body[] bodies = w.getBodies();
        for (int i = 0; i < w.getBodyCount(); i++) {
            bodies[i].translate(FXVector.newVector(dx, 0), 0);
        }
    }
    
    
    
    
    
    void placeMGStructByRelativeID(int relID) {
        int id = relID - floorStatWheightInRandom - stdStructsNumber;
        if (!resettingPosition) {
            int[] log = {relID, lastX, lastY};
            structlogger(log);
        }
        placeMGStructByID(id);
    }
    
    void placeMGStructByID(int id) {
        Main.print("byID" + id);
        placeMGStruct(id);
    }
    
    void placeMGStruct(int id) {
        short[][] data = mgStruct.structBuffer[id];
        Main.print("placemg" + mgStruct.structSizes[id]);
        for (int i = 1; i < mgStruct.structSizes[id]; i++) {
            placePrimitive(data[i]);
        }
        lastX+=data[0][1];
        lastY+=data[0][2];
    }
    
    void placePrimitive(short[] data) {
        short id = data[0];
        for (int i = 0; i < data.length; i++) {
            System.out.print(data[i] + " ");
        }
        System.out.println(" - prim placed");
        if (id == 2) {
            line(data[1] + lastX, data[2] + lastY, data[3] + lastX, data[4] + lastY);
        } else if (id == 3) {
            arc(data[1]+lastX, data[2]+lastY, data[3], data[4], data[5], data[6] / 10, data[7] / 10);
        } else if (id == 4 & !resettingPosition) {
            int x1 = data[1];
            int y1 = data[2];
            int x2 = data[3];
            int y2 = data[4];
            int dx = x2 - x1;
            int dy = y2 - y1;
            
            /*int l;
            if (dy == 0) {
                l = dx;
            } else if (dx == 0) {
                l = dy;
            } else {
                l = distance(x1, y1, x2, y2);
            }*/
            int platfH = data[5];
            int platfL = data[6];
            int spacing = data[7];
            int l = data[8];
            int ang = data[9];
            Shape rect = Shape.createRectangle(platfL, platfH);
            rect.setMass(1);
            rect.setFriction(0);
            rect.setElasticity(50);
            dx/=(l/platfL);
            int spX = spacing * dx / l;
            dy/=(l/platfL);
            int spY = spacing * dy / l;
            int plLX = platfL * dx / l;
            int plLY = platfL * dy / l;
            System.out.println("dx=" + dx + "dy=" + dy);
            for (int i = 0; i < l / platfL; i++) {
                Body fallinPlatf = new Body(lastX + x1 + i*(dx+spX) - plLX/2, lastY + y1 + i*(dy+spY) - plLY/2, rect, true);
                fallinPlatf.setRotation2FX(FXUtil.TWO_PI_2FX / 360 * ang);
                fallinPlatf.setDynamic(false);
                //fallinPlatf.setInteracting(false);
                w.addBody(fallinPlatf);
            }
            
        } else if (id == 5) {
            arc(data[1]+lastX, data[2]+lastY, data[3], 360, 0);
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
     * STRUCTURES                              *
     * 
     *******************************************/
    
    
    
    
    private void circ1(int x, int y, int r, int sn, int va) { // 0
        int[] h = {0, x, y, r, sn, va};
        
        x+=r;
        
        //sn = 360 / sl - va/sl;
        /*for(int i = 0; i < 300/sl; i++) {
            mCanvas.l.addSegment(FXVector.newVector(x + Mathh.cos(i*sl+120) * r / 1000, y-2*r + Mathh.sin(120+i*sl) * r / 1000), FXVector.newVector(x + Mathh.cos(120+(i+1)*sl) * r / 1000, y-2*r + Mathh.sin(120+(i+1)*sl) * r / 1000), u);
        }*/
        int r2 = r*3/2;
        
        arc(x-r, y-r2, r2, 60, 30);
        arc(x+r/2, y-r*2, r, 300, va);
        int ofs = (1000 - Mathh.cos(30))*2*r2/1000;
        arc(x+r*2-ofs, y-r2, r2, 60, 90);
        //waitinForDel.addElement(new Integer(toD));
        toD = 0;
        //mCanvas.l.addSegment(FXVector.newVector(x, 0), FXVector.newVector(x + 50, 0), u);
        //return l;
        /*for(int i = 0; i < 90/sl; i++) {
            mCanvas.l.addSegment(FXVector.newVector(x + Mathh.cos(i*sl) * r / 1000 - r, y + Mathh.sin(i*sl) * r / 1000), FXVector.newVector(x + Mathh.cos((i+1)*sl) * r / 1000 - r, y + Mathh.sin((i+1)*sl) * r / 1000), u);
        }
        for(int i = 0; i < 90/sl; i++) {
            mCanvas.l.addSegment(FXVector.newVector(x + Mathh.cos(i*sl + 90) * r / 1000 + r, y + Mathh.sin(i*sl + 90) * r / 1000), FXVector.newVector(x + Mathh.cos((i+1)*sl + 90) * r / 1000 + r, y + Mathh.sin((i+1)*sl + 90) * r / 1000), u);
        }*/
        
        
        int l = r2+r2-ofs;
        lastX += l;
        if (!resettingPosition) {
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
        toD++;
        arc(x+r, y, r2, 90, 90, 10, 5);
        line(x+r, y+r2/2, x+2*r, y+r2/2);
        toD++;
        //waitinForDel.addElement(new Integer(toD));
        toD = 0;
        lastY += r2/2;
        
        if (!resettingPosition) {
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
                fallinPlatf.setDynamic(false);
                w.addBody(fallinPlatf);
            }
        }
        
        int l = r+r;
        lastX += l;
        if (!resettingPosition) {
            h[1] = l;
            structlogger(h);
        }
    }
    
    private void floor(int x, int y, int l, int y2) {    //2
        /*if (!resettingPosition) {
            int[] h = {2, l, y, l, y2};
            structlogger(h);
        }*/
        toD = 0;
        //nextY = lastY;
//        if (y == y2) {
//            line(x, y, x + l, y2);
//            lastX += l;
//        } else {
        int amp = (y2 - y) / 2;
        sin(x, y + amp, l, 1, 270, amp);
//        }
        
        
        //lastY = y2;
    }
    private void floorStat(int x, int y, int l) {      // 1
        if (!resettingPosition) {
            int[] h = {1, l, y, l};
            structlogger(h);
        }
        toD = 0;
        //nextY = lastY;
        line1(x, y, x + l, y);
        lastX += l;
        
    }
    private void abyss(int x, int y, int l) {
        if (!resettingPosition) {
            int[] h = {2, x, y, l};
            structlogger(h);
        }
        int ang = 60; // springboard angle
        int r = l / 8;
        arc(x, y-r, r, ang, 90 - ang, 15, 10);
        line(x+l - l / 5, y - r * Mathh.cos(ang) / 1000, x+l, y - r * Mathh.cos(ang) / 1000);
        lastX += l;
        lastY -= r * Mathh.cos(ang) / 1000;
    }
    private void dotline(int x, int y, int n) {
        if (!resettingPosition) {
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
        if (!resettingPosition) {
            int[] h = {3, l, y, l, halfperiods, offset, amp};
            structlogger(h);
        }
        toD = 0;
        int sn = l/20;
        int sl = l/sn;
        //int lInSl = l/sl;
        for(int i = sl; i <= l; i+=sl) {
            line1(x + (i-sl), y + amp * Mathh.sin(180*(i-sl)/l*halfperiods+offset) / 1000, x + i, y + amp*Mathh.sin(180*i/l*halfperiods+offset)/1000);
            toD += 1;
        }
        lastX += l;
        lastY = y + Mathh.sin(offset+180*halfperiods) * amp / 1000;
        //waitinForDel.addElement(new Integer(toD));
    }
    private void arc(int x, int y, int r, int ang, int of) {
        for(int i = sl; i <= ang; i+=sl) {
            line(x+Mathh.cos(i+of)*r/1000, y+Mathh.sin(i+of)*r/1000, x+Mathh.cos((i-sl)+of)*r/1000,y+Mathh.sin((i-sl)+of)*r/1000);
            toD += 1;
        }
    }
    private void arc(int x, int y, int r, int ang, int of, int kx, int ky) { //k: 10 = 1.0
        while (of < 0) {
            of += 360;
        }
        for(int i = sl; i <= ang; i+=sl) {
            line(x+Mathh.cos(i+of)*kx*r/10000, y+Mathh.sin(i+of)*ky*r/10000, x+Mathh.cos((i-sl)+of)*kx*r/10000,y+Mathh.sin((i-sl)+of)*ky*r/10000);
            toD += 1;
        }
    }
    void line(int x1, int y1, int x2, int y2) {
        lndscp.addSegment(FXVector.newVector(x1, y1), FXVector.newVector(x2, y2), u);
        //waitinForDel.addElement(new Integer(1));
    }
    private void line1(int x1, int y1, int x2, int y2) {
        lndscp.addSegment(FXVector.newVector(x1, y1), FXVector.newVector(x2, y2), u1);
        //waitinForDel.addElement(new Integer(1));
    }
    /*******************************************
     * 
     * STRUCTURES                              *
     * 
     * 
     * 
     *******************************************/
    
    
}