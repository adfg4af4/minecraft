package biz.minecraft.launcher.layout.updater;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

import javax.swing.*;

public class UpdaterLayout extends JFrame {

    private final static Logger logger = LoggerFactory.getLogger(UpdaterLayout.class);

    private JLabel icon, label;
    private JProgressBar progressBar;

    // Constructor

    public UpdaterLayout() {

        super("Обновление игры");

        Image image = getToolkit().getImage(getClass().getClassLoader().getResource("grass.png"));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(image);
        setResizable(false);

        icon = new JLabel(new ImageIcon(image.getScaledInstance(45, 48, Image.SCALE_SMOOTH)));

        label = new JLabel("Получение данных с сервера");

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true); // Temp visual effect
        progressBar.setPreferredSize(new Dimension(275, 20));

        add(getUpdaterPanel());

        pack();

        setLocationRelativeTo(null);
        setVisible(true);

        // Starting Game-Updater Thread & Game next

        SwingUtilities.invokeLater(() -> {
            GameUpdater updater = new GameUpdater(this);
        });
    }

    /**
     * Building Updater Panel using MiG Layout.
     *
     * @return JPanel Updater Panel.
     */
    private JPanel getUpdaterPanel() {

        JPanel updaterPanel = new JPanel(new MigLayout("insets 12 15 8 15", "[]15[]", "[][][]"));

        updaterPanel.add(icon, "span 1 2");
        updaterPanel.add(label, "wrap");

        updaterPanel.add(progressBar, "");

        return updaterPanel;
    }

    public JLabel getLabel() {
        return label;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }
}