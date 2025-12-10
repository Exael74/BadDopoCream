package presentation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CharacterSelectionPanel extends JPanel {

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;

    private ResourceLoader resources;
    private FontLoader fontLoader;
    private int selectedLevel;
    private int numberOfPlayers;
    private boolean isP2CPU; // New flag

    private String hoveredCharacter = null;

    // Selection state
    private String selectedCharacterP1 = null;
    private String selectedCharacterP2 = null;
    private String p1Name = "P1";
    private String p2Name = "P2";
    private boolean isSelectingP2 = false; // False = Selecting P1, True = Selecting P2

    // Áreas de los personajes (clickeables)
    private Rectangle chocolateArea;
    private Rectangle fresaArea;
    private Rectangle vainillaArea;

    // GIFs animados de personajes
    private ImageIcon currentChocolateGif;
    private ImageIcon currentFresaGif;
    private ImageIcon currentVainillaGif;

    // Timer para actualizar las animaciones
    private javax.swing.Timer animationTimer;

    // Tamaño del marco y del personaje
    private static final int MARCO_SIZE = 220;
    private static final int CHARACTER_SIZE = 150;

    // Control de animación de victoria
    private boolean showingVictory = false;
    private ImageIcon victoryGif = null;
    private Rectangle victoryArea = null;

    // AI Types for MvM
    private domain.entity.AIType aiTypeP1 = null;
    private domain.entity.AIType aiTypeP2 = null;

    private domain.entity.AIType selectAIType(String playerName) {
        String[] options = { "Hambriento (Frutas)", "Miedoso (Seguro)", "Experto (Balanceado)" };
        int choice = JOptionPane.showOptionDialog(this,
                "Selecciona la personalidad de " + playerName + ":",
                "Personalidad IA",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]); // Default to Expert

        switch (choice) {
            case 0:
                return domain.entity.AIType.HUNGRY;
            case 1:
                return domain.entity.AIType.FEARFUL;
            case 2:
                return domain.entity.AIType.EXPERT;
            default:
                return domain.entity.AIType.EXPERT;
        }
    }

    // ... (fields)

    public CharacterSelectionPanel(int selectedLevel, int numberOfPlayers, ResourceLoader resources, boolean isP2CPU) {
        this.selectedLevel = selectedLevel;
        this.numberOfPlayers = numberOfPlayers;
        this.resources = resources;
        this.isP2CPU = isP2CPU;
        this.fontLoader = FontLoader.getInstance();

        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setLayout(null);

        // Inicializar con animaciones de quieto
        currentChocolateGif = resources.chocolateIdleDownGif;
        currentFresaGif = resources.rosaIdleDownGif;
        currentVainillaGif = resources.vainillaIdleDownGif;

        setupCharacterAreas();
        setupMouseListeners();
        setupBackButton();
        startAnimationTimer();
    }

    private void setupCharacterAreas() {
        int spacing = 100;
        int totalWidth = (MARCO_SIZE * 3) + (spacing * 2);
        int startX = (WINDOW_WIDTH - totalWidth) / 2;
        int characterY = 320;

        chocolateArea = new Rectangle(startX, characterY, MARCO_SIZE, MARCO_SIZE);
        fresaArea = new Rectangle(startX + MARCO_SIZE + spacing, characterY, MARCO_SIZE, MARCO_SIZE);
        vainillaArea = new Rectangle(startX + (MARCO_SIZE + spacing) * 2, characterY, MARCO_SIZE, MARCO_SIZE);
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (showingVictory)
                    return;

                Point clickPoint = e.getPoint();
                String clickedChar = null;

                if (chocolateArea.contains(clickPoint)) {
                    clickedChar = "Chocolate";
                } else if (fresaArea.contains(clickPoint)) {
                    clickedChar = "Fresa";
                } else if (vainillaArea.contains(clickPoint)) {
                    clickedChar = "Vainilla";
                }

                if (clickedChar != null) {
                    handleSelection(clickedChar);
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (showingVictory)
                    return;

                Point mousePoint = e.getPoint();
                String previousHovered = hoveredCharacter;

                if (chocolateArea.contains(mousePoint)) {
                    hoveredCharacter = "Chocolate";
                } else if (fresaArea.contains(mousePoint)) {
                    hoveredCharacter = "Fresa";
                } else if (vainillaArea.contains(mousePoint)) {
                    hoveredCharacter = "Vainilla";
                } else {
                    hoveredCharacter = null;
                }

                if (!java.util.Objects.equals(previousHovered, hoveredCharacter)) {
                    updateCharacterAnimations();
                }
            }
        });
    }

    private void updateCharacterAnimations() {
        if ("Chocolate".equals(hoveredCharacter)) {
            currentChocolateGif = resources.chocolateWalkDownGif;
        } else {
            currentChocolateGif = resources.chocolateIdleDownGif;
        }

        if ("Fresa".equals(hoveredCharacter)) {
            currentFresaGif = resources.rosaWalkDownGif;
        } else {
            currentFresaGif = resources.rosaIdleDownGif;
        }

        if ("Vainilla".equals(hoveredCharacter)) {
            currentVainillaGif = resources.vainillaWalkDownGif;
        } else {
            currentVainillaGif = resources.vainillaIdleDownGif;
        }
    }

    private void startAnimationTimer() {
        animationTimer = new javax.swing.Timer(33, e -> repaint());
        animationTimer.start();
    }

    private void handleSelection(String character) {
        if (numberOfPlayers == 2 || numberOfPlayers == 0) {
            if (!isSelectingP2) {
                // P1 selection
                String title = (numberOfPlayers == 0) ? "Confirmar Máquina 1" : "Confirmar Jugador 1";
                String msg = (numberOfPlayers == 0) ? "Máquina 1: ¿Elegir a " + character + "?"
                        : "Jugador 1: ¿Elegir a " + character + "?";

                int response = JOptionPane.showConfirmDialog(
                        this,
                        msg,
                        title,
                        JOptionPane.NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (response == JOptionPane.YES_OPTION) {
                    selectedCharacterP1 = character;

                    // Ask for name if Machine vs Machine OR PvP
                    String defaultName = (numberOfPlayers == 0) ? "Máquina 1" : "Jugador 1";
                    String inputName = JOptionPane.showInputDialog(this, "Nombre para " + defaultName + ":", "P1");
                    if (inputName != null && !inputName.trim().isEmpty()) {
                        p1Name = inputName.trim();
                    }

                    // For MvM, select AI Type for P1
                    if (numberOfPlayers == 0) {
                        aiTypeP1 = selectAIType("Máquina 1");
                    }

                    isSelectingP2 = true;
                    repaint();
                }
            } else {
                // P2 selection
                String title;
                String msg;
                if (numberOfPlayers == 0) {
                    title = "Confirmar Máquina 2";
                    msg = "Máquina 2: ¿Elegir a " + character + "?";
                } else if (isP2CPU) {
                    title = "Confirmar Máquina (P2)";
                    msg = "Máquina (P2): ¿Elegir a " + character + "?";
                } else {
                    title = "Confirmar Jugador 2";
                    msg = "Jugador 2: ¿Elegir a " + character + "?";
                }

                int response = JOptionPane.showConfirmDialog(
                        this,
                        msg,
                        title,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (response == JOptionPane.YES_OPTION) {
                    selectedCharacterP2 = character;

                    // Ask for name
                    String defaultName = (numberOfPlayers == 0 || isP2CPU) ? "Máquina 2" : "Jugador 2";
                    String inputName = JOptionPane.showInputDialog(this, "Nombre para " + defaultName + ":", "P2");
                    if (inputName != null && !inputName.trim().isEmpty()) {
                        p2Name = inputName.trim();
                    }

                    // For MvM OR P1 vs Machine, select AI Type for P2
                    if (numberOfPlayers == 0 || isP2CPU) {
                        aiTypeP2 = selectAIType((isP2CPU) ? "Máquina (P2)" : "Máquina 2");
                    }

                    showVictoryAnimation(character); // Show victory for P2's choice then start
                }
            }
        } else {
            // Single player
            int response = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de elegir a " + character + "?",
                    "Confirmar selección",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (response == JOptionPane.YES_OPTION) {
                selectedCharacterP1 = character;

                // Ask for name for Single Player
                String inputName = JOptionPane.showInputDialog(this, "Nombre para Jugador 1:", "P1");
                if (inputName != null && !inputName.trim().isEmpty()) {
                    p1Name = inputName.trim();
                }

                showVictoryAnimation(character);
            }
        }
    }

    private void showVictoryAnimation(String character) {
        showingVictory = true;

        switch (character) {
            case "Chocolate":
                victoryGif = resources.chocolateVictoryGif;
                victoryArea = chocolateArea;
                break;
            case "Fresa":
                victoryGif = resources.rosaVictoryGif;
                victoryArea = fresaArea;
                break;
            case "Vainilla":
                victoryGif = resources.vainillaVictoryGif;
                victoryArea = vainillaArea;
                break;
        }

        javax.swing.Timer victoryTimer = new javax.swing.Timer(2000, e -> {
            startGame();
        });
        victoryTimer.setRepeats(false);
        victoryTimer.start();
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
            public void mouseClicked(MouseEvent e) {
                if (!showingVictory) {
                    if (isSelectingP2) {
                        // Go back to P1 selection
                        isSelectingP2 = false;
                        selectedCharacterP1 = null;
                        repaint();
                    } else {
                        goBackToLevelSelection();
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!showingVictory) {
                    backButton.setIcon(new ImageIcon(hoverButton));
                    int newX = originalX - (backButtonWidthHover - backButtonWidth) / 2;
                    int newY = originalY - (backButtonHeightHover - backButtonHeight) / 2;
                    backButton.setBounds(newX, newY, backButtonWidthHover, backButtonHeightHover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setIcon(new ImageIcon(normalButton));
                backButton.setBounds(originalX, originalY, backButtonWidth, backButtonHeight);
            }
        });

        add(backButton);
    }

    private void startGame() {
        domain.BadDopoLogger.logInfo("Starting game...");
        domain.BadDopoLogger.logInfo("P1: " + selectedCharacterP1 + " (" + p1Name + ")");
        if (numberOfPlayers == 2 || numberOfPlayers == 0) {
            domain.BadDopoLogger.logInfo("P2: " + selectedCharacterP2 + " (" + p2Name + ")");
        }

        cleanup();

        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            frame.dispose();

            // Pass both characters if 2 players or Machine vs Machine, otherwise just P1
            String p2Char = (numberOfPlayers == 2 || numberOfPlayers == 0) ? selectedCharacterP2 : null;

            // Note: We need to update GameWindow constructor to accept p2Char and names
            new GameWindow(selectedCharacterP1, p2Char, p1Name, p2Name, selectedLevel, numberOfPlayers, resources,
                    aiTypeP1, aiTypeP2, isP2CPU);
        }
    }

    private void goBackToLevelSelection() {
        cleanup();

        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            frame.getContentPane().removeAll();

            LevelSelectionPanel levelPanel = new LevelSelectionPanel(numberOfPlayers, resources, isP2CPU);
            frame.add(levelPanel);
            frame.revalidate();
            frame.repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Dibujar fondo
        if (resources.wallpaperImage != null) {
            g2d.drawImage(resources.wallpaperImage, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, this);
        }

        // Dibujar título de selección de personaje
        if (resources.characterSelectionGif != null) {
            Image titleImage = resources.characterSelectionGif.getImage();
            int titleWidth = 600;
            int titleHeight = 150;
            int titleX = (WINDOW_WIDTH - titleWidth) / 2;
            int titleY = 50;
            g2d.drawImage(titleImage, titleX, titleY, titleWidth, titleHeight, this);
        }

        // Draw prompt for current player
        g2d.setColor(Color.WHITE);
        g2d.setFont(fontLoader.getBoldFont(32f));
        String prompt = "Selecciona tu personaje";
        if (numberOfPlayers == 2 || numberOfPlayers == 0) {
            if (!isSelectingP2) {
                prompt = (numberOfPlayers == 0) ? "Máquina 1: Selecciona personaje"
                        : "Jugador 1: Selecciona tu personaje";
                g2d.setColor(new Color(100, 200, 255)); // Blueish for P1
            } else {
                prompt = (numberOfPlayers == 0) ? "Máquina 2: Selecciona personaje"
                        : "Jugador 2: Selecciona tu personaje";
                g2d.setColor(new Color(255, 100, 100)); // Reddish for P2
            }
        }
        FontMetrics fmPrompt = g2d.getFontMetrics();
        int promptWidth = fmPrompt.stringWidth(prompt);
        g2d.drawString(prompt, (WINDOW_WIDTH - promptWidth) / 2, 220);

        if (showingVictory && victoryGif != null && victoryArea != null) {
            // Dibujar el marco del personaje seleccionado
            if (resources.marcoSeleccionImage != null) {
                g2d.drawImage(resources.marcoSeleccionImage, victoryArea.x, victoryArea.y, MARCO_SIZE, MARCO_SIZE,
                        this);
            }

            // Dibujar la animación de victoria centrada
            int offsetX = (MARCO_SIZE - CHARACTER_SIZE) / 2;
            int offsetY = (MARCO_SIZE - CHARACTER_SIZE) / 2;

            Image victoryImage = victoryGif.getImage();
            g2d.drawImage(victoryImage,
                    victoryArea.x + offsetX,
                    victoryArea.y + offsetY,
                    CHARACTER_SIZE,
                    CHARACTER_SIZE,
                    this);

            // Dibujar nombre del personaje con fuente personalizada
            g2d.setColor(Color.WHITE);
            g2d.setFont(fontLoader.getBoldFont(28f));
            FontMetrics fm = g2d.getFontMetrics();
            String charName = (isSelectingP2) ? selectedCharacterP2 : selectedCharacterP1;
            if (charName == null)
                charName = ""; // Safety check
            int textWidth = fm.stringWidth(charName);
            g2d.drawString(charName, victoryArea.x + (MARCO_SIZE - textWidth) / 2, victoryArea.y + MARCO_SIZE + 40);

        } else {
            // Mostrar todos los personajes normalmente

            // Dibujar marcos
            if (resources.marcoSeleccionImage != null) {
                if ("Chocolate".equals(hoveredCharacter)) {
                    g2d.drawImage(resources.marcoSeleccionImage, chocolateArea.x, chocolateArea.y, MARCO_SIZE,
                            MARCO_SIZE, this);
                }
                if ("Fresa".equals(hoveredCharacter)) {
                    g2d.drawImage(resources.marcoSeleccionImage, fresaArea.x, fresaArea.y, MARCO_SIZE, MARCO_SIZE,
                            this);
                }
                if ("Vainilla".equals(hoveredCharacter)) {
                    g2d.drawImage(resources.marcoSeleccionImage, vainillaArea.x, vainillaArea.y, MARCO_SIZE, MARCO_SIZE,
                            this);
                }
            }

            // Dibujar personajes
            int offsetX = (MARCO_SIZE - CHARACTER_SIZE) / 2;
            int offsetY = (MARCO_SIZE - CHARACTER_SIZE) / 2;

            if (currentChocolateGif != null) {
                Image chocolateImg = currentChocolateGif.getImage();
                g2d.drawImage(chocolateImg,
                        chocolateArea.x + offsetX,
                        chocolateArea.y + offsetY,
                        CHARACTER_SIZE,
                        CHARACTER_SIZE,
                        this);
            }

            if (currentFresaGif != null) {
                Image fresaImg = currentFresaGif.getImage();
                g2d.drawImage(fresaImg,
                        fresaArea.x + offsetX,
                        fresaArea.y + offsetY,
                        CHARACTER_SIZE,
                        CHARACTER_SIZE,
                        this);
            }

            if (currentVainillaGif != null) {
                Image vainillaImg = currentVainillaGif.getImage();
                g2d.drawImage(vainillaImg,
                        vainillaArea.x + offsetX,
                        vainillaArea.y + offsetY,
                        CHARACTER_SIZE,
                        CHARACTER_SIZE,
                        this);
            }

            // Draw "P1" or "P2" badges on selected characters if in 2 player mode
            if ((numberOfPlayers == 2 || numberOfPlayers == 0) && selectedCharacterP1 != null) {
                // Find which area corresponds to P1 selection
                Rectangle p1Area = null;
                if ("Chocolate".equals(selectedCharacterP1))
                    p1Area = chocolateArea;
                else if ("Fresa".equals(selectedCharacterP1))
                    p1Area = fresaArea;
                else if ("Vainilla".equals(selectedCharacterP1))
                    p1Area = vainillaArea;

                if (p1Area != null) {
                    g2d.setColor(new Color(100, 200, 255, 200));
                    g2d.fillOval(p1Area.x + 10, p1Area.y + 10, 40, 40);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(fontLoader.getBoldFont(20f));
                    g2d.drawString("P1", p1Area.x + 18, p1Area.y + 38);
                }
            }

            // Dibujar nombres con fuente personalizada
            g2d.setColor(Color.WHITE);
            g2d.setFont(fontLoader.getBoldFont(24f));

            FontMetrics fm = g2d.getFontMetrics();

            String chocolateText = "Chocolate";
            int chocolateTextWidth = fm.stringWidth(chocolateText);
            g2d.drawString(chocolateText, chocolateArea.x + (MARCO_SIZE - chocolateTextWidth) / 2,
                    chocolateArea.y + MARCO_SIZE + 40);

            String fresaText = "Fresa";
            int fresaTextWidth = fm.stringWidth(fresaText);
            g2d.drawString(fresaText, fresaArea.x + (MARCO_SIZE - fresaTextWidth) / 2, fresaArea.y + MARCO_SIZE + 40);

            String vainillaText = "Vainilla";
            int vainillaTextWidth = fm.stringWidth(vainillaText);
            g2d.drawString(vainillaText, vainillaArea.x + (MARCO_SIZE - vainillaTextWidth) / 2,
                    vainillaArea.y + MARCO_SIZE + 40);
        }
    }

    public void cleanup() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}