package domain.service;

import domain.entity.*;
import domain.state.GameState;
import java.awt.Point;
import java.util.*;

/**
 * Controlador de inteligencia artificial para el modo Machine vs Machine.
 * Gestiona el comportamiento automático de dos jugadores (helados).
 */
public class AIController {

    private GameState gameState;
    private GameLogic gameLogic;
    private Random aiRandom;

    // IA - Jugador 1
    private int aiPlayer1MoveTimer;
    private int aiPlayer1ActionTimer;
    private Point aiPlayer1Target;
    private Point aiPreviousPlayer1Position;
    private int aiPlayer1ConsecutiveFailedMoves;
    private List<Point> aiPlayer1RecentPositions;

    // IA - Jugador 2
    private int aiPlayer2MoveTimer;
    private int aiPlayer2ActionTimer;
    private Point aiPlayer2Target;
    private Point aiPreviousPlayer2Position;
    private int aiPlayer2ConsecutiveFailedMoves;
    private List<Point> aiPlayer2RecentPositions;

    // Constantes
    private static final int AI_MOVE_INTERVAL = 400;
    private static final int AI_ACTION_INTERVAL = 800;

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

        // Inicializar variables IA Jugador 1
        this.aiPlayer1MoveTimer = 0;
        this.aiPlayer1ActionTimer = 0;
        this.aiPlayer1Target = null;
        this.aiPreviousPlayer1Position = new Point(gameState.getPlayer().getPosition());
        this.aiPlayer1ConsecutiveFailedMoves = 0;
        this.aiPlayer1RecentPositions = new ArrayList<>();

