package biz.minecraft.launcher.game.runner.json;

import java.util.LinkedList;

/**
 * Server list deserialization class.
 * https://cloud.minecraft.biz/game/servers.json
 */
public class ServerList {

    private LinkedList<Server> servers;

    public LinkedList<Server> getServers() { return servers; }
}
