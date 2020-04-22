package biz.minecraft.launcher.util;

import biz.minecraft.launcher.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipFile;

public class Helper {

    private final static Logger logger = LoggerFactory.getLogger(Helper.class);

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

    /**
     * Silent closing closeable's.
     *
     * @param closeable
     */
    public static void closeSilently(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (IOException ex) {}
        }
    }

    public static void logLauncherInfo(Logger logger) {
        logger.debug("Mincraft.biz launcher {}", Configuration.CURRENT_VERSION);
        logger.debug("Supported operating system: {}", OperatingSystem.getCurrentPlatform().isSupported());
        logger.debug("Auto-generated Java path: {}", OperatingSystem.getCurrentPlatform().getJavaDir());
        logger.debug("Game directory for current OS: '{}'", Helper.getWorkingDirectory());
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
