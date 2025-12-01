package domain.service;

import domain.entity.*;
import domain.state.GameState;
import java.awt.Point;
import java.util.*;

/**
 * Controlador de inteligencia artificial para el modo Machine vs Machine.
 * Gestiona el comportamiento automático del jugador y del enemigo controlado.
 */
public class AIController {

    private GameState gameState;
    private GameLogic gameLogic;
    private Random aiRandom;

    // IA - Jugador
    private int aiPlayerMoveTimer;
    private int aiActionTimer;
    private Point aiPlayerTarget;
    private Point aiLastPlayerPosition;
    private Point aiPreviousPlayerPosition;
    private int aiStuckCounter;
    private int aiConsecutiveFailedMoves;
    private List<Point> aiRecentPositions;

    // IA - Enemigo
    private int aiEnemyMoveTimer;
    private int aiEnemyActionTimer;

    // Constantes
    private static final int AI_MOVE_INTERVAL = 400;
    private static final int AI_ACTION_INTERVAL = 800;
    private static final int AI_ENEMY_ACTION_INTERVAL = 1200;

    /**
     * Constructor del controlador de IA.
     *
     * @param gameState Estado del juego
     * @param gameLogic Lógica del juego
     */
    public AIController(GameState gameState, GameLogic gameLogic) {
        this.gameState = gameState;
        this.gameLogic = gameLogic;
        this.aiRandom = new Random();

        // Inicializar variables IA Jugador
        this.aiPlayerMoveTimer = 0;
        this.aiActionTimer = 0;
        this.aiPlayerTarget = null;
        this.aiLastPlayerPosition = new Point(gameState.getPlayer().getPosition());
        this.aiPreviousPlayerPosition = new Point(gameState.getPlayer().getPosition());
        this.aiStuckCounter = 0;
        this.aiConsecutiveFailedMoves = 0;
        this.aiRecentPositions = new ArrayList<>();

        // Inicializar variables IA Enemigo
        this.aiEnemyMoveTimer = 0;
        this.aiEnemyActionTimer = 0;
    }

    /**
     * Actualiza la IA del jugador y del enemigo.
     */
    public void updateAI(int deltaTime) {
        updateAIPlayer(deltaTime);
        updateAIEnemy(deltaTime);
    }

    // ==================== IA - JUGADOR ====================

    /**
     * Actualiza el comportamiento de IA del jugador.
     */
    private void updateAIPlayer(int deltaTime) {
        Player player = gameState.getPlayer();
        if (!player.isAlive() || player.isDying()) return;

        aiPlayerMoveTimer += deltaTime;

        if (aiPlayerMoveTimer >= AI_MOVE_INTERVAL) {
            aiPlayerMoveTimer = 0;

            Point playerPos = player.getPosition();

            // Detectar si está atascado
            if (aiPreviousPlayerPosition != null) {
                if (aiPreviousPlayerPosition.equals(playerPos)) {
                    aiConsecutiveFailedMoves++;
                } else {
                    aiConsecutiveFailedMoves = 0;
                }
            }

            // Guardar historial de posiciones
            aiRecentPositions.add(new Point(playerPos));
            if (aiRecentPositions.size() > 8) {
                aiRecentPositions.remove(0);
            }

            boolean inLoop = detectPositionLoop();
            aiPreviousPlayerPosition = new Point(playerPos);

            // Si está muy atascado, movimiento aleatorio
            if (aiConsecutiveFailedMoves > 4 || inLoop) {
                System.out.println("✓ IA detectó atasco - movimiento aleatorio");
                moveAIPlayerRandom();
                aiConsecutiveFailedMoves = 0;
                aiRecentPositions.clear();
                return;
            }

            int currentLevel = gameState.getLevel();

            // Estrategia por nivel
            if (currentLevel == 1) {
                processAILevel1Movement(playerPos);
            } else if (currentLevel == 2) {
                processAILevel2Movement(playerPos);
            } else if (currentLevel == 3) {
                processAILevel3Movement(playerPos);
            }
        }

        // Procesar acciones IA
        updateAIPlayerActions(deltaTime);
    }

