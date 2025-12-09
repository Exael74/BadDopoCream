package domain.entity;

/**
 * Tipos de enemigos con sus características específicas.
 */
public enum EnemyType {
    TROLL(1000), // Movimiento lento
    MACETA(600), // Movimiento rápido
    CALAMAR(700), // Movimiento medio
    NARVAL(800); // Movimiento medio-lento

    private final int moveInterval;

    EnemyType(int moveInterval) {
        this.moveInterval = moveInterval;
    }

    /**
     * Obtiene el intervalo de movimiento en milisegundos.
     */
    public int getMoveInterval() {
        return moveInterval;
    }

    /**
     * Verifica si este tipo de enemigo puede romper hielo.
     */
    public boolean canBreakIce() {
        return this == CALAMAR || this == NARVAL;
    }

    /**
     * Verifica si este tipo de enemigo debe perseguir al jugador en modo 1P.
     */
    public boolean shouldChasePlayer() {
        return this == MACETA || this == CALAMAR || this == NARVAL;
    }
}