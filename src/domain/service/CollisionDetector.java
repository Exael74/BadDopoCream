package domain.service;

import domain.entity.*;
import domain.state.GameState;
import java.awt.Point;

/**
 * Servicio responsable de detectar colisiones entre entidades del juego.
 * Separa la lógica de detección de colisiones de la lógica principal del juego.
 */
public class CollisionDetector {

    private GameState gameState;

    /**
     * Constructor del detector de colisiones.
     *
     * @param gameState Estado del juego
     */
    public CollisionDetector(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Verifica si hay un enemigo en la posición especificada.
     *
     * @param position Posición a verificar
     * @return true si hay un enemigo activo en esa posición
     */
    public boolean hasEnemyAt(Point position) {
        for (Enemy enemy : gameState.getEnemies()) {
            if (enemy.isActive() && enemy.isAt(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si hay un enemigo diferente al especificado en la posición.
     *
     * @param position     Posición a verificar
     * @param currentEnemy Enemigo a excluir de la verificación
     * @return true si hay otro enemigo en esa posición
     */
    public boolean hasOtherEnemyAt(Point position, Enemy currentEnemy) {
        for (Enemy enemy : gameState.getEnemies()) {
            if (enemy != currentEnemy && enemy.isActive() && enemy.isAt(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si hay hielo en la posición especificada.
     *
     * @param position Posición a verificar
     * @return true si hay un bloque de hielo en esa posición
     */
    public boolean hasIceAt(Point position) {
        for (IceBlock ice : gameState.getIceBlocks()) {
            if (ice.isAt(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene el bloque de hielo en la posición especificada.
     *
     * @param position Posición a verificar
     * @return Bloque de hielo o null si no hay ninguno
     */
    public IceBlock getIceAt(Point position) {
        for (IceBlock ice : gameState.getIceBlocks()) {
            if (ice.isAt(position)) {
                return ice;
            }
        }
        return null;
    }

    /**
     * Verifica si hay una fruta no recolectada en la posición especificada.
     *
     * @param position Posición a verificar
     * @return true si hay una fruta no recolectada en esa posición
     */
    public boolean hasFruitAt(Point position) {
        for (Fruit fruit : gameState.getFruits()) {
            if (!fruit.isCollected() && fruit.isAt(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si el jugador está en la posición especificada.
     *
     * @param position Posición a verificar
     * @return true si el jugador está en esa posición
     */
    public boolean isPlayerAt(Point position) {
        return gameState.getPlayer().isAt(position);
    }

    /**
     * Verifica si la posición está bloqueada (por hielo u otro obstáculo).
     *
     * @param position Posición a verificar
     * @return true si la posición está bloqueada
     */
    public boolean isPositionBlocked(Point position) {
        return hasIceAt(position);
    }

    /**
     * Verifica si la posición está válida dentro del grid del juego.
     *
     * @param position Posición a verificar
     * @return true si la posición está dentro de los límites
     */
    public boolean isValidPosition(Point position) {
        int gridSize = GameState.getGridSize();
        return position.x >= 0 && position.x < gridSize &&
                position.y >= 0 && position.y < gridSize;
    }

    /**
     * Detecta y procesa colisiones de los jugadores con otras entidades.
     * Maneja colisiones con enemigos (muerte) y frutas (recolección).
     */
    public void checkCollisions() {
        // Verificar colisiones para Jugador 1
        checkPlayerCollisions(gameState.getPlayer(), false);

        // Verificar colisiones para Jugador 2 (si existe)
        if (gameState.getPlayer2() != null) {
            checkPlayerCollisions(gameState.getPlayer2(), true);
        }
    }

    private void checkPlayerCollisions(Player player, boolean isPlayer2) {
        if (player.isDying())
            return;

        Point playerPos = player.getPosition();

        // Verificar colisión con enemigos
        for (Enemy enemy : gameState.getEnemies()) {
            if (enemy.isActive() && enemy.isAt(playerPos)) {
                if (!gameState.isGameOver()) {
                    player.die();
                    // Si muere cualquiera, el juego termina (o reinicia nivel)
                    // En este diseño, marcamos game over y el GameLogic/Facade maneja el reinicio
                    gameState.setGameOver(true);

                    String pName = isPlayer2 ? "P2" : "P1";
                    System.out.println("✗ " + pName + " fue tocado por un enemigo - Game Over");
                }
                return;
            }
        }

        // Verificar colisión con frutas
        for (Fruit fruit : gameState.getFruits()) {
            if (!fruit.isCollected() && fruit.isAt(playerPos)) {
                fruit.collect();
                int points = fruit.getType().getScore();

                if (isPlayer2) {
                    gameState.addScorePlayer2(points);
                    System.out.println("✓ P2 recolectó fruta: " + fruit.getType() + " (+" + points + " pts)");
                } else {
                    gameState.addScore(points);
                    System.out.println("✓ P1 recolectó fruta: " + fruit.getType() + " (+" + points + " pts)");
                }
            }
        }
    }
}