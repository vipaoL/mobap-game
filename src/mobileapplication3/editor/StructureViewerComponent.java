package mobileapplication3.editor;

import mobileapplication3.editor.elements.Element;
import mobileapplication3.editor.elements.EndPoint;
import mobileapplication3.editor.elements.StartPoint;
import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.UIComponent;

public class StructureViewerComponent extends UIComponent {

	protected static final int MIN_ZOOM_OUT = 8, MAX_ZOOM_OUT = 200000;

    protected int offsetX, offsetY, zoomOut = 8192;
	protected short start, end;

	protected Element[] elements = new Element[0];

	public StructureViewerComponent() {
		setBgColor(COLOR_ACCENT_MUTED);
	}

	public StructureViewerComponent(Element[] elements) {
		this();
		setElements(elements);
	}

	public void setElements(Element[] elements) {
		this.elements = elements;
		start = StartPoint.findStartPoint(elements)[0];
		end = EndPoint.findEndPoint(elements)[0];
	}

	public boolean canBeFocused() {
		return false;
	}

	protected boolean handlePointerClicked(int x, int y) {
		return false;
	}

	protected boolean handleKeyPressed(int keyCode, int count) {
		return false;
	}

	protected void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
		try {
			drawElements(g, x0, y0, elements);
		} catch (Exception ex) {
			g.drawString(ex.toString(), x0, y0, TOP | LEFT);
		}
	}

	protected void drawElements(Graphics g, int x0, int y0, Element[] elements) {
        for (int i = 0; i < elements.length; i++) {
        	try {
	        	elements[i].paint(g, zoomOut, x0 + offsetX, y0 + offsetY, true, false);
        	} catch (Exception ignored) { }
        }
    }

	protected void onSetBounds(int x0, int y0, int w, int h) {
		zoomOut = Mathh.constrain(MIN_ZOOM_OUT, 4000000 / Math.min(w, h), MAX_ZOOM_OUT);
		zoomOut = Math.max(zoomOut, 1000*(end - start)*3/w/2);
		offsetX = w/2;
		if (zoomOut != 0) {
			offsetX -= (end + start) / 2 * 1000 / zoomOut;
			}
		offsetY = h/2;
	}

}
