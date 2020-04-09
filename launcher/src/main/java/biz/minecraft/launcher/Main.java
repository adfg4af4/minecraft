package biz.minecraft.launcher;

import biz.minecraft.launcher.updater.DownloadTask;
import biz.minecraft.launcher.updater.Updater;
import biz.minecraft.launcher.updater.version.Download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

// import java.awt.*;
import java.io.File;
// import java.net.URL;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static LauncherWindow launcherWindow;

    public static void main(String[] args) throws Exception {

        logger.debug("Mincraft.biz launcher {}", Config.LAUNCHER_VERSION);
        logger.debug("Supporting your system: {}", OperatingSystem.getCurrentPlatform().isSupported());
        logger.debug("Expected Java path: {}", OperatingSystem.getCurrentPlatform().getJavaDir());
        logger.debug("Game directory based on your OS: '{}'", getWorkingDirectory());

        // Set system laf
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            logger.error("Can't set system look and feel!", ex);
        }

        // Check app version
        if (!Launcher.isLastVersion()) {
            logger.debug("Update founded! New version: {}", Launcher.getVersionState());

            int result = JOptionPane.showConfirmDialog(null, "Do you want to update?", "New version founded", JOptionPane.YES_NO_OPTION);
            if (result == 0) {
                // TODO: Autoupdate things
            }

            System.exit(0);
        }

        // Init window
        launcherWindow = new LauncherWindow();
        launcherWindow.setVisible(true);

        int result = JOptionPane.showConfirmDialog(null, "Do you want to update client?", "Mincraft.biz launcher", JOptionPane.YES_NO_OPTION);
        if (result == 0) {
            // Update game cache
            Updater updater = new Updater();
            updater.setName("Updater");
            updater.start();
        }

        // Launch game
        // GameLauncher game = new GameLauncher();
    }

    public static File getWorkingDirectory() {

        final String userHome = System.getProperty("user.home", ".");
        File workingDirectory = null;

        switch (OperatingSystem.getCurrentPlatform()) {
            case LINUX: {
                workingDirectory = new File(userHome, "Minecraft.biz/");
                break;
            }
            case WINDOWS: {
                final String applicationData = System.getenv("APPDATA");
                final String folder = (applicationData != null) ? applicationData : userHome;
                workingDirectory = new File(folder, "Minecraft.biz/");
                break;
            }
            case OSX: {
                workingDirectory = new File(userHome, "Library/Application Support/Minecraft.biz");
                break;
            }
            default: {
                workingDirectory = new File(userHome, "minecraft.biz/");
                break;
            }
        }

        return workingDirectory;
    }
}
