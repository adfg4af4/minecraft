package biz.minecraft.launcher.updater;

import biz.minecraft.launcher.Main;
import biz.minecraft.launcher.updater.launcher.Version;
import biz.minecraft.launcher.util.Helper;
import biz.minecraft.launcher.util.Launcher;
import biz.minecraft.launcher.util.OperatingSystem;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LauncherUpdater implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(LauncherUpdater.class);
    private final List<String> args;
    private final Thread thread;

    /**
     * Launcher-Updater Thread Constructor.
     *
     * Starts the thread and waits until it dies.
     * Handles command-line arguments.
     *
     * @param args Launcher starts a newly downloaded version from the temp directory with its path as a command-line argument.
     */
    public LauncherUpdater(String[] args) {

        this.args = Arrays.asList(args); // Passing possible arguments

        thread = new Thread(this, "Launcher-Updater");
        thread.start(); // Native method starts run method in new thread

        try {
            thread.join(); // Waits for this thread to die.
        } catch (InterruptedException e) {
            logger.warn("Main Thread has been interrupted. Failed to join Launcher-Updater Thread.", e);
            JOptionPane.showConfirmDialog(null, "Ошибка обновления, пожалуйста перезапустите лаунчер.", "Minecraft.biz Launcher", JOptionPane.OK_OPTION);
            System.exit(0);
        }
    }

    /**
     * Launcher-Updater Thread.
     *
     * If the Launcher is outdated (compare current version with https://cloud.minecraft.biz/launcher/version.json)
     *
     * - Download new version into temp directory
     * - Start new version passing path of old version as a command-line argument
     * - Die
     *
     * If the Launcher is not outdated and one command-line argument handled
     *
     * - Check if process has been started with one command-line argument (old versions path expected as a 0-index array element)
     * - Delete old version
     * - Copy the new version to replace the old one
     * - Delete temp directory
     * - Start new version from old place
     * - Die
     */
    public void run() {

        Version newLauncherVersion = Launcher.getVersion(); // Deserialized version.json object
        URL newLauncherURL = newLauncherVersion.getUrl(); // URL for downloading new launcher
        File newLauncherTempPath = Helper.getWorkingDirectory().toPath().resolve(newLauncherVersion.getPath()).toFile(); // Temp path for new launcher

        if (Launcher.isOutdated()) {

            // Launcher is outdated

            logger.debug("Launcher version is outdated. Preparing for update.");

            // Downloading new version to the temp folder

            while (true) {
                try {
                    FileUtils.copyURLToFile(newLauncherURL, newLauncherTempPath);
                    logger.debug("Downloaded " + newLauncherURL + " -> " + newLauncherTempPath);
                    break;
                } catch (IOException e) {
                    logger.warn("Download failed " + newLauncherURL + " -> " + newLauncherTempPath, e);
                    int userChoice = JOptionPane.showConfirmDialog(null, "Ошибка загрузки новой версии лаунчера, повторить?", "Minecraft.biz Launcher", JOptionPane.YES_NO_OPTION);

                    if (userChoice == 0) {
                        continue;
                    } else {
                        System.exit(0);
                    }
                }
            }

            // Starting new version passing old version's path as a command-line argument

            String oldLauncherPath = null;

            try {
                oldLauncherPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            } catch (URISyntaxException e) {
                logger.warn("Launcher path URL is not formatted to be converted to a URI.", e);
                JOptionPane.showConfirmDialog(null, "Ошибка определения пути лаунчера, пожалуйста обратитесь к администратору.", "Minecraft.biz Launcher", JOptionPane.OK_CANCEL_OPTION);
                System.exit(0);
            }

            ArrayList<String> params = new ArrayList<>();

            params.add(OperatingSystem.getCurrentPlatform().getJavaDir()); // Java path (string)
            params.add("-jar");
            params.add(newLauncherTempPath.toString()); // Start new launcher from the temp folder
            params.add(oldLauncherPath); // Pass old launcher's path as a command-line argument

            ProcessBuilder pb = new ProcessBuilder(params);

            Process process = null;

            try {
                process = pb.start();
            } catch (IOException e) {
                logger.warn("Error starting new launcher from the temp folder.", e);
                JOptionPane.showConfirmDialog(null, "Ошибка запуска новой версии лаунчера из временной папки, пожалуйста обратитесь к администратору.", "Minecraft.biz Launcher", JOptionPane.OK_CANCEL_OPTION);
                System.exit(0);
            }

            // Die

            logger.debug("New version successfully started from temp folder. Dying.");

            System.exit(0);

        } else if (args.size() == 1) {

            // Launcher is not outdated and one command-line argument handled

            File oldLauncherPath = null;

            try {
                oldLauncherPath = new File(args.get(0));
            } catch (Exception e) {
                logger.warn("Failed to parse old version launcher path from command-line argument.", e);
                JOptionPane.showConfirmDialog(null, "Ошибка запуска новой версии лаунчера из временной папки, пожалуйста обратитесь к администратору.", "Minecraft.biz Launcher", JOptionPane.OK_CANCEL_OPTION);
                System.exit(0);
            }

            // Delete old version

            try {
                FileUtils.forceDelete(oldLauncherPath);
                logger.debug("Deleted " + oldLauncherPath);
            } catch (IOException e) {
                logger.warn("Failed to delete " + oldLauncherPath, e);
                JOptionPane.showConfirmDialog(null, "Не удалось удалить старую вресию лаунчера, пожалуйста обратитесь к администратору.", "Minecraft.biz Launcher", JOptionPane.OK_CANCEL_OPTION);
                System.exit(0);
            }

            // Copy the new version to replace the old one

            try {
                FileUtils.copyFile(newLauncherTempPath, oldLauncherPath);
                logger.debug("Copied " + newLauncherTempPath + " -> " + oldLauncherPath);
            } catch (IOException e) {
                logger.warn("Failed to copy " + newLauncherTempPath + " -> " + oldLauncherPath, e);
                JOptionPane.showConfirmDialog(null, "Не удалось скопировать новую версию лаунчера на старое место, пожалуйста обратитесь к администратору.", "Minecraft.biz Launcher", JOptionPane.OK_CANCEL_OPTION);
                System.exit(0);
            }

            // Delete temp directory

            try {
                FileUtils.forceDelete(newLauncherTempPath);
                logger.debug("Deleted " + newLauncherTempPath);
            } catch (IOException e) {
                logger.warn("Failed to delete new launcher version from temp folder: " + newLauncherTempPath, e);
            }

            // Start new version from old place

            ArrayList<String> params = new ArrayList<>();

            params.add(OperatingSystem.getCurrentPlatform().getJavaDir()); // Java path (string)
            params.add("-jar");
            params.add(oldLauncherPath.getPath()); // Start new launcher from old place

            ProcessBuilder pb = new ProcessBuilder(params);

            Process process = null;

            try {
                process = pb.start();
            } catch (IOException e) {
                logger.warn("Error starting new launcher from old place.", e);
                JOptionPane.showConfirmDialog(null, "Ошибка запуска новой версии лаунчера на старом месте, пожалуйста обратитесь к администратору.", "Minecraft.biz Launcher", JOptionPane.OK_CANCEL_OPTION);
                System.exit(0);
            }

            // Die

            logger.debug("New version successfully started from old version's folder. Dying.");

            System.exit(0);

        }
    }
}
