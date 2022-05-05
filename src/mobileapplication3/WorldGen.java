/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mobileapplication3;

import at.emini.physics2D.Body;
import at.emini.physics2D.Constraint;
import at.emini.physics2D.Shape;
import at.emini.physics2D.util.FXVector;
import java.util.Random;
import java.util.Vector;

/**
 *
 * @author vipaol
 */
public class WorldGen implements Runnable{
    
    private int prevR;
    private int i;
    private int lastX = -8000;
    private int lastY = 0;
    private short u = 0;
    private Random rand;
    private int sn = 36;
    private int sl = 360 / sn;
    //private Vector waitinForDel;
    private int toD = 0;
    private int t = 0;
    private boolean reseted = true;
    private boolean stopped = false;
    public boolean resettingPosition = false;
    private Vector structLog;
    //static Vector l_log;
    private boolean waitinForReset = false;
    private int savedPoints = 0;
    private int lowestY = 0;
    private boolean paused;
    //private Thread thread;
    boolean needSpeed = true;
    boolean ready = true;
    
    public void start() {
        ready = false;
        structLog = new Vector();
        rand = new Random();
        stopped = false;
        waitinForReset = true;
        resume();
        Main.print("gen:start()");
        (new Thread(this, "world generator")).start();
        //thread = new Thread(this);
        //thread.start();
    }
    public void stop() {
        stopped = true;
        //thread = null;
    }
    public void resume() {
        paused = false;
    }
    public void pause() {
        paused = true;
    }
    public boolean isReady() {
        return ready;
    }
    public int getLowestY() {
        return lowestY;
    }
    public void resetToQue() {
        waitinForReset = true;
    }
    private void reset() {
        ready = false;
        needSpeed = true;
        waitinForReset = false;
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
        gCanvas.addCar();
        ready = true;
    }
    
