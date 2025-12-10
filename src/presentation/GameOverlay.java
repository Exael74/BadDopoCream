package presentation;

import domain.GameFacade;
import domain.BadDopoException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Maneja el renderizado de menús superpuestos (Pausa, Game Over, Carga).
 * Actúa como una Vista auxiliar.
 */
public class GameOverlay {

    // Referencias
    private GameFacade gameFacade;
    private FontLoader fontLoader;
    private int windowWidth;
    private int windowHeight;

    // Estado del Menu (Mirror del GamePanel o gestionado aquí)
    // Para simplificar, recibimos el estado en draw

    // Rectángulos de botones (para detección de click)
    private Rectangle resumeButtonRect;
    private Rectangle saveButtonRect;
    private Rectangle loadButtonRect;
    private Rectangle restartButtonRect;
    private Rectangle exitButtonRect;
    private Rectangle backButtonRect;
    private List<Rectangle> loadGameButtonRects = new ArrayList<>();

    private Rectangle summaryRestartButton;
    private Rectangle summaryMenuButton;
    private Rectangle summaryNextLevelButton;

    public GameOverlay(GameFacade gameFacade, FontLoader fontLoader, int width, int height) {
        this.gameFacade = gameFacade;
        this.fontLoader = fontLoader;
        this.windowWidth = width;
        this.windowHeight = height;
    }

