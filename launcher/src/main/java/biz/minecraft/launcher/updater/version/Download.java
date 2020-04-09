package biz.minecraft.launcher.updater.version;

import java.net.URL;

import biz.minecraft.launcher.Util;

public class Download {

    private URL url;
    private String path; // TODO: Probably change type to File or Path

    public Download() { }

    public Download(String url, String path) {
        this.url = Util.getURL(url);
        this.path = path;
    }

    public URL getUrl() {
        return url;
    }

    public String getPath() {
        return path;
    }

    public void setPathParent(String parent) {
        this.path = parent + this.path;
    }
}
