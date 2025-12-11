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
    private static final int AI_ACTION_INTERVAL = 300; // Faster action checks (was 800)

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
        // En MvM (0 players) actualizamos ambos.
        // En P1 vs CPU (2 players + p2CPU) solo actualizamos P2 (P1 es humano).
        if (gameState.getNumberOfPlayers() == 0) {
            updateAIPlayer1(deltaTime);
            updateAIPlayer2(deltaTime);
        } else if (gameState.getNumberOfPlayers() == 2 && gameState.isP2CPU()) {
            updateAIPlayer2(deltaTime);
        }
    }

    // ==================== IA - JUGADOR 1 ====================

    private void updateAIPlayer1(int deltaTime) {
        Player player = gameState.getPlayer();
        if (player == null || !player.isAlive() || player.isDying())
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

        // Si está atascado, intentar liberarse
        if (failedMoves > 4 || inLoop) {
            handleStuckState(player, isPlayer1);
            if (isPlayer1) {
                aiPlayer1ConsecutiveFailedMoves = 0;
                aiPlayer1RecentPositions.clear();
            } else {
                aiPlayer2ConsecutiveFailedMoves = 0;
                aiPlayer2RecentPositions.clear();
            }
            return;
        }

        // Strategy switching based on AI Type
        AIType type = player.getAIType();
        if (type == null) {
            type = AIType.EXPERT; // Default
        }

        switch (type) {
            case HUNGRY:
                processHungryStrategy(player, isPlayer1, failedMoves);
                break;
            case FEARFUL:
                processFearfulStrategy(player, isPlayer1, failedMoves);
                break;
            case EXPERT:
            default:
                processExpertStrategy(player, isPlayer1, failedMoves);
                break;
        }
    }

    private void processHungryStrategy(Player player, boolean isPlayer1, int failedMoves) {
        // HUNGRY: Únicamente va por las frutas, ignorando enemigos completamente
        Point playerPos = player.getPosition();

        Fruit closestFruit = findClosestFruit(playerPos);
        if (closestFruit != null) {
            moveToTarget(playerPos, closestFruit.getPosition(), isPlayer1, failedMoves);
        } else {
            moveRandomly(isPlayer1);
        }
    }

    private void processFearfulStrategy(Player player, boolean isPlayer1, int failedMoves) {
        // FEARFUL: Prioritize Safety > Fruit.
        Point playerPos = player.getPosition();
        Enemy nearestEnemy = findNearestEnemy(playerPos);

        if (nearestEnemy != null) {
            int dist = manhattanDistance(playerPos, nearestEnemy.getPosition());

            // Si el enemigo está MUY cerca, CORRER (prioridad sobre estornudar)
            if (dist <= 3) {
                moveAwayFrom(playerPos, nearestEnemy.getPosition(), isPlayer1);
            }
            // Si está a media distancia, intentar alejarse, pero processAIPlayerActions
            // intentará estornudar si es posible
            else if (dist <= 6) {
                moveAwayFrom(playerPos, nearestEnemy.getPosition(), isPlayer1);
            } else {
                moveRandomly(isPlayer1);
            }
        } else {
            moveRandomly(isPlayer1);
        }
    }

    private void processExpertStrategy(Player player, boolean isPlayer1, int failedMoves) {
        // EXPERT: Pathfinding inteligente que esquiva enemigos.
        Point playerPos = player.getPosition();
        Enemy nearestEnemy = findNearestEnemy(playerPos);

        // 1. Supervivencia Inmediata: Si hay un enemigo pegado (distancia <= 2), huir
        // usando lógica de evasión directa
        if (nearestEnemy != null && manhattanDistance(playerPos, nearestEnemy.getPosition()) <= 2) {
            moveAwayFrom(playerPos, nearestEnemy.getPosition(), isPlayer1);
            return;
        }

        // 2. Buscar fruta usando Pathfinding (BFS) CON EVASIÓN DE ENEMIGOS
        Fruit targetFruit = findBestFruitBFS(playerPos, true); // true = avoid enemies

        if (targetFruit != null) {
            Point nextStep = getNextStepBFS(playerPos, targetFruit.getPosition(), true);
            if (nextStep != null) {
                moveToStep(playerPos, nextStep, isPlayer1);
                return;
            }
        }

        // 3. Fallback: moverse random (que suele ser evasivo si se bloquea)
        moveRandomly(isPlayer1);
    }

    // Movimiento directo a un paso adyacente (calculado por BFS)
    private void moveToStep(Point current, Point nextInfo, boolean isPlayer1) {
        int dx = nextInfo.x - current.x;
        int dy = nextInfo.y - current.y;
        Direction direction = null;

        if (dx == 1)
            direction = Direction.RIGHT;
        else if (dx == -1)
            direction = Direction.LEFT;
        else if (dy == 1)
            direction = Direction.DOWN;
        else if (dy == -1)
            direction = Direction.UP;

        if (direction != null) {
            executeMove(direction, isPlayer1);
        }
    }

    private void moveToTarget(Point playerPos, Point target, boolean isPlayer1, int failedMoves) {
        if (isPlayer1)
            aiPlayer1Target = target;
        else
            aiPlayer2Target = target;

        if (failedMoves > 2) {
            moveAlternative(playerPos, target, isPlayer1);
        } else {
            moveTowards(playerPos, target, isPlayer1);
        }
    }

    private void processAIPlayerActions(Player player, boolean isPlayer1) {
        Point playerPos = player.getPosition();
        Point target = isPlayer1 ? aiPlayer1Target : aiPlayer2Target;
        AIType type = player.getAIType();
        if (type == null)
            type = AIType.EXPERT;

        // FEARFUL: Logic improved to use Sneeze more often
        if (type == AIType.FEARFUL) {
            Enemy enemy = findNearestEnemy(playerPos);
            // Si el enemigo no esta en rango de panico inmediato, considerar estornudar
            if (enemy != null) {
                int dist = manhattanDistance(playerPos, enemy.getPosition());

                // Occasional defensive sneeze if enemy is mid-range (chance based)
                boolean randomSneeze = aiRandom.nextInt(100) < 30; // 30% chance per tick if conditions met

                if (dist > 2 && dist <= 7 && randomSneeze && shouldCreateIceFearful(player, enemy.getPosition())) {
                    if (isPlayer1)
                        gameLogic.performIceSneeze(player);
                    else
                        gameLogic.performIceSneeze(player);
                    return;
                }
            }
            return;
        }

        // EXPERT & HUNGRY Handling
        if (target == null)
            return;

        if (shouldBreakIce(player, target)) {
            if (isPlayer1)
                gameLogic.performIceKick(player);
            else
                gameLogic.performIceKick(player);
        } else if (type == AIType.EXPERT && shouldCreateIce(player)) {
            // EXPERT: Intelligent use of ice block
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
        // Mejorada: Evaluar los 4 movimientos posibles y elegir el que más aleja
        Direction bestDir = null;
        int maxDist = -1;

        for (Direction dir : Direction.values()) {
            Point next = new Point(from.x + dir.getDeltaX(), from.y + dir.getDeltaY());

            if (isValidMove(next)) {
                int dist = manhattanDistance(next, dangerPos); // Distancia futura
                if (dist > maxDist) {
                    maxDist = dist;
                    bestDir = dir;
                }
            }
        }

        if (bestDir != null) {
            executeMove(bestDir, isPlayer1);
        } else {
            // Si no hay escape, intentar moverse random (mejor que nada)
            moveRandomly(isPlayer1);
        }
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

    // BFS Simple con opción de evitar enemigos
    private Fruit findBestFruitBFS(Point start, boolean avoidEnemies) {
        Queue<Point> queue = new LinkedList<>();
        queue.add(start);
        Set<Point> visited = new HashSet<>();
        visited.add(start);

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            // Encontró fruta
            for (Fruit f : gameState.getFruits()) {
                if (!f.isCollected() && f.getPosition().equals(current)) {
                    if (f.isLethal())
                        continue;
                    return f; // Primera fruta encontrada es la más cercana accesible
                }
            }

            for (Direction dir : Direction.values()) {
                Point next = new Point(current.x + dir.getDeltaX(), current.y + dir.getDeltaY());
                if (isValidMove(next) && !visited.contains(next)) {
                    if (avoidEnemies && !isSafeFromEnemies(next)) {
                        continue; // Saltar casillas peligrosas
                    }
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return null;
    }

    private Point getNextStepBFS(Point start, Point target, boolean avoidEnemies) {
        Queue<Point> queue = new LinkedList<>();
        queue.add(start);
        Map<Point, Point> parents = new HashMap<>();
        parents.put(start, null);

        boolean found = false;

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.equals(target)) {
                found = true;
                break;
            }

            for (Direction dir : Direction.values()) {
                Point next = new Point(current.x + dir.getDeltaX(), current.y + dir.getDeltaY());
                if (isValidMove(next) && !parents.containsKey(next)) {
                    if (avoidEnemies && !isSafeFromEnemies(next)) {
                        continue;
                    }
                    parents.put(next, current);
                    queue.add(next);
                }
            }
        }

        if (found) {
            Point step = target;
            while (parents.get(step) != null && !parents.get(step).equals(start)) {
                step = parents.get(step);
            }
            return step;
        }
        return null;
    }

    // Check if position is safe (not adjacent to any enemy)
    private boolean isSafeFromEnemies(Point p) {
        for (Enemy e : gameState.getEnemies()) {
            if (!e.isActive())
                continue;
            // Si la distancia es <= 2, es peligroso (el enemigo puede moverse y alcanzarnos
            // rápido)
            if (manhattanDistance(p, e.getPosition()) <= 2) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidMove(Point p) {
        if (p.x < 0 || p.x >= GameState.getGridSize() || p.y < 0 || p.y >= GameState.getGridSize())
            return false;

        if (gameState.getIglu() != null) {
            Iglu iglu = gameState.getIglu();
            Point igluPos = iglu.getPosition();
            // 3x3 hardcoded check as fallback
            if (p.x >= igluPos.x && p.x < igluPos.x + 3 &&
                    p.y >= igluPos.y && p.y < igluPos.y + 3) {
                return false;
            }
        }

        for (UnbreakableBlock b : gameState.getUnbreakableBlocks()) {
            if (b.getPosition().equals(p))
                return false;
        }

        if (hasIceAt(p))
            return false;
        return true;
    }

    private boolean shouldBreakIce(Player player, Point target) {
        Direction dir = player.getFacingDirection();
        Point checkPos = new Point(
                player.getPosition().x + dir.getDeltaX(),
                player.getPosition().y + dir.getDeltaY());
        return hasIceAt(checkPos);
    }

    private boolean shouldCreateIceFearful(Player player, Point enemyPos) {
        Direction dir = player.getFacingDirection();
        Point checkPos = new Point(
                player.getPosition().x + dir.getDeltaX(),
                player.getPosition().y + dir.getDeltaY());
        return isValidMove(checkPos) && !hasIceAt(checkPos);
    }

    private boolean shouldCreateIce(Player player) {
        Enemy enemy = findNearestEnemy(player.getPosition());

        if (enemy != null) {
            int d = manhattanDistance(player.getPosition(), enemy.getPosition());

            // 1. Proximity Defense (Enemy close but not too close)
            if (d > 2 && d < 5) {
                return true;
            }

            // 2. Strategic Blocking (Alignment Sniping)
            // If enemy is aligned and we are facing them, trap them!
            Point pPos = player.getPosition();
            Point ePos = enemy.getPosition();
            Direction facing = player.getFacingDirection();

            boolean alignedX = (pPos.x == ePos.x);
            boolean alignedY = (pPos.y == ePos.y);

            // Only snipe if strictly aligned and within reasonable range (not across entire
            // map)
            if ((alignedX || alignedY) && d < 10 && d > 1) {
                if (alignedX) {
                    if (pPos.y < ePos.y && facing == Direction.DOWN)
                        return true; // Enemy below
                    if (pPos.y > ePos.y && facing == Direction.UP)
                        return true; // Enemy above
                }
                if (alignedY) {
                    if (pPos.x < ePos.x && facing == Direction.RIGHT)
                        return true; // Enemy right
                    if (pPos.x > ePos.x && facing == Direction.LEFT)
                        return true; // Enemy left
                }
            }
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
            if (!f.isCollected() && !f.isLethal()) {
                int d = manhattanDistance(from, f.getPosition());
                if (d < minDist) {
                    minDist = d;
                    closest = f;
                }
            }
        }
        return closest;
    }

    // ==================== UNSTUCK LOGIC ====================

    private void handleStuckState(Player player, boolean isPlayer1) {
        // First try: Kick surrounding ice
        Point p = player.getPosition();
        List<Direction> iceDirs = new ArrayList<>();

        for (Direction d : Direction.values()) {
            Point neighbor = new Point(p.x + d.getDeltaX(), p.y + d.getDeltaY());
            if (hasIceAt(neighbor)) {
                iceDirs.add(d);
            }
        }

        if (!iceDirs.isEmpty()) {
            // Found ice! Face it and kick.
            Direction kickDir = iceDirs.get(aiRandom.nextInt(iceDirs.size()));

            // Turn towards ice first (move in that direction but blocked)
            executeMove(kickDir, isPlayer1);

            // Then kick
            if (isPlayer1)
                gameLogic.performIceKick(player);
            else
                gameLogic.performIceKick(player);
            return;
        }

        // Second try: Random move (fallback)
        moveRandomly(isPlayer1);
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
