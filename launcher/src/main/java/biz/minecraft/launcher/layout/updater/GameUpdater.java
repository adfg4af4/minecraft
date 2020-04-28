package biz.minecraft.launcher.layout.updater;

import biz.minecraft.launcher.GameRunner;
import biz.minecraft.launcher.Main;
import biz.minecraft.launcher.OperatingSystem;
import biz.minecraft.launcher.layout.updater.entity.*;
import biz.minecraft.launcher.util.LauncherUtils;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

// TODO: Unhardcore

public class GameUpdater implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(GameUpdater.class);

    private final Gson gson = new Gson();
    private final OperatingSystem os = OperatingSystem.getCurrentPlatform();
    private final File workingDirectory = LauncherUtils.getWorkingDirectory();

    private UpdaterLayout layout;
    private Thread thread;

    // Constructor

    public GameUpdater(UpdaterLayout layout) {

        this.layout = layout;

        thread = new Thread(this, "Game-Updater");

        thread.start();

    }

    // Thread body

    public void run() {

        // Note: Patched AuthLib & Minecraft.jar must be overridden in version.json (!)

        final MinecraftVersion version = getMinecraftVersion("https://cloud.minecraft.biz/game/wasteland/version.json");

        final LinkedList<String> classpath = new LinkedList<>(); // Class path for game runner

        // Libraries-Updater loop

        for (final Library library : version.getLibraries()) {

            Download artifact = library.getDownloads().getArtifact();

            if (artifact != null) {

                if (library.getRules() == null) {

                    artifact.setPathParent("libraries/");

                    File file = workingDirectory.toPath().resolve(artifact.getPath()).toFile();

                    classpath.add(file.getAbsolutePath());

                    // Download library if file does not exist or the hash doesn't match

                    if ((file.exists() && !file.isDirectory() && !isHashMatches(file, artifact.getSha1()))
                            || (!file.exists())) {

                        download(artifact, "Updating libraries for your operating system");

                    }

                } else {

                    // Validating compatibility rules

                    CompatibilityRule.Action actionForCurrentOS = null;

                    for (CompatibilityRule rule : library.getRules()) {

                        if (rule.getAppliedAction() != null) {
                            actionForCurrentOS = rule.getAction();
                        }

                    }

                    if (actionForCurrentOS == CompatibilityRule.Action.ALLOW) {

                        artifact.setPathParent("libraries/");

                        File file = workingDirectory.toPath().resolve(artifact.getPath()).toFile();

                        classpath.add(file.getAbsolutePath());

                        // Download library if file does not exist or the hash doesn't match

                        if ((file.exists() && !file.isDirectory() && !isHashMatches(file, artifact.getSha1()))
                                || (!file.exists())) {

                            download(artifact, "Updating libraries for your operating system");

                        }
                    }
                }
            }

            // Library contains native libraries for the current OS

            if (library.getNatives() != null && library.getNatives().get(os) != null) {

                Download nativeLibrary = library.getDownloads().getClassifier(library.getNatives().get(os));
                nativeLibrary.setPathParent("libraries/");

                File file = workingDirectory.toPath().resolve(nativeLibrary.getPath()).toFile();

                // Native library's jar file does not exist or the hash doesn't match

                if ((file.exists() && !file.isDirectory() && !isHashMatches(file, nativeLibrary.getSha1()))
                        || (!file.exists())) {

                    // Download jar

                    download(nativeLibrary, "Updating native libraries");

                    // Unpack jar with extract rules

                    ExtractRules extractRules = library.getExtractRules();

                    unpackNative(nativeLibrary, extractRules, "versions/1.12.2/natives/");
                }

                // TODO: Also check for update exactly native library (.dll etc.)
            }
        }

        /**
         * Custom assets & minecraft.jar handling
         *
         * Note: assets.zip must be kept for current updater algorithm.
         */

        final Download minecraft = version.getClient();

        minecraft.setPath("versions/1.12.2/minecraft.jar");

        final Download assets = new Download(
            "https://cloud.minecraft.biz/game/wasteland/assets.zip",
            "assets.zip",
            "87cdda5240d8af4969b089152e2ff629f44775d1",
            114_927_430
        );

        List<Download> downloads = new LinkedList<>();

        downloads.add(minecraft);
        downloads.add(assets);

        for (Download download : downloads) {

            File file = workingDirectory.toPath().resolve(download.getPath()).toFile();

            // Download file if it does not exist or the hash doesn't match

            if ((file.exists() && !file.isDirectory() && !isHashMatches(file, download.getSha1()))
                    || (!file.exists())) {

                download(download, "Updating minecraft and downloading assets", true);

                // Unpack assets.zip

                if (file.getName().equals("assets.zip")) {
                    unpackAssets(file);
                }
            }
        }

        // Note: The assets.zip archive is an indicator that there is no need to update & unpack it

        GameRunner gameRunner = new GameRunner(Main.getAuthenticationResponse(), classpath);

    }

    /**
     *
     *
     * @param library Native library's path, sha1, size, url.
     * @param extractRules List of excludes when unpacking.
     * @param folder Target folder for unpacking.
     */
    private void unpackNative(Download library, ExtractRules extractRules, String folder) {

        final File path = new File(workingDirectory, folder);

        logger.debug("Unpacking " + library.getPath() + " -> " + path);

        try (ZipFile zip = new ZipFile(workingDirectory.toPath().resolve(library.getPath()).toFile())) {

            final Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {

                final ZipEntry entry = entries.nextElement();

                if (extractRules != null && !extractRules.shouldExtract(entry.getName())) {
                    continue;
                }

                final File targetFile = new File(path, entry.getName());

                if (targetFile.getParentFile() != null) {
                    targetFile.getParentFile().mkdirs();
                }

                if (entry.isDirectory()) {
                    continue;
                }

                final BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry));
                final byte[] buffer = new byte[2048];
                final FileOutputStream outputStream = new FileOutputStream(targetFile);
                final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

                try {
                    int length;
                    while ((length = inputStream.read(buffer, 0, buffer.length)) != -1) {
                        bufferedOutputStream.write(buffer, 0, length);
                    }
                } finally {
                    closeSilently(bufferedOutputStream);
                    closeSilently(outputStream);
                    closeSilently(inputStream);
                }
            }

            zip.close();

            logger.debug("Unpacked " + library.getPath() + " -> " + path);

        } catch (Exception e) {
            logger.warn("Error while unpacking native library.", e);
        }
    }

    /**
     * Get a deserialized minecraft version object.
     * https://cloud.minecraft.biz/game/wasteland/version.json
     *
     * @return Game version data.
     */
    private MinecraftVersion getMinecraftVersion(String versionURL) {

        while (true) {

            try (InputStream is = new URL(versionURL).openStream()) {

                String version = IOUtils.toString(is, StandardCharsets.UTF_8);

                return gson.fromJson(version, MinecraftVersion.class);

            } catch (IOException e) {

                logger.warn("Failed to get minecraft version from: " + versionURL, e);
                int userChoice = JOptionPane.showConfirmDialog(null, "Ошибка подключения, повторить?", "Minecraft.biz Launcher", JOptionPane.YES_NO_OPTION);

                if (userChoice == 0) {
                    continue;
                } else {
                    System.exit(0);
                }
            }
        }
    }

    private void download(Download download, String info) {

        JLabel label = this.layout.getLabel();

        SwingUtilities.invokeLater(() -> {
            label.setText(info);
        });

        URL url = download.getUrl();
        File path = workingDirectory.toPath().resolve(download.getPath()).toFile();

        while (true) {
            try {
                FileUtils.copyURLToFile(url, path);
                logger.debug("Downloaded " + url + " -> " + path);
                break;
            } catch (IOException e) {
                logger.warn("Download failed " + url + " -> " + path, e);
                int userChoice = JOptionPane.showConfirmDialog(null, "Ошибка обновления файлов игры, повторить?", "Minecraft.biz Launcher", JOptionPane.YES_NO_OPTION);

                if (userChoice == 0) {
                    continue;
                } else {
                    System.exit(0);
                }
            }
        }
    }

    private void download(Download download, String info, boolean showProgress) {

        JLabel label = this.layout.getLabel();
        JProgressBar progressBar = this.layout.getProgressBar();

        SwingUtilities.invokeLater(() -> {
            label.setText(info);
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(download.getSize());
        });

        URL url = download.getUrl();
        File path = workingDirectory.toPath().resolve(download.getPath()).toFile();

        logger.debug("Downloading " + url + " -> " + path);

        while (true) {
            try (BufferedInputStream in = new BufferedInputStream(url.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(path)) {

                byte dataBuffer[] = new byte[1024];

                int bytesRead;

                int total = 0;

                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {

                    fileOutputStream.write(dataBuffer, 0, bytesRead);

                    total = total + bytesRead;

                    int totalBytes = total;

                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(totalBytes);
                    });
                }

                logger.debug("Downloaded " + url + " -> " + path);
                break;
            } catch (IOException e) {
                logger.warn("Download failed " + url + " -> " + path, e);
                int userChoice = JOptionPane.showConfirmDialog(null, "Ошибка обновления файлов игры, повторить?", "Minecraft.biz Launcher", JOptionPane.YES_NO_OPTION);

                if (userChoice == 0) {
                    continue;
                } else {
                    System.exit(0);
                }
            }
        }
    }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {

        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    private boolean isHashMatches(File file, String hash) {

        String currentHash = null;

        try {
            currentHash = getFileChecksum(MessageDigest.getInstance("SHA-1"), file);
        } catch (IOException e) {
            logger.warn("Cannot check file hash.", e);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Incorrect hash-algorithm.", e);
        }

        if (currentHash.equals(hash)) {
            return true;
        }

        return false;
    }

    private void unpackAssets(File path) {

        try {

            final ZipFile zipFile = new ZipFile(workingDirectory.toPath().resolve(path.getPath()).toFile());

            final Enumeration<? extends ZipEntry> entries = zipFile.entries();

            logger.debug("Unpacking: " + path.getName());

            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();

                final File targetFile = new File(workingDirectory, entry.getName());
                if (targetFile.getParentFile() != null) {
                    targetFile.getParentFile().mkdirs();
                }
                if (entry.isDirectory()) {
                    continue;
                }
                final BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry));
                final byte[] buffer = new byte[2048];
                final FileOutputStream outputStream = new FileOutputStream(targetFile);
                final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                try {
                    int length;
                    while ((length = inputStream.read(buffer, 0, buffer.length)) != -1) {
                        bufferedOutputStream.write(buffer, 0, length);
                    }
                }
                finally {
                    closeSilently(bufferedOutputStream);
                    closeSilently(outputStream);
                    closeSilently(inputStream);
                }
            }

            zipFile.close();

            logger.info("Successfully unpacked: " + path.getName());

        } catch (ZipException e) {
            logger.warn("Unpacking error.", e);
        } catch (FileNotFoundException e) {
            logger.warn("Zip archive not found.", e);
        } catch (IOException e) {
            logger.warn("Unpacking error.", e);
        }
    }

    public static void closeSilently(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (IOException ex) {}
        }
    }
}
