package presentation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LevelSelectionPanel extends JPanel {

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;
    private static final int BUTTON_WIDTH = 380;
    private static final int BUTTON_HEIGHT = 200;
    private static final int BUTTON_WIDTH_HOVER = 400;
    private static final int BUTTON_HEIGHT_HOVER = 200;

    private ResourceLoader resources;
    private FontLoader fontLoader;
    private int numberOfPlayers;
    private boolean isP2CPU;

    public LevelSelectionPanel(int numberOfPlayers, ResourceLoader resources, boolean isP2CPU) {
        this.numberOfPlayers = numberOfPlayers;
        this.resources = resources;
        this.isP2CPU = isP2CPU;
        this.fontLoader = FontLoader.getInstance();

        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setLayout(null);

        setupLevelButtons();
        setupBackButton();
    }

    private void setupLevelButtons() {
        int startY = 150; // Raised start Y to fit 4 buttons
        int spacing = 110; // Slightly reduced spacing

        if (resources.nivel1Image != null) {
            JLabel nivel1Button = createLevelButton(resources.nivel1Image,
                    (WINDOW_WIDTH - BUTTON_WIDTH) / 2, startY, 1);
            add(nivel1Button);
        }

        if (resources.nivel2Image != null) {
            JLabel nivel2Button = createLevelButton(resources.nivel2Image,
                    (WINDOW_WIDTH - BUTTON_WIDTH) / 2, startY + spacing, 2);
            add(nivel2Button);
        }

        if (resources.nivel3Image != null) {
            JLabel nivel3Button = createLevelButton(resources.nivel3Image,
                    (WINDOW_WIDTH - BUTTON_WIDTH) / 2, startY + spacing * 2, 3);
            add(nivel3Button);
        }

        if (resources.nivel4Image != null) {
            JLabel nivel4Button = createLevelButton(resources.nivel4Image,
                    (WINDOW_WIDTH - BUTTON_WIDTH) / 2, startY + spacing * 3, 4);
            add(nivel4Button);
        }
    }

    private JLabel createLevelButton(Image buttonImage, int x, int y, int level) {
        Image normalButton = buttonImage.getScaledInstance(BUTTON_WIDTH, BUTTON_HEIGHT, Image.SCALE_SMOOTH);
        Image hoverButton = buttonImage.getScaledInstance(BUTTON_WIDTH_HOVER, BUTTON_HEIGHT_HOVER, Image.SCALE_SMOOTH);

        JLabel button = new JLabel(new ImageIcon(normalButton));
        button.setBounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        final int originalX = x;
        final int originalY = y;

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setIcon(new ImageIcon(hoverButton));
                int newX = originalX - (BUTTON_WIDTH_HOVER - BUTTON_WIDTH) / 2;
                int newY = originalY - (BUTTON_HEIGHT_HOVER - BUTTON_HEIGHT) / 2;
                button.setBounds(newX, newY, BUTTON_WIDTH_HOVER, BUTTON_HEIGHT_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setIcon(new ImageIcon(normalButton));
                button.setBounds(originalX, originalY, BUTTON_WIDTH, BUTTON_HEIGHT);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                goToCharacterSelection(level);
            }
        });

        return button;
    }

    private void setupBackButton() {
        int backButtonWidth = 200;
        int backButtonHeight = 100;
        int backButtonWidthHover = 220;
        int backButtonHeightHover = 110;

        Image normalButton = resources.backImage.getScaledInstance(backButtonWidth, backButtonHeight,
                Image.SCALE_SMOOTH);
        Image hoverButton = resources.backImage.getScaledInstance(backButtonWidthHover, backButtonHeightHover,
                Image.SCALE_SMOOTH);

        JLabel backButton = new JLabel(new ImageIcon(normalButton));
        int backX = 30;
        int backY = WINDOW_HEIGHT - backButtonHeight - 30;
        backButton.setBounds(backX, backY, backButtonWidth, backButtonHeight);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        final int originalX = backX;
        final int originalY = backY;

        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backButton.setIcon(new ImageIcon(hoverButton));
                int newX = originalX - (backButtonWidthHover - backButtonWidth) / 2;
                int newY = originalY - (backButtonHeightHover - backButtonHeight) / 2;
                backButton.setBounds(newX, newY, backButtonWidthHover, backButtonHeightHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setIcon(new ImageIcon(normalButton));
                backButton.setBounds(originalX, originalY, backButtonWidth, backButtonHeight);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                goBackToMainMenu();
            }
        });

        add(backButton);
    }

    private void goToCharacterSelection(int level) {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            frame.getContentPane().removeAll();

            CharacterSelectionPanel characterPanel = new CharacterSelectionPanel(level, numberOfPlayers, resources,
                    isP2CPU);
            frame.add(characterPanel);
            frame.revalidate();
            frame.repaint();
        }
    }

    private void goBackToMainMenu() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            frame.dispose();

            SwingUtilities.invokeLater(() -> {
                WelcomeScreen welcomeScreen = new WelcomeScreen();
                welcomeScreen.setVisible(true);
            });
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Activar antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Dibujar fondo
        if (resources.wallpaperImage != null) {
            g2d.drawImage(resources.wallpaperImage, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, this);
        }

        // Dibujar título
        if (resources.levelSelectionGif != null) {
            Image titleImage = resources.levelSelectionGif.getImage();
            int titleWidth = 600;
            int titleHeight = 150;
            int titleX = (WINDOW_WIDTH - titleWidth) / 2;
            int titleY = 50;
            g2d.drawImage(titleImage, titleX, titleY, titleWidth, titleHeight, this);
        }
    }
}