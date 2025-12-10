package presentation;

import domain.GameFacade;
import domain.BadDopoException;
import domain.dto.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Panel principal del juego que maneja renderizado y captura de inputs.
 * Solo responsable de la presentación visual, sin lógica de negocio.
 */
public class GamePanel extends JPanel implements java.awt.event.ActionListener {

    // Constantes del juego
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 768;
    private static final int GRID_SIZE = 13;
    private static final int CELL_SIZE = 50;
    private static final int SIDEBAR_WIDTH = 200;

    // Tamaños de los sprites
    private static final int PLAYER_SIZE = 45;
    private static final int TROLL_SIZE = 45;
    private static final int FRUIT_SIZE = 40;
    private static final int ICE_SIZE = 40;

    // Velocidad de movimiento
    private static final int PLAYER_ANIMATION_SPEED = 4; // Rápido para respuesta inmediata (Humanos)
    private static final int SMOOTH_ANIMATION_SPEED = 4; // Lento para suavidad visual (Enemigos/IA)
    private static final int FRAME_DELAY = 16;

    // Recursos
    private ResourceLoader resources;
    private FontLoader fontLoader;

    // Fachada del dominio
    private GameFacade gameFacade;

    // Datos del nivel
    private int currentLevel;
    private int numberOfPlayers;

    // Timers (javax.swing.Timer)
    private javax.swing.Timer gameTimer;
    private javax.swing.Timer animationTimer;

    // Helper Classes
    private GameInputHandler inputHandler;
    private GameHUD gameHUD;
    private GameOverlay gameOverlay;

    // Menú de Pausa y Resumen
    public enum MenuState {
        NONE, MAIN, SAVE, LOAD, SUMMARY
    }

    private MenuState menuState = MenuState.NONE;
    private List<String> savedGamesList = new ArrayList<>();
    private boolean isVictory = false;

    // AI Types (Strings)
    // AI Types (Strings)
    private boolean isP2CPU;

    // Variables para animación suave del jugador 1
    private Point targetGridPosition;
    private float currentPixelX;
    private float currentPixelY;
    private boolean isMoving;

    // Variables para animación suave del jugador 2
    private Point player2TargetGridPosition;
    private float player2CurrentPixelX;
    private float player2CurrentPixelY;
    private boolean player2IsMoving;

    // Variables para animación suave de enemigos (mapeadas por ID de entidad)
    private Map<String, Point> enemyTargetPositions;
    private Map<String, Float> enemyCurrentPixelX;
    private Map<String, Float> enemyCurrentPixelY;
    private Map<String, Boolean> enemyIsMoving;

    // Animación de hielo
    private Map<Point, Integer> iceAnimationProgress;
    private Queue<Point> icePlacementQueue;
    private javax.swing.Timer icePlacementTimer;

    // Control de reinicio
    private boolean restartScheduled;

    /**
     * Constructor del panel de juego.
     *
     * @param characterType   Personaje seleccionado P1
     * @param characterTypeP2 Personaje seleccionado P2 (puede ser null)
     * @param p1Name          Nombre del Jugador 1 / Máquina 1
     * @param p2Name          Nombre del Jugador 2 / Máquina 2
     * @param level           Nivel a jugar
     * @param numberOfPlayers Número de jugadores
     * @param loader          Recursos cargados
     * @param window          Ventana principal
     * @param aiTypeP1        Tipo de IA P1
     * @param aiTypeP2        Tipo de IA P2
     * @param isP2CPU         Si P2 es CPU
     * @param config          Configuración del nivel
     */
    public GamePanel(GameFacade facade, ResourceLoader loader, GameWindow window) {
        this.resources = loader;
        this.resources = loader;
        this.fontLoader = FontLoader.getInstance();
        this.gameFacade = facade;

        // Extract initial state from facade for local fields
        this.currentLevel = facade.getLevel();
        this.numberOfPlayers = facade.getNumberOfPlayers();
        // this.selectedCharacter = facade.getPlayerCharacterType(); // Accessor needed
        // if strictly required

        // We can't easily set other local fields (aiType, etc) without accessors from
        // facade,
        // but they might not be needed if facade handles logic.
        // Let's rely on facade for logic.

        // Active game state fields
        this.currentLevel = facade.getLevel();
        this.numberOfPlayers = facade.getNumberOfPlayers(); // Restored
        this.isP2CPU = facade.isP2CPU(); // Restored

        setPreferredSize(new Dimension(1280, 768));
        setBackground(Color.BLACK);
        setFocusable(true);

        // Facade is already initialized and passed
        // this.gameFacade = new GameFacade(characterType, characterTypeP2, p1Name,
        // p2Name, level, numberOfPlayers,
        // aiTypeP1, aiTypeP2, isP2CPU, config);

        // Initialize helper classes
        this.inputHandler = new GameInputHandler(this, gameFacade);

        // Correct Constructor Calls & Field Names
        this.gameOverlay = new GameOverlay(gameFacade, fontLoader, 1280, 768);
        this.gameHUD = new GameHUD(gameFacade, resources, fontLoader);

        // Listeners are setup by inputHandler.setupListeners() later

        gameTimer = new javax.swing.Timer(16, this); // ~60 FPS
        gameTimer.start();

        initializeAnimationTimers();

        // Animation State Initialization
        this.iceAnimationProgress = new HashMap<>();
        this.icePlacementQueue = new LinkedList<>();
        this.restartScheduled = false;

        this.enemyTargetPositions = new HashMap<>();
        this.enemyCurrentPixelX = new HashMap<>();
        this.enemyCurrentPixelY = new HashMap<>();
        this.enemyIsMoving = new HashMap<>();

        // Initialize Enemy Animation States
        for (domain.dto.EnemySnapshot enemySnapshot : gameFacade.getEnemySnapshots()) {
            String id = enemySnapshot.getId();
            Point pos = enemySnapshot.getPosition();
            enemyTargetPositions.put(id, new Point(pos));
            enemyCurrentPixelX.put(id, (float) (pos.x * CELL_SIZE));
            enemyCurrentPixelY.put(id, (float) (pos.y * CELL_SIZE));
            enemyIsMoving.put(id, false);
        }

        // Initialize Player 1 Animation State
        Point initialPos = gameFacade.getPlayerPosition();
        if (initialPos != null) {
            this.targetGridPosition = new Point(initialPos);
            this.currentPixelX = initialPos.x * CELL_SIZE;
            this.currentPixelY = initialPos.y * CELL_SIZE;
            this.isMoving = false;
        }

        // Initialize Player 2 Animation State
        domain.dto.PlayerSnapshot p2Snapshot = gameFacade.getPlayer2Snapshot();
        if (p2Snapshot != null) {
            Point p2Pos = p2Snapshot.getPosition();
            this.player2TargetGridPosition = new Point(p2Pos);
            this.player2CurrentPixelX = p2Pos.x * CELL_SIZE;
            this.player2CurrentPixelY = p2Pos.y * CELL_SIZE;
            this.player2IsMoving = false;
        }

        String modeText = numberOfPlayers == 0 ? "Machine vs Machine" : numberOfPlayers + " player(s)";
        domain.BadDopoLogger.logInfo("GamePanel initialized for level " + currentLevel + " with " + modeText);

        inputHandler.setupListeners();

        startTimers();
    }

