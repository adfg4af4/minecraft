package biz.minecraft.launcher.layout.login.json;

/**
 * Launcher profile serialization class.
 */
public class LauncherProfile {

    private String username;
    private String token;

    public LauncherProfile() { }

    public LauncherProfile(String username, String token) {
        this.username = username;
        this.token    = token;
    }

    public String getUsername() { return username; }

    public String getToken() { return token; }
}
