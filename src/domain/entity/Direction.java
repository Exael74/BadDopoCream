package domain.entity;

/**
 * Direcciones de movimiento en el juego.
 */
public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    IDLE;

    /** Las cuatro direcciones que representan un desplazamiento real. */
    private static final Direction[] MOVEMENT_DIRECTIONS = { UP, DOWN, LEFT, RIGHT };

    /**
     * Devuelve solo las direcciones que mueven a una entidad, es decir, todas menos
     * {@link #IDLE}. Recorrer {@code values()} para elegir un movimiento incluye
     * IDLE, cuyo delta es (0,0), y hace que "quedarse quieto" compita como si fuera
     * un movimiento válido.
     *
     * @return Copia del arreglo de direcciones de movimiento
     */
    public static Direction[] movementValues() {
        return MOVEMENT_DIRECTIONS.clone();
    }

    /**
     * Indica si esta dirección produce un desplazamiento.
     */
    public boolean isMovement() {
        return this != IDLE;
    }

    /**
     * Obtiene la dirección opuesta.
     */
    public Direction getOpposite() {
        switch (this) {
            case UP: return DOWN;
            case DOWN: return UP;
            case LEFT: return RIGHT;
            case RIGHT: return LEFT;
            default: return IDLE;
        }
    }

    /**
     * Calcula el delta X para esta dirección.
     */
    public int getDeltaX() {
        switch (this) {
            case LEFT: return -1;
            case RIGHT: return 1;
            default: return 0;
        }
    }

    /**
     * Calcula el delta Y para esta dirección.
     */
    public int getDeltaY() {
        switch (this) {
            case UP: return -1;
            case DOWN: return 1;
            default: return 0;
        }
    }
}
