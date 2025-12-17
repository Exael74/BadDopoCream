package presentation;

import javax.swing.*;
import java.awt.*;

public class WelcomeScreen extends JFrame {

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;

    private JPanel mainPanel;
    private JLabel titleLabel;
    private ResourceLoader resources;
    private ScreenManager screenManager;

    public WelcomeScreen() {
        setTitle("BAD DOPO CREAM");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        resources = new ResourceLoader();
        setupUI();

        screenManager = new ScreenManager(mainPanel, titleLabel, resources, this);
        screenManager.showStartScreen();
    }

    private void setupUI() {
        // Panel principal con fondo
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (resources.wallpaperImage != null) {
                    g.drawImage(resources.wallpaperImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        mainPanel.setLayout(null);

        // Título (GIF)
        if (resources.titleGif != null) {
            titleLabel = new JLabel(resources.titleGif);
            int titleWidth = 700;
            int titleHeight = (int) (titleWidth * 146.0 / 800.0);
            titleLabel.setBounds(
                    (WINDOW_WIDTH - titleWidth) / 2,
                    80,
                    titleWidth,
                    titleHeight
            );
            mainPanel.add(titleLabel);
        }

        add(mainPanel);
    }
}
