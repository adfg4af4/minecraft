package biz.minecraft.launcher;

import biz.minecraft.launcher.updater.Updater;
import biz.minecraft.launcher.updater.version.MinecraftVersion;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class Launcher {

    private final static Logger logger = LoggerFactory.getLogger(Updater.class);

    private final static Gson gson = new Gson();

    public static Version getVersionState() {

        InputStream is = null;

        try {
            is = Util.getURL("https://launcher.minecraft.biz/version.json").openStream();
        } catch (IOException e) {
            logger.error("Failed to get launcher version json.", e);
        }

        Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);

        return gson.fromJson(reader, Version.class);
    }

    public static boolean isLastVersion() {

        if (Config.LAUNCHER_VERSION == getVersionState().getLast()) {
            return true;
        }

        return false;
    }
}
