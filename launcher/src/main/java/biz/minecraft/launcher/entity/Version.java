package biz.minecraft.launcher.entity;

import biz.minecraft.launcher.util.LauncherUtils;

import java.net.URL;
import java.nio.file.Path;

/**
 * Launcher version deserialization class.
 * https://cloud.minecraft.biz/launcher/version.json
 */
public class Version {

    private Double version;
    private URL url;
    private String path;

    public Version() { }

    public Version(Double version, URL url, Path path) {
        this.version = version;
        this.url = url;
        this.path = path.toString();
    }

    public Version(Double version, String url, String path) {
        this(version, LauncherUtils.getURL(url), Path.of(path));
    }

    public double getVersion() { return version; }

    public URL getUrl() { return url; }

    public String getPath() { return path; }

    @Override
    public String toString() {
        return Double.toString(version);
    }
}
