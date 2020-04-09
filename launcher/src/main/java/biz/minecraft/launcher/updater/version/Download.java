package biz.minecraft.launcher.updater.version;

import java.net.URL;
import java.nio.file.Path;

import biz.minecraft.launcher.Util;

public class Download {

    private URL url;
    private Path path; 

    public Download() { }

    public Download(URL url, Path path) {
        this.url = url;
        this.path = path;
    }

    public Download(String url, String path) {
        this(Util.getURL(url), Path.of(path));
    }

    public URL getUrl() {
        return url;
    }

    public Path getPath() {
        return path;
    }

    public Download setPathParent(String parent) {
        this.path = Path.of(parent).resolve(this.path);
        return this;
    }
}
