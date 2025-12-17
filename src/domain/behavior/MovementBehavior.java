package domain.behavior;

import domain.entity.Direction;
import java.awt.Point;
import java.util.Random;

/**
 * Estrategia de movimiento para enemigos.
 * Implementa el patrón Strategy para diferentes comportamientos de movimiento.
 */
import java.io.Serializable;

/**
 * Estrategia de movimiento para enemigos.
 * Implementa el patrón Strategy para diferentes comportamientos de movimiento.
 */
public interface MovementBehavior extends Serializable {

    /**
     * Calcula la dirección de movimiento del enemigo.
     *
     * @param currentPosition Posición actual del enemigo
     * @param targetPosition  Posición objetivo (generalmente el jugador)
     * @param stuckCounter    Contador de veces que el enemigo se ha atascado
     * @param random          Generador de números aleatorios
     * @return Dirección calculada
     */
    Direction calculateDirection(Point currentPosition, Point targetPosition,
            int stuckCounter, Random random);
}
