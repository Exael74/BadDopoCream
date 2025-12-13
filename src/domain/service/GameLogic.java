package domain.service;

import domain.entity.enemy.Enemy;
import domain.entity.*;

import domain.state.GameState;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Lógica central del juego.
 * Maneja movimiento, acciones, actualización de entidades y detección de
 * victoria.
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

        // Crear AIController solo en modo Machine vs Machine O Player vs Machine
        if (gameState.getNumberOfPlayers() == 0
                || (gameState.getNumberOfPlayers() == 2 && gameState.isP2CPU())) {
            this.aiController = new AIController(gameState, this);
            domain.BadDopoLogger.logInfo("✓ AIController inicializado para modo IA (0 Players o P1 vs CPU)");
        }
    }

    // ==================== DECISIÓN DE ACCIONES ====================

    /**
     * Ejecuta la acción del jugador 1 al presionar SPACE.
     * Decide automáticamente: si hay hielo adelante → kick, sino → sneeze
     */
    public List<Point> performSpaceAction() {
        return performAction(gameState.getPlayer());
    }

    /**
     * Ejecuta la acción del Jugador 2 (M).
     */
    public List<Point> performActionPlayer2() {
        return performAction(gameState.getPlayer2());
    }

    /**
     * Lógica común para realizar acción (Sneeze/Kick) para cualquier jugador.
     */
    private List<Point> performAction(Player player) {
        if (player == null || player.isBusy()) {
            return new ArrayList<>();
        }

        Point playerPos = player.getPosition();
        Direction direction = player.getFacingDirection();

        Point checkPos = new Point(
                playerPos.x + direction.getDeltaX(),
                playerPos.y + direction.getDeltaY());

        if (collisionDetector.isValidPosition(checkPos) && collisionDetector.hasIceAt(checkPos)) {
            return performIceKick(player);
        } else {
            return performIceSneeze(player);
        }
    }

    // ==================== MOVIMIENTO DEL JUGADOR ====================

    /**
     * Mueve al jugador 1 en la dirección especificada.
     */
    public void movePlayer(Direction direction) {
        movePlayerEntity(gameState.getPlayer(), direction);
    }

    /**
     * Mueve al jugador 2 en la dirección especificada.
     */
    public void movePlayer2(Direction direction) {
        movePlayerEntity(gameState.getPlayer2(), direction);
    }

    /**
     * Mueve al enemigo controlado por el jugador (o IA) en la dirección
     * especificada.
     */
    public void movePlayerControlledEnemy(Direction direction) {
        for (Enemy enemy : gameState.getEnemies()) {
            if (enemy.isControlledByPlayer()) {
                Point oldPosition = enemy.getPosition();

                // Calcular nueva posición basada en la dirección
                Point newPosition = new Point(oldPosition);
                newPosition.x += direction.getDeltaX();
                newPosition.y += direction.getDeltaY();

                enemy.move(newPosition);
                enemy.setDirection(direction); // Actualizar dirección visual

                if (!collisionDetector.isValidPosition(enemy.getPosition()) ||
                        collisionDetector.isPositionBlocked(enemy.getPosition())) {
                    enemy.setPosition(oldPosition);
                } else {
                    // If moved successfully? No, user specified "player moves a tile".
                    // This is Player CONTROLLED Enemy (Machine vs Machine).
                    // Ideally, any turn-based step should trigger it.
                }
                return;
            }
        }
    }

    /**
     * Lógica común de movimiento para cualquier jugador.
     */
    private void movePlayerEntity(Player player, Direction direction) {
        if (gameState.isGameOver() || gameState.isVictory() || player == null || player.isBusy()) {
            return;
        }

        Point oldPosition = player.getPosition();
        player.move(direction);

        if (!collisionDetector.isValidPosition(player.getPosition()) ||
                collisionDetector.isPositionBlocked(player.getPosition())) {
            player.setPosition(oldPosition);
            player.stopMoving();
            return;
        }

        // Check for hot tile collision - kills player -> REMOVED per user request
        // if (isHotTile(player.getPosition())) {
        // player.die();
        // return;
        // }

        // IMPROVED: Check fruit collision immediately after player lands on tile
        // This ensures collection happens BEFORE fruits have a chance to move away
        checkPlayerFruitCollision(player);

        collisionDetector.checkCollisions();

        // After successful move, trigger turn-based fruit movement (Pineapples)
        moveFruitsAfterPlayerTurn();
    }

    /**
     * Detiene el movimiento del jugador 1.
     */
    public void stopPlayer() {
        gameState.getPlayer().stopMoving();
    }

    /**
     * Detiene el movimiento del jugador 2.
     */
    public void stopPlayer2() {
        if (gameState.getPlayer2() != null) {
            gameState.getPlayer2().stopMoving();
        }
    }

    // ==================== ACCIONES DE HIELO ====================

    /**
     * Crea una línea de hielo desde el jugador.
     */
    /**
     * Helper to trace a line from a starting point in a direction.
     * 
     * @param start             Starting position (exclusive)
     * @param dir               Direction to trace
     * @param continueCondition Predicate that returns true if tracing should
     *                          continue
     * @param action            Action to perform on each valid step
     * @return List of processed points
     */
    private List<Point> traceRay(Point start, Direction dir, java.util.function.Predicate<Point> continueCondition,
            java.util.function.Consumer<Point> action) {
        List<Point> processedPoints = new ArrayList<>();
        Point current = new Point(start.x + dir.getDeltaX(), start.y + dir.getDeltaY());

        while (continueCondition.test(current)) {
            processedPoints.add(new Point(current));
            if (action != null) {
                action.accept(current);
            }
            current.x += dir.getDeltaX();
            current.y += dir.getDeltaY();
        }
        return processedPoints;
    }

    public List<Point> performIceSneeze(Player player) {
        if (player.isBusy())
            return new ArrayList<>();
        player.startSneeze();
        Point playerPos = player.getPosition();
        Direction direction = player.getFacingDirection();

        return traceRay(playerPos, direction,
                pos -> {
                    // Continue if position is valid and NOT checking collision with obstacles
                    // Wait, Sneeze STOPS on obstacles.
                    // So condition is: Valid AND Not Blocked.
                    boolean valid = collisionDetector.isValidPosition(pos);
                    if (!valid)
                        return false;

                    boolean blocked = collisionDetector.hasEnemyAt(pos) || collisionDetector.hasIceAt(pos) ||
                            collisionDetector.hasIgluAt(pos) || collisionDetector.hasUnbreakableBlockAt(pos);
                    return !blocked;
                },
                pos -> {
                    if (!isHotTile(pos)) {
                        gameState.addIceBlock(new IceBlock(pos));
                    }
                });
    }

    public List<Point> performIceSneeze() {
        return performIceSneeze(gameState.getPlayer());
    }

    public List<Point> performIceKick(Player player) {
        if (player.isBusy())
            return new ArrayList<>();

        Point playerPos = player.getPosition();
        Direction direction = player.getFacingDirection();
        Point firstCheck = new Point(playerPos.x + direction.getDeltaX(), playerPos.y + direction.getDeltaY());

        // Kick requires immediate ice to start
        if (!collisionDetector.isValidPosition(firstCheck) || !collisionDetector.hasIceAt(firstCheck)) {
            return new ArrayList<>();
        }

        player.startKick();

        return traceRay(playerPos, direction,
                pos -> collisionDetector.isValidPosition(pos) && collisionDetector.hasIceAt(pos),
                pos -> {
                    IceBlock ice = collisionDetector.getIceAt(pos);
                    if (ice != null && !ice.isPermanent()) {
                        ice.startBreaking();
                    }
                });
    }

    public List<Point> performIceKick() {
        return performIceKick(gameState.getPlayer());
    }

    /**
     * Realiza la acción de romper hielo para el enemigo controlado (Player 2 / IA).
     */
    public Point performPlayer2IceBreak() {
        // Buscar el enemigo controlado
        Enemy controlledEnemy = null;
        for (Enemy enemy : gameState.getEnemies()) {
            if (enemy.isControlledByPlayer()) {
                controlledEnemy = enemy;
                break;
            }
        }

        if (controlledEnemy == null || !controlledEnemy.canBreakIce()) {
            return null;
        }

        Point enemyPos = controlledEnemy.getPosition();
        // Romper hielo en las 4 direcciones adyacentes
        for (Direction dir : Direction.values()) {
            Point checkPos = new Point(
                    enemyPos.x + dir.getDeltaX(),
                    enemyPos.y + dir.getDeltaY());

            if (collisionDetector.isValidPosition(checkPos) && collisionDetector.hasIceAt(checkPos)) {
                IceBlock ice = collisionDetector.getIceAt(checkPos);
                if (ice != null) {
                    ice.startBreaking();
                    return checkPos; // Retorna el punto roto
                }
            }
        }
        return null;
    }

    // ==================== ACTUALIZACIÓN DE ENTIDADES ====================

    /**
     * Actualiza todos los enemigos del juego.
     */
    public void updateEnemies(int deltaTime) {
        if (gameState.isVictory() || gameState.isGameOver())
            return;

        // Enemies target the closest player
        Point p1Pos = gameState.getPlayer().getPosition();
        Point p2Pos = (gameState.getPlayer2() != null) ? gameState.getPlayer2().getPosition() : null;

        int currentLevel = gameState.getLevel();
        int numberOfPlayers = gameState.getNumberOfPlayers();

        for (Enemy enemy : gameState.getEnemies()) {
            if (!enemy.isActive())
                continue;

            enemy.update(deltaTime);

            if (enemy.shouldMove()) {
                // Determine target based on distance AND liveness
                Point targetPos = p1Pos;
                boolean p1Alive = gameState.getPlayer().isAlive();
                boolean p2Alive = (gameState.getPlayer2() != null && gameState.getPlayer2().isAlive());

                if (p1Alive && !p2Alive) {
                    targetPos = p1Pos;
                } else if (!p1Alive && p2Alive) {
                    targetPos = p2Pos;
                } else if (p1Alive && p2Alive) {
                    // Both alive, pick closest
                    if (p2Pos != null) {
                        double dist1 = enemy.getPosition().distance(p1Pos.x, p1Pos.y);
                        double dist2 = enemy.getPosition().distance(p2Pos.x, p2Pos.y);
                        if (dist2 < dist1) {
                            targetPos = p2Pos;
                        }
                    }
                } else {
                    // Both dead, keep default (p1Pos) or stop?
                    // Game should be over, so it doesn't matter much, but let's default to P1
                    targetPos = p1Pos;
                }

                processEnemyMovement(enemy, targetPos, currentLevel, numberOfPlayers);
            }
        }

        if (!gameState.isGameOver()) {
            collisionDetector.checkCollisions();
            // Check fruit collisions separately as we need specific logic for
            // lethality/animations now
            checkPlayerFruitCollision(gameState.getPlayer());
            if (gameState.getPlayer2() != null) {
                checkPlayerFruitCollision(gameState.getPlayer2());
            }
        }
    }

    /**
     * Procesa el movimiento de un enemigo específico.
     */
    private void processEnemyMovement(domain.entity.enemy.Enemy enemy, Point targetPosition, int currentLevel,
            int numberOfPlayers) {
        // Delegar la lógica de movimiento a la propia entidad.
        // La entidad ya contiene su comportamiento (Strategy) y tipo.
        if (enemy.shouldChasePlayer()) {
            enemy.updateMovement(targetPosition, collisionDetector);
        } else {
            // Comportamiento por defecto (Troll) también se maneja en updateMovement
            enemy.updateMovement(targetPosition, collisionDetector);
        }
    }

    /**
     * Actualiza todas las frutas del juego.
     */
    private void checkPlayerFruitCollision(Player player) {
        Iterator<Fruit> iterator = gameState.getFruits().iterator();
        while (iterator.hasNext()) {
            Fruit fruit = iterator.next();
            if (fruit.isActive() && !fruit.isCollected() && fruit.getPosition().equals(player.getPosition())) {

                // If player is already dead/dying, ignore lethal collision to prevent infinite
                // death loop
                if (fruit.isLethal()) {
                    if (!player.isDying() && player.isAlive()) {
                        player.die();
                    }
                    return;
                }

                // Normal Collection
                fruit.collect(); // Sets state to COLLECTED
                gameState.addScore(fruit.getType().getScore()); // Score is in GameState usually? Or Player?
                // Checking Player.java will confirm where score is.
                // If Player has no addScore, GameState likely holds it.
                // Let's assume GameState.addScore based on previous logic view
                // (gameState.getScore()).
                // Wait, logic earlier said player.addScore().

                // Do NOT remove immediately.
                // iterator.remove();
            }
        }
    }

    /**
     * Actualiza todas las frutas del juego.
     */
    private void updateFruits(int deltaTime) {
        Iterator<Fruit> iterator = gameState.getFruits().iterator();
        while (iterator.hasNext()) {
            Fruit fruit = iterator.next();
            FruitState previousState = fruit.getState();
            fruit.update(deltaTime); // Update state/timers

            // Detect Teleport Trigger (Transition from TELEPORT_OUT to TELEPORT_IN)
            if (fruit.getType() == FruitType.CEREZA &&
                    previousState == FruitState.TELEPORT_OUT &&
                    fruit.getState() == FruitState.TELEPORT_IN) {

                Point newPos = findRandomEmptyPosition();
                if (newPos != null) {
                    fruit.move(newPos);
                    domain.BadDopoLogger.logInfo("✓ Cereza teletransportada a " + newPos);
                }
            }

            if (!fruit.isActive()) {
                iterator.remove();
                continue;
            }

            if (fruit.isCollected()) {
                // If collected, we just wait for it to become inactive (handled inside
                // Fruit.update eventually? No, I need to add that logic to Fruit or here)
                // Actually, let's make Fruit handle its own inactivation after animation.
                continue;
            }

            if (fruit.isCollected()) {
                // If collected, we just wait for it to become inactive
                continue;
            }

            // PINEAPPLE MOVEMENT WAS REMOVED FROM HERE
            // Moved to moveFruitsAfterPlayerTurn() to sync with player movement
            // as requested by user ("deben moverse cada vez que el jugador se mueve")
        }
    }

    /**
     * Mueve las frutas que reaccionan al turno del jugador (PIÑA).
     */
    private void moveFruitsAfterPlayerTurn() {
        for (Fruit fruit : gameState.getFruits()) {
            if (fruit.isActive() && !fruit.isCollected() && fruit.getType() == FruitType.PIÑA
                    && fruit.getState() == FruitState.IDLE) {
                Point newPos = fruit.getRandomAdjacentPosition();

                if (collisionDetector.isPositionFree(newPos) && !isHotTile(newPos)) {
                    fruit.move(newPos);
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
        Player player2 = gameState.getPlayer2();

        player.update(deltaTime);
        if (player2 != null) {
            player2.update(deltaTime);
        }

        // Actualizar temporizador
        gameState.updateTime(deltaTime);

        // Check for wave completion
        boolean allCollected = true;
        for (Fruit fruit : gameState.getFruits()) {
            if (!fruit.isCollected()) {
                allCollected = false;
                break;
            }
        }

        if (allCollected && !gameState.getPendingFruitWaves().isEmpty()) {
            List<Fruit> nextWave = gameState.getPendingFruitWaves().remove(0);
            for (Fruit fruit : nextWave) {
                gameState.addFruit(fruit);
            }
            domain.BadDopoLogger.logInfo("✓ Next wave spawned!");
        }

        // Actualizar IA si está activa
        if (aiController != null && !gameState.isGameOver()) {
            aiController.updateAI(deltaTime);
        }

        boolean p1Dying = player.isDying();
        boolean p2Dying = (player2 != null && player2.isDying());

        if (!gameState.isGameOver() || p1Dying || p2Dying) {
            if (!gameState.isGameOver()) {
                updateEnemies(deltaTime);
                updateFruits(deltaTime);
            }
            updateIceBlocks(deltaTime);

            // Check if ANY player has finished dying (now dead)
            // If death animation finished, trigger Game Over logic
            boolean p1Dead = (player != null) && !player.isAlive() && !player.isDying();
            boolean p2Dead = (player2 != null) && !player2.isAlive() && !player2.isDying();

            boolean shouldEndGame;
            if (gameState.getNumberOfPlayers() == 2 || gameState.getNumberOfPlayers() == 0) {
                // For 2 Players or MvM, BOTH must be dead
                shouldEndGame = p1Dead && p2Dead;
            } else {
                // For 1 Player, just P1 needs to be dead
                shouldEndGame = p1Dead;
            }

            if (shouldEndGame && !gameState.isGameOver()) {
                gameState.setGameOver(true);
            }

            if (!p1Dying && !p2Dying && !gameState.isVictory() && !gameState.isGameOver()) {
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

        // Fix: Check if game has actually started (time remaining < initial limit)
        // or if we simply check if ANY fruits were ever added.
        // Better: Check initialization via time remaining distinct from max
        // OR simply ensure we don't trigger on frame 0.
        // Assuming Time Limit is 180000ms.
        boolean gameStarted = gameState.getTimeRemaining() < 180000;

        if (gameStarted && allFruitsCollected && gameState.getPendingFruitWaves().isEmpty()) {
            gameState.setVictory(true);
            gameState.getPlayer().startCelebration();
            if (gameState.getPlayer2() != null) {
                gameState.getPlayer2().startCelebration();
            }

            // Determine winner based on score
            if (gameState.getNumberOfPlayers() == 2) {
                int score1 = gameState.getScore();
                int score2 = gameState.getScorePlayer2();
                domain.BadDopoLogger.logInfo("Victory! P1: " + score1 + " - P2: " + score2);
            }
        }
    }

    // ==================== UTILIDADES ====================

    /**
     * Encuentra una posición vacía aleatoria en el grid.
     */
    public Point findRandomEmptyPosition() {
        int gridSize = GameState.getGridSize();
        List<Point> emptyPositions = new ArrayList<>();

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                Point pos = new Point(x, y);
                if (collisionDetector.isPositionFree(pos) && !isHotTile(pos)) {
                    emptyPositions.add(pos);
                }
            }
        }

        if (emptyPositions.isEmpty())
            return null;

        return emptyPositions.get(random.nextInt(emptyPositions.size()));
    }

    // ==================== HOT TILE VALIDATION ====================

    /**
     * Verifica si una posición contiene una baldosa caliente.
     *
     * @param position Posición a verificar
     * @return true si hay una baldosa caliente en esa posición
     */
    private boolean isHotTile(Point position) {
        for (HotTile tile : gameState.getHotTiles()) {
            if (tile.getPosition().equals(position)) {
                return true;
            }
        }
        return false;
    }
}