    // Legacy/Helper constructors removed or adapted if strictly needed by existing
    // code NOT calling GameWindow
    // Ideally, GamePanel should ONLY be created by GameWindow with a Facade.

    // For "Quick Start" or Tests, we might need a helper that creates a default
    // facade.
    public GamePanel(String character, int level, int numberOfPlayers, ResourceLoader resources) {
        this(new GameFacade(character, level, numberOfPlayers), resources, null);
    }

    private void initializeAnimationTimers() {
        // Placeholder for additional animation timers if needed
    }

    // ==================== CONFIGURACIÓN DE LISTENERS ====================

    // ==================== MANEJO DE ACCIONES ====================

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getSource() == gameTimer) {
            if (menuState != MenuState.NONE && menuState != MenuState.SUMMARY) {
                repaint();
                return;
            }

            if (isVictory || gameFacade.isGameOver()) {
                // Logic for game over/victory handled in draw or separate check
            } else {
                updateGame();
            }
            repaint();
        }
    }

    private void updateGame() {
        gameFacade.update();
        processMovement();
        // Enemy animations are handled in updateAnimation() via animationTimer
    }

    // Removed duplicate updateEnemyAnimations() method

    /**
     * Maneja la acción de SPACE (P1).
     */
    /**
     * Maneja la acción de SPACE (P1).
     */
    void handleSpaceAction() {
        if (!gameFacade.isPlayerAlive() || gameFacade.isPlayerBusy())
            return;

        List<Point> affectedPositions = gameFacade.performSpaceAction();

        if (!affectedPositions.isEmpty()) {
            startIcePlacementAnimation(affectedPositions);
        }
    }

    /**
     * Maneja la acción de M (P2).
     */
    /**
     * Maneja la acción de M (P2).
     */
    void handleMAction() {
        // Check if P2 is alive/busy if needed, but performActionPlayer2 handles logic
        List<Point> affectedPositions = gameFacade.performActionPlayer2();

        if (!affectedPositions.isEmpty()) {
            startIcePlacementAnimation(affectedPositions);
        }
    }

    /**
     * Inicia la animación secuencial de colocación de hielo.
     */
    private void startIcePlacementAnimation(List<Point> icePositions) {
        if (icePlacementTimer != null) {
            icePlacementTimer.stop();
        }

        icePlacementQueue.clear();
        icePlacementQueue.addAll(icePositions);

        icePlacementTimer = new javax.swing.Timer(80, e -> {
            if (!icePlacementQueue.isEmpty()) {
                Point nextIce = icePlacementQueue.poll();
                iceAnimationProgress.put(nextIce, 0);
            } else {
                icePlacementTimer.stop();
            }
        });
        icePlacementTimer.start();
    }

    private void processMovement() {
        if (numberOfPlayers == 0) {
            updatePlayerAnimation();
            updatePlayer2Animation();
        } else {
            processHumanPlayerMovement();
            if (numberOfPlayers == 2) {
                if (isP2CPU) {
                    updatePlayer2Animation();
                } else {
                    processHumanPlayer2Movement();
                }
            }
        }
    }

    private void processHumanPlayerMovement() {
        if (!isMoving) {
            boolean moved = false;

            if (inputHandler.isKeyPressed(KeyEvent.VK_W)) {
                gameFacade.movePlayerUp();
                moved = true;
            } else if (inputHandler.isKeyPressed(KeyEvent.VK_S)) {
                gameFacade.movePlayerDown();
                moved = true;
            } else if (inputHandler.isKeyPressed(KeyEvent.VK_A)) {
                gameFacade.movePlayerLeft();
                moved = true;
            } else if (inputHandler.isKeyPressed(KeyEvent.VK_D)) {
                gameFacade.movePlayerRight();
                moved = true;
            } else {
                gameFacade.stopPlayer();
            }

            if (moved) {
                updatePlayerAnimation();
            }
        } else {
            // System.out.println("DEBUG: Input blocked because isMoving=true");
        }
    }

    /**
     * Actualiza la animación del jugador 1.
     */
    private void updatePlayerAnimation() {
        Point newGridPos = gameFacade.getPlayerPosition();
        if (!newGridPos.equals(targetGridPosition)) {
            targetGridPosition = new Point(newGridPos);
            isMoving = true;
        }
    }

    /**
     * Procesa el movimiento del jugador 2 (Flechas).
     */
    private void processHumanPlayer2Movement() {
        if (!player2IsMoving) {
            boolean moved = false;

            if (inputHandler.isKeyPressed(KeyEvent.VK_UP)) {
                gameFacade.movePlayer2Up();
                moved = true;
            } else if (inputHandler.isKeyPressed(KeyEvent.VK_DOWN)) {
                gameFacade.movePlayer2Down();
                moved = true;
            } else if (inputHandler.isKeyPressed(KeyEvent.VK_LEFT)) {
                gameFacade.movePlayer2Left();
                moved = true;
            } else if (inputHandler.isKeyPressed(KeyEvent.VK_RIGHT)) {
                gameFacade.movePlayer2Right();
                moved = true;
            } else {
                gameFacade.stopPlayer2();
            }

            if (moved) {
                updatePlayer2Animation();
            }
        }
    }

    /**
     * Actualiza la animación del jugador 2.
     */
    private void updatePlayer2Animation() {
        PlayerSnapshot p2 = gameFacade.getPlayer2Snapshot();
        if (p2 != null) {
            Point newGridPos = p2.getPosition();
            if (!newGridPos.equals(player2TargetGridPosition)) {
                player2TargetGridPosition = new Point(newGridPos);
                player2IsMoving = true;
            }
        }
    }

    /**
     * Actualiza la posición en píxeles del jugador 2.
     */
    private void updatePlayer2PixelPosition() {
        if (player2TargetGridPosition == null)
            return;

        if (player2IsMoving) {
            float targetPixelX = player2TargetGridPosition.x * CELL_SIZE;
            float targetPixelY = player2TargetGridPosition.y * CELL_SIZE;

            float deltaX = targetPixelX - player2CurrentPixelX;
            float deltaY = targetPixelY - player2CurrentPixelY;

            // En MvM/P1vsCPU usar velocidad suave
            int speed = SMOOTH_ANIMATION_SPEED;

            if (Math.abs(deltaX) < speed && Math.abs(deltaY) < speed) {
                player2CurrentPixelX = targetPixelX;
                player2CurrentPixelY = targetPixelY;
                player2IsMoving = false;
            } else {
                // MANHATTAN INTERPOLATION
                if (Math.abs(deltaX) > 0.5f) {
                    player2CurrentPixelX += Math.signum(deltaX) * speed;
                    if (Math.abs(deltaY) < speed)
                        player2CurrentPixelY = targetPixelY;
                } else if (Math.abs(deltaY) > 0.5f) {
                    player2CurrentPixelY += Math.signum(deltaY) * speed;
                    if (Math.abs(deltaX) < speed)
                        player2CurrentPixelX = targetPixelX;
                }
            }
        } else {
            // Force sync
            player2CurrentPixelX = player2TargetGridPosition.x * CELL_SIZE;
            player2CurrentPixelY = player2TargetGridPosition.y * CELL_SIZE;
        }
    }

    /**
     * Actualiza la posición en píxeles de enemigos.
     */
    /**
     * Actualiza la posición en píxeles de enemigos.
     */
    private void updateEnemiesPixelPosition() {
        if (!gameFacade.isVictory()) {
            // Limpiar enemigos que ya no existen
            Set<String> currentEnemyIds = new HashSet<>();

            for (EnemySnapshot enemySnapshot : gameFacade.getEnemySnapshots()) {
                if (!enemySnapshot.isActive())
                    continue;

                String enemyId = enemySnapshot.getId();
                currentEnemyIds.add(enemyId);
                Point actualPosition = enemySnapshot.getPosition();

                // Inicializar si es nuevo enemigo
                if (!enemyTargetPositions.containsKey(enemyId)) {
                    enemyTargetPositions.put(enemyId, new Point(actualPosition));
                    enemyCurrentPixelX.put(enemyId, (float) (actualPosition.x * CELL_SIZE));
                    enemyCurrentPixelY.put(enemyId, (float) (actualPosition.y * CELL_SIZE));
                    enemyIsMoving.put(enemyId, false);
                }

                Point currentTarget = enemyTargetPositions.get(enemyId);

                if (!actualPosition.equals(currentTarget)) {
                    enemyTargetPositions.put(enemyId, new Point(actualPosition));
                    enemyIsMoving.put(enemyId, true);
                }

                if (enemyIsMoving.get(enemyId)) {
                    float currentX = enemyCurrentPixelX.get(enemyId);
                    float currentY = enemyCurrentPixelY.get(enemyId);
                    float targetX = actualPosition.x * CELL_SIZE;
                    float targetY = actualPosition.y * CELL_SIZE;

                    float deltaX = targetX - currentX;
                    float deltaY = targetY - currentY;

                    int speed = SMOOTH_ANIMATION_SPEED;

                    // High speed interpolation for Drilling Narval
                    if (enemySnapshot.isDrilling()) {
                        speed = 12; // Adjusted for 120ms logic speed (~8px/frame needed)
                    }

                    if (Math.abs(deltaX) < speed && Math.abs(deltaY) < speed) {
                        enemyCurrentPixelX.put(enemyId, targetX);
                        enemyCurrentPixelY.put(enemyId, targetY);
                        enemyIsMoving.put(enemyId, false);
                    } else {
                        // MANHATTAN INTERPOLATION: Prevent diagonal slide
                        if (Math.abs(deltaX) > 0.5f) {
                            enemyCurrentPixelX.put(enemyId, currentX + Math.signum(deltaX) * speed);
                            if (Math.abs(deltaY) < speed)
                                enemyCurrentPixelY.put(enemyId, targetY);
                        } else if (Math.abs(deltaY) > 0.5f) {
                            enemyCurrentPixelY.put(enemyId, currentY + Math.signum(deltaY) * speed);
                            if (Math.abs(deltaX) < speed)
                                enemyCurrentPixelX.put(enemyId, targetX);
                        }
                    }
                }
            }

            // Limpiar posiciones de enemigos que ya no existen
            enemyTargetPositions.keySet().retainAll(currentEnemyIds);
            enemyCurrentPixelX.keySet().retainAll(currentEnemyIds);
            enemyCurrentPixelY.keySet().retainAll(currentEnemyIds);
            enemyIsMoving.keySet().retainAll(currentEnemyIds);
        }
    }

    /**
     * Actualiza el progreso de animación de hielo (fade in).
     */
    private void updateIceAnimationProgress() {
        Iterator<Map.Entry<Point, Integer>> iterator = iceAnimationProgress.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Point, Integer> entry = iterator.next();
            int progress = entry.getValue() + 10;
            if (progress >= 100) {
                iterator.remove();
            } else {
                entry.setValue(progress);
            }
        }
    }

    /**
     * Inicia los timers del juego y de animación.
     */
    private void startTimers() {
        if (gameTimer != null)
            gameTimer.stop();
        if (animationTimer != null)
            animationTimer.stop();

        gameTimer = new javax.swing.Timer(FRAME_DELAY, e -> {
            if (gameFacade.isPaused())
                return; // No actualizar lógica si está pausado

            // Debug prints (remove later)
            if (gameFacade.isGameOver()) {
                // domain.BadDopoLogger.logInfo("DEBUG: GameOver=true, RestartLevel=" +
                // gameFacade.shouldRestartLevel() +
                // ", DeathAnimComplete=" + gameFacade.isDeathAnimationComplete() +
                // ", RestartScheduled=" + restartScheduled);
            }

            // Verificar victoria
            if (gameFacade.isVictory()) {
                // Ensure local victory flag is synced with facade
                if (!isVictory) {
                    isVictory = true;
                }

                if (!restartScheduled) {
                    // domain.BadDopoLogger.logInfo("DEBUG: Victory detected! Setting menuState to
                    // SUMMARY.");
                    restartScheduled = true;
                    menuState = MenuState.SUMMARY;
                    repaint();
                } else {
                    // Failsafe: ensure menu is showing
                    // domain.BadDopoLogger.logInfo("DEBUG: Victory is true, restartScheduled is
                    // true, BUT menuState is "
                    // + menuState + ". Forcing SUMMARY.");
                    menuState = MenuState.SUMMARY;
                    gameTimer.stop(); // Stop here too
                    repaint();
                }
                if (menuState == MenuState.SUMMARY && gameTimer.isRunning()) {
                    gameTimer.stop();
                }
                return;
            }

            // Verificar derrota (tiempo agotado o jugador muerto)
            if (gameFacade.shouldRestartLevel() && !restartScheduled) {
                // domain.BadDopoLogger.logInfo("DEBUG: Triggering Game Over Menu");
                restartScheduled = true;
                isVictory = false; // Si alguien muere o se acaba el tiempo, es derrota (Game Over)
                menuState = MenuState.SUMMARY;

                // Stop timers to prevent CPU loop
                gameTimer.stop();

                repaint();
                return;
            }

            gameFacade.update();
            processMovement();
            repaint();
        });
        gameTimer.start();

        animationTimer = new javax.swing.Timer(FRAME_DELAY, e -> {
            try {
                if (gameFacade.isPaused())
                    return;
                // System.out.println("DEBUG: Animation Tick");
                updateAnimation();
                repaint();
            } catch (Exception ex) {
                System.err.println("CRITICAL: Animation Timer Crashed!");
                ex.printStackTrace();
            }
        });
        animationTimer.start();
    }

    /**
     * Actualiza la animación del juego (interpolación).
     */
    private void updateAnimation() {
        if (!isMoving) {
            // Force sync to prevent ghost sliding after restart
            float targetPixelX = targetGridPosition.x * CELL_SIZE;
            float targetPixelY = targetGridPosition.y * CELL_SIZE;
            if (currentPixelX != targetPixelX || currentPixelY != targetPixelY) {
                // System.out.println("DEBUG: Forcing pixel sync in updateAnimation"); //
                // Optional log
                currentPixelX = targetPixelX;
                currentPixelY = targetPixelY;
            }
        }

        if (isMoving) {
            float targetPixelX = targetGridPosition.x * CELL_SIZE;
            float targetPixelY = targetGridPosition.y * CELL_SIZE;

            float deltaX = targetPixelX - currentPixelX;
            float deltaY = targetPixelY - currentPixelY;

            // System.out.println("DEBUG: Animating P1. Curr: " + currentPixelX + "," +
            // currentPixelY + " Target: " + targetPixelX + "," + targetPixelY);

            // En MvM (0 players) usar velocidad suave, en PvP/1P usar velocidad rápida
            int speed = (numberOfPlayers == 0) ? SMOOTH_ANIMATION_SPEED : PLAYER_ANIMATION_SPEED;

            if (Math.abs(deltaX) < speed && Math.abs(deltaY) < speed) {
                currentPixelX = targetPixelX;
                currentPixelY = targetPixelY;
                isMoving = false;
            } else {
                // MANHATTAN INTERPOLATION: Move only one axis at a time to prevent diagonal
                // slide
                if (Math.abs(deltaX) > 0.5f) {
                    currentPixelX += Math.signum(deltaX) * speed;
                    // Snap Y if trying to move X to avoid micro-diagonals
                    if (Math.abs(deltaY) < speed)
                        currentPixelY = targetPixelY;
                } else if (Math.abs(deltaY) > 0.5f) {
                    currentPixelY += Math.signum(deltaY) * speed;
                    // Snap X
                    if (Math.abs(deltaX) < speed)
                        currentPixelX = targetPixelX;
                }
            }
        }

        updatePlayer2PixelPosition();
        updateEnemiesPixelPosition();
        updateIceAnimationProgress();
    }

    /**
     * Maneja el reinicio del nivel.
     */

    // ==================== RENDERING ====================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Renderizado del juego
        g2d.setColor(new Color(30, 30, 30));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int mapWidth = GRID_SIZE * CELL_SIZE;
        int mapHeight = GRID_SIZE * CELL_SIZE;
        int offsetX = (WINDOW_WIDTH - SIDEBAR_WIDTH - mapWidth) / 2 + SIDEBAR_WIDTH;
        int offsetY = (WINDOW_HEIGHT - mapHeight) / 2;

        g2d.translate(offsetX, offsetY);
        drawGridBackground(g2d);
        g2d.translate(-offsetX, -offsetY);

        drawUnbreakableBlocks(g2d, offsetX, offsetY);
        drawHotTiles(g2d, offsetX, offsetY);
        drawIglu(g2d, offsetX, offsetY);
        drawFruits(g2d, offsetX, offsetY);
        drawIceBlocks(g2d, offsetX, offsetY);
        drawEnemies(g2d, offsetX, offsetY);
        drawPlayer(g2d, offsetX, offsetY);

        if (numberOfPlayers == 2 || numberOfPlayers == 0) {
            drawPlayer2(g2d, offsetX, offsetY);
        }

        // Delegar dibujo de UI
        gameHUD.drawSidebar(g2d, offsetX, numberOfPlayers, currentLevel);

        // Menús superpuestos
        if (gameFacade.isPaused() && menuState != MenuState.SUMMARY) {
            gameOverlay.drawPauseMenu(g2d, menuState, savedGamesList, getMousePosition());
        }

        // Game Over / Victory
        if (gameFacade.isGameOver() || gameFacade.isVictory()) {
            boolean allPlayersDead = (numberOfPlayers > 0)
                    ? (!gameFacade.isPlayerAlive() && (!gameFacade.isPlayer2Alive() || numberOfPlayers == 1))
                    : false;
            // PvP Logic handled in Facade/Logic, assume facade.isGameOver() is truth

            if (gameFacade.isGameOver()) {
                isVictory = false;
                if (!gameFacade.isDeathAnimationComplete()) {
                    gameTimer.stop(); // Stop game timer but allow animation
                    return; // Don't show menu yet
                }
                menuState = MenuState.SUMMARY;
                gameOverlay.drawSummaryMenu(g2d, false, numberOfPlayers, getMousePosition());
            } else if (gameFacade.isVictory()) {
                isVictory = true;
                menuState = MenuState.SUMMARY;
                gameOverlay.drawSummaryMenu(g2d, true, numberOfPlayers, getMousePosition());
            }
        }
    }

    private void drawGridBackground(Graphics2D g2d) {
        if (resources.fondoMapa != null) {
            g2d.drawImage(resources.fondoMapa, 0, 0, GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE, this);
        }
    }

    /**
     * Dibuja todas las frutas del juego.
     */
    private void drawFruits(Graphics2D g2d, int offsetX, int offsetY) {
        for (FruitSnapshot fruitSnapshot : gameFacade.getFruitSnapshots()) {
            // Draw all fruits provided by the snapshot (GameLogic handles
            // visibility/activity)
            Point pos = fruitSnapshot.getPosition();
            String fruitType = fruitSnapshot.getFruitType();
            String state = fruitSnapshot.getState(); // Now available

            // Adjust size based on fruit type to normalize visual appearance
            int currentFruitSize = FRUIT_SIZE; // Default 40
            if (fruitType.equals("PLATANO")) {
                currentFruitSize = 55;
            } else if (fruitType.equals("CEREZA")) {
                currentFruitSize = 100; // Much larger for cherries due to small GIF content
            } else if (fruitType.equals("CACTUS")) {
                currentFruitSize = 50;
            }

            int x = offsetX + pos.x * CELL_SIZE + (CELL_SIZE - currentFruitSize) / 2;
            int y = offsetY + pos.y * CELL_SIZE + (CELL_SIZE - currentFruitSize) / 2;

            ImageIcon fruitGif = resources.getFruitGif(fruitType, state);
            if (fruitGif != null) {
                g2d.drawImage(fruitGif.getImage(), x, y, currentFruitSize, currentFruitSize, this);
            }
        }
    }

    /**
     * Dibuja todos los bloques de hielo del juego.
     */
    private void drawIceBlocks(Graphics2D g2d, int offsetX, int offsetY) {
        for (IceBlockSnapshot iceSnapshot : gameFacade.getIceBlockSnapshots()) {
            Point pos = iceSnapshot.getPosition();
            int x = offsetX + pos.x * CELL_SIZE + (CELL_SIZE - ICE_SIZE) / 2;
            int y = offsetY + pos.y * CELL_SIZE + (CELL_SIZE - ICE_SIZE) / 2;

            Integer progress = iceAnimationProgress.get(pos);
            if (progress != null) {
                // Animación de aparición (scale)
                float scale = progress / 100.0f;
                int scaledSize = (int) (ICE_SIZE * scale);
                int scaledX = x + (ICE_SIZE - scaledSize) / 2;
                int scaledY = y + (ICE_SIZE - scaledSize) / 2;
                g2d.drawImage(resources.iceBlockNormalImage, scaledX, scaledY, scaledSize, scaledSize, this);
            } else if (iceSnapshot.isBreaking()) {
                // Animación de ruptura (fade out + shrink)
                int breakProgress = iceSnapshot.getBreakProgress();
                float alpha = 1.0f - (breakProgress / 100.0f);

                Graphics2D g2dIce = (Graphics2D) g2d.create();
                g2dIce.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                float shrinkScale = 1.0f - (breakProgress / 200.0f);
                int shrunkSize = (int) (ICE_SIZE * shrinkScale);
                int shrunkX = x + (ICE_SIZE - shrunkSize) / 2;
                int shrunkY = y + (ICE_SIZE - shrunkSize) / 2;

                g2dIce.drawImage(resources.iceBlockBrokenImage, shrunkX, shrunkY, shrunkSize, shrunkSize, this);
                g2dIce.dispose();
            } else {
                g2d.drawImage(resources.iceBlockNormalImage, x, y, ICE_SIZE, ICE_SIZE, this);
            }
        }
    }

    /**
     * Dibuja todas las baldosas calientes del juego.
     */
    private void drawHotTiles(Graphics2D g2d, int offsetX, int offsetY) {
        for (HotTileSnapshot tileSnapshot : gameFacade.getHotTileSnapshots()) {
            Point pos = tileSnapshot.getPosition();
            int size = ICE_SIZE; // Use ICE_SIZE (40) as requested
            int x = offsetX + pos.x * CELL_SIZE + (CELL_SIZE - size) / 2;
            int y = offsetY + pos.y * CELL_SIZE + (CELL_SIZE - size) / 2;
            g2d.drawImage(resources.hotTileImage.getImage(), x, y, size, size, this);
        }
    }

    /**
     * Dibuja el Iglú central.
     */
    private void drawIglu(Graphics2D g2d, int offsetX, int offsetY) {
        IgluSnapshot iglu = gameFacade.getIgluSnapshot();
        if (iglu != null) {
            Point pos = iglu.getPosition();
            int width = iglu.getWidth() * CELL_SIZE;
            int height = iglu.getHeight() * CELL_SIZE;
            int x = offsetX + pos.x * CELL_SIZE;
            int y = offsetY + pos.y * CELL_SIZE;
            g2d.drawImage(resources.igluImage.getImage(), x, y, width, height, this);
        }
    }

    /**
     * Dibuja los bloques irrompibles.
     */
    private void drawUnbreakableBlocks(Graphics2D g2d, int offsetX, int offsetY) {
        for (UnbreakableBlockSnapshot block : gameFacade.getUnbreakableBlockSnapshots()) {
            Point pos = block.getPosition();
            int size = ICE_SIZE; // Matches ice size per requirement (40px)
            int x = offsetX + pos.x * CELL_SIZE + (CELL_SIZE - size) / 2;
            int y = offsetY + pos.y * CELL_SIZE + (CELL_SIZE - size) / 2;
            g2d.drawImage(resources.unbreakableBlockImage.getImage(), x, y, size, size, this);
        }
    }

    /**
     * Dibuja todos los enemigos del juego.
     */
    private void drawEnemies(Graphics2D g2d, int offsetX, int offsetY) {
        if (!gameFacade.isVictory()) {
            for (EnemySnapshot enemySnapshot : gameFacade.getEnemySnapshots()) {
                if (enemySnapshot.isActive()) {
                    drawEnemy(g2d, enemySnapshot, offsetX, offsetY);
                }
            }
        } else {
            // En victoria, enemigos en estado idle
            for (EnemySnapshot enemySnapshot : gameFacade.getEnemySnapshots()) {
                if (enemySnapshot.isActive()) {
                    drawEnemyIdle(g2d, enemySnapshot, offsetX, offsetY);
                }
            }
        }
    }

    /**
     * Dibuja un enemigo individual.
     */
    private void drawEnemy(Graphics2D g2d, EnemySnapshot enemySnapshot, int offsetX, int offsetY) {
        float pixelX, pixelY;
        Point gridPos = enemySnapshot.getPosition();

        pixelX = enemyCurrentPixelX.getOrDefault(enemySnapshot.getId(), (float) (gridPos.x * CELL_SIZE));
        pixelY = enemyCurrentPixelY.getOrDefault(enemySnapshot.getId(), (float) (gridPos.y * CELL_SIZE));

        int x = offsetX + (int) pixelX + (CELL_SIZE - TROLL_SIZE) / 2;
        int y = offsetY + (int) pixelY + (CELL_SIZE - TROLL_SIZE) / 2;

        String direction = enemySnapshot.getDirection();
        String enemyType = enemySnapshot.getEnemyType();
        boolean isBreakingIce = enemySnapshot.isBreakingIce();
        boolean isDrilling = enemySnapshot.isDrilling();

        ImageIcon enemyGif;
        if (enemyType.equals("NARVAL")) {
            enemyGif = resources.getNarvalGif(direction, isBreakingIce, isDrilling);
        } else {
            enemyGif = resources.getEnemyGif(enemyType, direction, isBreakingIce);
        }

        if (enemyGif != null) {
            Image enemyImage = enemyGif.getImage();
            g2d.drawImage(enemyImage, x, y, TROLL_SIZE, TROLL_SIZE, this);
        }

        if (numberOfPlayers == 0) {
            g2d.setColor(new Color(255, 165, 0, 180));
            g2d.setFont(fontLoader.getBoldFont(16f));
            String aiLabel = "AI";
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(aiLabel);
            g2d.drawString(aiLabel, x + (TROLL_SIZE - labelWidth) / 2, y - 5);
        }
    }

    /**
     * Dibuja un enemigo en estado idle (victoria).
     */
    private void drawEnemyIdle(Graphics2D g2d, EnemySnapshot enemySnapshot, int offsetX, int offsetY) {
        Point pos = enemySnapshot.getPosition();
        int x = offsetX + pos.x * CELL_SIZE + (CELL_SIZE - TROLL_SIZE) / 2;
        int y = offsetY + pos.y * CELL_SIZE + (CELL_SIZE - TROLL_SIZE) / 2;

        String enemyType = enemySnapshot.getEnemyType();
        ImageIcon idleGif;
        if (enemyType.equals("TROLL")) {
            idleGif = resources.trollIdleGif;
        } else if (enemyType.equals("MACETA")) {
            idleGif = resources.macetaWalkDownGif;
        } else if (enemyType.equals("NARVAL")) {
            idleGif = resources.narvalWalkDownGif;
        } else {
            idleGif = resources.calamarWalkDownGif;
        }

        if (idleGif != null) {
            Image enemyImage = idleGif.getImage();
            g2d.drawImage(enemyImage, x, y, TROLL_SIZE, TROLL_SIZE, this);
        }
    }

    /**
     * Dibuja el jugador 1.
     */
    private void drawPlayer(Graphics2D g2d, int offsetX, int offsetY) {
        PlayerSnapshot playerSnapshot = gameFacade.getPlayerSnapshot();
        if (playerSnapshot.isActive()) {
            drawPlayerEntity(g2d, playerSnapshot, currentPixelX, currentPixelY, offsetX, offsetY, "P1");
        }
    }

    /**
     * Dibuja el jugador 2.
     */
    private void drawPlayer2(Graphics2D g2d, int offsetX, int offsetY) {
        PlayerSnapshot playerSnapshot = gameFacade.getPlayer2Snapshot();
        if (playerSnapshot != null && playerSnapshot.isActive()) {
            drawPlayerEntity(g2d, playerSnapshot, player2CurrentPixelX, player2CurrentPixelY, offsetX, offsetY, "P2");
        }
    }

    private void drawPlayerEntity(Graphics2D g2d, PlayerSnapshot snapshot, float pixelX, float pixelY, int offsetX,
            int offsetY, String label) {
        String direction = snapshot.getDirection();
        boolean playerIsMoving = snapshot.isMoving();
        boolean playerIsSneezing = snapshot.isSneezing();
        boolean playerIsKicking = snapshot.isKicking();
        boolean playerIsDying = snapshot.isDying();
        boolean playerIsCelebrating = snapshot.isCelebrating();
        String characterType = snapshot.getCharacterType();

        ImageIcon playerGif = resources.getPlayerGif(characterType, direction, playerIsMoving,
                playerIsSneezing, playerIsKicking,
                playerIsDying, playerIsCelebrating);

        if (playerGif != null) {
            int playerX = offsetX + (int) pixelX + (CELL_SIZE - PLAYER_SIZE) / 2;
            int playerY = offsetY + (int) pixelY + (CELL_SIZE - PLAYER_SIZE) / 2;

            Image playerImage = playerGif.getImage();
            g2d.drawImage(playerImage, playerX, playerY, PLAYER_SIZE, PLAYER_SIZE, this);

            // Label P1/P2/AI
            // Label P1/P2/AI
            String name = snapshot.getName();
            if (name == null || name.isEmpty()) {
                name = (numberOfPlayers == 0) ? "AI" : label;
            }

            if (numberOfPlayers == 0) {
                g2d.setColor(label.equals("P1") ? new Color(0, 191, 255, 180) : new Color(255, 165, 0, 180));
            } else {
                g2d.setColor(label.equals("P1") ? new Color(100, 200, 255, 180) : new Color(255, 100, 100, 180));
            }

            g2d.setFont(fontLoader.getBoldFont(16f));
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(name);
            g2d.drawString(name, playerX + (PLAYER_SIZE - labelWidth) / 2, playerY - 5);
        }
    }

    /**
     * Dibuja los controles según el modo de juego.
     */

    // ==================== PANEL LATERAL ====================

    /**
     * Dibuja el panel lateral con temporizador y contador de frutas.
     */

    /**
     * Limpia los recursos del panel al cerrarse.
     */
    public void cleanup() {
        if (gameTimer != null)
            gameTimer.stop();
        if (animationTimer != null)
            animationTimer.stop();
        if (icePlacementTimer != null)
            icePlacementTimer.stop();
    }

    void handleMouseClick(Point point) {
        if (gameFacade.isPaused()) {
            handlePauseMenuClick(point);
        } else if (menuState == MenuState.SUMMARY) {
            handleSummaryMenuClick(point);
        }
    }

    void handleEscapeAction() {
        if (menuState == MenuState.NONE) {
            gameFacade.togglePause();
            menuState = MenuState.MAIN;
            repaint();
        } else if (menuState == MenuState.MAIN) {
            gameFacade.togglePause();
            menuState = MenuState.NONE;
            repaint();
        } else if (menuState == MenuState.LOAD || menuState == MenuState.SAVE) {
            menuState = MenuState.MAIN;
            repaint();
        }
    }

    private void handlePauseMenuClick(Point clickPoint) {
        if (menuState == MenuState.MAIN) {
            if (gameOverlay.getResumeButtonRect() != null && gameOverlay.getResumeButtonRect().contains(clickPoint)) {
                gameFacade.togglePause();
                menuState = MenuState.NONE;
            } else if (gameOverlay.getSaveButtonRect() != null
                    && gameOverlay.getSaveButtonRect().contains(clickPoint)) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("saves"));
                fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de guardado (*.dat)", "dat"));

                int result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (!selectedFile.getName().toLowerCase().endsWith(".dat")) {
                        selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".dat");
                    }
                    try {
                        gameFacade.saveGame(selectedFile);
                        JOptionPane.showMessageDialog(this, "Partida guardada exitosamente.");
                    } catch (BadDopoException e) {
                        JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
                    }
                }
            } else if (gameOverlay.getLoadButtonRect() != null
                    && gameOverlay.getLoadButtonRect().contains(clickPoint)) {
                savedGamesList = gameFacade.getSavedGames();
                menuState = MenuState.LOAD;
            } else if (gameOverlay.getRestartButtonRect() != null
                    && gameOverlay.getRestartButtonRect().contains(clickPoint)) {
                // Modified: Prompt for config even on mid-game restart
                promptForConfigurationAndStart(currentLevel);
                menuState = MenuState.NONE;
            } else if (gameOverlay.getExitButtonRect() != null
                    && gameOverlay.getExitButtonRect().contains(clickPoint)) {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.dispose();
                    Main.main(new String[] {});
                }
            }
        } else if (menuState == MenuState.LOAD) {
            if (gameOverlay.getBackButtonRect() != null && gameOverlay.getBackButtonRect().contains(clickPoint)) {
                menuState = MenuState.MAIN;
            } else {
                List<Rectangle> loadRects = gameOverlay.getLoadGameButtonRects();
                for (int i = 0; i < loadRects.size(); i++) {
                    if (loadRects.get(i).contains(clickPoint)) {
                        String saveFile = savedGamesList.get(i);
                        try {
                            gameFacade.loadGame(saveFile);
                            resetAnimationState();
                            currentLevel = gameFacade.getLevel();
                            menuState = MenuState.NONE;
                            JOptionPane.showMessageDialog(this, "Partida cargada exitosamente.");
                        } catch (BadDopoException e) {
                            JOptionPane.showMessageDialog(this, "Error al cargar: " + e.getMessage());
                        }
                    }
                }
            }
        }
        repaint();
    }

    private void handleSummaryMenuClick(Point clickPoint) {
        if (gameOverlay.getSummaryRestartButton() != null
                && gameOverlay.getSummaryRestartButton().contains(clickPoint)) {
            if (isVictory) {
                // Restart Current Level with Config Dialog
                promptForConfigurationAndStart(currentLevel);
            } else {
                // Restart Game (Defeat) -> Level 1 with Config Dialog
                promptForConfigurationAndStart(1);
            }
        } else if (gameOverlay.getSummaryMenuButton() != null
                && gameOverlay.getSummaryMenuButton().contains(clickPoint)) {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
                Main.main(new String[] {});
            }
        } else if (gameOverlay.getSummaryNextLevelButton() != null
                && gameOverlay.getSummaryNextLevelButton().contains(clickPoint)) {
            if (currentLevel < 4) {
                promptForConfigurationAndStart(currentLevel + 1);
            } else {
                JOptionPane.showMessageDialog(this, "¡Próximamente más niveles!", "Próximamente",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
        repaint();
    }

    /**
     * Shows the LevelConfigurationDialog for the target level and starts the game
     * with the chosen config.
     */
    private void promptForConfigurationAndStart(int targetLevel) {
        // Use a temporary facade to get defaults/types.
        // We reuse the existing context (players, etc.) from THIS GamePanel instance.
        // Or creates a temp one. existing gameFacade has necessary info? Not exactly,
        // getAvailableFruitTypes is static-like or simple getter in facade?

        // Let's rely on gameFacade for DTO/Lists.
        // Setup done inside dialog via facade

        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            // We use the CURRENT facade to configure settings.
            // Note: This modifies the configuration in the current facade instance.
            // We must retrieve it to pass to the next level's facade.

            // Ensure we have a default config loaded in the facade for the TARGET level
            // Ideally we should create a fresh config for the new level
            domain.dto.LevelConfigurationDTO defaultConfig = gameFacade.getDefaultConfiguration(targetLevel);
            gameFacade.setConfiguration(defaultConfig);

            LevelConfigurationDialog configDialog = new LevelConfigurationDialog(
                    (JFrame) window, gameFacade);
            configDialog.setVisible(true);

            if (configDialog.isConfirmed()) {
                // The facade now holds the updated configuration
                domain.dto.LevelConfigurationDTO newConfig = gameFacade.getConfiguration();
                startNewGameLevel(targetLevel, newConfig);
            }
        }
    }

    /**
     * Resets visual animation state to match current game state.
     * Prevents "ghosting" movement after level restart.
     */
    private void resetAnimationState() {
        // Reset Player 1
        Point p1Pos = gameFacade.getPlayerPosition();
        domain.BadDopoLogger.logInfo("DEBUG: resetAnimationState called. New Logical Pos: " + p1Pos +
                ", Old Pixel X: " + currentPixelX + ", Old Pixel Y: " + currentPixelY);

        this.targetGridPosition = new Point(p1Pos);
        this.currentPixelX = p1Pos.x * CELL_SIZE;
        this.currentPixelY = p1Pos.y * CELL_SIZE;
        this.isMoving = false;

        System.out
                .println("DEBUG: Reset Complete. Curr Pixel X: " + currentPixelX + ", Curr Pixel Y: " + currentPixelY);

        // Reset Player 2
        PlayerSnapshot p2Snapshot = gameFacade.getPlayer2Snapshot();
        if (p2Snapshot != null) {
            Point p2Pos = p2Snapshot.getPosition();
            this.player2TargetGridPosition = new Point(p2Pos);
            this.player2CurrentPixelX = p2Pos.x * CELL_SIZE;
            this.player2CurrentPixelY = p2Pos.y * CELL_SIZE;
            this.player2IsMoving = false;
        }

        // Reset Enemies
        this.enemyTargetPositions.clear();
        this.enemyCurrentPixelX.clear();
        this.enemyCurrentPixelY.clear();
        this.enemyIsMoving.clear();

        for (EnemySnapshot enemySnapshot : gameFacade.getEnemySnapshots()) {
            String id = enemySnapshot.getId();
            Point pos = enemySnapshot.getPosition();
            enemyTargetPositions.put(id, new Point(pos));
            enemyCurrentPixelX.put(id, (float) (pos.x * CELL_SIZE));
            enemyCurrentPixelY.put(id, (float) (pos.y * CELL_SIZE));
            enemyIsMoving.put(id, false);
        }

        // Reset inputs
        inputHandler.clearKeys();

        // Reset Ice Animation
        this.iceAnimationProgress.clear();
        this.icePlacementQueue.clear();
    }

    /**
     * Helper method to start a new game level with preserved settings.
     */
    /**
     * Helper method to start a new game level with preserved settings.
     */
    // Helper method startNewGameLevel(int) removed as it was unused locally

    private void startNewGameLevel(int targetLevel, domain.dto.LevelConfigurationDTO newConfig) {
        if (gameTimer != null)
            gameTimer.stop();
        if (animationTimer != null)
            animationTimer.stop();
        if (icePlacementTimer != null)
            icePlacementTimer.stop();

        SwingUtilities.invokeLater(() -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                JFrame frame = (JFrame) window;
                frame.getContentPane().removeAll();

                String p2Char = (gameFacade.getPlayer2Snapshot() != null)
                        ? gameFacade.getPlayer2Snapshot().getCharacterType()
                        : null;

                String p1Name = gameFacade.getPlayerSnapshot().getName();
                String p2Name = (gameFacade.getPlayer2Snapshot() != null)
                        ? gameFacade.getPlayer2Snapshot().getName()
                        : "P2";

                // Use new config if provided, otherwise fallback to last config (or default
                // inside facade if null)
                // Actually constructor expects a config.
                // If newConfig is null, we should use lastConfig?
                // Or if we claim this is "Next Level" without dialog, we might want default.
                // But promptForConfigurationAndStart ALWAYS provides non-null config if
                // confirmed.
                // If it was null (legacy call), we might want to fetch default or use last.
                // Let's use lastConfig if newConfig is null to preserve settings on quick
                // restart,
                // BUT "Restart Game" usually means reset.
                // Given the flow, promptForConfigurationAndStart handles the config creation.
                // If startNewGameLevel(int) is called directly (e.g. from debug?), use
                // lastConfig.

                // Determine configuration to use
                domain.dto.LevelConfigurationDTO currentConfig = gameFacade.getConfiguration();
                domain.dto.LevelConfigurationDTO configToUse = (newConfig != null) ? newConfig
                        : (currentConfig != null ? currentConfig : gameFacade.getDefaultConfiguration(targetLevel));

                // Create NEW Facade for the new level
                domain.GameFacade newFacade = new domain.GameFacade(
                        gameFacade.getPlayerCharacterType(),
                        p2Char, // P2 Character
                        p1Name,
                        p2Name,
                        targetLevel,
                        gameFacade.getNumberOfPlayers(),
                        gameFacade.getAITypeP1(),
                        gameFacade.getAITypeP2(),
                        gameFacade.isP2CPU(),
                        configToUse);

                GamePanel newGamePanel = new GamePanel(newFacade, resources, (GameWindow) window);

                frame.add(newGamePanel);
                frame.revalidate();
                frame.repaint();
                newGamePanel.requestFocusInWindow();
            }
        });
    }
}
