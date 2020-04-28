package biz.minecraft.launcher;

import biz.minecraft.launcher.layout.login.entity.AuthenticationResponse;
import biz.minecraft.launcher.util.LauncherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

// TODO: Unhardcore

public class GameRunner {

    private static final Logger logger = LoggerFactory.getLogger(GameRunner.class);

    public GameRunner(AuthenticationResponse authInfo, LinkedList<String> classpath) {

        logger.debug("Game runner started!");

        ArrayList<String> params = new ArrayList<>();

        params.add(OperatingSystem.getCurrentPlatform().getJavaDir()); // Java path (string)
        params.add("-Djava.library.path=" + new File(LauncherUtils.getWorkingDirectory(), "versions/1.12.2/natives").getAbsolutePath());
        params.add("-cp");

        // Get System properties
        Properties properties = System.getProperties();

        // Get the path separator which is unfortunately
        // using a different symbol in different OS platform.
        String pathSeparator = properties.getProperty("path.separator");

        String result = classpath.stream()
                .collect(Collectors.joining(pathSeparator));

        params.add(result + pathSeparator + new File(LauncherUtils.getWorkingDirectory(), "versions/1.12.2/minecraft.jar").getAbsolutePath());

        params.add("net.minecraft.client.main.Main");

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

        params.add("--userType");
        params.add("mojang");

        params.add("--versionType");
        params.add("release");

        params.add("--server");
        params.add("178.33.236.35");

        params.add("--port");
        params.add("25569");

        logger.debug("Seems parameters added successfully: ");

        for (String parameter : params) {
            System.out.println(parameter);
        }

        ProcessBuilder pb = new ProcessBuilder(params);

        pb.inheritIO();

        Process process = null;

        String out = null;

        try {
            process = pb.start();

            System.exit(0);

        } catch (IOException e) {
            logger.warn("Failed starting running game.", e);
            JOptionPane.showConfirmDialog(null, "Ошибка запуска игры, пожалуйста обратитесь к администратору.", "Minecraft.biz Launcher", JOptionPane.OK_CANCEL_OPTION);
            System.exit(0);
        }

    }

}
