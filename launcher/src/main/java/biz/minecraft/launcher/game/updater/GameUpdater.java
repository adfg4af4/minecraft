package biz.minecraft.launcher.game.updater;

import biz.minecraft.launcher.Constants;
import biz.minecraft.launcher.game.runner.GameRunner;
import biz.minecraft.launcher.Main;
import biz.minecraft.launcher.OperatingSystem;
import biz.minecraft.launcher.game.updater.json.*;
import biz.minecraft.launcher.layout.login.json.AuthenticationResponse;
import biz.minecraft.launcher.layout.updater.UpdaterLayout;
import biz.minecraft.launcher.layout.updater.forge.ForgeArtifact;
import biz.minecraft.launcher.layout.updater.forge.ForgeVersion;
import biz.minecraft.launcher.util.LauncherUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

        AuthenticationResponse authInfo = Main.getAuthenticationResponse();

        // Note: Patched AuthLib & Minecraft.jar must be overridden in minecraft.json (!)

        final MinecraftVersion version = getMinecraftVersion(Constants.MINECRAFT_VERSION);

        final LinkedList<String> classpath = new LinkedList<>(); // Class path for game runner

        /**
         * Minecraft libraries updater
         */

        for (final Library library : version.getLibraries()) {

            Download artifact = library.getDownloads().getArtifact();

            if (artifact != null) {

                if (library.getRules() == null) {

                    artifact.setPathParent("libraries/");

                    File file = workingDirectory.toPath().resolve(artifact.getPath()).toFile();

                    classpath.add(file.getAbsolutePath());

                    // Download library if file does not exist or the hash doesn't match

                    if ((file.exists() && !file.isDirectory() && !checksumValid(file, artifact.getSha1()))
                            || (!file.exists())) {

                        download(artifact, "Загрузка библиотек");

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

                        if ((file.exists() && !file.isDirectory() && !checksumValid(file, artifact.getSha1()))
                                || (!file.exists())) {

                            download(artifact, "Загрузка библиотек операционной системы");

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

                if ((file.exists() && !file.isDirectory() && !checksumValid(file, nativeLibrary.getSha1()))
                        || (!file.exists())) {

                    // Download jar

                    download(nativeLibrary, "Загрузка нативных библиотек");

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
            "https://cloud.minecraft.biz/game/wasteland/minecraft/assets.zip",
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

            if ((file.exists() && !file.isDirectory() && !checksumValid(file, download.getSha1()))
                    || (!file.exists())) {

                download(download, "Загрузка игры и компонентов", true);

                // Unpack assets.zip

                if (file.getName().equals("assets.zip")) {
                    unpackAssets(file);
                }
            }
        }

        // Note: The assets.zip archive is an indicator that there is no need to update & unpack it

        /**
         * Forge libraries updater
         *
         * Algorithm downloads only client-required libraries from minecraft.net or minecraft.biz repos.
         * Initially the Forge repository is used instead of the minecraft.biz but it stores libraries in .pack.xz format.
         *
         * TODO: Support for the original Forge repository
         */

        final ForgeVersion forgeVersion = getForgeVersion(Constants.FORGE_VERSION);

        for (ForgeVersion.Library library : forgeVersion.getLibraries()) {

            String clientSideLibrary = library.getClientreq();

            if (clientSideLibrary == null || (clientSideLibrary != null && clientSideLibrary.equals("true"))) {

                String path = library.getName().getPath();
                URL    url  = library.getUrl();

                if (url == null) {
                    url = LauncherUtils.getURL(Constants.MINECRAFT_LIBRARIES + path);
                } else
                    url = LauncherUtils.getURL(url + path);

                path = "libraries/" + path;

                File file = workingDirectory.toPath().resolve(path).toFile();

                String absolutePath = file.getAbsolutePath();

                classpath.add(absolutePath);

                // TODO: hash validation

                if (!file.exists())
                    download(new Download(url, absolutePath, "", 0), "Загрузка библиотек Forge");
            }

        }

        /**
         * Extra downloads updater (mods, resourcepacks, default configurations etc.)
         *
         * Each download consists of url, path, sha1 and size in bytes.
         * The algorithm does not reload files that do not have a checksum (sha1-field) specified.
         */

        final ExtraDownloads   mods = getExtraDownloads(Constants.CLIENT_MODS);
        final ExtraDownloads extras = getExtraDownloads(Constants.CLIENT_EXTRA);

        final LinkedList<Download> extraDownloads = new LinkedList<>();

        extraDownloads.addAll(mods.getDownloads());
        extraDownloads.addAll(extras.getDownloads());

        for (Download download : extraDownloads) {

            final File   file = workingDirectory.toPath().resolve(download.getPath()).toFile();
            final String hash = download.getSha1();

            // Download file if it does not exist or the hash is invalid

            if ((file.exists() && !file.isDirectory() && hash != null)) {

                if (!checksumValid(file, hash))
                    download(download, "Загрузка дополнений", true);

            } else if (!file.exists()) {

                download(download, "Загрузка дополнений", true);
            }
        }

        /**
         * Initializing GameRunner with generated libraries classpath,
         * specified main class and authorization information.
         */

        final String mainClass = forgeVersion.getMainClass();

        final LinkedList<File> modList = new LinkedList<>();

        for (Download mod : mods.getDownloads()) {
            modList.add(new File(LauncherUtils.getWorkingDirectory(), mod.getPath()));
        }

        GameRunner gameRunner = new GameRunner(authInfo, classpath, mainClass, modList);

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
     * https://cloud.minecraft.biz/game/wasteland/minecraft.json
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
                int userChoice = JOptionPane.showConfirmDialog(null, "Ошибка подключения, повторить?", Constants.LAUNCHER_TITLE, JOptionPane.YES_NO_OPTION);

                if (userChoice == 0) {
                    continue;
                } else {
                    System.exit(0);
                }
            }
        }
    }

    /**
     * Get a deserialized forge version object.
     * https://cloud.minecraft.biz/game/wasteland/forge.json
     *
     * @return Forge version data.
     */
    private ForgeVersion getForgeVersion(String versionURL) {

        while (true) {

            try (InputStream is = new URL(versionURL).openStream()) {

                String version = IOUtils.toString(is, StandardCharsets.UTF_8);

                Gson forgeGson = new GsonBuilder().setPrettyPrinting()
                        .registerTypeAdapter(ForgeArtifact.class, new ForgeArtifact.Adapter())
                        .create();

                return forgeGson.fromJson(version, ForgeVersion.class);

            } catch (IOException e) {

                logger.warn("Failed to get forge version from: " + versionURL, e);
                int userChoice = JOptionPane.showConfirmDialog(null, "Ошибка подключения, повторить?", Constants.LAUNCHER_TITLE, JOptionPane.YES_NO_OPTION);

                if (userChoice == 0) {
                    continue;
                } else {
                    System.exit(0);
                }
            }
        }
    }

    /**
     * Get a deserialized minecraft extra downloads object.
     * https://cloud.minecraft.biz/game/wasteland/extra.json
     *
     * @return Extra downloads data.
     */
    private ExtraDownloads getExtraDownloads(String versionURL) {

        while (true) {

            try (InputStream is = new URL(versionURL).openStream()) {

                String json = IOUtils.toString(is, StandardCharsets.UTF_8);

                return gson.fromJson(json, ExtraDownloads.class);

            } catch (IOException e) {

                logger.warn("Failed to get minecraft version from: " + versionURL, e);
                int userChoice = JOptionPane.showConfirmDialog(null, "Ошибка подключения, повторить?", Constants.LAUNCHER_TITLE, JOptionPane.YES_NO_OPTION);

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

        logger.debug("Downloading " + url + " :: " + path);

        while (true) {
            try {
                FileUtils.copyURLToFile(url, path);
                break;
            } catch (IOException e) {
                logger.warn("Не удалось загрузить " + url + " :: " + path, e);
                int userChoice = JOptionPane.showConfirmDialog(null, "Не удалось загрузить " + path.getName() + ", повторить?", Constants.LAUNCHER_TITLE, JOptionPane.YES_NO_OPTION);

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

        if (path.getParentFile().mkdirs()) {
            logger.debug("Created catalogs for download.");
        } else {
            logger.debug("Catalogs don't need to be created for download.");
        }

        logger.debug("Downloading " + url + " :: " + path);

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

                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(true);
                });

                break;
            } catch (IOException e) {
                logger.warn("Не удалось загрузить " + url + " :: " + path, e);
                int userChoice = JOptionPane.showConfirmDialog(null, "Не удалось загрузить " + path.getName() + ", повторить?", Constants.LAUNCHER_TITLE, JOptionPane.YES_NO_OPTION);

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

    private boolean checksumValid(File file, String hash) {

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
