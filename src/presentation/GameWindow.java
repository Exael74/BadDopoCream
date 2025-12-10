package presentation;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameWindow extends JFrame {

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 768;

    private GamePanel gamePanel;

    /**
     * Legacy constructor for Tests compatibility.
     */
    public GameWindow(String characterTypeP1, String characterTypeP2, String p1Name, String p2Name, int level,
            int numberOfPlayers, ResourceLoader loader, String aiTypeP1, String aiTypeP2, boolean isP2CPU) {
        this(characterTypeP1, characterTypeP2, p1Name, p2Name, level, numberOfPlayers, loader, aiTypeP1, aiTypeP2,
                isP2CPU, null);
    }

    public GameWindow(String characterTypeP1, String characterTypeP2, String p1Name, String p2Name, int level,
            int numberOfPlayers, ResourceLoader loader, String aiTypeP1, String aiTypeP2, boolean isP2CPU,
            domain.dto.LevelConfigurationDTO config) {
        setTitle("Bad Dopo Cream - Nivel " + level);
        // setSize(WINDOW_WIDTH, WINDOW_HEIGHT); // Removed as pack() will handle sizing
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setLocationRelativeTo(null); // Moved after pack()
        setResizable(false);

        // Crear el panel del juego pasando los recursos y ambos personajes
        gamePanel = new GamePanel(characterTypeP1, characterTypeP2, p1Name, p2Name, level, numberOfPlayers,
                loader, this, aiTypeP1, aiTypeP2, isP2CPU, config);
        add(gamePanel);

        pack(); // Adjusts window size to fit its contents
        setLocationRelativeTo(null); // Center the window on the screen

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