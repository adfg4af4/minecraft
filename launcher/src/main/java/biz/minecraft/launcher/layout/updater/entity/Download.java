package biz.minecraft.launcher.layout.updater.entity;

import biz.minecraft.launcher.util.LauncherUtils;

import java.net.URL;

/**
 * Minecraft version's download deserialization class.
 * https://cloud.minecraft.biz/game/wasteland/version.json
 */
public class Download {

    private URL url;
    private String path;
    private String sha1;
    private int size;

    public Download() { }

    public Download(URL url, String path, String sha1, int size) {
        this.url = url;
        this.path = path;
        this.sha1 = sha1;
        this.size = size;
    }

    public Download(String url, String path, String sha1, int size) {
        this(LauncherUtils.getURL(url), path, sha1, size);
    }

    public URL getUrl() {
        return url;
    }

    public String getPath() { return path; }

    public String getSha1() { return sha1; }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSize() { return size; }

    public void setPathParent(String parent) {
        this.path = parent + this.path;
    }

    @Override
    public String toString() {
        return "Download{" +
                "url=" + url +
                ", path='" + path + '\'' +
                ", sha1='" + sha1 + '\'' +
                '}';
    }
}
