package domain.entity;

/**
 * Tipos de frutas con sus capacidades específicas.
 */
public enum FruitType {
    UVA(false, false, 0, 50), // Estática, 50 puntos
    PLATANO(false, false, 0, 100), // Estática, 100 puntos
    PIÑA(true, false, 2000, 200), // Se mueve cada 2 segundos, 200 puntos
    CEREZA(false, true, 20000, 150), // Se teletransporta cada 20 segundos, 150 puntos
    CACTUS(false, false, 30000, 250); // Alterna espinas cada 30 segundos, 250 puntos

    private final boolean canMove;
    private final boolean canTeleport;
    private final int actionInterval;
    private final int score;

    FruitType(boolean canMove, boolean canTeleport, int actionInterval, int score) {
        this.canMove = canMove;
        this.canTeleport = canTeleport;
        this.actionInterval = actionInterval;
        this.score = score;
    }

    /**
     * Indica si esta fruta puede moverse a casillas adyacentes.
     */
    public boolean canMove() {
        return canMove;
    }

    /**
     * Indica si esta fruta puede teletransportarse.
     */
    public boolean canTeleport() {
        return canTeleport;
    }

    /**
     * Obtiene el intervalo de acción en milisegundos.
     */
    public int getActionInterval() {
        return actionInterval;
    }

    /**
     * Obtiene el puntaje que otorga esta fruta.
     */
    public int getScore() {
        return score;
    }

    /**
     * Verifica si esta fruta tiene alguna capacidad especial.
     */
    public boolean hasSpecialAbility() {
        return canMove || canTeleport;
    }
}