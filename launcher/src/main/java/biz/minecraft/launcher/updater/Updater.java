package biz.minecraft.launcher.updater;

import biz.minecraft.launcher.OperatingSystem;
import biz.minecraft.launcher.Util;
import biz.minecraft.launcher.updater.version.*;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class Updater extends Thread {

    private final static Logger logger = LoggerFactory.getLogger(Updater.class);
    private final Gson gson = new Gson();
    private final OperatingSystem os = OperatingSystem.getCurrentPlatform();
    private final File workingDirectory = getWorkingDirectory();

    public void run() {

        /** Deserialize minecraft version JSON from URL */

        final MinecraftVersion minecraftVersion = getMinecraftVersion(Util.getURL("https://launcher.minecraft.biz/client/1.12.2/version.json"));

        /** Get libraries collection */

        final Collection<Library> minecraftVersionLibraries = minecraftVersion.getLibraries();

        /** Prepare downloads */

        final Collection<Download> libraries = getRelevantLibraries(minecraftVersionLibraries);
        final Collection<Download> natives   = getRelevantNatives(minecraftVersionLibraries);

        final Download game   = new Download("https://launcher.minecraft.biz/client/1.12.2/minecraft.jar", "versions/1.12.2/minecraft.jar");
        final Download assets = new Download("https://launcher.minecraft.biz/client/1.12.2/assets.zip", "assets.zip");

        final Stream<Download> combinedStream = Stream.concat(libraries.stream(), natives.stream());

        final Collection<Download> downloads  = combinedStream.collect(Collectors.toList());

        downloads.add(game);
        downloads.add(assets);

        /** Download everything */

        download(downloads);

        /** Unpack natives */

        Map<Path, ExtractRules> nativesExtractRules = getRelevantNativesExtractRules(minecraftVersionLibraries);

        unpackNatives(natives, "versions/1.12.2/natives/", nativesExtractRules);

        /** Unpack & delete assets.zip */

        unpackAssets();
    }

    /**
     * Get Minecraft.biz root folder.
     *
     * @return File – path to Minecraft.biz root folder.
     */
    private File getWorkingDirectory() {

        final String userHome = System.getProperty("user.home", ".");
        File workingDirectory = null;

        switch (OperatingSystem.getCurrentPlatform()) {
            case LINUX: {
                workingDirectory = new File(userHome, "Minecraft.biz/");
                break;
            }
            case WINDOWS: {
                final String applicationData = System.getenv("APPDATA");
                final String folder = (applicationData != null) ? applicationData : userHome;
                workingDirectory = new File(folder, "Minecraft.biz/");
                break;
            }
            case OSX: {
                workingDirectory = new File(userHome, "Library/Application Support/Minecraft.biz");
                break;
            }
            default: {
                workingDirectory = new File(userHome, "minecraft.biz/");
                break;
            }
        }

        return workingDirectory;
    }

    /**
     * Deserialize Minecraft Version JSON from URL, to Java Object.
     *
     * @param jsonURL Minecraft version-info JSON URL
     * @return MinecraftVersion Java Object
     */
    private MinecraftVersion getMinecraftVersion(URL jsonURL) {

        InputStream is = null;

        try {
            is = jsonURL.openStream();
        } catch (IOException e) {
            logger.error("Failed to get minecraft version json.", e);
        }

        Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);

        return gson.fromJson(reader, MinecraftVersion.class);
    }

    /**
     * Gets libraries artifact collection without compatibility rules or
     * only allowed for the current operating system. And adds
     * 'libraries' to each library path.
     *
     * @param libraries Collection<Library>
     * @return relevantLibraries Collection<Download>
     */
    private Collection<Download> getRelevantLibraries(Collection<Library> libraries) {

        Collection<Download> relevantLibraries = new ArrayList<>();

        for (final Library library : libraries) {

            Download artifact = library.getDownloads().getArtifact();

            if (artifact != null) {

                if (library.getRules() == null) {

                    artifact.setPathParent("libraries/");
                    relevantLibraries.add(artifact);

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
                        relevantLibraries.add(artifact);

                    }
                }
            }
        }

        return relevantLibraries;
    }

    /**
     * Gets natives artifact collection allowed for the current operating system.
     *
     * @param libraries Collection<Library>
     * @return relevantNatives Collection<Download>
     */
    private Collection<Download> getRelevantNatives(Collection<Library> libraries) {

        Collection<Download> relevantNatives = new ArrayList<>();

        for (final Library library : libraries) {

            if (library.getNatives() != null && library.getNatives().get(os) != null) {

                Download nativeLibrary = library.getDownloads().getClassifier(library.getNatives().get(os));
                nativeLibrary.setPathParent("libraries/");
                relevantNatives.add(nativeLibrary);

            }
        }

        return relevantNatives;
    }

    /**
     * Download collection URL's to collection Path's.
     *
     * @param downloads
     */
    private void download(Collection<Download> downloads) {

        for (Download download: downloads) {

            try {
                FileUtils.copyURLToFile(
                    download.getUrl(), 
                    workingDirectory.toPath().resolve(download.getPath()).toFile()
                );
                logger.info("Downloading: " + download.getPath() + " From: " + download.getUrl());
            } catch (IOException e) {
                logger.error("Filesystem query error.", e);
            }

        }
    }

    /**
     * Get extract rules for native libraries.
     *
     * @param libraries
     * @return relevantNativesExtractRules Map<String, ExtractRules>
     */
    private Map<Path, ExtractRules> getRelevantNativesExtractRules(Collection<Library> libraries) {

        Map<Path, ExtractRules> relevantNativesExtractRules = new HashMap<>();

        for (final Library library : libraries) {

            if (library.getNatives() != null && library.getNatives().get(os) != null) {

                ExtractRules extractRules = library.getExtractRules();

                if (extractRules != null) {

                    relevantNativesExtractRules.put(library.getDownloads().getClassifier(library.getNatives().get(os)).getPath(), extractRules);

                }

            }
        }

        return relevantNativesExtractRules;
    }

    /**
     * Unpacking downloaded natives jar's with possible extract rules to selected target directory.
     *
     * @param natives Collection<Download>
     * @param path String
     * @param nativesExtractRules Map<String, ExtractRules>
     */
    private void unpackNatives(Collection<Download> natives, String path, Map<Path, ExtractRules> nativesExtractRules) {

        final File targetDir = new File(workingDirectory, path);

        for (Download nativeLibrary: natives) {

            logger.info("Unpacking native library: " + nativeLibrary.getPath() + " To: " + targetDir);

            ExtractRules nativeLibraryExtractRules = nativesExtractRules.get(nativeLibrary.getPath());

            try {
                final ZipFile zip = new ZipFile(workingDirectory.toPath().resolve(nativeLibrary.getPath()).toFile());
                
                final Enumeration<? extends ZipEntry> entries = zip.entries();

                while (entries.hasMoreElements()) {
                    final ZipEntry entry = entries.nextElement();
                    if (nativeLibraryExtractRules != null && !nativeLibraryExtractRules.shouldExtract(entry.getName())) {
                        continue;
                    }
                    final File targetFile = new File(targetDir, entry.getName());
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
                    }
                    finally {
                        Util.closeSilently(bufferedOutputStream);
                        Util.closeSilently(outputStream);
                        Util.closeSilently(inputStream);
                    }
                }

                zip.close();

                logger.info("Native library successfully unpacked.");
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void unpackAssets() {
        try {

            final ZipFile zip = new ZipFile(new File(workingDirectory, "assets.zip"));

            final Enumeration<? extends ZipEntry> entries = zip.entries();

            logger.info("Unpacking assets..");

            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();

                final File targetFile = new File(workingDirectory, entry.getName());
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
                }
                finally {
                    Util.closeSilently(bufferedOutputStream);
                    Util.closeSilently(outputStream);
                    Util.closeSilently(inputStream);
                }
            }

            zip.close();

            logger.info("Assets extracted.");

        } catch (ZipException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // zip.close();
        }

        // Remove assets.zip

        try {
            logger.info("Deleting assets.zip");
            FileUtils.forceDelete(FileUtils.getFile(workingDirectory, "assets.zip"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Unpacking, deleting assets.zip: DONE");
        logger.info("Updating all files: DONE");
    }
}