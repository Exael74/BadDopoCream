package domain.service;

import domain.entity.*;
import domain.state.GameState;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lógica central del juego.
 * Maneja movimiento, acciones, actualización de entidades y detección de victoria.
 */
public class GameLogic {

    private GameState gameState;
    private CollisionDetector collisionDetector;
    private Random random;
    private AIController aiController;

    /**
     * Constructor de la lógica del juego.
     *
     * @param gameState Estado del juego
     */
    public GameLogic(GameState gameState) {
        this.gameState = gameState;
        this.collisionDetector = new CollisionDetector(gameState);
        this.random = new Random();

        // Crear AIController solo en modo Machine vs Machine
        if (gameState.getNumberOfPlayers() == 0) {
            this.aiController = new AIController(gameState, this);
            System.out.println("✓ AIController inicializado para modo Machine vs Machine");
        }
    }

    // ==================== DECISIÓN DE ACCIONES ====================

    /**
     * Ejecuta la acción del jugador al presionar SPACE.
     * Decide automáticamente: si hay hielo adelante → kick, sino → sneeze
     */
    public List<Point> performSpaceAction() {
        Player player = gameState.getPlayer();

        // No hacer nada si el jugador está ocupado
        if (player.isBusy()) {
            return new ArrayList<>();
        }

        Point playerPos = player.getPosition();
        Direction direction = player.getFacingDirection();

        // Calcular posición adelante
        Point checkPos = new Point(
                playerPos.x + direction.getDeltaX(),
                playerPos.y + direction.getDeltaY()
        );

        // Decidir: ¿hay hielo adelante?
        if (collisionDetector.isValidPosition(checkPos) && collisionDetector.hasIceAt(checkPos)) {
            // Acción: KICK (romper hielo)
            return performIceKick();
        } else {
            // Acción: SNEEZE (crear hielo)
            return performIceSneeze();
        }
    }

    /**
     * Verifica si Player 2 puede romper hielo (solo CALAMAR).
     */
    public boolean canPlayer2BreakIce() {
        Enemy controlledEnemy = getPlayerControlledEnemy();
        if (controlledEnemy == null || !controlledEnemy.isActive()) {
            return false;
        }
        return controlledEnemy.getType().canBreakIce();
    }

    /**
     * Intenta romper hielo con Player 2 (solo si es CALAMAR).
     */
    public Point performPlayer2IceBreak() {
        if (!canPlayer2BreakIce()) {
            return null;
        }

        Enemy controlledEnemy = getPlayerControlledEnemy();
        Point enemyPos = controlledEnemy.getPosition();
        Direction direction = controlledEnemy.getCurrentDirection();

        Point checkPos = new Point(
                enemyPos.x + direction.getDeltaX(),
                enemyPos.y + direction.getDeltaY()
        );

        if (!collisionDetector.isValidPosition(checkPos) || !collisionDetector.hasIceAt(checkPos)) {
            return null;
        }

        // Romper UN SOLO bloque de hielo
        IceBlock ice = collisionDetector.getIceAt(checkPos);
        if (ice != null) {
            ice.startBreaking();
            controlledEnemy.startBreakIce();
            System.out.println("✓ Calamar (P2/IA) rompió hielo en " + checkPos);
            return checkPos;
        }

        return null;
    }

    // ==================== MOVIMIENTO DEL JUGADOR ====================

    /**
     * Mueve al jugador en la dirección especificada.
     */
    public void movePlayer(Direction direction) {
        if (gameState.isGameOver() || gameState.isVictory() || gameState.getPlayer().isBusy()) {
            return;
        }

        Player player = gameState.getPlayer();
        Point oldPosition = player.getPosition();

        player.move(direction);

        if (!collisionDetector.isValidPosition(player.getPosition()) ||
                collisionDetector.isPositionBlocked(player.getPosition())) {
            player.setPosition(oldPosition);
            player.stopMoving();
            return;
        }

        collisionDetector.checkCollisions();
    }

    /**
     * Detiene el movimiento del jugador.
     */
    public void stopPlayer() {
        gameState.getPlayer().stopMoving();
    }

    // ==================== MOVIMIENTO DEL ENEMIGO CONTROLADO ====================

    /**
     * Mueve al enemigo controlado por el jugador.
     */
    public void movePlayerControlledEnemy(Direction direction) {
        if (gameState.isGameOver()) return;

        Enemy controlledEnemy = getPlayerControlledEnemy();
        if (controlledEnemy == null || !controlledEnemy.isActive()) return;

        controlledEnemy.setDirection(direction);
        Point nextPos = controlledEnemy.getNextPosition();

        if (collisionDetector.isValidPosition(nextPos) &&
                !collisionDetector.hasIceAt(nextPos) &&
                !collisionDetector.hasOtherEnemyAt(nextPos, controlledEnemy)) {

            controlledEnemy.move(nextPos);
            collisionDetector.checkCollisions();

            if (gameState.isGameOver()) {
                System.out.println("✗ P2 (enemigo controlado) tocó a P1 - Game Over");
            }
        }
    }

    // ==================== ACCIONES DE HIELO ====================

