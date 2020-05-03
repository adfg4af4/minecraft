package biz.minecraft.launcher.game.runner;

import biz.minecraft.launcher.Configuration;
import biz.minecraft.launcher.OperatingSystem;
import biz.minecraft.launcher.game.runner.json.Server;
import biz.minecraft.launcher.game.runner.json.ServerList;
import biz.minecraft.launcher.game.updater.json.MinecraftVersion;
import biz.minecraft.launcher.layout.login.json.AuthenticationResponse;
import biz.minecraft.launcher.util.LauncherUtils;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

// TODO: Unhardcore

public class GameRunner {

    private static final Logger logger = LoggerFactory.getLogger(GameRunner.class);

    public GameRunner(AuthenticationResponse authInfo, LinkedList<String> classpath, String mainClass) {

        String pathSeparator = System.getProperties().getProperty("path.separator");

        LinkedList<Server> servers = getServerList(Configuration.SERVER_LIST_URL);

        String natives = new File(LauncherUtils.getWorkingDirectory(), "versions/1.12.2/natives").getAbsolutePath();
        String libraries = classpath.stream().collect(Collectors.joining(pathSeparator));

        ArrayList<String> params = new ArrayList<>();

        params.add(OperatingSystem.getCurrentPlatform().getJavaDir());
        params.add("-Djava.library.path=" + natives);
        params.add("-cp");
        // TODO: Minecraft.jar
        params.add(libraries + pathSeparator + new File(LauncherUtils.getWorkingDirectory(), "versions/1.12.2/minecraft.jar").getAbsolutePath());
        params.add(mainClass);

        params.add("--username");
        params.add(authInfo.getUsername());

        params.add("--version");
        params.add("1.12.2");

        params.add("--gameDir");
        params.add(LauncherUtils.getWorkingDirectory().getAbsolutePath());

        params.add("--assetsDir");
        params.add(new File(LauncherUtils.getWorkingDirectory(), "assets").getAbsolutePath());

        params.add("--assetIndex");
        params.add("1.12");

        params.add("--uuid");
        params.add(authInfo.getUuid());

        params.add("--accessToken");
        params.add(authInfo.getAccessToken());

        params.add("--userProperties");
        params.add("{}");

        params.add("--userType");
        params.add("mojang");

        params.add("--tweakClass");
        params.add("net.minecraftforge.fml.common.launcher.FMLTweaker");

        params.add("--versionType");
        params.add("Forge");

        params.add("--server");
        params.add(servers.get(0).getIp());

        params.add("--port");
        params.add(String.valueOf(servers.get(0).getPort()));

        logger.debug("Запуск игры с параметрами: ");

        for (String parameter : params) {
            logger.debug(parameter);
        }

        ProcessBuilder pb = new ProcessBuilder(params);

        pb.directory(LauncherUtils.getWorkingDirectory());
        pb.inheritIO();

        Process process = null;

        String out = null;

        try {
            process = pb.start();
            logger.debug("Процесс игры запущен. Лаунчер завершает работу.");
            System.exit(0);

        } catch (IOException e) {
            logger.warn("Не удалось запустить процесс игры.", e);
            JOptionPane.showConfirmDialog(null, "Не удалось запустить игру, обратитесь на форум Minecraft.biz в раздел \"Техническая поддержка\".", "Minecraft Пустоши", JOptionPane.OK_CANCEL_OPTION);
            System.exit(0);
        }

    }

    /**
     * Get a deserialized server list object.
     * https://cloud.minecraft.biz/game/servers.json
     *
     * @return Servers data.
     */
    private LinkedList<Server> getServerList(String serversURL) {

        while (true) {

            try (InputStream is = new URL(serversURL).openStream()) {

                String json = IOUtils.toString(is, StandardCharsets.UTF_8);

                return new Gson().fromJson(json, ServerList.class).getServers();

            } catch (IOException e) {

                logger.warn("Не удалось получить список серверов.", e);
                int userChoice = JOptionPane.showConfirmDialog(null, "Не удалось получить список серверов, повторить?", "Minecraft Пустоши", JOptionPane.YES_NO_OPTION);

                if (userChoice == 0) {
                    continue;
                } else {
                    System.exit(0);
                }
            }
        }
    }

}
