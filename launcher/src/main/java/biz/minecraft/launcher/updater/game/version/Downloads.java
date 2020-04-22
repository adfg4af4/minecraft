package biz.minecraft.launcher.updater.game.version;

import java.util.Map;

public class Downloads {

    private Download artifact;
    private Map<String, Download> classifiers;

    public Download getArtifact() {
        return artifact;
    }

    public Download getClassifier(String classifier) {
        return this.classifiers.get(classifier);
    }
}
