package mobileapplication3.editor.elements;

public class LevelFinish extends AbstractRectBodyElement {

	public LevelFinish() {
		super(0x00ff00);
	}

	public short getID() {
		return LEVEL_FINISH;
	}

	public String getName() {
		return "Level Finish";
	}

}