    /**
     * Crea una línea de hielo desde el jugador.
     */
    public List<Point> performIceSneeze() {
        Player player = gameState.getPlayer();
        if (player.isBusy()) {
            return new ArrayList<>();
        }

        player.startSneeze();

        List<Point> icePositions = new ArrayList<>();
        Point playerPos = player.getPosition();
        Direction direction = player.getFacingDirection();

        Point current = new Point(
                playerPos.x + direction.getDeltaX(),
                playerPos.y + direction.getDeltaY()
        );

        while (collisionDetector.isValidPosition(current)) {
            if (collisionDetector.hasEnemyAt(current) || collisionDetector.hasIceAt(current)) {
                break;
            }

            gameState.addIceBlock(new IceBlock(current));
            icePositions.add(new Point(current));

            current.x += direction.getDeltaX();
            current.y += direction.getDeltaY();
        }

        return icePositions;
    }

    /**
     * Rompe una línea de hielo desde el jugador.
     */
    public List<Point> performIceKick() {
        Player player = gameState.getPlayer();
        if (player.isBusy()) {
            return new ArrayList<>();
        }

        Point playerPos = player.getPosition();
        Direction direction = player.getFacingDirection();

        Point checkPos = new Point(
                playerPos.x + direction.getDeltaX(),
                playerPos.y + direction.getDeltaY()
        );

        if (!collisionDetector.isValidPosition(checkPos) || !collisionDetector.hasIceAt(checkPos)) {
            return new ArrayList<>();
        }

        player.startKick();

        List<Point> brokenIcePositions = new ArrayList<>();
        Point current = new Point(checkPos);

        while (collisionDetector.isValidPosition(current) && collisionDetector.hasIceAt(current)) {
            IceBlock ice = collisionDetector.getIceAt(current);
            if (ice != null && !ice.isPermanent()) {
                ice.startBreaking();
                brokenIcePositions.add(new Point(current));
            }
            current.x += direction.getDeltaX();
            current.y += direction.getDeltaY();
        }

        return brokenIcePositions;
    }

    // ==================== ACTUALIZACIÓN DE ENTIDADES ====================

    /**
     * Actualiza todos los enemigos del juego.
     */
    public void updateEnemies(int deltaTime) {
        if (gameState.isVictory() || gameState.isGameOver()) return;

        Point playerPosition = gameState.getPlayer().getPosition();
        int currentLevel = gameState.getLevel();
        int numberOfPlayers = gameState.getNumberOfPlayers();

        for (Enemy enemy : gameState.getEnemies()) {
            if (!enemy.isActive()) continue;

            enemy.update(deltaTime);

            // Solo mover automáticamente si NO es controlado por jugador
            if (!enemy.isControlledByPlayer() && enemy.shouldMove()) {
                processEnemyMovement(enemy, playerPosition, currentLevel, numberOfPlayers);
            }
        }

        if (!gameState.isGameOver()) {
            collisionDetector.checkCollisions();
        }
    }

    /**
     * Procesa el movimiento de un enemigo específico.
     */
    private void processEnemyMovement(Enemy enemy, Point playerPosition, int currentLevel, int numberOfPlayers) {
        EnemyType type = enemy.getType();

        // Comportamiento especial en modo 1 jugador
        if (numberOfPlayers == 1 && type.shouldChasePlayer()) {
            if (currentLevel == 2 && type == EnemyType.MACETA) {
                processMacetaMovement(enemy, playerPosition);
            } else if (currentLevel == 3 && type == EnemyType.CALAMAR) {
                processCalamarMovement(enemy, playerPosition);
            } else {
                processDefaultMovement(enemy);
            }
        } else {
            processDefaultMovement(enemy);
        }
    }

    /**
     * Procesa movimiento de Maceta (persecución con anti-atasco).
     */
    private void processMacetaMovement(Enemy enemy, Point playerPosition) {
        enemy.chasePlayer(playerPosition);
        Point nextPos = enemy.getNextPosition();

        if (collisionDetector.isValidPosition(nextPos) &&
                !collisionDetector.hasIceAt(nextPos) &&
                !collisionDetector.hasOtherEnemyAt(nextPos, enemy)) {
            enemy.move(nextPos);
        } else {
            boolean moved = false;
            int attempts = 0;

            while (!moved && attempts < 4) {
                enemy.chasePlayer(playerPosition);
                nextPos = enemy.getNextPosition();

                if (collisionDetector.isValidPosition(nextPos) &&
                        !collisionDetector.hasIceAt(nextPos) &&
                        !collisionDetector.hasOtherEnemyAt(nextPos, enemy)) {
                    enemy.move(nextPos);
                    enemy.resetStuckCounter();
                    moved = true;
                } else {
                    enemy.changeDirection();
                    attempts++;
                }
            }

            if (!moved) {
                enemy.resetStuckCounter();
            }
        }
    }