        // Inicializar variables IA Jugador 2
        this.aiPlayer2MoveTimer = 0;
        this.aiPlayer2ActionTimer = 0;
        this.aiPlayer2Target = null;
        if (gameState.getPlayer2() != null) {
            this.aiPreviousPlayer2Position = new Point(gameState.getPlayer2().getPosition());
        }
        this.aiPlayer2ConsecutiveFailedMoves = 0;
        this.aiPlayer2RecentPositions = new ArrayList<>();
    }

    /**
     * Actualiza la IA de ambos jugadores.
     */
    public void updateAI(int deltaTime) {
        updateAIPlayer1(deltaTime);
        updateAIPlayer2(deltaTime);
    }

    // ==================== IA - JUGADOR 1 ====================

    private void updateAIPlayer1(int deltaTime) {
        Player player = gameState.getPlayer();
        if (!player.isAlive() || player.isDying())
            return;

        aiPlayer1MoveTimer += deltaTime;

        if (aiPlayer1MoveTimer >= AI_MOVE_INTERVAL) {
            aiPlayer1MoveTimer = 0;
            processAIPlayerLogic(player, true);
        }

        updateAIPlayer1Actions(deltaTime);
    }

    private void updateAIPlayer1Actions(int deltaTime) {
        aiPlayer1ActionTimer += deltaTime;
        if (aiPlayer1ActionTimer >= AI_ACTION_INTERVAL) {
            aiPlayer1ActionTimer = 0;
            processAIPlayerActions(gameState.getPlayer(), true);
        }
    }

    // ==================== IA - JUGADOR 2 ====================

    private void updateAIPlayer2(int deltaTime) {
        Player player2 = gameState.getPlayer2();
        if (player2 == null || !player2.isAlive() || player2.isDying())
            return;

        aiPlayer2MoveTimer += deltaTime;

        if (aiPlayer2MoveTimer >= AI_MOVE_INTERVAL) {
            aiPlayer2MoveTimer = 0;
            processAIPlayerLogic(player2, false);
        }

        updateAIPlayer2Actions(deltaTime);
    }

    private void updateAIPlayer2Actions(int deltaTime) {
        aiPlayer2ActionTimer += deltaTime;
        if (aiPlayer2ActionTimer >= AI_ACTION_INTERVAL) {
            aiPlayer2ActionTimer = 0;
            processAIPlayerActions(gameState.getPlayer2(), false);
        }
    }

    // ==================== LÓGICA COMÚN DE IA ====================

    private void processAIPlayerLogic(Player player, boolean isPlayer1) {
        Point playerPos = player.getPosition();
        Point prevPos = isPlayer1 ? aiPreviousPlayer1Position : aiPreviousPlayer2Position;
        int failedMoves = isPlayer1 ? aiPlayer1ConsecutiveFailedMoves : aiPlayer2ConsecutiveFailedMoves;
        List<Point> recentPositions = isPlayer1 ? aiPlayer1RecentPositions : aiPlayer2RecentPositions;

        // Detectar atasco
        if (prevPos != null) {
            if (prevPos.equals(playerPos)) {
                failedMoves++;
            } else {
                failedMoves = 0;
            }
        }

        // Actualizar estado
        if (isPlayer1) {
            aiPreviousPlayer1Position = new Point(playerPos);
            aiPlayer1ConsecutiveFailedMoves = failedMoves;
        } else {
            aiPreviousPlayer2Position = new Point(playerPos);
            aiPlayer2ConsecutiveFailedMoves = failedMoves;
        }

        // Historial de posiciones
        recentPositions.add(new Point(playerPos));
        if (recentPositions.size() > 8) {
            recentPositions.remove(0);
        }

        boolean inLoop = detectPositionLoop(recentPositions);

        // Si está atascado, movimiento aleatorio
        if (failedMoves > 4 || inLoop) {
            moveRandomly(isPlayer1);
            if (isPlayer1) {
                aiPlayer1ConsecutiveFailedMoves = 0;
                aiPlayer1RecentPositions.clear();
            } else {
                aiPlayer2ConsecutiveFailedMoves = 0;
                aiPlayer2RecentPositions.clear();
            }
            return;
        }

        // Estrategia general: Evitar enemigos y buscar frutas
        processGeneralStrategy(player, isPlayer1, failedMoves);
    }

    private void processGeneralStrategy(Player player, boolean isPlayer1, int failedMoves) {
        Point playerPos = player.getPosition();
        Enemy nearestEnemy = findNearestEnemy(playerPos);

        // 1. Evitar enemigos cercanos
        if (nearestEnemy != null) {
            int distance = manhattanDistance(playerPos, nearestEnemy.getPosition());
            if (distance <= 3) {
                moveAwayFrom(playerPos, nearestEnemy.getPosition(), isPlayer1);
                return;
            }
        }

        // 2. Buscar fruta más cercana
        Fruit closestFruit = findClosestFruit(playerPos);
        if (closestFruit != null) {
            Point target = closestFruit.getPosition();
            if (isPlayer1)
                aiPlayer1Target = target;
            else
                aiPlayer2Target = target;

            if (failedMoves > 2) {
                moveAlternative(playerPos, target, isPlayer1);
            } else {
                moveTowards(playerPos, target, isPlayer1);
            }
        } else {
            // No hay frutas, moverse aleatoriamente
            moveRandomly(isPlayer1);
        }
    }

    private void processAIPlayerActions(Player player, boolean isPlayer1) {
        Point playerPos = player.getPosition();
        Point target = isPlayer1 ? aiPlayer1Target : aiPlayer2Target;

        if (target == null)
            return;

        // Decidir si romper hielo
        if (shouldBreakIce(player, target)) {
            if (isPlayer1)
                gameLogic.performIceKick(player);
            else
                gameLogic.performIceKick(player); // GameLogic supports passing player
        }
        // Decidir si crear hielo (defensivo)
        else if (shouldCreateIce(player)) {
            if (isPlayer1)
                gameLogic.performIceSneeze(player);
            else
                gameLogic.performIceSneeze(player);
        }
    }

    // ==================== MOVIMIENTOS ====================

    private void moveRandomly(boolean isPlayer1) {
        Direction dir = Direction.values()[aiRandom.nextInt(4)];
        executeMove(dir, isPlayer1);
    }

    private void moveTowards(Point from, Point to, boolean isPlayer1) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;

        Direction direction;
        if (Math.abs(dx) > Math.abs(dy)) {
            direction = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        } else if (Math.abs(dy) > 0) {
            direction = (dy > 0) ? Direction.DOWN : Direction.UP;
        } else {
            return;
        }
        executeMove(direction, isPlayer1);
    }

    private void moveAwayFrom(Point from, Point dangerPos, boolean isPlayer1) {
        int dx = from.x - dangerPos.x;
        int dy = from.y - dangerPos.y;

        Direction direction;
        if (Math.abs(dx) > Math.abs(dy)) {
            direction = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        } else {
            direction = (dy > 0) ? Direction.DOWN : Direction.UP;
        }
        executeMove(direction, isPlayer1);
    }

    private void moveAlternative(Point from, Point to, boolean isPlayer1) {
        List<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));
        Collections.shuffle(directions);
        executeMove(directions.get(0), isPlayer1);
    }

    private void executeMove(Direction dir, boolean isPlayer1) {
        if (isPlayer1) {
            gameLogic.movePlayer(dir);
        } else {
            gameLogic.movePlayer2(dir);
        }
    }

    // ==================== UTILIDADES ====================

    private boolean shouldBreakIce(Player player, Point target) {
        Direction dir = player.getFacingDirection();
        Point checkPos = new Point(
                player.getPosition().x + dir.getDeltaX(),
                player.getPosition().y + dir.getDeltaY());
        // Simplificación: si hay hielo enfrente y nos acerca al objetivo
        return hasIceAt(checkPos);
    }

    private boolean shouldCreateIce(Player player) {
        // Crear hielo si hay un enemigo cerca en línea recta (simplificado)
        Enemy enemy = findNearestEnemy(player.getPosition());
        if (enemy != null && manhattanDistance(player.getPosition(), enemy.getPosition()) < 4) {
            return true;
        }
        return false;
    }

    private boolean detectPositionLoop(List<Point> history) {
        if (history.size() < 6)
            return false;
        Point last = history.get(history.size() - 1);
        int count = 0;
        for (Point p : history) {
            if (p.equals(last))
                count++;
        }
        return count >= 3;
    }

    private Fruit findClosestFruit(Point from) {
        Fruit closest = null;
        int minDist = Integer.MAX_VALUE;
        for (Fruit f : gameState.getFruits()) {
            if (!f.isCollected()) {
                int d = manhattanDistance(from, f.getPosition());
                if (d < minDist) {
                    minDist = d;
                    closest = f;
                }
            }
        }
        return closest;
    }

    private Enemy findNearestEnemy(Point from) {
        Enemy nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (Enemy e : gameState.getEnemies()) {
            if (e.isActive()) {
                int d = manhattanDistance(from, e.getPosition());
                if (d < minDist) {
                    minDist = d;
                    nearest = e;
                }
            }
        }
        return nearest;
    }

    private int manhattanDistance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private boolean hasIceAt(Point pos) {
        for (IceBlock ice : gameState.getIceBlocks()) {
            if (ice.isAt(pos))
                return true;
        }
        return false;
    }
}