    /**
     * Actualiza las acciones de la IA del jugador (romper/crear hielo).
     */
    private void updateAIPlayerActions(int deltaTime) {
        aiActionTimer += deltaTime;

        if (aiActionTimer >= AI_ACTION_INTERVAL) {
            aiActionTimer = 0;

            Point playerPos = gameState.getPlayer().getPosition();

            if (shouldBreakIce(playerPos)) {
                List<Point> brokenIce = gameLogic.performIceKick();
                if (!brokenIce.isEmpty()) {
                    System.out.println("✓ IA rompió hielo para despejar camino");
                }
            } else if (gameState.getLevel() == 2 && shouldCreateIce(playerPos)) {
                gameLogic.performIceSneeze();
                System.out.println("✓ IA creó barrera de hielo defensiva");
            }
        }
    }

    // ==================== ESTRATEGIAS POR NIVEL ====================

    /**
     * Estrategia nivel 1: Evitar trolls y recolectar frutas.
     */
    private void processAILevel1Movement(Point playerPos) {
        Enemy nearestEnemy = findNearestEnemy(playerPos);

        if (nearestEnemy != null) {
            int distance = manhattanDistance(playerPos, nearestEnemy.getPosition());

            if (distance <= 3) {
                moveAIPlayerAwayFrom(playerPos, nearestEnemy.getPosition());
                return;
            }
        }

        Fruit closestFruit = findClosestFruit(playerPos);

        if (closestFruit != null) {
            aiPlayerTarget = closestFruit.getPosition();

            if (aiConsecutiveFailedMoves > 3) {
                moveAIPlayerAlternative(playerPos, aiPlayerTarget);
                aiConsecutiveFailedMoves = 0;
            } else {
                moveAIPlayerTowardsTarget(playerPos, aiPlayerTarget);
            }
        }
    }

    /**
     * Estrategia nivel 2: Evitar maceta y usar hielo defensivo.
     */
    private void processAILevel2Movement(Point playerPos) {
        Enemy maceta = findEnemyOfType(EnemyType.MACETA);

        if (maceta != null) {
            int distance = manhattanDistance(playerPos, maceta.getPosition());

            if (distance <= 4) {
                moveAIPlayerAwayFrom(playerPos, maceta.getPosition());
                return;
            }
        }

        Fruit closestFruit = findClosestFruit(playerPos);

        if (closestFruit != null) {
            aiPlayerTarget = closestFruit.getPosition();

            if (aiConsecutiveFailedMoves > 2) {
                if (maceta != null && manhattanDistance(playerPos, maceta.getPosition()) <= 6) {
                    gameLogic.performIceSneeze();
                }
                moveAIPlayerAlternative(playerPos, aiPlayerTarget);
                aiConsecutiveFailedMoves = 0;
            } else {
                moveAIPlayerTowardsTarget(playerPos, aiPlayerTarget);
            }
        }
    }

    /**
     * Estrategia nivel 3: Evitar calamar y recolectar frutas móviles.
     */
    private void processAILevel3Movement(Point playerPos) {
        Enemy calamar = findEnemyOfType(EnemyType.CALAMAR);

        if (calamar != null) {
            int distance = manhattanDistance(playerPos, calamar.getPosition());

            if (distance <= 5) {
                moveAIPlayerAwayFrom(playerPos, calamar.getPosition());
                return;
            }
        }

        Fruit closestFruit = findClosestFruit(playerPos);

        if (closestFruit != null) {
            aiPlayerTarget = closestFruit.getPosition();

            if (aiConsecutiveFailedMoves > 2) {
                moveAIPlayerAlternative(playerPos, aiPlayerTarget);
                aiConsecutiveFailedMoves = 0;
            } else {
                moveAIPlayerTowardsTarget(playerPos, aiPlayerTarget);
            }
        }
    }

    // ==================== MOVIMIENTOS DE IA JUGADOR ====================

    /**
     * Mueve la IA del jugador de forma aleatoria.
     */
    private void moveAIPlayerRandom() {
        Direction randomDirection = Direction.values()[aiRandom.nextInt(4)];
        gameLogic.movePlayer(randomDirection);
    }

