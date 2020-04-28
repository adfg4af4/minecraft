package biz.minecraft.launcher;

import biz.minecraft.launcher.entity.Version;
import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Launcher {

    private final static Logger logger = LoggerFactory.getLogger(Launcher.class);

    private final static Gson gson = new Gson();

    private final static Version version = getVersion();

    /**
     * Get a deserialized launcher version object.
     * https://cloud.minecraft.biz/launcher/version.json
     *
     * @return Latest launcher's version data.
     */
    public static Version getVersion() {

        while (true) {

            try (InputStream is = new URL(Configuration.VERSION_URL).openStream()) {

                String version = IOUtils.toString(is, StandardCharsets.UTF_8);

                return gson.fromJson(version, Version.class);

            } catch (IOException e) {

                logger.warn("Failed to get launcher version from: " + Configuration.VERSION_URL, e);
                int userChoice = JOptionPane.showConfirmDialog(null, "Ошибка подключения, повторить?", "Minecraft.biz Launcher", JOptionPane.YES_NO_OPTION);

                if (userChoice == 0) {
                    continue;
                } else {
                    System.exit(0);
                }
            }
        }
    }

    /**
     * Compare the current version with the latest one.
     *
     * @return True or False.
     */
    public static boolean isOutdated() {

        if (Configuration.CURRENT_VERSION == version.getVersion()) {
            return false;
        }

        return true;
    }
}
