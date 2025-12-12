package presentation;

import domain.GameFacade;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * Maneja el renderizado de la interfaz de usuario durante el juego.
 * (Barra lateral, contadores, temporizador).
 * Actúa como una Vista auxiliar.
 */
public class GameHUD {

    private GameFacade gameFacade;
    private ResourceLoader resources;
    private FontLoader fontLoader;

    private static final int SIDEBAR_WIDTH = 200;
    private static final int SIDEBAR_PADDING = 20;
    private static final int SIDEBAR_FRUIT_SIZE = 40;

    public GameHUD(GameFacade gameFacade, ResourceLoader resources, FontLoader fontLoader) {
        this.gameFacade = gameFacade;
        this.resources = resources;
        this.fontLoader = fontLoader;
    }

    /**
     * Dibuja el panel lateral con temporizador y contador de frutas.
     */
    public void drawSidebar(Graphics2D g2d, int mapOffsetX, int numberOfPlayers, int currentLevel) {
        int sidebarX = mapOffsetX - SIDEBAR_WIDTH - 20;
        int sidebarY = 100;
        int sidebarHeight = 600;

        // Fondo del panel lateral con transparencia
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(sidebarX, sidebarY, SIDEBAR_WIDTH, sidebarHeight, 15, 15);

        // Borde del panel
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(sidebarX, sidebarY, SIDEBAR_WIDTH, sidebarHeight, 15, 15);

        int currentY = sidebarY + SIDEBAR_PADDING;

        // ==================== TEMPORIZADOR ====================
        drawTimer(g2d, sidebarX, currentY);
        currentY += 80;

        // Línea separadora
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.drawLine(sidebarX + 20, currentY, sidebarX + SIDEBAR_WIDTH - 20, currentY);
        currentY += 30;

        // ==================== FRUTAS RESTANTES ====================
        currentY = drawFruitsRemaining(g2d, sidebarX, currentY);

        // ==================== PUNTUACIÓN ====================
        currentY += 10;
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.drawLine(sidebarX + 20, currentY, sidebarX + SIDEBAR_WIDTH - 20, currentY);
        currentY += 30;

        drawScores(g2d, sidebarX, currentY, numberOfPlayers);

        // ==================== CONTROLES ====================
        drawControlsHint(g2d, numberOfPlayers, currentLevel);
    }

    private void drawTimer(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(fontLoader.getBoldFont(20f));
        String timeLabel = "TIEMPO";
        FontMetrics fmLabel = g2d.getFontMetrics();
        g2d.drawString(timeLabel, x + (SIDEBAR_WIDTH - fmLabel.stringWidth(timeLabel)) / 2, y);

        y += 30;
        String timeRemaining = gameFacade.getFormattedTime();
        long timeInMs = gameFacade.getTimeRemaining();

        if (timeInMs <= 30000) {
            g2d.setColor(new Color(255, 50, 50));
        } else if (timeInMs <= 60000) {
            g2d.setColor(new Color(255, 255, 0));
        } else {
            g2d.setColor(new Color(100, 255, 100));
        }

        g2d.setFont(fontLoader.getBoldFont(32f));
        FontMetrics fmTime = g2d.getFontMetrics();
        g2d.drawString(timeRemaining, x + (SIDEBAR_WIDTH - fmTime.stringWidth(timeRemaining)) / 2, y);

        // Warning Blink
        if (timeInMs <= 30000 && timeInMs > 0) {
            y += 20;
            g2d.setColor(new Color(255, 50, 50));
            g2d.setFont(fontLoader.getBoldFont(16f));
            String warning = "¡APÚRATE!";
            int alpha = (int) ((Math.sin(System.currentTimeMillis() / 200.0) + 1) * 127.5);
            g2d.setColor(new Color(255, 50, 50, alpha));
            g2d.drawString(warning, x + (SIDEBAR_WIDTH - g2d.getFontMetrics().stringWidth(warning)) / 2, y + 30); // pushed
                                                                                                                  // down
                                                                                                                  // a
                                                                                                                  // bit
        }
    }

    private int drawFruitsRemaining(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(fontLoader.getBoldFont(18f));
        String fruitLabel = "FRUTAS";
        g2d.drawString(fruitLabel, x + (SIDEBAR_WIDTH - g2d.getFontMetrics().stringWidth(fruitLabel)) / 2, y);
        y += 35;

        List<String> fruitTypes = gameFacade.getUniqueFruitTypes();
        for (String fruitType : fruitTypes) {
            int remainingCount = gameFacade.countRemainingFruits(fruitType);
            ImageIcon fruitImage = resources.getFruitImage(fruitType);
            int fruitX = x + 30;

            if (fruitImage != null) {
                g2d.drawImage(fruitImage.getImage(), fruitX, y, SIDEBAR_FRUIT_SIZE, SIDEBAR_FRUIT_SIZE, null);
            }

            g2d.setFont(fontLoader.getBoldFont(24f));
            if (remainingCount == 0) {
                g2d.setColor(new Color(100, 100, 100));
            } else {
                g2d.setColor(Color.WHITE);
            }
            g2d.drawString("x " + remainingCount, fruitX + SIDEBAR_FRUIT_SIZE + 15, y + 28);
            y += SIDEBAR_FRUIT_SIZE + 15;
        }
        return y;
    }

    private void drawScores(Graphics2D g2d, int x, int y, int numberOfPlayers) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(fontLoader.getBoldFont(18f));
        String scoreLabel = "PUNTOS";
        g2d.drawString(scoreLabel, x + (SIDEBAR_WIDTH - g2d.getFontMetrics().stringWidth(scoreLabel)) / 2, y);
        y += 35;

        int score = gameFacade.getScore();
        g2d.setFont(fontLoader.getBoldFont(32f));
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString(String.valueOf(score),
                x + (SIDEBAR_WIDTH - g2d.getFontMetrics().stringWidth(String.valueOf(score))) / 2, y);

        if (numberOfPlayers == 2) {
            y += 40;
            int score2 = gameFacade.getScorePlayer2();
            g2d.setColor(new Color(255, 100, 100));
            g2d.drawString(String.valueOf(score2),
                    x + (SIDEBAR_WIDTH - g2d.getFontMetrics().stringWidth(String.valueOf(score2))) / 2, y);

            g2d.setFont(fontLoader.getBoldFont(14f));
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawString("P1", x + 20, y - 40);
            g2d.setColor(new Color(255, 100, 100));
            g2d.drawString("P2", x + 20, y);
        }
    }

    private void drawControlsHint(Graphics2D g2d, int numberOfPlayers, int currentLevel) {
        g2d.setFont(fontLoader.getBoldFont(14f));
        if (numberOfPlayers == 2) {
            g2d.setColor(Color.WHITE);
            g2d.drawString("P1: WASD + SPACE", 10, 20);
            g2d.setColor(new Color(255, 100, 100));
            g2d.drawString("P2: Flechas + M", 10, 40);
        } else if (numberOfPlayers == 0) {
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawString("Modo: MACHINE vs MACHINE", 10, 20);
            g2d.setColor(new Color(150, 150, 150));
            g2d.drawString("Nivel " + currentLevel, 10, 40);
        }
    }
}

// *
// FruitType.java - Definir tipo
// FruitState.java - Definir estados
// Fruit.java - Lógica de transiciones de estado
// GameLogic.java - Efecto de congelamiento
// GameFacade.java - Configurar oleadas
// ResourceLoader.java - Cargar sprites
// GamePanel.java - Renderizar efecto visual
// LevelConfigurationDialog.java - Menú de configuración
// Resources/Fruits/Strawberry/ - Agregar archivos gráficos
// */
