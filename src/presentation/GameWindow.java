package presentation;

import domain.GameFacade;
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
    public GameWindow(GameFacade gameFacade, ResourceLoader loader) {
        // Since we have the facade, we can get level info from it?
        // Facade has getLevel()
        int level = gameFacade.getLevel();

        setTitle("Bad Dopo Cream - Nivel " + level);
        // setSize(WINDOW_WIDTH, WINDOW_HEIGHT); // Removed as pack() will handle sizing
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setLocationRelativeTo(null); // Moved after pack()
        setResizable(false);

        // Crear el panel del juego pasando los recursos y ambos personajes
        // Create GamePanel with the existing Facade
        gamePanel = new GamePanel(gameFacade, loader, this);
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