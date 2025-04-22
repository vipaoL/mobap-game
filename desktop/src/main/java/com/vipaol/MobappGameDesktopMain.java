package com.vipaol;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import mobileapplication3.game.DebugMenu;
import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.MobappDesktopMain;
import mobileapplication3.platform.PlatformSettings;
import mobileapplication3.platform.ui.RootContainer;

import java.io.IOException;
import java.util.Arrays;

public class MobappGameDesktopMain extends MobappDesktopMain {
    public static void main(String[] args) {
        parseArgs(args);
        new MobappGameDesktopMain(args);
    }

    private static void parseArgs(String[] args) {
        OptionParser parser = new OptionParser();
        OptionSpec<Void> debug = parser.acceptsAll(Arrays.asList("debug", "d"), "Enable debug mode");
        OptionSpec<Void> verbose = parser.acceptsAll(Arrays.asList("verbose", "v"), "Print logs to stdout");
        OptionSpec<String> workdirOpt = parser.accepts("workdir", "Directory for game settings, records and custom content")
                .withRequiredArg()
                .describedAs("directory")
                .defaultsTo(FileUtils.getStoragePath());
        OptionSpec<Integer> fontSize = parser.accepts("font-size", "Override the default font size")
                .withRequiredArg()
                .ofType(Integer.class)
                .defaultsTo(PlatformSettings.getFontSize());
        OptionSpec<Void> fullscreenMode = parser.accepts("fullscreen", "Enable fullscreen mode");
        OptionSpec<Void> disableFullscreenMode = parser.accepts("no-fullscreen", "Disable fullscreen mode");
        parser.acceptsAll(Arrays.asList("help", "h", "?"), "Show help").forHelp();

        try {
            OptionSet options = parser.parse(args);
            if (options.has("help")) {
                parser.printHelpOn(System.out);
                System.exit(0);
            }
            DebugMenu.isDebugEnabled = options.has(debug);
            Logger.logToStdout(options.has(verbose));
            FileUtils.setStoragePath(options.valueOf(workdirOpt));
            if (options.has(fullscreenMode)) {
                PlatformSettings.setFullscreenModeOverride(true);
            } else if (options.has(disableFullscreenMode)) {
                PlatformSettings.setFullscreenModeOverride(false);
            }
            if (options.has(fontSize)) {
                PlatformSettings.setFontSizeOverride(options.valueOf(fontSize));
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.out.println();
            try {
                parser.printHelpOn(System.out);
            } catch (IOException ex) {
                System.err.println("Use --help for usage information.");
            }
            System.exit(1);
        }
    }

    public MobappGameDesktopMain(String[] args) {
        super(args);
        RootContainer.setRootUIComponent(new MenuCanvas());
    }
}