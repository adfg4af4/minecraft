package biz.minecraft.launcher.layout.login;

import biz.minecraft.launcher.Main;
import biz.minecraft.launcher.layout.login.json.AuthenticationResponse;
import biz.minecraft.launcher.layout.login.json.UserCredentials;
import biz.minecraft.launcher.layout.updater.UpdaterLayout;
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Authenticator implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Authenticator.class);

    private final Thread thread;

    private final String username, password;

    /**
     * Authenticator Thread Constructor.
     *
     * Handles user data and starts the thread.
     *
     * @param username
     * @param password
     */
    public Authenticator(String username, String password) {

        this.username = username;
        this.password = password;

        thread = new Thread(this, "Authenticator");
        thread.start();
    }

    // Thread body

    public void run() { authenticate(username, password); }

    /**
     * Makes authentication request and starts Game-Updater.
     *
     * @param username
     * @param password
     */
    public void authenticate(String username, String password) {

        // Sending POST request with username & password to https://auth.minecraft.biz/authenticate

        UserCredentials userCredentials = new UserCredentials(username, password);
        Gson gson                       = new Gson();
        HttpClient httpClient           = HttpClientBuilder.create().build();
        HttpPost post                   = new HttpPost("https://auth.minecraft.biz/authenticate");

        StringEntity postingString   = null;

        try {
            postingString = new StringEntity(gson.toJson(userCredentials));
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
                UpdaterLayout updaterLayout = new UpdaterLayout();
            } else if (errorMessage != null) {
                JOptionPane.showMessageDialog(Main.getLoginLayout(), "Неверный логин или пароль.", "Не удалось войти", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(Main.getLoginLayout(), "Ошибка сервера авторизации. Пожалуйста свяжитесь с администратором.", "Ошибка сервера авторизации", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
