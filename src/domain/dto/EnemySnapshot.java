package domain.dto;

import domain.entity.Enemy;

/**
 * Snapshot de un enemigo para transferencia a la capa de presentación.
 */
public class EnemySnapshot extends EntitySnapshot {

    private String enemyType;
    private String direction;
    private boolean controlledByPlayer;
    private boolean breakingIce;

    /**
     * Constructor privado. Usar Builder.
     */
    private EnemySnapshot(Builder builder) {
        this.position = builder.position;
        this.active = builder.active;
        this.enemyType = builder.enemyType;
        this.direction = builder.direction;
        this.controlledByPlayer = builder.controlledByPlayer;
        this.breakingIce = builder.breakingIce;
    }

    @Override
    public SnapshotType getType() {
        return SnapshotType.ENEMY;
    }

    /**
     * Crea un snapshot desde una entidad Enemy.
     *
     * @param enemy Enemigo del cual crear el snapshot
     * @return Snapshot del enemigo
     */
    public static EnemySnapshot from(Enemy enemy) {
        return new Builder()
                .position(enemy.getPosition())
                .active(enemy.isActive())
                .enemyType(enemy.getType().toString())
                .direction(enemy.getCurrentDirection().toString())
                .controlledByPlayer(enemy.isControlledByPlayer())
                .breakingIce(enemy.isBreakingIce())
                .build();
    }

    // ==================== GETTERS ====================

    public String getEnemyType() {
        return enemyType;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isControlledByPlayer() {
        return controlledByPlayer;
    }

    public boolean isBreakingIce() {
        return breakingIce;
    }

    // ==================== BUILDER ====================

    /**
     * Builder para EnemySnapshot siguiendo el patrón Builder.
     */
    public static class Builder {
        private java.awt.Point position;
        private boolean active;
        private String enemyType;
        private String direction;
        private boolean controlledByPlayer;
        private boolean breakingIce;

        public Builder position(java.awt.Point position) {
            this.position = new java.awt.Point(position);
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder enemyType(String enemyType) {
            this.enemyType = enemyType;
            return this;
        }

        public Builder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public Builder controlledByPlayer(boolean controlledByPlayer) {
            this.controlledByPlayer = controlledByPlayer;
            return this;
        }

        public Builder breakingIce(boolean breakingIce) {
            this.breakingIce = breakingIce;
            return this;
        }

        public EnemySnapshot build() {
            return new EnemySnapshot(this);
        }
    }
}