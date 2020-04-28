package biz.minecraft.launcher.layout.updater.entity;

import biz.minecraft.launcher.OperatingSystem;

import java.util.List;
import java.util.Map;

public class Library {

    private Downloads downloads;
    private ExtractRules extract;
    private String name;
    private Map<OperatingSystem, String> natives;
    private List<CompatibilityRule> rules;

    public Downloads getDownloads() {
        return downloads;
    }

    public List<CompatibilityRule> getRules() {
        return rules;
    }

    public Map<OperatingSystem, String> getNatives() {
        return this.natives;
    }

    public ExtractRules getExtractRules() {
        return this.extract;
    }
}