    /**
     * Procesa movimiento de Calamar (persecución + romper hielo).
     */
    private void processCalamarMovement(Enemy enemy, Point playerPosition) {
        enemy.chasePlayer(playerPosition);
        Point nextPos = enemy.getNextPosition();

        // Si hay hielo en el camino, romperlo
        if (collisionDetector.isValidPosition(nextPos) && collisionDetector.hasIceAt(nextPos)) {
            IceBlock ice = collisionDetector.getIceAt(nextPos);
            if (ice != null) {
                ice.startBreaking();
                enemy.startBreakIce();
                System.out.println("✓ Calamar IA rompió hielo automáticamente");
            }
        }
        // Si no hay hielo, moverse normalmente
        else if (collisionDetector.isValidPosition(nextPos) &&
                !collisionDetector.hasOtherEnemyAt(nextPos, enemy)) {
            enemy.move(nextPos);
        } else {
            enemy.changeDirection();
        }
    }

    /**
     * Procesa movimiento por defecto (cambiar dirección si está bloqueado).
     */
    private void processDefaultMovement(Enemy enemy) {
        Point nextPos = enemy.getNextPosition();

        if (collisionDetector.isValidPosition(nextPos) &&
                !collisionDetector.hasIceAt(nextPos) &&
                !collisionDetector.hasOtherEnemyAt(nextPos, enemy)) {
            enemy.move(nextPos);
        } else {
            enemy.changeDirection();
        }
    }

    /**
     * Actualiza todas las frutas del juego.
     */
    public void updateFruits(int deltaTime) {
        for (Fruit fruit : gameState.getFruits()) {
            if (fruit.isCollected()) continue;

            fruit.update(deltaTime);

            if (fruit.shouldMove()) {
                // Cerezas se teletransportan a cualquier casilla vacía
                if (fruit.canTeleport()) {
                    Point newPos = findRandomEmptyPosition();
                    if (newPos != null) {
                        fruit.move(newPos);
                        System.out.println("✓ Cereza se teletransportó a " + newPos);
                    }
                }
                // Piñas se mueven a casilla adyacente
                else if (fruit.canMove()) {
                    Point newPos = fruit.getRandomAdjacentPosition();

                    if (collisionDetector.isValidPosition(newPos) &&
                            !collisionDetector.hasIceAt(newPos) &&
                            !collisionDetector.hasEnemyAt(newPos) &&
                            !collisionDetector.hasFruitAt(newPos) &&
                            !collisionDetector.isPlayerAt(newPos)) {
                        fruit.move(newPos);
                    }
                }
            }
        }
    }

    /**
     * Actualiza todos los bloques de hielo del juego.
     */
    public void updateIceBlocks(int deltaTime) {
        List<IceBlock> toRemove = new ArrayList<>();

        for (IceBlock ice : gameState.getIceBlocks()) {
            ice.update(deltaTime);
            if (ice.isFullyBroken()) {
                toRemove.add(ice);
            }
        }

        for (IceBlock ice : toRemove) {
            gameState.removeIceBlock(ice);
        }
    }

    // ==================== ACTUALIZACIÓN PRINCIPAL ====================

    /**
     * Actualiza todo el estado del juego.
     */
    public void update(int deltaTime) {
        Player player = gameState.getPlayer();

        player.update(deltaTime);

        // Actualizar temporizador
        gameState.updateTime(deltaTime);

        // Actualizar IA si está activa
        if (aiController != null && !gameState.isGameOver()) {
            aiController.updateAI(deltaTime);
        }

        if (!gameState.isGameOver() || player.isDying()) {
            if (!gameState.isGameOver()) {
                updateEnemies(deltaTime);
                updateFruits(deltaTime);
            }
            updateIceBlocks(deltaTime);

            if (!player.isDying() && !gameState.isVictory() && !gameState.isGameOver()) {
                checkVictory();
            }
        }
    }

    // ==================== DETECCIÓN DE VICTORIA ====================

    /**
     * Verifica si el jugador ha ganado (todas las frutas recolectadas).
     */
    private void checkVictory() {
        boolean allFruitsCollected = true;
        for (Fruit fruit : gameState.getFruits()) {
            if (!fruit.isCollected()) {
                allFruitsCollected = false;
                break;
            }
        }

        if (allFruitsCollected) {
            gameState.setVictory(true);
            gameState.getPlayer().startCelebration();
        }
    }

    // ==================== UTILIDADES ====================

    /**
     * Encuentra una posición vacía aleatoria en el grid.
     */
    private Point findRandomEmptyPosition() {
        int gridSize = GameState.getGridSize();
        List<Point> emptyPositions = new ArrayList<>();

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                Point pos = new Point(x, y);
                if (!collisionDetector.isPlayerAt(pos) &&
                        !collisionDetector.hasEnemyAt(pos) &&
                        !collisionDetector.hasFruitAt(pos) &&
                        !collisionDetector.hasIceAt(pos)) {
                    emptyPositions.add(pos);
                }
            }
        }

        if (emptyPositions.isEmpty()) return null;

        return emptyPositions.get(random.nextInt(emptyPositions.size()));
    }

    /**
     * Obtiene el enemigo controlado por el jugador.
     */
    private Enemy getPlayerControlledEnemy() {
        for (Enemy enemy : gameState.getEnemies()) {
            if (enemy.isControlledByPlayer()) {
                return enemy;
            }
        }
        return null;
    }
}