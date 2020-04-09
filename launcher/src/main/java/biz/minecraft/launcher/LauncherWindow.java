package biz.minecraft.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.*;

public class LauncherWindow extends JFrame {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    private JLabel icon;
    private JLabel label;
    private JProgressBar progressBar;

    public LauncherWindow() {

        // Load window icon
        Image grassImage = loadImage("/grass.png");

        // Frame parameters
        setTitle("Updating Minecraft");
        setResizable(false);
        setIconImage(grassImage);

        // Handlers
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Components
        Image resizedImage = grassImage.getScaledInstance(45, 48, Image.SCALE_SMOOTH);
        this.icon = new JLabel(new ImageIcon(resizedImage));
        this.label = new JLabel("Updating Minecraft");
        this.progressBar = new JProgressBar(0, 100);

        // Progress bar properties
        this.progressBar.setIndeterminate(true);
        this.progressBar.setPreferredSize(new Dimension(300, 10));

        setLayout(constructLayout());
        pack();

        // Frame parameters after packing
        setLocationRelativeTo(null);
    }

    private GroupLayout constructLayout() {

        GroupLayout layout = new GroupLayout(getContentPane());

        // Automatic gap insertion
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Root horizontal group
        layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(icon)
                .addGroup(layout.createParallelGroup().addComponent(label).addComponent(progressBar)));

        // Root vertical group
        layout.setVerticalGroup(layout.createParallelGroup().addComponent(icon)
                .addGroup(layout.createSequentialGroup().addComponent(label).addComponent(progressBar)));

        // layout.linkSize(SwingConstants.HORIZONTAL, icon, progressBar);

        return layout;
    }

    public JLabel getLabel() {
        return this.label;
    }

    public JProgressBar getProgressBar() {
        return this.progressBar;
    }

    public static Image loadImage(String path) {

        try (InputStream is = LauncherWindow.class.getResourceAsStream(path)) {

            return ImageIO.read(is);
        } catch (IOException ex) {
            logger.error("Can't read image!", ex);
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
    }
}