package biz.minecraft.launcher.layout.updater.forge;

import java.net.URL;

/**
 * Forge version deserialization class.
 * https://cloud.minecraft.biz/game/wasteland/forge.json
 */

public class ForgeVersion {

    private String mainClass;
    private Library[] libraries;

    public String getMainClass() { return mainClass; }

    public Library[] getLibraries() {
        return libraries == null ? new Library[0] : libraries;
    }

    public static class Library {

        private ForgeArtifact name;
        private URL url;
        private String serverreq; // if null default: false
        private String clientreq; // if null default: true

        public ForgeArtifact getName() {
            return name;
        }

        public URL getUrl() {
            return url;
        }

        public String getServerreq() {
            return serverreq;
        }

        public String getClientreq() {
            return clientreq;
        }
    }
}
