package com.vipaol;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import mobileapplication3.editor.EditorSettings;
import mobileapplication3.editor.EditorUI;
import mobileapplication3.editor.MGStructs;
import mobileapplication3.editor.elements.Element;
import mobileapplication3.game.DebugMenu;
import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.MobappDesktopMain;
import mobileapplication3.platform.PlatformSettings;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.IUIComponent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MobappGameDesktopMain extends MobappDesktopMain {

    private IUIComponent root = new MenuCanvas();

    public static void main(String[] args) {
        new MobappGameDesktopMain(args);
    }

    protected void parseArgs(String[] args) {
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
        OptionSpec<Void> blackAndWhiteMode = parser.acceptsAll(Arrays.asList("black-and-white", "bw"), "Enable black and white mode");
        OptionSpec<Void> disableBlackAndWhiteMode = parser.acceptsAll(Arrays.asList("no-black-and-white", "no-bw"), "Disable black and white mode");
        parser.acceptsAll(Arrays.asList("help", "h", "?"), "Show help").forHelp();
        parser.nonOptions("Path to level or structure to open in view mode")
                .ofType(File.class)
                .describedAs("*.mgstruct or *.mglvl");

        try {
            OptionSet options = parser.parse(args);
            if (options.has("help")) {
                parser.printHelpOn(System.out);
                System.exit(0);
            }
            DebugMenu.isDebugEnabled = options.has(debug);
            Logger.logToStdout(options.has(verbose) || DebugMenu.isDebugEnabled);
            FileUtils.setStoragePath(options.valueOf(workdirOpt));
            if (options.has(fullscreenMode)) {
                PlatformSettings.setFullscreenModeOverride(true);
            } else if (options.has(disableFullscreenMode)) {
                PlatformSettings.setFullscreenModeOverride(false);
            }
            if (options.has(blackAndWhiteMode)) {
                PlatformSettings.setBlackAndWhiteModeOverride(true);
            } else if (options.has(disableBlackAndWhiteMode)) {
                PlatformSettings.setBlackAndWhiteModeOverride(false);
            }
            if (options.has(fontSize)) {
                PlatformSettings.setFontSizeOverride(options.valueOf(fontSize));
            }
            List<File> nonOptions = (List<File>) options.nonOptionArguments();
            if (nonOptions != null && !nonOptions.isEmpty()) {
                if (nonOptions.size() > 1) {
                    throw new IllegalArgumentException("Too many arguments");
                } else {
                    root = openFile(nonOptions.get(0));
                }
            }
        } catch (Exception e) {
            Logger.log(e);
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

    private static IUIComponent openFile(File file) {
        Element[] elements = MGStructs.readMGStruct(file.getPath());
        if (elements == null) {
            elements = new Element[0];
        }
        String name = file.getName();
        int mode = name.endsWith(".mgstruct") ? EditorUI.MODE_STRUCTURE : EditorUI.MODE_LEVEL;
        String newPath = (mode == EditorUI.MODE_STRUCTURE ?
                EditorSettings.getStructsFolderPath() :
                EditorSettings.getLevelsFolderPath())
                + FileUtils.SEP + name;
        return new EditorUI(mode, elements, newPath).setViewMode(true);
    }

    public MobappGameDesktopMain(String[] args) {
        super(args);
        RootContainer.setRootUIComponent(root);
    }
}