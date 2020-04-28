package biz.minecraft.launcher.layout.login;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

public class LoginLayout extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(LoginLayout.class);

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton submitButton, cancelButton;

    // Constructor

    public LoginLayout() {

        super("Вход на Minecraft.biz");

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

        submitButton = new JButton("Войти");
        submitButton.addActionListener(new SubmitButtonActionListener());
        submitButton.setEnabled(false);

        cancelButton = new JButton("Закрыть");
        cancelButton.addActionListener(new CancelButtonActionListener());

        JPanel loginPanel = getLoginPanel();

        // Make HotKeyListener working on any focused component

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
        loginPanel.add(cancelButton, "sg buttons");

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
     * Listens for submit button is being pressed.
     * Closes window and sends authentication request.
     */
    class SubmitButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            logger.debug("Authentication requested.");

            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            setVisible(false);

            Authenticator authenticator = new Authenticator(username, password);
        }
    }

    /**
     * Listens for cancel button is being pressed.
     */
    class CancelButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
}
