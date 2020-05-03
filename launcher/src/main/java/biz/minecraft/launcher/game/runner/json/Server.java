package biz.minecraft.launcher.game.runner.json;

/**
 * Server deserialization class.
 * https://cloud.minecraft.biz/game/servers.json
 */
public class Server {

    private String name;
    private String version;
    private String ip;
    private int port;

    public String getName() { return name; }

    public String getVersion() { return version; }

    public String getIp() { return ip; }

    public int getPort() { return port; }
}
