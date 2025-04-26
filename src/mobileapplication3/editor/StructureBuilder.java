/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor;

import java.io.IOException;
import java.util.Vector;

import mobileapplication3.editor.elements.Element;
import mobileapplication3.editor.elements.EndPoint;
import mobileapplication3.editor.elements.LevelStart;
import mobileapplication3.editor.elements.Element.PlacementStep;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;

/**
 *
 * @author vipaol
 */

public abstract class StructureBuilder {

    public static final int MODE_STRUCTURE = EditorUI.MODE_STRUCTURE, MODE_LEVEL = EditorUI.MODE_LEVEL;

    private int mode;
    private Vector buffer;
    public Element placingNow;
    private NextPointHandler nextPointHandler;
    public boolean isEditing = false;
    private String path = null;

    public StructureBuilder(int mode) {
        reset(mode);
    }

    public void reset(int mode) {
        this.mode = mode;
        buffer = new Vector();
        if (mode == MODE_STRUCTURE) {
            buffer.addElement(new EndPoint().setArgs(new short[]{0, 0}));
        } else if (mode == MODE_LEVEL) {
            buffer.addElement(new LevelStart().setArgs(new short[]{200, -200}));
        }
        onUpdate();
    }

    public void place(short id, short x, short y) throws IllegalArgumentException {
    	isEditing = false;
        placingNow = Element.createTypedInstance(id);
        Logger.log("Placing " + id);
        nextPointHandler = new NextPointHandler();
        handleNextPoint(x, y, false);
        add(placingNow);
        onUpdate();
        handleNextPoint(x, y, true);
    }

    public void edit(Element e, int step) {
    	Element[] elements = getElementsAsArray();
    	for (int i = 0; i < elements.length; i++) {
			if (elements[i] != null && elements[i].equals(e)) {
				edit(i, step);
				break;
			}
			
		}
    }

    public void edit(int i, int step) {
    	placingNow = getElementsAsArray()[i];
    	nextPointHandler = new NextPointHandler(step);
    	isEditing = true;
    }

    public void handleNextPoint(short x, short y, boolean isPreview) {
        if (placingNow == null) {
            return;
        }

        nextPointHandler.showingPreview = isPreview;
        nextPointHandler.handleNextPoint(x, y);

        // stop if the last step is done
        if (nextPointHandler.step >= placingNow.getStepsToPlace() || isEditing) {
            if (!isPreview) {
            	if (placingNow.getID() != Element.END_POINT) {
            		recalculateEndPoint();
            	}
                placingNow = null;
                nextPointHandler = null;
                onUpdate();
            }
        }
    }

    public void add(Element element) {
        if (element != null && !(element instanceof EndPoint)) {
            buffer.addElement(element);
        }
    }

    public short[] asShortArray() {
    	int carriage = 0;
        // {file format version, count of elements, ...data..., eof mark}
        short[] data = new short[1 + 1 + getDataLengthInShorts() + 1];

        data[carriage] = 1;
        carriage++;
        data[carriage] = (short) getElementsCount();
        carriage++;

        for (int i = 0; i < getElementsCount(); i++) {
            Element element = (Element) buffer.elementAt(i);

            short[] elementArgs = element.getAsShortArray();
            for (int j = 0; j < elementArgs.length; j++) {
                data[carriage] = elementArgs[j];
                carriage++;
            }
        }

        data[carriage] = 0;
        return data;
    }

    public short[][] asShortArrays() {
        short[][] data = new short[getElementsCount()][];
        for (int i = 0; i < data.length; i++) {
            data[i] = ((Element) buffer.elementAt(i)).getAsShortArray();
        }
        return data;
    }

    public void saveToFile(String path) throws IOException, SecurityException {
        FileUtils.saveShortArrayToFile(asShortArray(), path);
    }

    public void loadFile(String path) {
        try {
            Element[] elements = MGStructs.readMGStruct(path);
            if (elements == null) {
                Logger.log("error: elements array is null");
                return;
            }
            setElements(elements);
            this.path = path;
        } catch(Exception ex) {
            Logger.log(ex);
        }
    }

    public String getFilePath() {
        return path;
    }

    public void setFilePath(String path) {
    	this.path = path;
    }

    public void setElements(Element[] elements) {
    	buffer = new Vector();
    	for (int i = 0; i < elements.length; i++) {
            if (elements[i] != null) {
                buffer.addElement(elements[i]);
            } else {
                Logger.log("elements["+i+"] is null. skipping");
            }
        }
        onUpdate();
    }

    public void remove(int i) {
        if (buffer.elementAt(i) instanceof EndPoint || buffer.elementAt(i) instanceof LevelStart) {
            return;
        }

        boolean needToRecalculateEndPoint = true;
//        try {
//            (EndPoint) buffer.elementAt(0)).getArgs()
//            needToRecalculateEndPoint = ( == ((Element) buffer.elementAt(i)).getEndPoint();
//        } catch (Exception ex) {
//            Logger.log(ex);
//        }
//        Logger.log("needToRecalculateEndPoint=" + needToRecalculateEndPoint);
        buffer.removeElementAt(i);
        if (needToRecalculateEndPoint) {
            recalculateEndPoint();
        }
        onUpdate();
    }

    public void remove(Element e) {
    	remove(findInBuffer(e));
    }

    public int findInBuffer(Element e) {
    	Element[] elements = getElementsAsArray();
    	for (int i = 0; i < elements.length; i++) {
			if (elements[i] != null && elements[i].equals(e)) {
				return i;
			}
		}
    	throw new IllegalArgumentException("Element " + e + " not found in buffer");
    }

    public void recalculateEndPoint() {
    	if (mode == MODE_LEVEL) {
    		return;
    	}

    	Element[] elements = getElementsAsArray();
        EndPoint endPoint = (EndPoint) elements[0];
        endPoint.setArgs(EndPoint.findEndPoint(elements));
    }

    public Vector getElements() {
        return buffer;
    }

    public int getElementsCount() {
        return buffer.size();
    }

    public Element[] getElementsAsArray() {
        Element[] elements = new Element[getElementsCount()];
        for (int i = 0; i < getElementsCount(); i++) {
            elements[i] = (Element) buffer.elementAt(i);
        }
        return elements;
    }

    public int getDataLengthInShorts() {
        int l = 0;
        for (int i = 0; i < getElementsCount(); i++) {
            l += 1/*id*/ + ((Element) buffer.elementAt(i)).getArgsCount()/*args*/;
        }
        return l;
    }

    public String getPlacingInfo() {
    	if (nextPointHandler != null && placingNow != null) {
    		return nextPointHandler.getCurrentPlacementStep().getCurrentStepInfo();
    	} else {
    		return "";
    	}
    }

    public int getMode() {
    	return mode;
    }

    public void setMode(int mode) {
        Vector oldBuffer = buffer;
        reset(mode);
        for (int i = 1; i < oldBuffer.size(); i++) {
            buffer.addElement(oldBuffer.elementAt(i));
        }
        onUpdate();
    }

    public abstract void onUpdate();

    private class NextPointHandler {
        public int step;
        public boolean showingPreview = false;

        public NextPointHandler(int step) {
        	this.step = step;
		}

        public NextPointHandler() {
        	this(0);
		}

        void handleNextPoint(short x, short y) {
            try {
                getCurrentPlacementStep().place(x, y);
            } catch (Exception ex) {
                Logger.log(ex);
            }
            if (!showingPreview) {
                step++;
            }
        }

        public PlacementStep getCurrentPlacementStep() {
        	return placingNow.getAllSteps()[step];
        }

    }
}
