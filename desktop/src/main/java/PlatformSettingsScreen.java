import mobileapplication3.game.SettingsScreen;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.PlatformSettings;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.*;

public class PlatformSettingsScreen extends Page {
    private static final String TITLE = "Desktop settings";
    private Button fontSizeButton = null;

    public PlatformSettingsScreen() {
        super(TITLE);
    }

    @Override
    public IUIComponent refreshSizes() {
        fontSizeButton.setTitle(fontSizeButton.getTitle());
        return super.refreshSizes();
    }

    @Override
    protected Button[] getActionButtons() {
        return new Button[] {
                new Button("Back") {
                    @Override
                    public void buttonPressed() {
                        RootContainer.setRootUIComponent(new SettingsScreen());
                    }
                }.setBindedKeyCode(Keys.KEY_NUM0)
        };
    }

    @Override
    protected IUIComponent initAndGetPageContent() {
        return new ButtonCol(new Button[]{
                new Switch("Fullscreen mode"
                        + (PlatformSettings.fullscreenModeOverride == PlatformSettings.UNDEF ? "" : "\n(Is set by command line arguments)")) {
                    @Override
                    public boolean getValue() {
                        return PlatformSettings.getFullscreenMode();
                    }

                    @Override
                    public void setValue(boolean value) {
                        PlatformSettings.setFullscreenMode(value);
                        Platform.refreshPlatformSettings();
                    }
                }.setIsActive(PlatformSettings.fullscreenModeOverride == PlatformSettings.UNDEF),
                fontSizeButton = new Button(null) {
                    @Override
                    public void buttonPressed() {
                        showPopup(new FontSizeSettingsScreen());
                    }
                    @Override
                    public String getTitle() {
                        return "Font size: " + PlatformSettings.getFontSize()
                                + (PlatformSettings.fontSizeOverride == PlatformSettings.UNDEF ? "" : "\n(Is set by command line arguments)");
                    }
                }.setIsActive(PlatformSettings.fontSizeOverride == PlatformSettings.UNDEF),
                new Switch("Black and white mode"
                        + (PlatformSettings.blackAndWhiteModeOverride == PlatformSettings.UNDEF ? "" : "\n(Is set by command line arguments)")) {
                    @Override
                    public boolean getValue() {
                        return PlatformSettings.getBlackAndWhiteMode();
                    }

                    @Override
                    public void setValue(boolean value) {
                        PlatformSettings.setBlackAndWhiteMode(value);
                        Platform.refreshPlatformSettings();
                    }
                }.setIsActive(PlatformSettings.blackAndWhiteModeOverride == PlatformSettings.UNDEF),
                new Button("Reset desktop settings") {
                    @Override
                    public void buttonPressed() {
                        PlatformSettings.reset();
                        Platform.refreshPlatformSettings();
                        refreshSizes();
                    }
                }
        });
    }

    @Override
    public String toString() {
        return TITLE;
    }
}
