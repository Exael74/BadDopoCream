package domain.dto;

import domain.entity.Player;
import domain.entity.Direction;

/**
 * Snapshot de un jugador para transferencia a la capa de presentación.
 */
public class PlayerSnapshot extends EntitySnapshot {

    private String characterType;
    private String name;
    private String direction;
    private boolean moving;
    private boolean sneezing;
    private boolean kicking;
    private boolean dying;
    private boolean celebrating;

    /**
     * Constructor privado. Usar Builder.
     */
    private PlayerSnapshot(Builder builder) {
        this.position = builder.position;
        this.active = builder.active;
        this.characterType = builder.characterType;
        this.name = builder.name;
        this.direction = builder.direction;
        this.moving = builder.moving;
        this.sneezing = builder.sneezing;
        this.kicking = builder.kicking;
        this.dying = builder.dying;
        this.celebrating = builder.celebrating;
    }

    @Override
    public SnapshotType getType() {
        return SnapshotType.PLAYER;
    }

    /**
     * Crea un snapshot desde una entidad Player.
     *
     * @param player Jugador del cual crear el snapshot
     * @return Snapshot del jugador
     */
    public static PlayerSnapshot from(Player player) {
        Direction dir = player.getCurrentDirection();
        if (dir == Direction.IDLE) {
            dir = player.getFacingDirection();
        }

        return new Builder()
                .position(player.getPosition())
                .active(player.isAlive() || player.isDying())
                .characterType(player.getCharacterType())
                .name(player.getName())
                .direction(dir.toString())
                .moving(player.getCurrentDirection() != Direction.IDLE)
                .sneezing(player.isSneezing())
                .kicking(player.isKicking())
                .dying(player.isDying())
                .celebrating(player.isCelebrating())
                .build();
    }

    // ==================== GETTERS ====================

    public String getCharacterType() {
        return characterType;
    }

    public String getName() {
        return name;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isMoving() {
        return moving;
    }

    public boolean isSneezing() {
        return sneezing;
    }

    public boolean isKicking() {
        return kicking;
    }

    public boolean isDying() {
        return dying;
    }

    public boolean isCelebrating() {
        return celebrating;
    }

    // ==================== BUILDER ====================

    /**
     * Builder para PlayerSnapshot siguiendo el patrón Builder.
     */
    public static class Builder {
        private java.awt.Point position;
        private boolean active;
        private String characterType;
        private String name;
        private String direction;
        private boolean moving;
        private boolean sneezing;
        private boolean kicking;
        private boolean dying;
        private boolean celebrating;

        public Builder position(java.awt.Point position) {
            this.position = new java.awt.Point(position);
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder characterType(String characterType) {
            this.characterType = characterType;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public Builder moving(boolean moving) {
            this.moving = moving;
            return this;
        }

        public Builder sneezing(boolean sneezing) {
            this.sneezing = sneezing;
            return this;
        }

        public Builder kicking(boolean kicking) {
            this.kicking = kicking;
            return this;
        }

        public Builder dying(boolean dying) {
            this.dying = dying;
            return this;
        }

        public Builder celebrating(boolean celebrating) {
            this.celebrating = celebrating;
            return this;
        }

        public PlayerSnapshot build() {
            return new PlayerSnapshot(this);
        }
    }
}