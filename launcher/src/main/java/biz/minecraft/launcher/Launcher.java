package biz.minecraft.launcher;

import biz.minecraft.launcher.json.LauncherVersion;
import biz.minecraft.launcher.layout.login.json.LauncherProfile;
import biz.minecraft.launcher.util.LauncherUtils;
import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Launcher {

    private final static Logger logger = LoggerFactory.getLogger(Launcher.class);
    private final static Gson gson = new Gson();
    private final static LauncherVersion version = getVersion();
    private final static File profile = new File(LauncherUtils.getWorkingDirectory(), Constants.LAUNCHER_PROFILE);

    /**
     * Get a deserialized launcher version object.
     * https://cloud.minecraft.biz/launcher/version.json
     *
     * @return Latest launcher's version data.
     */
    public static LauncherVersion getVersion() {

        while (true) {

            try (InputStream is = new URL(Constants.LAUNCHER_VERSION).openStream()) {

                String version = IOUtils.toString(is, StandardCharsets.UTF_8);

                return gson.fromJson(version, LauncherVersion.class);

            } catch (IOException e) {

                logger.warn("Failed to get launcher version from: " + Constants.LAUNCHER_VERSION, e);
                int userChoice = JOptionPane.showConfirmDialog(null, "Ошибка подключения, повторить?", Constants.LAUNCHER_TITLE, JOptionPane.YES_NO_OPTION);

                if (userChoice == 0) {
                    continue;
                } else {
                    System.exit(0);
                }
            }
        }
    }

    /**
     * Check whether local launcher profile file exists.
     *
     * @return boolean
     */
    public static boolean profileExists() {
        return profile.exists() && !profile.isDirectory() ? true : false;
    }

    /**
     * Check whether profile contains username & token.
     *
     * @return boolean
     */
    public static boolean profileValid() {
        LauncherProfile lp = getProfile();
        return lp.getUsername() != null && lp.getToken() != null ? true : false;
    }

    /**
     * Get serialized launcher profile from local json file.
     *
     * @return Serialized launcher profile Object.
     */
    public static LauncherProfile getProfile() {
        try (Reader reader = new CharSequenceReader(
                new String(FileUtils.readFileToByteArray(profile)))) {
            return gson.fromJson(reader, LauncherProfile.class);
        } catch (IOException e) {
            logger.warn("Failed to get launcher profile.", e);
            JOptionPane.showMessageDialog(null, "Не удалось найти файл с вашими сохраненными учетными данными. \nПожалуйста введите логин и пароль заново.", Constants.LAUNCHER_TITLE, JOptionPane.ERROR_MESSAGE);
            return new LauncherProfile(null, null);
        }
    }

    /**
     * Compare the current version with the latest one.
     *
     * @return True or False.
     */
    public static boolean isOutdated() {

        if (Constants.CURRENT_VERSION == version.getVersion()) {
            return false;
        }

        return true;
    }
}