    /**
     * Mueve la IA del jugador hacia un objetivo.
     */
    private void moveAIPlayerTowardsTarget(Point from, Point to) {
        Point oldPos = new Point(from);

        int dx = to.x - from.x;
        int dy = to.y - from.y;

        Direction direction;
        if (Math.abs(dx) > Math.abs(dy)) {
            direction = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        } else if (Math.abs(dy) > 0) {
            direction = (dy > 0) ? Direction.DOWN : Direction.UP;
        } else {
            return; // Ya está en el objetivo
        }

        gameLogic.movePlayer(direction);

        Point newPos = gameState.getPlayer().getPosition();
        if (oldPos.equals(newPos)) {
            aiConsecutiveFailedMoves++;
        }
    }

    /**
     * Mueve la IA del jugador alejándose de un peligro.
     */
    private void moveAIPlayerAwayFrom(Point from, Point dangerPos) {
        Point oldPos = new Point(from);

        int dx = from.x - dangerPos.x;
        int dy = from.y - dangerPos.y;

        Direction direction;
        if (Math.abs(dx) > Math.abs(dy)) {
            direction = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        } else {
            direction = (dy > 0) ? Direction.DOWN : Direction.UP;
        }

        gameLogic.movePlayer(direction);

        Point newPos = gameState.getPlayer().getPosition();
        if (oldPos.equals(newPos)) {
            aiConsecutiveFailedMoves++;
        }
    }

    /**
     * Mueve la IA del jugador con estrategia alternativa (para salir de atascos).
     */
    private void moveAIPlayerAlternative(Point from, Point to) {
        Point oldPos = new Point(from);

        List<Direction> directions = new ArrayList<>();
        directions.add(Direction.UP);
        directions.add(Direction.DOWN);
        directions.add(Direction.LEFT);
        directions.add(Direction.RIGHT);

        Collections.shuffle(directions);

        for (Direction dir : directions) {
            gameLogic.movePlayer(dir);

            Point testPos = gameState.getPlayer().getPosition();
            if (!testPos.equals(oldPos)) {
                aiConsecutiveFailedMoves = 0;
                return;
            }
        }

        aiConsecutiveFailedMoves++;
    }

    // ==================== DECISIONES DE ACCIONES ====================

    /**
     * Decide si la IA debe romper hielo.
     */
    private boolean shouldBreakIce(Point playerPos) {
        if (aiPlayerTarget == null) return false;

        Direction playerDir = gameState.getPlayer().getFacingDirection();
        Point checkPos = new Point(
                playerPos.x + playerDir.getDeltaX(),
                playerPos.y + playerDir.getDeltaY()
        );

        if (hasIceAt(checkPos)) {
            int distanceWithIce = manhattanDistance(playerPos, aiPlayerTarget);
            int distanceAfterBreak = manhattanDistance(checkPos, aiPlayerTarget);

            return distanceAfterBreak < distanceWithIce;
        }

        return false;
    }

