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
            if (currentLevel == 2 && type == EnemyType.MACETA) {
                processMacetaMovement(enemy, targetPosition);
            } else if (currentLevel == 3 && type == EnemyType.CALAMAR) {
                processCalamarMovement(enemy, targetPosition);
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
    private void processMacetaMovement(Enemy enemy, Point targetPosition) {
        enemy.chasePlayer(targetPosition);
        Point nextPos = enemy.getNextPosition();

        if (collisionDetector.isValidPosition(nextPos) &&
                !collisionDetector.hasIceAt(nextPos) &&
                !collisionDetector.hasOtherEnemyAt(nextPos, enemy)) {
            enemy.move(nextPos);
        } else {
            boolean moved = false;
            int attempts = 0;

            while (!moved && attempts < 4) {
                enemy.chasePlayer(targetPosition);
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
                        !collisionDetector.hasIceAt(pos)) {
                    emptyPositions.add(pos);
                }
            }
        }

        if (emptyPositions.isEmpty())
            return null;

        return emptyPositions.get(random.nextInt(emptyPositions.size()));
    }
}