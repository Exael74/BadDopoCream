package domain.service;

import domain.entity.*;
import domain.state.GameState;
import java.awt.Point;
import java.util.ArrayList;
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

        // Crear AIController solo en modo Machine vs Machine
        if (gameState.getNumberOfPlayers() == 0) {
            this.aiController = new AIController(gameState, this);
            System.out.println("✓ AIController inicializado para modo Machine vs Machine");
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

        // Check for hot tile collision - kills player
        if (isHotTile(player.getPosition())) {
            player.die();
            return;
        }

        collisionDetector.checkCollisions();
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
    public List<Point> performIceSneeze(Player player) {
        if (player.isBusy()) {
            return new ArrayList<>();
        }

        player.startSneeze();

        List<Point> icePositions = new ArrayList<>();
        Point playerPos = player.getPosition();
        Direction direction = player.getFacingDirection();

        Point current = new Point(
                playerPos.x + direction.getDeltaX(),
                playerPos.y + direction.getDeltaY());

        while (collisionDetector.isValidPosition(current)) {
            if (collisionDetector.hasEnemyAt(current) || collisionDetector.hasIceAt(current) ||
                    collisionDetector.hasIgluAt(current) || collisionDetector.hasUnbreakableBlockAt(current)) {
                break;
            }

            // Cannot place ice on hot tiles
            if (isHotTile(current)) {
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
     * Sobrecarga para IA: Realiza sneeze con el jugador principal.
     */
    public List<Point> performIceSneeze() {
        return performIceSneeze(gameState.getPlayer());
    }

    /**
     * Rompe una línea de hielo desde el jugador.
     */
    public List<Point> performIceKick(Player player) {
        if (player.isBusy()) {
            return new ArrayList<>();
        }

        Point playerPos = player.getPosition();
        Direction direction = player.getFacingDirection();

        Point checkPos = new Point(
                playerPos.x + direction.getDeltaX(),
                playerPos.y + direction.getDeltaY());

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

    /**
     * Sobrecarga para IA: Realiza kick con el jugador principal.
     */
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

        if (controlledEnemy == null || !controlledEnemy.getType().canBreakIce()) {
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
                // Determine target based on distance
                Point targetPos = p1Pos;
                if (p2Pos != null) {
                    double dist1 = enemy.getPosition().distance(p1Pos.x, p1Pos.y);
                    double dist2 = enemy.getPosition().distance(p2Pos.x, p2Pos.y);
                    if (dist2 < dist1) {
                        targetPos = p2Pos;
                    }
                }

                processEnemyMovement(enemy, targetPos, currentLevel, numberOfPlayers);
            }
        }

        if (!gameState.isGameOver()) {
            collisionDetector.checkCollisions();
        }
    }

    /**
     * Procesa el movimiento de un enemigo específico.
     */
    private void processEnemyMovement(Enemy enemy, Point targetPosition, int currentLevel, int numberOfPlayers) {
        EnemyType type = enemy.getType();

        // Logic adapted for PvP: Enemies chase the closest player if applicable
        if (type.shouldChasePlayer()) {
            if (enemy.getType() == EnemyType.MACETA) {
                processMacetaMovement(enemy, targetPosition);
            } else if (enemy.getType() == EnemyType.CALAMAR) {
                processCalamarMovement(enemy, targetPosition);
            } else if (enemy.getType() == EnemyType.NARVAL) {
                processNarvalMovement(enemy, targetPosition);
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
    /**
     * Procesa movimiento de Maceta (persecución con anti-atasco).
     */
    private void processMacetaMovement(Enemy enemy, Point targetPosition) {
        enemy.chasePlayer(targetPosition);
        Point nextPos = enemy.getNextPosition();

        if (collisionDetector.isValidPosition(nextPos) &&
                !collisionDetector.isPositionBlocked(nextPos) &&
                !collisionDetector.hasOtherEnemyAt(nextPos, enemy)) {
            enemy.move(nextPos);
        } else {
            boolean moved = false;
            int attempts = 0;

            while (!moved && attempts < 4) {
                enemy.chasePlayer(targetPosition);
                nextPos = enemy.getNextPosition();

                if (collisionDetector.isValidPosition(nextPos) &&
                        !collisionDetector.isPositionBlocked(nextPos) &&
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
    private void processCalamarMovement(Enemy enemy, Point targetPosition) {
        enemy.chasePlayer(targetPosition);
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
        // Si no hay hielo, moverse normalmente (pero respetar Iglú y Bloques
        // Irrompibles)
        else if (collisionDetector.isValidPosition(nextPos) &&
                !collisionDetector.hasIgluAt(nextPos) &&
                !collisionDetector.hasUnbreakableBlockAt(nextPos) &&
                !collisionDetector.hasOtherEnemyAt(nextPos, enemy)) {
            enemy.move(nextPos);
        } else {
            enemy.changeDirection();
        }
    }

    /**
     * Procesa movimiento de Narval (persecución + taladrar/romper hielo).
     */
    /**
     * Procesa movimiento de Narval (Wander + Charge).
     * Behavior:
     * 1. If Charging (isDrilling):
     * - Move forward.
     * - If Ice -> Break instantly.
     * - If Wall/Iglu/Border -> Stop Charging.
     * 2. If Not Charging:
     * - Check Line of Sight to Player (Horiz/Vert).
     * - If aligned -> Start Charging in that direction.
     * - Else -> Random Wander.
     */
    private void processNarvalMovement(Enemy narval, Point targetPosition) {
        // Heartbeat log to confirm method is running
        // System.out.println("Processing Narval at " + narval.getPosition());

        // 1. Detection (Always active if not drilling)
        if (!narval.isDrilling()) {
            // System.out.println("[DEBUG] Checking Narval: " + narval.getPosition() + " vs
            // Player: " + targetPosition);

            Direction chargeDir = getPlayerDirectionIfSeeing(narval.getPosition(), targetPosition);
            if (chargeDir != null) {
                // System.out.println("[NARVAL] Player spotted at " + chargeDir + "! engaging
                // Drill Mode.");
                narval.setDirection(chargeDir);
                narval.startDrilling();
                // Force immediate movement if we want instant reaction,
                // but changing to isDrilling will lower the threshold in shouldMove()
                // causing it to trigger very soon (likely this frame or next).
            }
        }

        // 2. Movement Check
        if (!narval.shouldMove())
            return;

        // 3. Execution
        if (narval.isDrilling()) {
            Point nextPos = narval.getNextPosition();

            // Check bounds/obstacles
            if (!collisionDetector.isValidPosition(nextPos) ||
                    collisionDetector.hasIgluAt(nextPos) ||
                    collisionDetector.hasUnbreakableBlockAt(nextPos)) {

                // Hit wall -> Stop
                narval.stopDrilling();
                return; // Stop this turn
            }

            // Check Ice -> Destroy
            IceBlock ice = collisionDetector.getIceBlockAt(nextPos);
            if (ice != null) {
                ice.startBreaking();
                // Instant destroy for charge feeling
                gameState.removeIceBlock(ice);
                System.out.println("[NARVAL] SMASHED Ice at " + nextPos);
            }

            // Move
            narval.move(nextPos);
            return;
        }

        // 4. Wander (Only if not drilling)
        Point nextPos = narval.getNextPosition();
        if (collisionDetector.isValidPosition(nextPos) &&
                !collisionDetector.isPositionBlocked(nextPos) &&
                !collisionDetector.hasEnemyAt(nextPos)) {
            narval.move(nextPos);
        } else {
            narval.changeDirection();
        }
    }

    /**
     * Checks if player is aligned with enemy and visible (no unbreakable walls).
     * Ice does NOT block vision for Narval.
     */
    private Direction getPlayerDirectionIfSeeing(Point enemyPos, Point playerPos) {
        // Log attempts periodically or just always for now (user says it doesn't print)
        // System.out.println("[DEBUG] Checking Narval: " + enemyPos + " vs Player: " +
        // playerPos);

        if (enemyPos.x == playerPos.x) {
            // System.out.println("[DEBUG] Aligned Vertically! X=" + enemyPos.x);
            // Vertical Alignment
            if (enemyPos.y > playerPos.y) {
                // Player is ABOVE
                if (isPathClear(enemyPos, playerPos, Direction.UP)) {
                    // System.out.println("[DEBUG] Seeing Player UP");
                    return Direction.UP;
                } else {
                    // System.out.println("[DEBUG] Path BLOCKED UP");
                }
            } else {
                // Player is BELOW
                if (isPathClear(enemyPos, playerPos, Direction.DOWN)) {
                    // System.out.println("[DEBUG] Seeing Player DOWN");
                    return Direction.DOWN;
                } else {
                    // System.out.println("[DEBUG] Path BLOCKED DOWN");
                }
            }
        } else if (enemyPos.y == playerPos.y) {
            // Horizontal Alignment
            // System.out.println("[DEBUG] Aligned Horizontally! Y=" + enemyPos.y);
            if (enemyPos.x > playerPos.x) {
                // Player is LEFT
                if (isPathClear(enemyPos, playerPos, Direction.LEFT)) {
                    // System.out.println("[DEBUG] Seeing Player LEFT");
                    return Direction.LEFT;
                } else {
                    // System.out.println("[DEBUG] Path BLOCKED LEFT");
                }
            } else {
                // Player is RIGHT
                if (isPathClear(enemyPos, playerPos, Direction.RIGHT)) {
                    // System.out.println("[DEBUG] Seeing Player RIGHT");
                    return Direction.RIGHT;
                } else {
                    // System.out.println("[DEBUG] Path BLOCKED RIGHT");
                }
            }
        }
        return null;
    }

    private boolean isPathClear(Point start, Point end, Direction dir) {
        Point p = new Point(start);
        p.x += dir.getDeltaX();
        p.y += dir.getDeltaY();

        while (!p.equals(end)) {
            // Check obstacles that block VISION (Unbreakable, Iglu).
            // Ice does NOT block vision for Narval charge.
            if (collisionDetector.hasUnbreakableBlockAt(p) || collisionDetector.hasIgluAt(p)) {
                return false;
            }
            p.x += dir.getDeltaX();
            p.y += dir.getDeltaY();

            // Safety break for infinite loose loop (shouldn't happen on grid)
            if (!collisionDetector.isValidPosition(p))
                return false;
        }
        return true;
    }

    /**
     * Procesa movimiento por defecto (cambiar dirección si está bloqueado).
     */
    private void processDefaultMovement(Enemy enemy) {
        Point nextPos = enemy.getNextPosition();

        if (collisionDetector.isValidPosition(nextPos) &&
                !collisionDetector.isPositionBlocked(nextPos) &&
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
            if (fruit.isCollected())
                continue;

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
                            !collisionDetector.isPositionBlocked(newPos) && // Covers Ice, Iglu, Unbreakable
                            !collisionDetector.hasEnemyAt(newPos) &&
                            !collisionDetector.hasFruitAt(newPos) &&
                            !collisionDetector.isPlayerAt(newPos) &&
                            !isHotTile(newPos)) { // Also avoid Hot Tiles
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
            System.out.println("✓ Next wave spawned!");
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
            boolean p1Dead = !player.isAlive() && !player.isDying();
            boolean p2Dead = (player2 != null) && !player2.isAlive() && !player2.isDying();

            if ((p1Dead || p2Dead) && !gameState.isGameOver()) {
                // In Co-op or Single Player, if anyone dies -> Game Over
                // In PvP, we might want different logic, but for now standard rules:
                // Actually, in PvP, if opponent dies, it's victory.
                // But wait, GamePanel handles "Victory vs GameOver" via shouldRestartLevel
                // We just need to ensure the game stops.
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

        if (allFruitsCollected) {
            gameState.setVictory(true);
            gameState.getPlayer().startCelebration();
            if (gameState.getPlayer2() != null) {
                gameState.getPlayer2().startCelebration();
            }

            // Determine winner based on score
            if (gameState.getNumberOfPlayers() == 2) {
                int score1 = gameState.getScore();
                int score2 = gameState.getScorePlayer2();
                System.out.println("Victory! P1: " + score1 + " - P2: " + score2);
            }
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
                        !collisionDetector.hasIceAt(pos) &&
                        !collisionDetector.hasIgluAt(pos) &&
                        !collisionDetector.hasUnbreakableBlockAt(pos) &&
                        !isHotTile(pos)) {
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