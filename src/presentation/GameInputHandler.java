package presentation;

import domain.GameFacade;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Maneja las entradas de teclado y mouse para el GamePanel.
 * Actúa como un Controlador de Entrada dentro de la capa de Presentación.
 */
public class GameInputHandler {

    private GamePanel gamePanel;
    private GameFacade gameFacade;
    private Set<Integer> pressedKeys;
    private boolean spaceWasPressed;
    private boolean mWasPressed;

    public GameInputHandler(GamePanel gamePanel, GameFacade gameFacade) {
        this.gamePanel = gamePanel;
        this.gameFacade = gameFacade;
        this.pressedKeys = new HashSet<>();
        this.spaceWasPressed = false;
        this.mWasPressed = false;
    }

    public void setupListeners() {
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                pressedKeys.add(keyCode);

                if (keyCode == KeyEvent.VK_ESCAPE) {
                    gamePanel.handleEscapeAction();
                } else if (keyCode == KeyEvent.VK_SPACE) {
                    if (!spaceWasPressed) {
                        spaceWasPressed = true;
                        gamePanel.handleSpaceAction();
                    }
                } else if (keyCode == KeyEvent.VK_M) {
                    if (!mWasPressed) {
                        mWasPressed = true;
                        gamePanel.handleMAction();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    spaceWasPressed = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_M) {
                    mWasPressed = false;
                }
            }
        });

        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                gamePanel.handleMouseClick(e.getPoint());
            }
        });
    }

    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    // Helper para detectar "Just Pressed" (flanco de subida) si fuera necesario,
    // pero por ahora mantenemos la lógica original de flags con reset externo o
    // interno.
    // La lógica original en GamePanel reseteaba spaceWasPressed al usarla?
    // Revisando GamePanel: "if (pressedKeys.contains(KeyEvent.VK_SPACE) &&
    // !spaceWasPressed) { spaceWasPressed = true; ... }"

    // Mejor enfoque: GamePanel consulta el Raw State (pressedKeys) y maneja su
    // propio "wasPressed" flag para acciones únicas?
    // O InputHandler maneja esa lógica "one-shot"?

    // Para simplificar y limpiar GamePanel, InputHandler debería exponer métodos de
    // alto nivel:
    // "shouldPerformActionP1()"

    public Set<Integer> getPressedKeys() {
        return pressedKeys;
    }

    public void clearKeys() {
        pressedKeys.clear();
        spaceWasPressed = false;
        mWasPressed = false;
    }
}
