package biz.minecraft.launcher.game.updater.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Game version deserialization class.
 * https://cloud.minecraft.biz/game/wasteland/minecraft.json
 */
public class MinecraftVersion {

    private String mainClass;
    private LinkedHashMap<String, Download> downloads;
    private ArrayList<Library> libraries;

    /**
     * Gets list of required libraries
     *
     * @return the list value of libraries key, or null if the value is not specified
     */
    public List<Library> getLibraries() {
        return this.libraries;
    }

    /**
     * Gets the minecraft.jar URL.
     *
     * @return this downloads -> client -> URL.
     */
    public Download getClient()
    {
        return this.downloads.get("client");
    }

    public String getMainClass() { return mainClass; }
}
