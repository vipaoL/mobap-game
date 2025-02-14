package mobileapplication3.editor.elements;

import mobileapplication3.ui.Property;

public class Trampoline extends AbstractRectBodyElement {

	private short elasticity = 100;

	public Trampoline() {
		super(0xffaa00);
	}

	public Element setArgs(short[] args) {
		elasticity = args[5];
		return super.setArgs(args);
	}

	public short[] getArgsValues() {
		return new short[] {x, y, l, thickness, angle, elasticity};
	}

	public Property[] getArgs() {
		return concatArrays(super.getArgs(), new Property[] {
				new Property("Elasticity") {
					public void setValue(short value) {
						elasticity = value;
					}

					public short getValue() {
						return elasticity;
					}

					public short getMinValue() {
						return 0;
					}

					public short getMaxValue() {
						return 1000;
					}
				}
		});
    }

	public short getID() {
		return TRAMPOLINE;
	}

	public String getName() {
		return "Trampoline";
	}

}
