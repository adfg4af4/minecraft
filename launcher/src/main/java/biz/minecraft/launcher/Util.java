package biz.minecraft.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipFile;

public class Util {

    private final static Logger logger = LoggerFactory.getLogger(Util.class);

    /**
     * One-line transforming string to URL.
     *
     * @param string
     * @return URL
     */
    public static URL getURL(String string) {
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            logger.error("Failed generating URL from a string.", e);
            return null;
        }
    }

    /**
     * One-line transforming File to Zip File.
     *
     * @param file
     * @return URL
     */
    public static ZipFile getZipFile(File file) {
        try {
            return new ZipFile(file);
        } catch (IOException e) {
            logger.error("Failed reading zip file.", e);
            return null;
        }
    }

    /**
     * Silent closing closeable's.
     *
     * @param closeable
     */
    public static void closeSilently(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (IOException ex) {}
        }
    }

}
