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
