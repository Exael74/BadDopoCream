package domain.dto;

import domain.entity.IceBlock;

/**
 * Snapshot de un bloque de hielo para transferencia a la capa de presentación.
 */
public class IceBlockSnapshot extends EntitySnapshot {

    private boolean breaking;
    private int breakProgress;

    /**
     * Constructor privado. Usar Builder.
     */
    private IceBlockSnapshot(Builder builder) {
        this.position = builder.position;
        this.active = builder.active;
        this.breaking = builder.breaking;
        this.breakProgress = builder.breakProgress;
    }

    @Override
    public SnapshotType getType() {
        return SnapshotType.ICE_BLOCK;
    }

    /**
     * Crea un snapshot desde una entidad IceBlock.
     *
     * @param iceBlock Bloque de hielo del cual crear el snapshot
     * @return Snapshot del bloque de hielo
     */
    public static IceBlockSnapshot from(IceBlock iceBlock) {
        return new Builder()
                .position(iceBlock.getPosition())
                .active(true)
                .breaking(iceBlock.isBreaking())
                .breakProgress(iceBlock.getBreakProgress())
                .build();
    }

    // ==================== GETTERS ====================

    public boolean isBreaking() {
        return breaking;
    }

    public int getBreakProgress() {
        return breakProgress;
    }

    // ==================== BUILDER ====================

    /**
     * Builder para IceBlockSnapshot siguiendo el patrón Builder.
     */
    public static class Builder {
        private java.awt.Point position;
        private boolean active;
        private boolean breaking;
        private int breakProgress;

        public Builder position(java.awt.Point position) {
            this.position = new java.awt.Point(position);
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder breaking(boolean breaking) {
            this.breaking = breaking;
            return this;
        }

        public Builder breakProgress(int breakProgress) {
            this.breakProgress = breakProgress;
            return this;
        }

        public IceBlockSnapshot build() {
            return new IceBlockSnapshot(this);
        }
    }
}