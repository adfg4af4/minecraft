package biz.minecraft.launcher.updater.version;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MinecraftVersion {

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
    public String getClientDownloadURL()
    {
        return this.downloads.get("client").getUrl();
    }
}