    /**
     * Decide si la IA debe crear hielo defensivo.
     */
    private boolean shouldCreateIce(Point playerPos) {
        Enemy maceta = findEnemyOfType(EnemyType.MACETA);

        if (maceta != null) {
            Point enemyPos = maceta.getPosition();
            int distance = manhattanDistance(playerPos, enemyPos);

            if (distance >= 4 && distance <= 6) {
                if (playerPos.x == enemyPos.x || playerPos.y == enemyPos.y) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Detecta si la IA está en un loop de posiciones.
     */
    private boolean detectPositionLoop() {
        if (aiRecentPositions.size() < 6) return false;

        int recentSize = aiRecentPositions.size();
        Point pos1 = aiRecentPositions.get(recentSize - 1);
        Point pos2 = aiRecentPositions.get(recentSize - 2);
        Point pos3 = aiRecentPositions.get(recentSize - 3);

        int repeatCount = 0;
        for (int i = 0; i < recentSize - 3; i++) {
            if (aiRecentPositions.get(i).equals(pos1) ||
                    aiRecentPositions.get(i).equals(pos2) ||
                    aiRecentPositions.get(i).equals(pos3)) {
                repeatCount++;
            }
        }

        return repeatCount >= 3;
    }

    // ==================== IA - ENEMIGO ====================

    /**
     * Actualiza el comportamiento de IA del enemigo controlado.
     */
    private void updateAIEnemy(int deltaTime) {
        Enemy controlledEnemy = getPlayerControlledEnemy();
        if (controlledEnemy == null) return;

        aiEnemyMoveTimer += deltaTime;

        if (aiEnemyMoveTimer >= AI_MOVE_INTERVAL) {
            aiEnemyMoveTimer = 0;

            Point enemyPos = controlledEnemy.getPosition();
            Point playerPos = gameState.getPlayer().getPosition();

            int currentLevel = gameState.getLevel();

            if (currentLevel == 1) {
                // Nivel 1: 40% persecución, 60% aleatorio
                if (aiRandom.nextDouble() < 0.4) {
                    moveAIEnemyTowardsTarget(enemyPos, playerPos);
                } else {
                    moveAIEnemyRandom();
                }
            } else if (currentLevel == 2 || currentLevel == 3) {
                // Niveles 2 y 3: persecución activa
                moveAIEnemyTowardsTarget(enemyPos, playerPos);
            }
        }

        // Acciones especiales del enemigo (romper hielo - Calamar)
        updateAIEnemyActions(deltaTime, controlledEnemy);
    }

    /**
     * Actualiza las acciones del enemigo controlado por IA.
     */
    private void updateAIEnemyActions(int deltaTime, Enemy controlledEnemy) {
        if (!controlledEnemy.getType().canBreakIce()) return;

        aiEnemyActionTimer += deltaTime;

        if (aiEnemyActionTimer >= AI_ENEMY_ACTION_INTERVAL) {
            aiEnemyActionTimer = 0;

            Point brokenIce = gameLogic.performPlayer2IceBreak();
            if (brokenIce != null) {
                System.out.println("✓ IA (Calamar) rompió hielo estratégicamente");
            }
        }
    }

    /**
     * Mueve el enemigo controlado de forma aleatoria.
     */
    private void moveAIEnemyRandom() {
        Direction randomDirection = Direction.values()[aiRandom.nextInt(4)];
        gameLogic.movePlayerControlledEnemy(randomDirection);
    }

    /**
     * Mueve el enemigo controlado hacia un objetivo.
     */
    private void moveAIEnemyTowardsTarget(Point from, Point to) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;

        Direction direction;
        if (Math.abs(dx) > Math.abs(dy)) {
            direction = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        } else if (Math.abs(dy) > 0) {
            direction = (dy > 0) ? Direction.DOWN : Direction.UP;
        } else {
            return; // Ya está en el objetivo
        }

        gameLogic.movePlayerControlledEnemy(direction);
    }

    // ==================== UTILIDADES ====================

    /**
     * Encuentra la fruta más cercana no recolectada.
     */
    private Fruit findClosestFruit(Point from) {
        Fruit closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Fruit fruit : gameState.getFruits()) {
            if (!fruit.isCollected()) {
                Point fruitPos = fruit.getPosition();
                int distance = manhattanDistance(from, fruitPos);

                if (distance < minDistance) {
                    minDistance = distance;
                    closest = fruit;
                }
            }
        }

        return closest;
    }

    /**
     * Encuentra el enemigo más cercano (excluyendo el controlado).
     */
    private Enemy findNearestEnemy(Point from) {
        Enemy nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Enemy enemy : gameState.getEnemies()) {
            if (enemy.isActive() && !enemy.isControlledByPlayer()) {
                Point enemyPos = enemy.getPosition();
                int distance = manhattanDistance(from, enemyPos);

                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = enemy;
                }
            }
        }

        return nearest;
    }

    /**
     * Encuentra un enemigo del tipo especificado.
     */
    private Enemy findEnemyOfType(EnemyType type) {
        for (Enemy enemy : gameState.getEnemies()) {
            if (enemy.isActive() && enemy.getType() == type) {
                return enemy;
            }
        }
        return null;
    }

    /**
     * Obtiene el enemigo controlado por el jugador/IA.
     */
    private Enemy getPlayerControlledEnemy() {
        for (Enemy enemy : gameState.getEnemies()) {
            if (enemy.isControlledByPlayer()) {
                return enemy;
            }
        }
        return null;
    }

    /**
     * Calcula la distancia Manhattan entre dos puntos.
     */
    private int manhattanDistance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /**
     * Verifica si hay hielo en una posición.
     */
    private boolean hasIceAt(Point pos) {
        for (IceBlock ice : gameState.getIceBlocks()) {
            if (ice.isAt(pos)) {
                return true;
            }
        }
        return false;
    }
}