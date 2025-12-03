package presentation;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameWindow extends JFrame {

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;

    private GamePanel gamePanel;

    public GameWindow(String character, String characterP2, String p1Name, String p2Name, int level,
            int numberOfPlayers, ResourceLoader resources) {
        setTitle("BAD DOPO CREAM - Nivel " + level);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Crear el panel del juego pasando los recursos y ambos personajes
        gamePanel = new GamePanel(character, characterP2, p1Name, p2Name, level, numberOfPlayers, resources);
        add(gamePanel);

        // Asegurar que el panel tenga el foco para capturar teclas
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                gamePanel.requestFocusInWindow();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                gamePanel.cleanup();
            }
        });

        setVisible(true);
    }
}