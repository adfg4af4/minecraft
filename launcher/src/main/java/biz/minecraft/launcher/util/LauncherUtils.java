package biz.minecraft.launcher.util;

import biz.minecraft.launcher.Configuration;
import biz.minecraft.launcher.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipFile;

public class LauncherUtils {

    private final static Logger logger = LoggerFactory.getLogger(LauncherUtils.class);

    /**
     * One-line transforming string to URL.
     *
     * @param string
     * @return URL
     */
    public static URL getURL(String string) {
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            logger.error("Failed generating URL from a string.", e);
            return null;
        }
    }

    /**
     * One-line transforming File to Zip File.
     *
     * @param file
     * @return URL
     */
    public static ZipFile getZipFile(File file) {
        try {
            return new ZipFile(file);
        } catch (IOException e) {
            logger.error("Failed reading zip file.", e);
            return null;
        }
    }

    public static void getLauncherInfo(Logger logger) {
        logger.debug("Mincraft.biz Launcher {}", Configuration.CURRENT_VERSION);
        logger.debug("Working directory: '{}'", LauncherUtils.getWorkingDirectory());
        logger.debug("Java path: {}", OperatingSystem.getCurrentPlatform().getJavaDir());
        logger.debug("Operating System: {}", OperatingSystem.getCurrentPlatform().toString());
    }

    /**
     * Get Minecraft.biz root folder.
     *
     * @return File – path to Minecraft.biz root folder.
     */
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
