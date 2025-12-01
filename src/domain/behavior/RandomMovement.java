package domain.behavior;

import domain.entity.Direction;
import java.awt.Point;
import java.util.Random;

/**
 * Estrategia de movimiento aleatorio.
 * Usada por enemigos tipo TROLL.
 */
public class RandomMovement implements MovementBehavior {

    @Override
    public Direction calculateDirection(Point currentPosition, Point targetPosition,
                                        int stuckCounter, Random random) {
        Direction[] directions = Direction.values();
        // Excluir IDLE de las opciones
        int randomIndex = random.nextInt(4); // Solo UP, DOWN, LEFT, RIGHT
        return directions[randomIndex];
    }
}