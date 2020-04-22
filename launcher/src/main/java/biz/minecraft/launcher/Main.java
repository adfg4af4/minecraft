package biz.minecraft.launcher;

import biz.minecraft.launcher.updater.GameUpdater;
import biz.minecraft.launcher.updater.LauncherUpdater;
import biz.minecraft.launcher.layout.UpdaterLayout;
import biz.minecraft.launcher.util.Helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;

/**
 * This is the main entry point of Minecraft.biz Launcher.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static UpdaterLayout updaterLayout;

    public static void main(String[] args) {

        final GameUpdater gameUpdater;

        Helper.logLauncherInfo(logger);

        /**
         * Updating Launcher
         *
         * Causes this thread to begin execution and waits for this thread to die.
         * TODO: Describe passing args array
         */

        final LauncherUpdater launcherUpdater = new LauncherUpdater(args);

        // Авторизация

        /**
         * Updating Game
         *
         * Causes this thread to begin execution and waits for this thread to die.
         */

        // Вход в игру


        // Set system laf
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            logger.error("Can't set system look and feel!", ex);
        }

        // Init window
        updaterLayout = new UpdaterLayout();
        updaterLayout.setVisible(true);

        int result = JOptionPane.showConfirmDialog(null, "Do you want to update client?", "Mincraft.biz launcher", JOptionPane.YES_NO_OPTION);

        if (result == 0) {
            // Update game cache
            gameUpdater = new GameUpdater();
        }

        // Launch game
        // GameLauncher game = new GameLauncher();
    }
}
