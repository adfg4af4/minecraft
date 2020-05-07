package biz.minecraft.launcher.layout.login;

import biz.minecraft.launcher.Configuration;
import biz.minecraft.launcher.Launcher;
import biz.minecraft.launcher.layout.login.json.LauncherProfile;
import biz.minecraft.launcher.util.LauncherUtils;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class LoginLayout extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(LoginLayout.class);

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton submitButton;
    private JCheckBox rememberMeCheckbox;

    private boolean remember;

    // Constructor

    public LoginLayout() {

        super("Вход на Minecraft Пустоши");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(getToolkit().getImage("grass.png"));
        setResizable(false);
        setFocusable(true);

        addWindowListener(new OpenWindowListener());

        CredentialsListener credentialsListener = new CredentialsListener();

        usernameField = new JTextField(20);
        usernameField.getDocument().addDocumentListener(credentialsListener);

        passwordField = new JPasswordField(20);
        passwordField.getDocument().addDocumentListener(credentialsListener);

        rememberMeCheckbox = new JCheckBox("Запомнить меня");
        rememberMeCheckbox.addItemListener(new RememberMeItemListener());

        submitButton = new JButton("Войти");
        submitButton.addActionListener(new SubmitButtonActionListener());
        submitButton.setEnabled(false);

        if (Launcher.profileExists()) {
            if (Launcher.profileValid()) {
                LauncherProfile lp = Launcher.getProfile();

                usernameField.setText(lp.getUsername());
                passwordField.setText("************");
                rememberMeCheckbox.setSelected(true); // TODO: Fix hotkeys
                submitButton.setEnabled(true);
            }
        }

        JPanel loginPanel = getLoginPanel();

        // Make HotKeyListener working on any focused component.

        for (Component c : loginPanel.getComponents()) {
            c.addKeyListener(new HotKeyListener());
        }

        add(loginPanel);

        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Building Login Panel using MiG Layout.
     *
     * @return JPanel Login Panel.
     */
    private JPanel getLoginPanel() {

        JPanel loginPanel = new JPanel(new MigLayout("", "[][grow]"));

        loginPanel.add(new JLabel("Логин: "));
        loginPanel.add(usernameField, "gap rel, wrap rel, growx");

        loginPanel.add(new JLabel("Пароль: "));
        loginPanel.add(passwordField, "gap rel, wrap rel, growx");

        loginPanel.add(submitButton, "skip 1, split, sg buttons, align right");
        loginPanel.add(rememberMeCheckbox, "sg buttons");

        return loginPanel;
    }

    /**
     * Checks whether the user's data is entered and activates the submit button depending on it.
     */
    private void validateCredentials() {

        String username = usernameField.getText();
        char[] password = passwordField.getPassword();

        if ((username != null && username.trim().length() > 0) && (password != null && password.length > 0)) {
            submitButton.setEnabled(true);
        } else {
            submitButton.setEnabled(false);
        }
    }

    /**
     * Listens for Text-fields changes.
     */
    class CredentialsListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            validateCredentials();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validateCredentials();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            validateCredentials();
        }
    }

    /**
     * Makes username field focused after the window is opened.
     */
    class OpenWindowListener extends WindowAdapter {
        public void windowOpened(WindowEvent e) {
            usernameField.grabFocus();
        }
    }

    /**
     * Listens for keys pressed on focused component.
     */
    class HotKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                submitButton.doClick();
            }
        }
    }

    /**
     * Submit button listener.
     *
     * Closes window and sends authentication request either with given username & password
     * or if possible using parsed from launcher profile username & authentication token.
     */
    class SubmitButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            setVisible(false);

            if (!remember) {
                if (Launcher.profileExists()) {
                    try {
                        FileUtils.forceDelete(new File(LauncherUtils.getWorkingDirectory(), Configuration.LAUNCHER_PROFILE));
                        logger.warn("Launcher profile has been deleted successfully.");
                    } catch (IOException ex) {
                        logger.warn("Failed to delete launcher profile.", ex);
                    }
                }
            }

            if (Launcher.profileExists()) {
                if (Launcher.profileValid()) {
                    LauncherProfile lp = Launcher.getProfile();

                    String username = lp.getUsername();
                    String    token = lp.getToken();

                    logger.debug("Authentication requested with parsed username & token from launcher profile.");

                    // Requesting authentication using launcher profile's username & token
                    Authenticator authenticator = new Authenticator(username, token, remember, true);
                }
            } else {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                logger.debug("Authentication requested with given username & password.");
                // Requesting authentication using given username & password
                Authenticator authenticator = new Authenticator(username, password, remember, false);
            }
        }
    }

    /**
     * Listens for remember me checkbox and changes `remember` boolean field state.
     */
    class RememberMeItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {

            remember = e.getStateChange() == 1 ?  true : false;

            boolean state = remember ? false : true;

            usernameField.setEnabled(state);
            passwordField.setEnabled(state);

            logger.debug("Remember me: " + remember);
        }
    }
}
