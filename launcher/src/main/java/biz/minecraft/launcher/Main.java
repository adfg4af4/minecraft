package biz.minecraft.launcher;

import biz.minecraft.launcher.updater.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

// import java.awt.*;
import java.io.File;
// import java.net.URL;
import java.util.Scanner;

public class Main extends JFrame {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);
/*
    public Main() {

        // Defining icons

        URL originalImageURL = this.getClass().getClassLoader().getResource("grass.png");
        ImageIcon originalIcon = new ImageIcon(originalImageURL);
        Image originalImage = originalIcon.getImage();

        Image resizedImage = originalImage.getScaledInstance(45, 48, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);

        // Frame parameters

        setTitle("Updating Minecraft");
        setIconImage(originalImage);
        setResizable(false);

        // Handlers

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Components

        JLabel icon = new JLabel(resizedIcon);
        JLabel label = new JLabel("Updating Minecraft");
        JProgressBar progressBar = new JProgressBar(0, 100);

        // Progress bar properties

        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 10));

        // Layout manager

        Container contentPane = getContentPane();
        GroupLayout layout = new GroupLayout(contentPane);
        contentPane.setLayout(layout);

        // Automatic gap insertion

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Root horizontal group

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addComponent(icon)
                .addGroup(layout.createParallelGroup()
                    .addComponent(label)
                    .addComponent(progressBar))
        );

        // Root vertical group

        layout.setVerticalGroup(
            layout.createParallelGroup()
                .addComponent(icon)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(label)
                    .addComponent(progressBar))
        );
        
        // layout.linkSize(SwingConstants.HORIZONTAL, icon, progressBar);

        pack();

        // Frame parameters after packing

        setLocationRelativeTo(null);
    }
*/
    public static void main(String[] args) throws InterruptedException {
        /*
        java.awt.EventQueue.invokeLater(() -> {
            JFrame.setDefaultLookAndFeelDecorated(true);
            new Main().setVisible(true);
        });
        */

        System.out.println("Mincraft.biz Launcher " + Config.LAUNCHER_VERSION);
        System.out.println("Last version is: " + Launcher.getVersionState().toString());
        System.out.println("The current launcher version is the latest: " + Launcher.isLastVersion());

        System.out.println("Minecraft.biz Launcher 0.1");
        System.out.println("Supporting your system: " + OperatingSystem.getCurrentPlatform().isSupported());
        System.out.println("Expected Java path: " + OperatingSystem.getCurrentPlatform().getJavaDir());
        System.out.println("Game directory based on your OS: '" + getWorkingDirectory());
        System.out.println("Do you want to update client? (Y/n)");

        Scanner in = new Scanner(System.in);
        String answer = in.nextLine().toLowerCase();

        if (answer.equals("y") || answer.equals("yes"))
        {
            Updater updater = new Updater();
            updater.setName("Updater");
            updater.start();
        }
        else {
            System.out.println("Unexpected command.");
        }

        in.close();

    }

    public static File getWorkingDirectory() {

        final String userHome = System.getProperty("user.home", ".");
        File workingDirectory = null;

        switch (OperatingSystem.getCurrentPlatform()) {
            case LINUX: {
                workingDirectory = new File(userHome, "Minecraft.biz/");
                break;
            }
            case WINDOWS: {
                final String applicationData = System.getenv("APPDATA");
                final String folder = (applicationData != null) ? applicationData : userHome;
                workingDirectory = new File(folder, "Minecraft.biz/");
                break;
            }
            case OSX: {
                workingDirectory = new File(userHome, "Library/Application Support/Minecraft.biz");
                break;
            }
            default: {
                workingDirectory = new File(userHome, "minecraft.biz/");
                break;
            }
        }

        return workingDirectory;
    }
}
