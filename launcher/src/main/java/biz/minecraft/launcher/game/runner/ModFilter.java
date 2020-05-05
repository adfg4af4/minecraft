package biz.minecraft.launcher.game.runner;

import biz.minecraft.launcher.util.LauncherUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class ModFilter {

    LinkedList<File> permittedMods;

    private static final Logger logger = LoggerFactory.getLogger(GameRunner.class);

    public ModFilter(LinkedList<File> permittedMods) { this.permittedMods = permittedMods; }

    public void revision() {

        if (permittedMods != null) {

            IOFileFilter forbiddenModsFileFilter = new IOFileFilter() {
                @Override
                public boolean accept(File file) {
                    if (!permittedMods.contains(file)) {
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean accept(File dir, String name) {
                    File file = new File(dir, name);

                    if (!permittedMods.contains(file)) {
                        return true;
                    }
                    return false;
                }
            };

            File workingDirectory = LauncherUtils.getWorkingDirectory();

            // TODO: iterate directories as well
            Iterator<File> iterator = FileUtils.iterateFiles(new File(workingDirectory, "mods"), forbiddenModsFileFilter, null);

            while (iterator.hasNext()) {
                try {
                    File forbiddenFile = iterator.next();
                    FileUtils.forceDelete(forbiddenFile);
                    logger.debug("Deleted " + forbiddenFile);
                } catch (IOException e) {
                    logger.warn("Failed deleting forbidden mod.", e);
                }
            }
        }
    }
}
