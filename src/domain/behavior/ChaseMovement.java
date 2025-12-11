package domain.behavior;

import domain.entity.Direction;
import java.awt.Point;
import java.util.Random;

/**
 * Estrategia de movimiento de persecución.
 * Usada por enemigos tipo MACETA y CALAMAR.
 * Persigue al jugador usando distancia Manhattan con lógica anti-atasco.
 */
public class ChaseMovement implements MovementBehavior {

    @Override
    public Direction calculateDirection(Point currentPosition, Point targetPosition,
                                        int stuckCounter, Random random) {
        int dx = targetPosition.x - currentPosition.x;
        int dy = targetPosition.y - currentPosition.y;

        // Si está muy atascado, intentar movimiento alternativo
        if (stuckCounter > 2) {
            return calculateAlternativeDirection(dx, dy, random);
        }

        int distance = Math.abs(dx) + Math.abs(dy);

        // Estrategia diferente según distancia
        if (distance > 10) {
            return calculateDistantDirection(dx, dy);
        } else {
            return calculateCloseDirection(dx, dy, random);
        }
    }

    /**
     * Calcula dirección cuando el enemigo está lejos del objetivo.
     */
    private Direction calculateDistantDirection(int dx, int dy) {
        if (Math.abs(dx) >= Math.abs(dy)) {
            return (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        } else {
            return (dy > 0) ? Direction.DOWN : Direction.UP;
        }
    }

    /**
     * Calcula dirección cuando el enemigo está cerca del objetivo.
     * Introduce algo de aleatoriedad para hacer el movimiento más natural.
     */
    private Direction calculateCloseDirection(int dx, int dy, Random random) {
        boolean prioritizeMain = random.nextDouble() < 0.7;

        if (prioritizeMain) {
            if (Math.abs(dx) > Math.abs(dy)) {
                return (dx > 0) ? Direction.RIGHT : Direction.LEFT;
            } else if (Math.abs(dy) > 0) {
                return (dy > 0) ? Direction.DOWN : Direction.UP;
            } else if (Math.abs(dx) > 0) {
                return (dx > 0) ? Direction.RIGHT : Direction.LEFT;
            }
        } else {
            if (Math.abs(dy) > 0 && Math.abs(dx) >= Math.abs(dy)) {
                return (dy > 0) ? Direction.DOWN : Direction.UP;
            } else if (Math.abs(dx) > 0) {
                return (dx > 0) ? Direction.RIGHT : Direction.LEFT;
            } else if (Math.abs(dy) > 0) {
                return (dy > 0) ? Direction.DOWN : Direction.UP;
            }
        }

        return Direction.DOWN; // Dirección por defecto
    }

    /**
     * Calcula dirección alternativa cuando el enemigo está atascado.
     */
    private Direction calculateAlternativeDirection(int dx, int dy, Random random) {
        if (random.nextBoolean()) {
            // Priorizar movimiento vertical
            if (dy > 0) return Direction.DOWN;
            else if (dy < 0) return Direction.UP;
            else if (dx > 0) return Direction.RIGHT;
            else if (dx < 0) return Direction.LEFT;
        } else {
            // Priorizar movimiento horizontal
            if (dx > 0) return Direction.RIGHT;
            else if (dx < 0) return Direction.LEFT;
            else if (dy > 0) return Direction.DOWN;
            else if (dy < 0) return Direction.UP;
        }

        return Direction.DOWN; // Dirección por defecto
    }
}
