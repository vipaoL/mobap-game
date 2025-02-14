package mobileapplication3.game;

import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.CanvasComponent;

public class LevelCompletedScreen extends CanvasComponent {
    private final GameplayCanvas game;

    public LevelCompletedScreen(GameplayCanvas game) {
        this.game = game;
        setBgColor(COLOR_TRANSPARENT);
    }

    protected void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        g.setColor(0x222222);
        g.fillTriangle(x0, y0, x0+w, y0+h, x0+w/2, y0);
        g.setColor(0x5555ff);
        g.setFontSize(Font.SIZE_LARGE);
        g.drawString("Level completed!", x0 + w/2, y0 + h/3, VCENTER | HCENTER);
    }

    public boolean canBeFocused() {
        return true;
    }

    protected void onSetBounds(int x0, int y0, int w, int h) { }

    public boolean handlePointerClicked(int x, int y) {
        closePopup();
        game.stop(true, false);
        return true;
    }

    public boolean handleKeyPressed(int keyCode, int count) {
        closePopup();
        game.stop(true, false);
        return true;
    }

    public void init() { }
}
