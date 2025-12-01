package presentation;

import domain.*;
import domain.dto.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Panel principal del juego que maneja renderizado y captura de inputs.
 * Solo responsable de la presentación visual, sin lógica de negocio.
 */
public class GamePanel extends JPanel {

    // Constantes del juego
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;
    private static final int GRID_SIZE = 13;
    private static final int CELL_SIZE = 50;

    // Constantes del sidebar
    private static final int SIDEBAR_WIDTH = 200;
    private static final int SIDEBAR_FRUIT_SIZE = 40;
    private static final int SIDEBAR_PADDING = 20;

    // Tamaños de los sprites
    private static final int PLAYER_SIZE = 45;
    private static final int TROLL_SIZE = 45;
    private static final int FRUIT_SIZE = 35;
    private static final int ICE_SIZE = 40;

    // Velocidad de movimiento
    private static final int ANIMATION_SPEED = 4;
    private static final int FRAME_DELAY = 16;

    // Recursos
    private ResourceLoader resources;
    private FontLoader fontLoader;

    // Fachada del dominio
    private GameFacade gameFacade;

    // Datos del nivel
    private String selectedCharacter;
    private int currentLevel;
    private int numberOfPlayers;

    // Timers (javax.swing.Timer)
    private javax.swing.Timer gameTimer;
    private javax.swing.Timer animationTimer;

    // Control de teclas
    private Set<Integer> pressedKeys;
    private boolean spaceWasPressed;
    private boolean mWasPressed;

    // Variables para animación suave del jugador 1
    private Point targetGridPosition;
    private float currentPixelX;
    private float currentPixelY;
    private boolean isMoving;

    // Variables para animación suave del jugador 2 (enemigo controlado)
    private Point player2TargetGridPosition;
    private float player2CurrentPixelX;
    private float player2CurrentPixelY;
    private boolean player2IsMoving;

    // Variables para animación suave de enemigos (mapeadas por posición de grid)
    private Map<Point, Point> enemyTargetPositions;
    private Map<Point, Float> enemyCurrentPixelX;
    private Map<Point, Float> enemyCurrentPixelY;
    private Map<Point, Boolean> enemyIsMoving;

    // Animación de hielo
    private Map<Point, Integer> iceAnimationProgress;
    private Queue<Point> icePlacementQueue;
    private javax.swing.Timer icePlacementTimer;

    // Control de reinicio
    private boolean restartScheduled;

    // Menú de Pausa
    private enum MenuState {
        NONE, MAIN, SAVE, LOAD
    }

    private MenuState menuState = MenuState.NONE;
    private List<String> savedGamesList = new ArrayList<>();

    // Rectángulos de botones (para detección de clicks)
    private Rectangle resumeButtonRect, saveButtonRect, loadButtonRect, restartButtonRect, exitButtonRect;
    private List<Rectangle> loadGameButtonRects = new ArrayList<>();
    private Rectangle backButtonRect;

    /**
     * Constructor del panel de juego.
     *
     * @param character       Personaje seleccionado
     * @param level           Nivel a jugar
     * @param numberOfPlayers Número de jugadores
     * @param resources       Recursos cargados
     */
    public GamePanel(String character, int level, int numberOfPlayers, ResourceLoader resources) {
        this.resources = resources;
        this.fontLoader = FontLoader.getInstance();
        this.selectedCharacter = character;
        this.currentLevel = level;
        this.numberOfPlayers = numberOfPlayers;
        this.gameFacade = new GameFacade(character, level, numberOfPlayers);
        this.pressedKeys = new HashSet<>();
        this.spaceWasPressed = false;
        this.mWasPressed = false;
        this.iceAnimationProgress = new HashMap<>();
        this.icePlacementQueue = new LinkedList<>();
        this.restartScheduled = false;

        // Inicializar animación de enemigos
        this.enemyTargetPositions = new HashMap<>();
        this.enemyCurrentPixelX = new HashMap<>();
        this.enemyCurrentPixelY = new HashMap<>();
        this.enemyIsMoving = new HashMap<>();

        // Inicializar posiciones de enemigos usando snapshots
        for (EnemySnapshot enemySnapshot : gameFacade.getEnemySnapshots()) {
            Point pos = enemySnapshot.getPosition();
            enemyTargetPositions.put(pos, new Point(pos));
            enemyCurrentPixelX.put(pos, (float) (pos.x * CELL_SIZE));
            enemyCurrentPixelY.put(pos, (float) (pos.y * CELL_SIZE));
            enemyIsMoving.put(pos, false);

            if (enemySnapshot.isControlledByPlayer()) {
                player2TargetGridPosition = new Point(pos);
                player2CurrentPixelX = pos.x * CELL_SIZE;
                player2CurrentPixelY = pos.y * CELL_SIZE;
                player2IsMoving = false;
            }
        }

        // Inicializar posición de animación del jugador 1
        Point initialPos = gameFacade.getPlayerPosition();
        this.targetGridPosition = new Point(initialPos);
        this.currentPixelX = initialPos.x * CELL_SIZE;
        this.currentPixelY = initialPos.y * CELL_SIZE;
        this.isMoving = false;

        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setLayout(null);
        setFocusable(true);
        requestFocusInWindow();

        if (numberOfPlayers != 0) {
            setupKeyListeners();
        }

        setupMouseListeners();

        startTimers();

        String modeText = numberOfPlayers == 0 ? "Machine vs Machine" : numberOfPlayers + " player(s)";
        System.out.println("GamePanel initialized for level " + level + " with " + modeText);
    }

