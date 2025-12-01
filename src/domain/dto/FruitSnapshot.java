package domain.dto;

import domain.entity.Fruit;

/**
 * Snapshot de una fruta para transferencia a la capa de presentación.
 */
public class FruitSnapshot extends EntitySnapshot {

    private String fruitType;
    private boolean collected;

    /**
     * Constructor privado. Usar Builder.
     */
    private FruitSnapshot(Builder builder) {
        this.position = builder.position;
        this.active = builder.active;
        this.fruitType = builder.fruitType;
        this.collected = builder.collected;
    }

    @Override
    public SnapshotType getType() {
        return SnapshotType.FRUIT;
    }

    /**
     * Crea un snapshot desde una entidad Fruit.
     *
     * @param fruit Fruta de la cual crear el snapshot
     * @return Snapshot de la fruta
     */
    public static FruitSnapshot from(Fruit fruit) {
        return new Builder()
                .position(fruit.getPosition())
                .active(!fruit.isCollected())
                .fruitType(fruit.getType().toString())
                .collected(fruit.isCollected())
                .build();
    }

    // ==================== GETTERS ====================

    public String getFruitType() {
        return fruitType;
    }

    public boolean isCollected() {
        return collected;
    }

    // ==================== BUILDER ====================

    /**
     * Builder para FruitSnapshot siguiendo el patrón Builder.
     */
    public static class Builder {
        private java.awt.Point position;
        private boolean active;
        private String fruitType;
        private boolean collected;

        public Builder position(java.awt.Point position) {
            this.position = new java.awt.Point(position);
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder fruitType(String fruitType) {
            this.fruitType = fruitType;
            return this;
        }

        public Builder collected(boolean collected) {
            this.collected = collected;
            return this;
        }

        public FruitSnapshot build() {
            return new FruitSnapshot(this);
        }
    }
}