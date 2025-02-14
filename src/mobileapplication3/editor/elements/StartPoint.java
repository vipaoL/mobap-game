package mobileapplication3.editor.elements;

public class StartPoint {
	private StartPoint() { }
	
	public static void moveToZeros(Element[] elements) {
		short[] startPoint = findStartPoint(elements);
		for (int i = 0; i < elements.length; i++) {
			elements[i].move((short) -startPoint[0], (short) -startPoint[1]);
		}
	}
	
	public static boolean checkStartPoint(Element[] elements) {
		short[] startPoint = findStartPoint(elements);
		return startPoint[0] == 0 && startPoint[1] == 0;
	}
	
	public static short[] findStartPoint(Element[] elements) {
		if (elements == null || elements.length == 0) {
			return new short[] {0, 0};
		}
		short[] ret = elements[0].getStartPoint();
		for (int i = 1; i < elements.length; i++) {
			short[] currElemStartPoint = elements[i].getStartPoint();
			if (compareAsStartPoints(ret[0], ret[1], currElemStartPoint[0], currElemStartPoint[1])) {
				ret = currElemStartPoint;
			}
		}
		return ret;
	}
	
	public static short[] compareAsStartPoints(short[] a, short[] b) {
		if (compareAsStartPoints(a[0], a[1], b[0], b[1])) {
			return b;
		} else {
			return a;
		}
	}
	
	/**
     * @return true if the second point is more likely the start point than the first one
     */
    public static boolean compareAsStartPoints(short x, short y, short currCheckingX, short currCheckingY) {
        if (currCheckingX <= x) {
            if (currCheckingX < x | (currCheckingY < y)) {
                return true;
            }
        }
        return false;
    }
}
