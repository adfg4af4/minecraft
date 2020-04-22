package biz.minecraft.launcher.util;

import com.google.gson.annotations.SerializedName;

import java.io.File;

public enum OperatingSystem
{
    @SerializedName("linux") LINUX("linux", new String[] { "linux", "unix" }),
    @SerializedName("windows") WINDOWS("windows", new String[] { "win" }),
    @SerializedName("osx") OSX("osx", new String[] { "mac" }),
    @SerializedName("unknown") UNKNOWN("unknown", new String[0]);

    private final String name;
    private final String[] aliases;

    OperatingSystem(final String name, final String[] aliases) {
        this.name = name;
        this.aliases = ((aliases == null) ? new String[0] : aliases);
    }

    public String getName() {
        return this.name;
    }

    public String[] getAliases() {
        return this.aliases;
    }

    public boolean isSupported() {
        return this != OperatingSystem.UNKNOWN;
    }

    public String getJavaDir() {
        final String separator = System.getProperty("file.separator");
        final String path = System.getProperty("java.home") + separator + "bin" + separator;
        if (getCurrentPlatform() == OperatingSystem.WINDOWS && new File(path + "javaw.exe").isFile()) {
            return path + "javaw.exe";
        }
        return path + "java";
    }

    public static OperatingSystem getCurrentPlatform() {

        final String osName = System.getProperty("os.name").toLowerCase();

        for (final OperatingSystem os : values()) {
            for (final String alias : os.getAliases()) {
                if (osName.contains(alias)) {
                    return os;
                }
            }
        }

        return OperatingSystem.UNKNOWN;
    }

}