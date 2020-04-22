package biz.minecraft.launcher.updater.game.version;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.apache.commons.io.input.AutoCloseInputStream;

import biz.minecraft.launcher.util.Helper;

public class Download {

    private URL url;
    private String path;

    public Download() {
    }

    public Download(URL url, Path path) {
        this.url = url;
        this.path = path.toString();
    }

    public Download(String url, String path) {
        this(Helper.getURL(url), Path.of(path));
    }

    public URL getUrl() {
        return url;
    }

    public Path getPath() {
        return Path.of(this.path);
    }

    public Download setPathParent(String parent) {
        this.path = parent + this.path;
        return this;
    }

    public void download(final JProgressBar jProgressBar) throws IOException, InterruptedException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int completeFileSize = connection.getContentLength();
        int downloadedFileSize = 0;

        jProgressBar.setMaximum(completeFileSize);

        AutoCloseInputStream in = new AutoCloseInputStream(connection.getInputStream());
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(this.getPath().toFile()));

        byte data[] = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(data, 0, 1024)) != -1) {

            downloadedFileSize += bytesRead;
            // calculate progress
            final int currentProgress = (int) ((((double) downloadedFileSize) / ((double) completeFileSize)) * 100000d);
            // update progress bar
            final int value = downloadedFileSize;
            SwingUtilities.invokeLater(() -> jProgressBar.setValue(value));
            out.write(data, 0, bytesRead);
            Thread.sleep(1);
        }

        out.close();
        System.out.println("Downloading: " + path + " From: " + url);
    }
}