    // ==================== CONFIGURACIÓN DE LISTENERS ====================

    /**
     * Configura los listeners de teclado para capturar inputs del jugador.
     */
    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();

                if (!pressedKeys.contains(keyCode)) {
                    pressedKeys.add(keyCode);

                    if (keyCode == KeyEvent.VK_ESCAPE) {
                        handleEscapeAction();
                    }

                    if (gameFacade.isPaused())
                        return; // No procesar otros inputs si está pausado

                    if (keyCode == KeyEvent.VK_SPACE && !spaceWasPressed) {
                        spaceWasPressed = true;
                        handleSpaceAction();
                    }

                    // Tecla M para romper hielo (P2 - solo si controla calamar)
                    if (keyCode == KeyEvent.VK_M && !mWasPressed && numberOfPlayers == 2) {
                        mWasPressed = true;
                        handleMAction();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();
                pressedKeys.remove(keyCode);

                if (keyCode == KeyEvent.VK_SPACE) {
                    spaceWasPressed = false;
                }

                if (keyCode == KeyEvent.VK_M) {
                    mWasPressed = false;
                }
            }
        });
    }

    // ==================== MANEJO DE ACCIONES ====================

    /**
     * Maneja la acción de SPACE.
     * La decisión kick vs sneeze se hace en el dominio.
     */
    private void handleSpaceAction() {
        if (!gameFacade.isPlayerAlive() || gameFacade.isPlayerBusy())
            return;

        // El dominio decide automáticamente: kick o sneeze
        List<Point> affectedPositions = gameFacade.performSpaceAction();

        if (!affectedPositions.isEmpty()) {
            // Iniciar animación visual
            startIcePlacementAnimation(affectedPositions);
        }
    }

    /**
     * Maneja la acción de M (romper hielo con P2).
     */
    private void handleMAction() {
        // La validación de si es CALAMAR se hace en el dominio
        if (!gameFacade.canPlayer2BreakIce())
            return;

        Point brokenIce = gameFacade.performPlayer2IceBreak();
        if (brokenIce != null) {
            System.out.println("✓ P2 (Calamar) rompió hielo con tecla M en " + brokenIce);
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

    // ==================== PROCESAMIENTO DE MOVIMIENTO ====================

    /**
     * Procesa el movimiento de los jugadores humanos.
     * La IA se ejecuta automáticamente en el dominio.
     */
    private void processMovement() {
        if (!gameFacade.isPlayerAlive() || gameFacade.isPlayerDying())
            return;

        // Solo procesar movimiento humano - la IA se maneja en el dominio
        if (numberOfPlayers == 1) {
            processHumanPlayerMovement();
        } else if (numberOfPlayers == 2) {
            processHumanPlayerMovement();
            processHumanPlayer2Movement();
        }
        // Si numberOfPlayers == 0, la IA se ejecuta automáticamente en
        // GameLogic.update()
    }

    /**
     * Procesa el movimiento del jugador 1 (WASD).
     */
    private void processHumanPlayerMovement() {
        if (!isMoving) {
            boolean moved = false;

            if (pressedKeys.contains(KeyEvent.VK_W)) {
                gameFacade.movePlayerUp();
                moved = true;
            } else if (pressedKeys.contains(KeyEvent.VK_S)) {
                gameFacade.movePlayerDown();
                moved = true;
            } else if (pressedKeys.contains(KeyEvent.VK_A)) {
                gameFacade.movePlayerLeft();
                moved = true;
            } else if (pressedKeys.contains(KeyEvent.VK_D)) {
                gameFacade.movePlayerRight();
                moved = true;
            } else {
                gameFacade.stopPlayer();
            }

            if (moved) {
                updatePlayerAnimation();
            }
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
        EnemySnapshot controlledEnemy = gameFacade.getControlledEnemySnapshot();
        if (controlledEnemy != null && !player2IsMoving) {
            boolean moved = false;

            if (pressedKeys.contains(KeyEvent.VK_UP)) {
                gameFacade.movePlayer2Up();
                moved = true;
            } else if (pressedKeys.contains(KeyEvent.VK_DOWN)) {
                gameFacade.movePlayer2Down();
                moved = true;
            } else if (pressedKeys.contains(KeyEvent.VK_LEFT)) {
                gameFacade.movePlayer2Left();
                moved = true;
            } else if (pressedKeys.contains(KeyEvent.VK_RIGHT)) {
                gameFacade.movePlayer2Right();
                moved = true;
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
        EnemySnapshot controlledEnemy = gameFacade.getControlledEnemySnapshot();
        if (controlledEnemy != null) {
            Point newGridPos = controlledEnemy.getPosition();
            if (!newGridPos.equals(player2TargetGridPosition)) {
                player2TargetGridPosition = new Point(newGridPos);
                player2IsMoving = true;
            }
        }
    }

    // ==================== ACTUALIZACIÓN DE ANIMACIONES ====================

    /**
     * Actualiza todas las animaciones suaves de las entidades.
     */
    private void updateAnimation() {
        // Actualizar animación del jugador 1
        updatePlayerPixelPosition();

        // Actualizar animación del jugador 2 / IA
        updatePlayer2PixelPosition();

        // Actualizar animación de enemigos NO controlados
        updateEnemiesPixelPosition();

        // Actualizar animación de hielo (fade in)
        updateIceAnimationProgress();

        // Sincronizar animaciones con los cambios del dominio (para Machine vs Machine)
        if (numberOfPlayers == 0) {
            syncPlayerAnimationWithDomain();
            syncPlayer2AnimationWithDomain();
        }
    }

    /**
     * Actualiza la posición en píxeles del jugador 1.
     */
    private void updatePlayerPixelPosition() {
        if (isMoving && !gameFacade.isPlayerDying()) {
            float targetPixelX = targetGridPosition.x * CELL_SIZE;
            float targetPixelY = targetGridPosition.y * CELL_SIZE;

            float deltaX = targetPixelX - currentPixelX;
            float deltaY = targetPixelY - currentPixelY;

            if (Math.abs(deltaX) < ANIMATION_SPEED && Math.abs(deltaY) < ANIMATION_SPEED) {
                currentPixelX = targetPixelX;
                currentPixelY = targetPixelY;
                isMoving = false;
            } else {
                if (Math.abs(deltaX) > 0.5f) {
                    currentPixelX += Math.signum(deltaX) * ANIMATION_SPEED;
                }
                if (Math.abs(deltaY) > 0.5f) {
                    currentPixelY += Math.signum(deltaY) * ANIMATION_SPEED;
                }
            }
        }
    }

    /**
     * Actualiza la posición en píxeles del jugador 2 / IA.
     */
    private void updatePlayer2PixelPosition() {
        if ((numberOfPlayers == 2 || numberOfPlayers == 0) && player2IsMoving) {
            float targetPixelX = player2TargetGridPosition.x * CELL_SIZE;
            float targetPixelY = player2TargetGridPosition.y * CELL_SIZE;

            float deltaX = targetPixelX - player2CurrentPixelX;
            float deltaY = targetPixelY - player2CurrentPixelY;

            if (Math.abs(deltaX) < ANIMATION_SPEED && Math.abs(deltaY) < ANIMATION_SPEED) {
                player2CurrentPixelX = targetPixelX;
                player2CurrentPixelY = targetPixelY;
                player2IsMoving = false;
            } else {
                if (Math.abs(deltaX) > 0.5f) {
                    player2CurrentPixelX += Math.signum(deltaX) * ANIMATION_SPEED;
                }
                if (Math.abs(deltaY) > 0.5f) {
                    player2CurrentPixelY += Math.signum(deltaY) * ANIMATION_SPEED;
                }
            }
        }
    }

    /**
     * Actualiza la posición en píxeles de enemigos NO controlados.
     */
    private void updateEnemiesPixelPosition() {
        if (!gameFacade.isVictory()) {
            // Limpiar enemigos que ya no existen
            Set<Point> currentEnemyPositions = new HashSet<>();

            for (EnemySnapshot enemySnapshot : gameFacade.getEnemySnapshots()) {
                if (!enemySnapshot.isActive())
                    continue;
                if (enemySnapshot.isControlledByPlayer())
                    continue;

                Point actualPosition = enemySnapshot.getPosition();
                currentEnemyPositions.add(actualPosition);

                // Inicializar si es nuevo enemigo
                if (!enemyTargetPositions.containsKey(actualPosition)) {
                    enemyTargetPositions.put(actualPosition, new Point(actualPosition));
                    enemyCurrentPixelX.put(actualPosition, (float) (actualPosition.x * CELL_SIZE));
                    enemyCurrentPixelY.put(actualPosition, (float) (actualPosition.y * CELL_SIZE));
                    enemyIsMoving.put(actualPosition, false);
                }

                Point currentTarget = enemyTargetPositions.get(actualPosition);

                if (!actualPosition.equals(currentTarget)) {
                    enemyTargetPositions.put(actualPosition, new Point(actualPosition));
                    enemyIsMoving.put(actualPosition, true);
                }

                if (enemyIsMoving.get(actualPosition)) {
                    float currentX = enemyCurrentPixelX.get(actualPosition);
                    float currentY = enemyCurrentPixelY.get(actualPosition);
                    float targetX = actualPosition.x * CELL_SIZE;
                    float targetY = actualPosition.y * CELL_SIZE;

                    float deltaX = targetX - currentX;
                    float deltaY = targetY - currentY;

                    if (Math.abs(deltaX) < ANIMATION_SPEED && Math.abs(deltaY) < ANIMATION_SPEED) {
                        enemyCurrentPixelX.put(actualPosition, targetX);
                        enemyCurrentPixelY.put(actualPosition, targetY);
                        enemyIsMoving.put(actualPosition, false);
                    } else {
                        if (Math.abs(deltaX) > 0.5f) {
                            enemyCurrentPixelX.put(actualPosition, currentX + Math.signum(deltaX) * ANIMATION_SPEED);
                        }
                        if (Math.abs(deltaY) > 0.5f) {
                            enemyCurrentPixelY.put(actualPosition, currentY + Math.signum(deltaY) * ANIMATION_SPEED);
                        }
                    }
                }
            }

            // Limpiar posiciones de enemigos que ya no existen
            enemyTargetPositions.keySet().retainAll(currentEnemyPositions);
            enemyCurrentPixelX.keySet().retainAll(currentEnemyPositions);
            enemyCurrentPixelY.keySet().retainAll(currentEnemyPositions);
            enemyIsMoving.keySet().retainAll(currentEnemyPositions);
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
     * Sincroniza la posición visual del jugador con el dominio (para IA).
     */
    private void syncPlayerAnimationWithDomain() {
        Point domainPos = gameFacade.getPlayerPosition();
        if (!domainPos.equals(targetGridPosition)) {
            targetGridPosition = new Point(domainPos);
            isMoving = true;
        }
    }

    /**
     * Sincroniza la posición visual del enemigo controlado con el dominio (para
     * IA).
     */
    private void syncPlayer2AnimationWithDomain() {
        EnemySnapshot controlledEnemy = gameFacade.getControlledEnemySnapshot();
        if (controlledEnemy != null) {
            Point domainPos = controlledEnemy.getPosition();
            if (!domainPos.equals(player2TargetGridPosition)) {
                player2TargetGridPosition = new Point(domainPos);
                player2IsMoving = true;
            }
        }
    }

    // ==================== TIMERS ====================

    /**
     * Inicia los timers del juego y de animación.
     */
    private void startTimers() {
        gameTimer = new javax.swing.Timer(FRAME_DELAY, e -> {
            if (gameFacade.isPaused())
                return; // No actualizar lógica si está pausado

            processMovement();
            updateAnimation();
            gameFacade.update(); // ← Aquí se ejecuta la IA automáticamente si numberOfPlayers == 0

            // Verificar si se debe reiniciar (lógica en el dominio)
            if (gameFacade.shouldRestartLevel() && !restartScheduled) {
                restartScheduled = true;

                if (gameFacade.isTimeUp()) {
                    System.out.println("✗ ¡Tiempo agotado! Reiniciando nivel...");
                } else {
                    System.out.println("✗ Jugador murió. Reiniciando nivel...");
                }

                // Pequeño delay para que se vea el mensaje
                javax.swing.Timer delayTimer = new javax.swing.Timer(1500, evt -> {
                    handleLevelRestart();
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            }
        });
        gameTimer.start();

        animationTimer = new javax.swing.Timer(FRAME_DELAY, e -> repaint());
        animationTimer.start();
    }

    /**
     * Maneja el reinicio del nivel.
     */
    private void handleLevelRestart() {
        if (gameTimer != null)
            gameTimer.stop();
        if (animationTimer != null)
            animationTimer.stop();
        if (icePlacementTimer != null)
            icePlacementTimer.stop();

        System.out.println("✓ Timers detenidos");

        javax.swing.Timer restartTimer = new javax.swing.Timer(500, e -> {
            SwingUtilities.invokeLater(() -> {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window instanceof JFrame) {
                    JFrame frame = (JFrame) window;
                    frame.getContentPane().removeAll();

                    System.out.println("✓ Creando nuevo panel de juego...");
                    GamePanel newGamePanel = new GamePanel(selectedCharacter, currentLevel, numberOfPlayers, resources);
                    frame.add(newGamePanel);
                    frame.revalidate();
                    frame.repaint();
                    newGamePanel.requestFocusInWindow();
                    System.out.println("✓ Nivel reiniciado exitosamente!");
                }
            });
        });
        restartTimer.setRepeats(false);
        restartTimer.start();
    }

    // ==================== RENDERING ====================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int mapWidth = GRID_SIZE * CELL_SIZE;
        int mapHeight = GRID_SIZE * CELL_SIZE;
        int offsetX = (WINDOW_WIDTH - mapWidth) / 2;
        int offsetY = (WINDOW_HEIGHT - mapHeight) / 2;

        // Dibujar fondo
        if (resources.fondoMapa != null) {
            g2d.drawImage(resources.fondoMapa, offsetX, offsetY, mapWidth, mapHeight, this);
        }

        // Panel lateral
        drawSidebar(g2d, offsetX);

        // Dibujar frutas usando FruitSnapshot
        drawFruits(g2d, offsetX, offsetY);

        // Dibujar hielo usando IceBlockSnapshot
        drawIceBlocks(g2d, offsetX, offsetY);

        // Dibujar enemigos usando EnemySnapshot
        drawEnemies(g2d, offsetX, offsetY);

        // Dibujar jugador usando PlayerSnapshot
        drawPlayer(g2d, offsetX, offsetY);

        // Mostrar controles según modo
        drawControls(g2d);

        // Dibujar menú de pausa si es necesario
        if (gameFacade.isPaused()) {
            drawPauseMenu(g2d);
        }
    }

    /**
     * Dibuja todas las frutas del juego.
     */
    private void drawFruits(Graphics2D g2d, int offsetX, int offsetY) {
        for (FruitSnapshot fruitSnapshot : gameFacade.getFruitSnapshots()) {
            if (!fruitSnapshot.isCollected()) {
                Point pos = fruitSnapshot.getPosition();
                int x = offsetX + pos.x * CELL_SIZE + (CELL_SIZE - FRUIT_SIZE) / 2;
                int y = offsetY + pos.y * CELL_SIZE + (CELL_SIZE - FRUIT_SIZE) / 2;

                String fruitType = fruitSnapshot.getFruitType();
                Image fruitImage = resources.getFruitImage(fruitType);
                g2d.drawImage(fruitImage, x, y, FRUIT_SIZE, FRUIT_SIZE, this);
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

        if (enemySnapshot.isControlledByPlayer()) {
            pixelX = player2CurrentPixelX;
            pixelY = player2CurrentPixelY;
        } else {
            pixelX = enemyCurrentPixelX.getOrDefault(gridPos, (float) (gridPos.x * CELL_SIZE));
            pixelY = enemyCurrentPixelY.getOrDefault(gridPos, (float) (gridPos.y * CELL_SIZE));
        }

        int x = offsetX + (int) pixelX + (CELL_SIZE - TROLL_SIZE) / 2;
        int y = offsetY + (int) pixelY + (CELL_SIZE - TROLL_SIZE) / 2;

        String direction = enemySnapshot.getDirection();
        String enemyType = enemySnapshot.getEnemyType();
        boolean isBreakingIce = enemySnapshot.isBreakingIce();
        ImageIcon enemyGif = resources.getEnemyGif(enemyType, direction, isBreakingIce);

        if (enemyGif != null) {
            Image enemyImage = enemyGif.getImage();
            g2d.drawImage(enemyImage, x, y, TROLL_SIZE, TROLL_SIZE, this);
        }

        // Etiqueta P2 o AI
        if (enemySnapshot.isControlledByPlayer()) {
            drawEnemyLabel(g2d, x, y);
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
        } else {
            idleGif = resources.calamarWalkDownGif;
        }

        if (idleGif != null) {
            Image enemyImage = idleGif.getImage();
            g2d.drawImage(enemyImage, x, y, TROLL_SIZE, TROLL_SIZE, this);
        }
    }

    /**
     * Dibuja la etiqueta del enemigo controlado (P2 o AI).
     */
    private void drawEnemyLabel(Graphics2D g2d, int x, int y) {
        if (numberOfPlayers == 2) {
            g2d.setColor(new Color(0, 255, 0, 180));
            g2d.setFont(fontLoader.getBoldFont(16f));
            String p2Label = "P2";
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(p2Label);
            g2d.drawString(p2Label, x + (TROLL_SIZE - labelWidth) / 2, y - 5);
        } else if (numberOfPlayers == 0) {
            g2d.setColor(new Color(255, 165, 0, 180));
            g2d.setFont(fontLoader.getBoldFont(16f));
            String aiLabel = "AI";
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(aiLabel);
            g2d.drawString(aiLabel, x + (TROLL_SIZE - labelWidth) / 2, y - 5);
        }
    }

    /**
     * Dibuja el jugador.
     */
    private void drawPlayer(Graphics2D g2d, int offsetX, int offsetY) {
        PlayerSnapshot playerSnapshot = gameFacade.getPlayerSnapshot();
        if (playerSnapshot.isActive()) {
            String direction = playerSnapshot.getDirection();
            boolean playerIsMoving = playerSnapshot.isMoving();
            boolean playerIsSneezing = playerSnapshot.isSneezing();
            boolean playerIsKicking = playerSnapshot.isKicking();
            boolean playerIsDying = playerSnapshot.isDying();
            boolean playerIsCelebrating = playerSnapshot.isCelebrating();
            String characterType = playerSnapshot.getCharacterType();

            ImageIcon playerGif = resources.getPlayerGif(characterType, direction, playerIsMoving,
                    playerIsSneezing, playerIsKicking,
                    playerIsDying, playerIsCelebrating);

            if (playerGif != null) {
                int playerX = offsetX + (int) currentPixelX + (CELL_SIZE - PLAYER_SIZE) / 2;
                int playerY = offsetY + (int) currentPixelY + (CELL_SIZE - PLAYER_SIZE) / 2;

                Image playerImage = playerGif.getImage();
                g2d.drawImage(playerImage, playerX, playerY, PLAYER_SIZE, PLAYER_SIZE, this);

                if (numberOfPlayers == 0) {
                    g2d.setColor(new Color(0, 191, 255, 180));
                    g2d.setFont(fontLoader.getBoldFont(16f));
                    String aiLabel = "AI";
                    FontMetrics fm = g2d.getFontMetrics();
                    int labelWidth = fm.stringWidth(aiLabel);
                    g2d.drawString(aiLabel, playerX + (PLAYER_SIZE - labelWidth) / 2, playerY - 5);
                }
            }
        }
    }

    /**
     * Dibuja los controles según el modo de juego.
     */
    private void drawControls(Graphics2D g2d) {
        g2d.setFont(fontLoader.getBoldFont(14f));
        if (numberOfPlayers == 2) {
            g2d.setColor(Color.WHITE);
            g2d.drawString("P1: WASD + SPACE", 10, 20);
            g2d.setColor(new Color(0, 255, 0));

            if (gameFacade.canPlayer2BreakIce()) {
                g2d.drawString("P2: Flechas + M (Romper Hielo)", 10, 40);
            } else {
                g2d.drawString("P2: Flechas", 10, 40);
            }
        } else if (numberOfPlayers == 0) {
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawString("Modo: MACHINE vs MACHINE", 10, 20);
            g2d.setColor(new Color(150, 150, 150));
            g2d.drawString("Nivel " + currentLevel, 10, 40);
        }
    }

    // ==================== PANEL LATERAL ====================

    /**
     * Dibuja el panel lateral con temporizador y contador de frutas.
     */
    private void drawSidebar(Graphics2D g2d, int mapOffsetX) {
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
        g2d.setColor(Color.WHITE);
        g2d.setFont(fontLoader.getBoldFont(20f));
        String timeLabel = "TIEMPO";
        FontMetrics fmLabel = g2d.getFontMetrics();
        int timeLabelWidth = fmLabel.stringWidth(timeLabel);
        g2d.drawString(timeLabel, sidebarX + (SIDEBAR_WIDTH - timeLabelWidth) / 2, currentY);

        currentY += 30;

        // Mostrar tiempo restante
        String timeRemaining = gameFacade.getFormattedTime();
        long timeInMs = gameFacade.getTimeRemaining();

        // Cambiar color según el tiempo restante
        if (timeInMs <= 30000) { // Menos de 30 segundos - ROJO
            g2d.setColor(new Color(255, 50, 50));
        } else if (timeInMs <= 60000) { // Menos de 1 minuto - AMARILLO
            g2d.setColor(new Color(255, 255, 0));
        } else { // Más de 1 minuto - VERDE
            g2d.setColor(new Color(100, 255, 100));
        }

        g2d.setFont(fontLoader.getBoldFont(32f));
        FontMetrics fmTime = g2d.getFontMetrics();
        int timeWidth = fmTime.stringWidth(timeRemaining);
        g2d.drawString(timeRemaining, sidebarX + (SIDEBAR_WIDTH - timeWidth) / 2, currentY);

        currentY += 50;

        // Línea separadora
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.drawLine(sidebarX + 20, currentY, sidebarX + SIDEBAR_WIDTH - 20, currentY);

        currentY += 30;

        // ==================== FRUTAS RESTANTES ====================
        g2d.setColor(Color.WHITE);
        g2d.setFont(fontLoader.getBoldFont(18f));
        String fruitLabel = "FRUTAS";
        FontMetrics fmFruit = g2d.getFontMetrics();
        int fruitLabelWidth = fmFruit.stringWidth(fruitLabel);
        g2d.drawString(fruitLabel, sidebarX + (SIDEBAR_WIDTH - fruitLabelWidth) / 2, currentY);

        currentY += 35;

        // Obtener tipos únicos de frutas en el nivel
        List<String> fruitTypes = gameFacade.getUniqueFruitTypes();

        for (String fruitType : fruitTypes) {
            int remainingCount = gameFacade.countRemainingFruits(fruitType);

            // Dibujar imagen de la fruta
            Image fruitImage = resources.getFruitImage(fruitType);
            int fruitX = sidebarX + 30;
            g2d.drawImage(fruitImage, fruitX, currentY, SIDEBAR_FRUIT_SIZE, SIDEBAR_FRUIT_SIZE, this);

            // Dibujar contador
            g2d.setFont(fontLoader.getBoldFont(24f));

            // Color según cantidad restante
            if (remainingCount == 0) {
                g2d.setColor(new Color(100, 100, 100)); // Gris si está completo
            } else {
                g2d.setColor(Color.WHITE);
            }

            String countText = "x " + remainingCount;
            g2d.drawString(countText, fruitX + SIDEBAR_FRUIT_SIZE + 15, currentY + 28);

            currentY += SIDEBAR_FRUIT_SIZE + 15;
        }

        // ==================== PUNTUACIÓN ====================
        currentY += 10;

        // Línea separadora
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.drawLine(sidebarX + 20, currentY, sidebarX + SIDEBAR_WIDTH - 20, currentY);

        currentY += 30;

        g2d.setColor(Color.WHITE);
        g2d.setFont(fontLoader.getBoldFont(18f));
        String scoreLabel = "PUNTOS";
        FontMetrics fmScore = g2d.getFontMetrics();
        int scoreLabelWidth = fmScore.stringWidth(scoreLabel);
        g2d.drawString(scoreLabel, sidebarX + (SIDEBAR_WIDTH - scoreLabelWidth) / 2, currentY);

        currentY += 35;

        int score = gameFacade.getScore();
        String scoreText = String.valueOf(score);
        g2d.setFont(fontLoader.getBoldFont(32f));
        g2d.setColor(new Color(255, 215, 0)); // Dorado
        FontMetrics fmScoreVal = g2d.getFontMetrics();
        int scoreValWidth = fmScoreVal.stringWidth(scoreText);
        g2d.drawString(scoreText, sidebarX + (SIDEBAR_WIDTH - scoreValWidth) / 2, currentY);

        // ==================== ADVERTENCIA DE TIEMPO ====================
        if (timeInMs <= 30000 && timeInMs > 0) {
            currentY += 20;
            g2d.setColor(new Color(255, 50, 50));
            g2d.setFont(fontLoader.getBoldFont(16f));
            String warning = "¡APÚRATE!";
            FontMetrics fmWarning = g2d.getFontMetrics();
            int warningWidth = fmWarning.stringWidth(warning);

            // Efecto parpadeante
            int alpha = (int) ((Math.sin(System.currentTimeMillis() / 200.0) + 1) * 127.5);
            g2d.setColor(new Color(255, 50, 50, alpha));
            g2d.drawString(warning, sidebarX + (SIDEBAR_WIDTH - warningWidth) / 2, currentY);
        }
    }

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

    // ==================== MENÚ DE PAUSA ====================

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameFacade.isPaused()) {
                    handlePauseMenuClick(e.getPoint());
                }
            }
        });
    }

    private void handleEscapeAction() {
        gameFacade.togglePause();
        if (gameFacade.isPaused()) {
            menuState = MenuState.MAIN;
        } else {
            menuState = MenuState.NONE;
        }
        repaint();
    }

    private void drawPauseMenu(Graphics2D g2d) {
        // Fondo semitransparente
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        if (menuState == MenuState.MAIN) {
            drawMainMenu(g2d);
        } else if (menuState == MenuState.LOAD) {
            drawLoadMenu(g2d);
        }
    }

    private void drawMainMenu(Graphics2D g2d) {
        int centerX = WINDOW_WIDTH / 2;
        int startY = 200;
        int buttonWidth = 300;
        int buttonHeight = 50;
        int spacing = 20;

        g2d.setColor(Color.WHITE);
        g2d.setFont(fontLoader.getBoldFont(48f));
        String title = "PAUSA";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, centerX - fm.stringWidth(title) / 2, 150);

        g2d.setFont(fontLoader.getBoldFont(24f));

        resumeButtonRect = drawButton(g2d, "REANUDAR", centerX, startY, buttonWidth, buttonHeight);
        saveButtonRect = drawButton(g2d, "GUARDAR PARTIDA", centerX, startY + (buttonHeight + spacing), buttonWidth,
                buttonHeight);
        loadButtonRect = drawButton(g2d, "CARGAR PARTIDA", centerX, startY + (buttonHeight + spacing) * 2, buttonWidth,
                buttonHeight);
        restartButtonRect = drawButton(g2d, "REINICIAR NIVEL", centerX, startY + (buttonHeight + spacing) * 3,
                buttonWidth, buttonHeight);
        exitButtonRect = drawButton(g2d, "SALIR", centerX, startY + (buttonHeight + spacing) * 4, buttonWidth,
                buttonHeight);
    }

    private void drawLoadMenu(Graphics2D g2d) {
        int centerX = WINDOW_WIDTH / 2;
        int startY = 150;
        int buttonWidth = 400;
        int buttonHeight = 40;
        int spacing = 10;

        g2d.setColor(Color.WHITE);
        g2d.setFont(fontLoader.getBoldFont(36f));
        String title = "CARGAR PARTIDA";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, centerX - fm.stringWidth(title) / 2, 100);

        g2d.setFont(fontLoader.getBoldFont(18f));

        loadGameButtonRects.clear();

        if (savedGamesList.isEmpty()) {
            g2d.drawString("No hay partidas guardadas", centerX - fm.stringWidth("No hay partidas guardadas") / 2,
                    startY);
        } else {
            for (int i = 0; i < savedGamesList.size(); i++) {
                String saveName = savedGamesList.get(i);
                Rectangle rect = drawButton(g2d, saveName, centerX, startY + i * (buttonHeight + spacing), buttonWidth,
                        buttonHeight);
                loadGameButtonRects.add(rect);
            }
        }

        backButtonRect = drawButton(g2d, "VOLVER", centerX, WINDOW_HEIGHT - 100, 200, 50);
    }

    private Rectangle drawButton(Graphics2D g2d, String text, int centerX, int y, int width, int height) {
        int x = centerX - width / 2;
        Rectangle rect = new Rectangle(x, y, width, height);

        // Detectar hover (opcional, requiere MouseMotionListener)
        Point mousePos = getMousePosition();
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
        int textX = centerX - fm.stringWidth(text) / 2;
        int textY = y + (height + fm.getAscent()) / 2 - 5;
        g2d.drawString(text, textX, textY);

        return rect;
    }

    private void handlePauseMenuClick(Point clickPoint) {
        if (menuState == MenuState.MAIN) {
            if (resumeButtonRect != null && resumeButtonRect.contains(clickPoint)) {
                gameFacade.togglePause();
                menuState = MenuState.NONE;
            } else if (saveButtonRect != null && saveButtonRect.contains(clickPoint)) {
                try {
                    gameFacade.saveGame();
                    JOptionPane.showMessageDialog(this, "Partida guardada exitosamente.");
                } catch (BadDopoException e) {
                    JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
                }
            } else if (loadButtonRect != null && loadButtonRect.contains(clickPoint)) {
                savedGamesList = gameFacade.getSavedGames();
                menuState = MenuState.LOAD;
            } else if (restartButtonRect != null && restartButtonRect.contains(clickPoint)) {
                gameFacade.restartLevel();
                menuState = MenuState.NONE;
            } else if (exitButtonRect != null && exitButtonRect.contains(clickPoint)) {
                // Salir al menú principal (cerrar ventana actual)
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.dispose();
                    // Reiniciar la app
                    Main.main(new String[] {});
                }
            }
        } else if (menuState == MenuState.LOAD) {
            if (backButtonRect != null && backButtonRect.contains(clickPoint)) {
                menuState = MenuState.MAIN;
            } else {
                for (int i = 0; i < loadGameButtonRects.size(); i++) {
                    if (loadGameButtonRects.get(i).contains(clickPoint)) {
                        String saveFile = savedGamesList.get(i);
                        try {
                            gameFacade.loadGame(saveFile);
                            menuState = MenuState.NONE;
                            JOptionPane.showMessageDialog(this, "Partida cargada exitosamente.");
                        } catch (BadDopoException e) {
                            JOptionPane.showMessageDialog(this, "Error al cargar: " + e.getMessage());
                        }
                        return;
                    }
                }
            }
        }
        repaint();
    }
}