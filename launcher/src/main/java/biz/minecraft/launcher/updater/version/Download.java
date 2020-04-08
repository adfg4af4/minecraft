package biz.minecraft.launcher.updater.version;

public class Download {

    private String url; // TODO: Change type to URL
    private String path; // TODO: Probably change type to File or Path

    public Download() { }

    public Download(String url, String path) {
        this.url = url;
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public String getPath() {
        return path;
    }

    public void setPathParent(String parent) {
        this.path = parent + this.path;
    }
}
