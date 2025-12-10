package presentation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ScreenManager {

    // Constantes de tamaño
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;
    private static final int BUTTON_WIDTH = 380;
    private static final int BUTTON_HEIGHT = 200;
    private static final int BUTTON_WIDTH_HOVER = 400;
    private static final int BUTTON_HEIGHT_HOVER = 200;

    private final JPanel mainPanel;
    private final JLabel titleLabel;
    private final ResourceLoader resources;
    private final WelcomeScreen parentWindow;

    // Variables de estado
    private int numberOfPlayers = 0;
    private boolean isP2CPU = false;

    public ScreenManager(JPanel mainPanel, JLabel titleLabel, ResourceLoader resources, WelcomeScreen parentWindow) {
        this.mainPanel = mainPanel;
        this.titleLabel = titleLabel;
        this.resources = resources;
        this.parentWindow = parentWindow;
    }

    // ==================== PANTALLA 1: START ====================
    public void showStartScreen() {
        clearButtons();
        setTitle(resources.titleGif);

        if (resources.startButtonImage != null) {
            JLabel startButton = createButton(
                    resources.startButtonImage,
                    (WINDOW_WIDTH - BUTTON_WIDTH) / 2,
                    330,
                    this::showMainMenu);
            mainPanel.add(startButton);
        }

        refreshPanel();
    }

    // ==================== PANTALLA 2: MAIN MENU ====================
    public void showMainMenu() {
        clearButtons();
        setTitle(resources.titleGif);

        int startY = 250;
        int spacing = 100;

        // Botón Start Game
        if (resources.startGameImage != null) {
            JLabel startGameButton = createButton(
                    resources.startGameImage,
                    (WINDOW_WIDTH - BUTTON_WIDTH) / 2,
                    startY,
                    this::showPlayerSelectionMenu);
            mainPanel.add(startGameButton);
        }

        // Botón Options
        if (resources.optionsImage != null) {
            JLabel optionsButton = createButton(
                    resources.optionsImage,
                    (WINDOW_WIDTH - BUTTON_WIDTH) / 2,
                    startY + spacing,
                    () -> JOptionPane.showMessageDialog(
                            parentWindow,
                            "Juego en construcción",
                            "BAD DOPO CREAM",
                            JOptionPane.INFORMATION_MESSAGE));
            mainPanel.add(optionsButton);
        }

        // Botón Back
        if (resources.backImage != null) {
            JLabel backButton = createButton(
                    resources.backImage,
                    (WINDOW_WIDTH - BUTTON_WIDTH) / 2,
                    startY + spacing * 2,
                    this::showStartScreen);
            mainPanel.add(backButton);
        }

        refreshPanel();
    }

    // ==================== PANTALLA 3: PLAYER SELECTION ====================
    public void showPlayerSelectionMenu() {
        clearButtons();
        setTitle(resources.titleGif);

        int startY = 250;
        int spacing = 100;

        // Botón 1 Player
        if (resources.onePlayerImage != null) {
            JLabel onePlayerButton = createButton(
                    resources.onePlayerImage,
                    (WINDOW_WIDTH - BUTTON_WIDTH) / 2,
                    startY,
                    () -> {
                        numberOfPlayers = 1;
                        showLevelSelection();
                    });
            mainPanel.add(onePlayerButton);
        }

        // Botón 2 Player
        if (resources.twoPlayerImage != null) {
            JLabel twoPlayerButton = createButton(
                    resources.twoPlayerImage,
                    (WINDOW_WIDTH - BUTTON_WIDTH) / 2,
                    startY + spacing,
                    this::showTwoPlayerModeSelection // Call new method
            );
            mainPanel.add(twoPlayerButton);
        }

        // Botón Machine vs Machine (reemplaza al Back)
        if (resources.machineVsMachineImage != null) {
            JLabel machineButton = createButton(
                    resources.machineVsMachineImage,
                    (WINDOW_WIDTH - BUTTON_WIDTH) / 2,
                    startY + spacing * 2,
                    () -> {
                        numberOfPlayers = 0; // 0 = Machine vs Machine
                        isP2CPU = true; // Implicitly true for MvM, but MvM has its own logic (numberOfPlayers=0)
                        showLevelSelection();
                    });
            mainPanel.add(machineButton);
        }

        // Botón Back en la esquina inferior izquierda
        if (resources.backImage != null) {
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
                    showMainMenu();
                }
            });

            mainPanel.add(backButton);
        }

        refreshPanel();
    }

    // ==================== PANTALLA 3.5: 2 PLAYER MODE SELECTION
    // ====================
    public void showTwoPlayerModeSelection() {
        clearButtons();
        setTitle(resources.titleGif);

        int startY = 250;
        int spacing = 100;

        // Botón P1 vs P2
        if (resources.p1VsP2Image != null) {
            JLabel p1VsP2Button = createButton(
                    resources.p1VsP2Image,
                    (WINDOW_WIDTH - BUTTON_WIDTH) / 2,
                    startY,
                    () -> {
                        numberOfPlayers = 2;
                        isP2CPU = false;
                        showLevelSelection();
                    });
            mainPanel.add(p1VsP2Button);
        }

        // Botón P1 vs Machine
        if (resources.p1VsMachineImage != null) {
            JLabel p1VsMachineButton = createButton(
                    resources.p1VsMachineImage,
                    (WINDOW_WIDTH - BUTTON_WIDTH) / 2,
                    startY + spacing,
                    () -> {
                        numberOfPlayers = 2; // Treat as 2 players for game logic
                        isP2CPU = true; // Flag to indicate P2 is AIcontrolled
                        showLevelSelection();
                    });
            mainPanel.add(p1VsMachineButton);
        }

        addBackButton(this::showPlayerSelectionMenu);

        refreshPanel();
    }

    private void addBackButton(Runnable onClick) {
        if (resources.backImage != null) {
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
                    onClick.run();
                }
            });

            mainPanel.add(backButton);
        }
    }

    // ==================== PANTALLA 4: LEVEL SELECTION ====================
    public void showLevelSelection() {
        // Cerrar la ventana actual y abrir nueva con LevelSelectionPanel
        parentWindow.dispose();

        SwingUtilities.invokeLater(() -> {
            JFrame levelFrame = new JFrame("BAD DOPO CREAM - Selección de Nivel");
            levelFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            levelFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            levelFrame.setLocationRelativeTo(null);
            levelFrame.setResizable(false);

            LevelSelectionPanel levelPanel = new LevelSelectionPanel(numberOfPlayers, resources, isP2CPU);
            levelFrame.add(levelPanel);

            levelFrame.setVisible(true);
        });
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private JLabel createButton(Image buttonImage, int x, int y, Runnable onClick) {
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
                onClick.run();
            }
        });

        return button;
    }

    private void setTitle(ImageIcon newTitleGif) {
        if (titleLabel != null && newTitleGif != null) {
            titleLabel.setIcon(newTitleGif);

            int titleWidth = 800;
            int originalWidth = newTitleGif.getIconWidth();
            int originalHeight = newTitleGif.getIconHeight();
            int titleHeight = (int) (titleWidth * originalHeight / (double) originalWidth);

            titleLabel.setBounds(
                    (WINDOW_WIDTH - titleWidth) / 2,
                    60,
                    titleWidth,
                    titleHeight);
        }
    }

    private void clearButtons() {
        Component[] components = mainPanel.getComponents();
        for (Component component : components) {
            if (component != titleLabel) {
                mainPanel.remove(component);
            }
        }
    }

    private void refreshPanel() {
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}