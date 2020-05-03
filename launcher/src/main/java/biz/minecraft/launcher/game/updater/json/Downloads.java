package biz.minecraft.launcher.game.updater.json;

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
