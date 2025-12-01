package domain.entity;

import domain.dto.EntitySnapshot;
import domain.dto.FruitSnapshot;
import java.awt.Point;
import java.util.Random;

/**
 * Entidad que representa una fruta en el juego.
 * Las frutas pueden tener habilidades especiales según su tipo.
 */
public class Fruit extends Entity {

    private FruitType type;
    private boolean collected;
    private int moveTimer;
    private Random random;

    /**
     * Constructor de la fruta.
     *
     * @param position Posición inicial
     * @param type Tipo de fruta
     */
    public Fruit(Point position, FruitType type) {
        super(position);
        this.type = type;
        this.collected = false;
        this.moveTimer = 0;
        this.random = new Random();
    }

    @Override
    public void update(int deltaTime) {
        if (collected) return;

        if (type.hasSpecialAbility()) {
            moveTimer += deltaTime;
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.FRUIT;
    }

    @Override
    public EntitySnapshot createSnapshot() {
        return FruitSnapshot.from(this);
    }

    // ==================== MOVIMIENTO ====================

    /**
     * Verifica si la fruta debe moverse/teletransportarse.
     */
    public boolean shouldMove() {
        if (type.canTeleport()) {
            return moveTimer >= type.getActionInterval();
        }
        return type.canMove() && moveTimer >= type.getActionInterval();
    }

    /**
     * Mueve la fruta a una nueva posición.
     *
     * @param newPosition Nueva posición
     */
    public void move(Point newPosition) {
        this.position = new Point(newPosition);
        this.moveTimer = 0;
    }

    /**
     * Obtiene una posición adyacente aleatoria.
     * Usado por frutas que se mueven (PIÑA).
     */
    public Point getRandomAdjacentPosition() {
        Point[] adjacentPositions = {
                new Point(position.x, position.y - 1), // UP
                new Point(position.x, position.y + 1), // DOWN
                new Point(position.x - 1, position.y), // LEFT
                new Point(position.x + 1, position.y)  // RIGHT
        };

        return adjacentPositions[random.nextInt(adjacentPositions.length)];
    }

    // ==================== RECOLECCIÓN ====================

    /**
     * Marca la fruta como recolectada.
     */
    public void collect() {
        this.collected = true;
        this.active = false;
    }

    // ==================== GETTERS ====================

    public FruitType getType() {
        return type;
    }

    public boolean isCollected() {
        return collected;
    }

    /**
     * Verifica si puede moverse (delegado al tipo).
     */
    public boolean canMove() {
        return type.canMove();
    }

    /**
     * Verifica si puede teletransportarse (delegado al tipo).
     */
    public boolean canTeleport() {
        return type.canTeleport();
    }
}