    private void random() {
        Main.print("gen:random()");
        while (i == prevR) {
            i = rand.nextInt(5);
        }
        prevR = i;
        if (i == 0) {
            circ1(lastX, lastY, 400, 15, 120);
        } else if (i == 1) {
            int l = 720 + rand.nextInt(8) * 180;
            sin(lastX, lastY, l, l / 180, 0, 15 + rand.nextInt(15));
        } else if (i == 2) {
            floor(lastX, lastY, 400 + rand.nextInt(10) * 100, (rand.nextInt(6) - 3) * 100);
        } else if (i == 3) {
            circ2(lastX, lastY, 1000, 20);
        } else {
            floorStat(lastX, lastY, 400 + rand.nextInt(10) * 100);
        }
        Main.print("" + lastX);
        resettingPosition = false;
        lowestY = Math.max(lastY, lowestY);
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
            gCanvas.world.addBody(fallinPlatf);
        }
        rect = Shape.createRectangle(l2, platfHeight);
        rect.setMass(1);
        rect.setFriction(0);
        rect.setElasticity(50);
        for (int i = 0; i < 2; i++) {
            Body fallinPlatf = new Body(x+r-r2+i*l2+l2/2, y+platfHeight/2, rect, true);
            fallinPlatf.setDynamic(false);
            gCanvas.world.addBody(fallinPlatf);
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
        
        
        lastY = y2;
    }
    private void floorStat(int x, int y, int l) {      // 1
        if (!resettingPosition) {
            int[] h = {1, l, y, l};
            structlogger(h);
        }
        toD = 0;
        //nextY = lastY;
        line(x, y, x + l, y);
        lastX += l;
        
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
            gCanvas.l.addSegment(FXVector.newVector(x + (i-sl), y + amp * Mathh.sin(180*(i-sl)/l*halfperiods+offset) / 1000), FXVector.newVector(x + i, y + amp*Mathh.sin(180*i/l*halfperiods+offset)/1000), u);
            toD += 1;
        }
        lastX += l;
        //waitinForDel.addElement(new Integer(toD));
    }
    private void arc(int x, int y, int r, int ang, int of) {
        for(int i = sl; i <= ang; i+=sl) {
            gCanvas.l.addSegment(FXVector.newVector(x+Mathh.cos(i+of)*r/1000, y+Mathh.sin(i+of)*r/1000), FXVector.newVector(x+Mathh.cos((i-sl)+of)*r/1000,y+Mathh.sin((i-sl)+of)*r/1000), u);
            toD += 1;
        }
    }
    private void arc(int x, int y, int r, int ang, int of, int kx, int ky) { //k: 10 = 1.0
        for(int i = sl; i <= ang; i+=sl) {
            gCanvas.l.addSegment(FXVector.newVector(x+Mathh.cos(i+of)*kx*r/10000, y+Mathh.sin(i+of)*ky*r/10000), FXVector.newVector(x+Mathh.cos((i-sl)+of)*kx*r/10000,y+Mathh.sin((i-sl)+of)*ky*r/10000), u);
            toD += 1;
        }
    }
    private void line(int x1, int y1, int x2, int y2) {
        gCanvas.l.addSegment(FXVector.newVector(x1, y1), FXVector.newVector(x2, y2), u);
        //waitinForDel.addElement(new Integer(1));
    }
    /*******************************************
     * 
     * STRUCTURES                              *
     * 
     * 
     * 
     *******************************************/
    
    
        
    
    

    public void run() {
        t = 1;
        Main.print("gen:run()");
        while(mnCanvas.wg) {
            if (!paused) {
                if (waitinForReset) {
                    reset();
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
                    if (structLog.size() >= 2) {
                        if (GraphicsWorld.carX > 8000 & gCanvas.flying > 1) {
                            resetPosition();
                        }
                    }
                }
                
                if (t <= 20) {
                    t++;
                } else {
                    GraphicsWorld.points = Math.max(GraphicsWorld.points, savedPoints + (GraphicsWorld.carX+8000)/2000);
                    t = 1;
                }
            }
            try {
                if (!needSpeed) {
                    Thread.sleep(200);
                } else {
                    if (t > 15) {
                        needSpeed = false;
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    
    private void structlogger(int[] a) {
        if (!resettingPosition) {
            structLog.addElement(a);
            if (structLog.size() > 6) {
                structLog.removeElementAt(0);
            }
        }
    }
    private void resetPosition() {
        
        // костыль для обхода бага движка
        
        ready = false;
        resettingPosition = true;
        needSpeed = true;
        int prevLastX = lastX;
        lastX = -8000;
        savedPoints += (prevLastX - lastX - 8000) / 2000;
        Main.print("REGEN");
        
        rmSegs();
        rmBodies();
        reproduce();
        
        gCanvas.carbody.translate(FXVector.newVector(lastX - prevLastX, 0), 0);
        gCanvas.leftwheel.translate(FXVector.newVector(lastX - prevLastX, 0), 0);
        gCanvas.rightwheel.translate(FXVector.newVector(lastX - prevLastX, 0), 0);

        resettingPosition = false;
        needSpeed = false;
        ready = true;
    }
    
    private void cleanWorld() {
        gCanvas.paused = true;
        Constraint[] constraints = gCanvas.world.getConstraints();
        toD = gCanvas.world.getConstraintCount();
        //Main.print("toD:" + toD);
        while (gCanvas.world.getConstraintCount() > 0) {
            gCanvas.world.removeConstraint(constraints[0]);
        }
        rmAllBodies();
        rmSegs();
        constraints = null;
        //waitinForDel.removeAllElements();
        toD = 0;
        gCanvas.paused = false;
    }
    private void rmSegs() {
        toD = gCanvas.l.segmentCount();
        //Main.print("toD:" + toD);
        while (gCanvas.l.segmentCount() > 0) {
            gCanvas.l.removeSegment(0);
        }
    }
    private void rmBodies() {
        Body[] bodies = gCanvas.world.getBodies();
        toD = gCanvas.world.getBodyCount();
        //Main.print("toD:" + toD);
        int to = gCanvas.world.getBodyCount();
        for (int i = to - 1; i >= 0; i--) {
            if (bodies[i] != gCanvas.carbody & bodies[i] != gCanvas.leftwheel & bodies[i] != gCanvas.rightwheel)
            gCanvas.world.removeBody(bodies[i]);
        }
        bodies = null;
    }
    private void rmAllBodies() {
        Body[] bodies = gCanvas.world.getBodies();
        toD = gCanvas.world.getBodyCount();
        //Main.print("toD:" + toD);
        while (gCanvas.world.getBodyCount() > 0) {
            gCanvas.world.removeBody(bodies[0]);
        }
        bodies = null;
    }
    private void reproduce() {
        Main.print("REPRODUCE:" + structLog.size());
        //for (int i = 0; i < structLog.size(); i++) {
        while (!structLog.isEmpty()) {
            int[] struct = (int[]) structLog.elementAt(0);
            int structID = struct[0];

            int y = struct[2];
            lastY = y;
            Main.print("REPRODUCE:" + structID + ",y:" + y);
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
            if (structID == 2) {
                int l = struct[3];
                int x2 = struct[4];
                floor(lastX, y, l, x2);
                Main.print("fl");
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
            //WorldGen.lastX = lastX;
            structLog.removeElementAt(0);
        }
    }
}