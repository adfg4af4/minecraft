package biz.minecraft.launcher.layout.login;

import biz.minecraft.launcher.Configuration;
import biz.minecraft.launcher.Main;
import biz.minecraft.launcher.layout.login.json.AuthenticationResponse;
import biz.minecraft.launcher.layout.login.json.LauncherProfile;
import biz.minecraft.launcher.layout.login.json.UserCredentials;
import biz.minecraft.launcher.layout.updater.UpdaterLayout;
import biz.minecraft.launcher.util.LauncherUtils;
import com.google.gson.Gson;
import org.apache.commons.codec.Charsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Authenticator implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Authenticator.class);

    private final Thread thread;

    private final String username, password, token;

    private final boolean remember;

    /**
     * Authenticator Thread Constructor.
     *
     * Handles user data and starts the thread.
     *
     * @param username
     * @param authMethod
     * @param remember
     * @param usingToken
     */
    public Authenticator(String username, String authMethod, boolean remember, boolean usingToken) {

        this.username = username;
        this.remember = remember;

        if (usingToken) {
            this.password = null;
            this.token    = authMethod;
        } else {
            this.password = authMethod;
            this.token    = null;
        }

        thread = new Thread(this, "Authenticator");
        thread.start();
    }

    // Thread body

    public void run() {

        if (password == null) {
            authenticate(username, token, remember, true);
        } else {
            authenticate(username, password, remember, false);
        }

    }

    /**
     * Makes authentication request using the password or token and if successful run game-updater.
     *
     * @param username simply username
     * @param authMethod password or token
     * @param remember creating launcher profile locally
     * @param usingToken clear indication of the token-use authentication type
     */
    public void authenticate(String username, String authMethod, boolean remember, boolean usingToken) {

        // Sending POST request with username & password to https://auth.minecraft.biz/authenticate

        Gson gson                       = new Gson();
        HttpClient httpClient           = HttpClientBuilder.create().build();
        HttpPost post                   = new HttpPost("https://auth.minecraft.biz/authenticate");

        StringEntity postingString   = null;

        try {
            // Generating JSON-request based on usingToken argument
            postingString = new StringEntity(gson.toJson(usingToken ? new LauncherProfile(username, authMethod) : new UserCredentials(username, authMethod)));
        } catch (UnsupportedEncodingException e) {
            logger.warn("Serialization error.", e);
        }

        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");

        HttpResponse response = null;

        try {
            response = httpClient.execute(post);
        } catch (IOException e) {
            logger.warn("Sending POST request error.", e);
        }

        HttpEntity entity = response.getEntity();
        Header encodingHeader = entity.getContentEncoding();

        Charset encoding = encodingHeader == null ? StandardCharsets.UTF_8 :
                Charsets.toCharset(encodingHeader.getValue());

        String json = null;

        try {
            json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("Parsing json response error.", e);
        }

        AuthenticationResponse authenticationResponse = gson.fromJson(json, AuthenticationResponse.class);

        String errorMessage = authenticationResponse.getErrorMessage();

        // Remember authentication response for later running game

        Main.setAuthenticationResponse(authenticationResponse);

        SwingUtilities.invokeLater(() -> {
            if (errorMessage == null) {
                // The authentication server response does not contain any errors, so starting game-updater
                UpdaterLayout updaterLayout = new UpdaterLayout();

                // Create a launcher profile using the username and token received from the authentication server if the user asked
                if (remember) {
                    LauncherProfile lp = new LauncherProfile(authenticationResponse.getUsername(), authenticationResponse.getToken());
                    String        data = gson.toJson(lp);
                    File          file = new File(LauncherUtils.getWorkingDirectory(), Configuration.LAUNCHER_PROFILE);

                    try (FileWriter fileWriter = new FileWriter(file, false)) {
                        fileWriter.write(data);
                    } catch (IOException e) {
                        logger.warn("Failed to create launcher profile.", e);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(Main.getLoginLayout(), errorMessage, "Ошибка авторизации", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        });
    }
}