    public void drawPauseMenu(Graphics2D g2d, GamePanel.MenuState menuState, List<String> savedGamesList,
            Point mousePos) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, windowWidth, windowHeight);

        if (menuState == GamePanel.MenuState.MAIN) {
            drawMainMenu(g2d, mousePos);
        } else if (menuState == GamePanel.MenuState.LOAD) {
            drawLoadMenu(g2d, savedGamesList, mousePos);
        }
    }

    private void drawMainMenu(Graphics2D g2d, Point mousePos) {
        int centerX = windowWidth / 2;
        int startY = 200;
        int buttonWidth = 300;
        int buttonHeight = 50;
        int spacing = 20;

        g2d.setColor(Color.WHITE);
        g2d.setFont(fontLoader.getBoldFont(48f));
        String title = "PAUSA";
        g2d.drawString(title, centerX - g2d.getFontMetrics().stringWidth(title) / 2, 150);

        g2d.setFont(fontLoader.getBoldFont(24f));

        resumeButtonRect = drawButton(g2d, "REANUDAR", centerX, startY, buttonWidth, buttonHeight, mousePos);
        saveButtonRect = drawButton(g2d, "GUARDAR PARTIDA", centerX, startY + (buttonHeight + spacing), buttonWidth,
                buttonHeight, mousePos);
        loadButtonRect = drawButton(g2d, "CARGAR PARTIDA", centerX, startY + (buttonHeight + spacing) * 2, buttonWidth,
                buttonHeight, mousePos);
        restartButtonRect = drawButton(g2d, "REINICIAR NIVEL", centerX, startY + (buttonHeight + spacing) * 3,
                buttonWidth, buttonHeight, mousePos);
        exitButtonRect = drawButton(g2d, "SALIR", centerX, startY + (buttonHeight + spacing) * 4, buttonWidth,
                buttonHeight, mousePos);
    }

    private void drawLoadMenu(Graphics2D g2d, List<String> savedGamesList, Point mousePos) {
        int centerX = windowWidth / 2;
        int startY = 150;
        int buttonWidth = 400;
        int buttonHeight = 40;
        int spacing = 10;

        g2d.setColor(Color.WHITE);
        g2d.setFont(fontLoader.getBoldFont(36f));
        String title = "CARGAR PARTIDA";
        g2d.drawString(title, centerX - g2d.getFontMetrics().stringWidth(title) / 2, 100);

        g2d.setFont(fontLoader.getBoldFont(18f));
        loadGameButtonRects.clear();

        if (savedGamesList.isEmpty()) {
            String msg = "No hay partidas guardadas";
            g2d.drawString(msg, centerX - g2d.getFontMetrics().stringWidth(msg) / 2, startY);
        } else {
            for (int i = 0; i < savedGamesList.size(); i++) {
                String saveName = savedGamesList.get(i);
                Rectangle rect = drawButton(g2d, saveName, centerX, startY + i * (buttonHeight + spacing), buttonWidth,
                        buttonHeight, mousePos);
                loadGameButtonRects.add(rect);
            }
        }
        backButtonRect = drawButton(g2d, "VOLVER", centerX, windowHeight - 100, 200, 50, mousePos);
    }

    public void drawSummaryMenu(Graphics2D g2d, boolean isVictory, int numberOfPlayers, Point mousePos) {
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, windowWidth, windowHeight);

        int centerX = windowWidth / 2;
        int startY = 150;
        int buttonWidth = 300;
        int buttonHeight = 50;
        int spacing = 20;

        g2d.setFont(fontLoader.getBoldFont(60f));
        String title = isVictory ? "¡VICTORIA!" : "GAME OVER";
        Color titleColor = isVictory ? new Color(255, 215, 0) : new Color(255, 50, 50);
        g2d.setColor(titleColor);
        g2d.drawString(title, centerX - g2d.getFontMetrics().stringWidth(title) / 2, 120);

        // Scores logic duplicated from GamePanel (simplify)
        g2d.setFont(fontLoader.getBoldFont(30f));
        g2d.setColor(Color.WHITE);

        int scoreY = 200;
        if (numberOfPlayers == 0 || numberOfPlayers == 2) {
            String p1Name = (numberOfPlayers == 0) ? "Máquina 1" : "Jugador 1";
            String p2Name = (numberOfPlayers == 0) ? "Máquina 2" : "Jugador 2";
            String winnerText = "¡Empate!";

            boolean p1Alive = gameFacade.isPlayerAlive();
            boolean p2Alive = gameFacade.isPlayer2Alive();

            if (p1Alive && !p2Alive)
                winnerText = "Ganador: " + p1Name;
            else if (!p1Alive && p2Alive)
                winnerText = "Ganador: " + p2Name;
            else {
                if (gameFacade.getScore() > gameFacade.getScorePlayer2())
                    winnerText = "Ganador: " + p1Name;
                else if (gameFacade.getScorePlayer2() > gameFacade.getScore())
                    winnerText = "Ganador: " + p2Name;
            }

            g2d.drawString(winnerText, centerX - g2d.getFontMetrics().stringWidth(winnerText) / 2, scoreY);
            scoreY += 40;
            String scoreText = p1Name + ": " + gameFacade.getScore() + " - " + p2Name + ": "
                    + gameFacade.getScorePlayer2();
            g2d.drawString(scoreText, centerX - g2d.getFontMetrics().stringWidth(scoreText) / 2, scoreY);
        } else {
            String scoreText = "Puntuación Final: " + gameFacade.getScore();
            g2d.drawString(scoreText, centerX - g2d.getFontMetrics().stringWidth(scoreText) / 2, scoreY);
        }

        int buttonY = 350;
        g2d.setFont(fontLoader.getBoldFont(24f));
        String restartText = isVictory ? "REINICIAR NIVEL" : "REINICIAR JUEGO";
        summaryRestartButton = drawButton(g2d, restartText, centerX, buttonY, buttonWidth, buttonHeight, mousePos);
        buttonY += buttonHeight + spacing;

        if (isVictory) {
            summaryNextLevelButton = drawButton(g2d, "SIGUIENTE NIVEL", centerX, buttonY, buttonWidth, buttonHeight,
                    mousePos);
            buttonY += buttonHeight + spacing;
        } else {
            summaryNextLevelButton = null;
        }
        summaryMenuButton = drawButton(g2d, "MENÚ PRINCIPAL", centerX, buttonY, buttonWidth, buttonHeight, mousePos);
    }

    private Rectangle drawButton(Graphics2D g2d, String text, int centerX, int y, int width, int height,
            Point mousePos) {
        int x = centerX - width / 2;
        Rectangle rect = new Rectangle(x, y, width, height);
        boolean hover = mousePos != null && rect.contains(mousePos);

        if (hover) {
            g2d.setColor(new Color(255, 255, 255, 50));
            g2d.fill(rect);
            g2d.setColor(Color.YELLOW);
        } else {
            g2d.setColor(Color.WHITE);
        }
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(rect);

        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, centerX - fm.stringWidth(text) / 2, y + (height + fm.getAscent()) / 2 - 5);
        return rect;
    }

    // Getters for Rectangles to check clicks in GamePanel logic
    public Rectangle getResumeButtonRect() {
        return resumeButtonRect;
    }

    public Rectangle getSaveButtonRect() {
        return saveButtonRect;
    }

    public Rectangle getLoadButtonRect() {
        return loadButtonRect;
    }

    public Rectangle getRestartButtonRect() {
        return restartButtonRect;
    }

    public Rectangle getExitButtonRect() {
        return exitButtonRect;
    }

    public Rectangle getBackButtonRect() {
        return backButtonRect;
    }

    public List<Rectangle> getLoadGameButtonRects() {
        return loadGameButtonRects;
    }

    public Rectangle getSummaryRestartButton() {
        return summaryRestartButton;
    }

    public Rectangle getSummaryMenuButton() {
        return summaryMenuButton;
    }

    public Rectangle getSummaryNextLevelButton() {
        return summaryNextLevelButton;
    }
}
