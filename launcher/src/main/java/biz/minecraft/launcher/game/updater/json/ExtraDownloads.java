package biz.minecraft.launcher.game.updater.json;

import java.util.LinkedList;

/**
 * Extra downloads deserialization class.
 * https://cloud.minecraft.biz/game/wasteland/extra.json
 */
public class ExtraDownloads {

    private LinkedList<Download> downloads;

    public LinkedList<Download> getDownloads() { return downloads; }
}
