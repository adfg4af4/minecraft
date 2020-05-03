package biz.minecraft.launcher;

import biz.minecraft.launcher.layout.login.LoginLayout;
import biz.minecraft.launcher.layout.login.json.AuthenticationResponse;
import biz.minecraft.launcher.util.LauncherUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static LoginLayout loginLayout;
    private static AuthenticationResponse authInfo;

    public static void main(String[] args) {

        LauncherUtils.getLauncherInfo(logger);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.debug("Failed setting default system's look & feel.", e);
        }

        /**
         * Background Launcher-Updater Thread
         *
         * Launcher will continue executing only after this thread has processed.
         */

        final LauncherUpdater launcherUpdater = new LauncherUpdater(args);

        /**
         * Entry point of the main functional logic
         *
         * Stages of the normal process:
         *
         * - Login layout: handles user credentials
         * - Authenticator: makes request to the authentication server
         * - Updater layout: shows game-updating process
         * - Game-Updater: updates game files
         * - Game-Runner: runs minecraft using authentication server response data
         */

        loginLayout = new LoginLayout();

    }

    public static LoginLayout getLoginLayout() {
        return loginLayout;
    }

    public static AuthenticationResponse getAuthenticationResponse() { return authInfo; }

    public static void setAuthenticationResponse(AuthenticationResponse authenticationResponse) { authInfo = authenticationResponse; }
}
