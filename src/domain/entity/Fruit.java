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
    private FruitState state;
    private boolean collected;
    private int moveTimer;
    private int animationTimer;
    private Random random;

    /**
     * Constructor de la fruta.
     *
     * @param position Posición inicial
     * @param type     Tipo de fruta
     */
    public Fruit(Point position, FruitType type) {
        super(position);
        this.type = type;
        this.state = FruitState.SPAWNING;
        this.collected = false;
        this.random = new Random();
        this.moveTimer = random.nextInt(1000); // Desynchronize movement by up to 1 second
    }

    @Override
    public void update(int deltaTime) {
        if (collected && state == FruitState.COLLECTED) {
            animationTimer += deltaTime;
            if (animationTimer >= 1000) { // 1.0s collection animation (Slower)
                active = false; // Finally remove from game
            }
            return;
        }

        animationTimer += deltaTime;

        switch (state) {
            case SPAWNING:
                if (animationTimer >= 1000) { // 1 second spawn animation
                    state = FruitState.IDLE;
                    animationTimer = 0;
                }
                break;
            case IDLE:
                if (type == FruitType.CACTUS) {
                    // Cactus cycle: Idle (30s) -> Warning
                    if (animationTimer >= type.getActionInterval()) {
                        state = FruitState.SPIKES_WARNING;
                        animationTimer = 0;
                    }
                } else if (type == FruitType.CEREZA) {
                    // Cherry Teleport Trigger
                    if (shouldMove()) {
                        state = FruitState.TELEPORT_OUT;
                        animationTimer = 0;
                        moveTimer = 0; // Reset move timer
                    }
                }
                break;
            case SPIKES_WARNING:
                if (animationTimer >= 2000) { // 2 seconds warning
                    state = FruitState.SPIKES_ACTIVE;
                    animationTimer = 0;
                }
                break;
            case SPIKES_ACTIVE:
                // Lethal state (30s)
                if (animationTimer >= type.getActionInterval()) {
                    state = FruitState.SPIKES_COOLDOWN;
                    animationTimer = 0;
                }
                break;
            case SPIKES_COOLDOWN:
                if (animationTimer >= 1000) { // 1 second cooldown anim
                    state = FruitState.IDLE;
                    animationTimer = 0;
                }
                break;
            case TELEPORT_OUT:
                if (animationTimer >= 500) { // Teleport out duration
                    state = FruitState.TELEPORT_IN;
                    animationTimer = 0;
                    // Actual teleport moves happens via GameLogic calling move()
                }
                break;
            case TELEPORT_IN:
                if (animationTimer >= 500) { // Teleport in duration
                    state = FruitState.IDLE;
                    animationTimer = 0;
                }
                break;
            case COLLECTED:
                // Handled in separate if block at top of update()
                break;
        }

        if (type.hasSpecialAbility() && state == FruitState.IDLE) {
            moveTimer += deltaTime;
        }
    }

    public boolean isLethal() {
        return state == FruitState.SPIKES_ACTIVE;
    }

    public FruitState getState() {
        return state;
    }

    public void setState(FruitState state) {
        this.state = state;
        this.animationTimer = 0;
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
        // For standard moving fruits (like Pineapple), we don't trigger state change
        // here
        // Logic handled in GameLogic::moveFruitsAfterPlayerTurn
        return false;
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
                new Point(position.x + 1, position.y) // RIGHT
        };

        return adjacentPositions[random.nextInt(adjacentPositions.length)];
    }

    // ==================== RECOLECCIÓN ====================

    /**
     * Marca la fruta como recolectada.
     */
    public void collect() {
        this.collected = true;
        this.state = FruitState.COLLECTED;
        // logic will keep it active until animation finishes or handled by GameLogic
        // cleanup
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