import mobileapplication3.platform.Platform;
import mobileapplication3.platform.PlatformSettings;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.ui.*;

public class FontSizeSettingsScreen extends Page {
    private final Property fontSize;

    public FontSizeSettingsScreen() {
        super("Change font size");
        fontSize = new Property("Base font size").setMinValue(2).setMaxValue(192);
        fontSize.setValue(PlatformSettings.getFontSize());
    }

    @Override
    protected Button[] getActionButtons() {
        return new Button[] {
                new Button("Save") {
                    @Override
                    public void buttonPressed() {
                        PlatformSettings.setFontSize(fontSize.getValue());
                        Platform.refreshPlatformSettings();
                        IContainer p = parent;
                        ((IPopupFeedback) p).closePopup();
                        ((Container) p).refreshSizes();
                    }
                },
                new BackButton((IPopupFeedback) parent).setTitle("Cancel")
        };
    }

    @Override
    protected IUIComponent initAndGetPageContent() {
        return new Container() {
            final Slider slider = new Slider(fontSize);
            final CanvasComponent canvas = new CanvasComponent() {
                @Override
                protected void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
                    g.setColor(COLOR_ACCENT_MUTED);
                    g.fillRect(x0, y0, w, h);
                    g.setColor(0x8888ff);

                    g.setFont(new DemoFont(Font.SIZE_SMALL, fontSize.getValue()));
                    g.drawString("Small", x0 + w / 4, y0 + h / 4, VCENTER | HCENTER);

                    g.setFont(new DemoFont(Font.SIZE_MEDIUM, fontSize.getValue()));
                    g.drawString("Medium", x0 + w / 2, y0 + h / 2, VCENTER | HCENTER);

                    g.setFont(new DemoFont(Font.SIZE_LARGE, fontSize.getValue()));
                    g.drawString("Large", x0 + w * 3 / 4, y0 + h * 3 / 4, VCENTER | HCENTER);
                }

                @Override
                public boolean canBeFocused() {
                    return false;
                }

                @Override
                protected void onSetBounds(int x0, int y0, int w, int h) { }
            };

            @Override
            public void init() {
                setComponents(new IUIComponent[]{canvas, slider});
                super.init();
            }

            @Override
            protected void onSetBounds(int x0, int y0, int w, int h) {
                slider.setSize(w, Font.getDefaultFontHeight() * 3).setPos(x0, y0 + h, BOTTOM | LEFT);
                canvas.setSize(w, slider.getTopY() - y0).setPos(x0, y0, TOP | LEFT);
            }
        };
    }

    private static class DemoFont extends Font {
        public DemoFont(int size, int baseFontSize) {
            super(size, baseFontSize);
        }
    }
}
