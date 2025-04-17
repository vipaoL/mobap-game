package com.vipaol;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import mobileapplication3.game.DebugMenu;
import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.MobappDesktopMain;
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
        OptionSpec<Void> debugOpt = parser.acceptsAll(Arrays.asList("debug", "d"), "Enable debug mode");
        OptionSpec<String> workdirOpt = parser.accepts("workdir", "Directory for game settings, records and custom content")
                .withRequiredArg()
                .describedAs("directory")
                .defaultsTo(FileUtils.getStoragePath());
        parser.acceptsAll(Arrays.asList("help", "h", "?"), "Show help").forHelp();

        try {
            OptionSet options = parser.parse(args);
            if (options.has("help")) {
                parser.printHelpOn(System.out);
                System.exit(0);
            }
            DebugMenu.isDebugEnabled = options.has(debugOpt);
            FileUtils.setStoragePath(options.valueOf(workdirOpt));
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