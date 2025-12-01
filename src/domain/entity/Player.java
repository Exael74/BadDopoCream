package domain.entity;

import domain.dto.EntitySnapshot;
import domain.dto.PlayerSnapshot;
import java.awt.Point;

/**
 * Entidad que representa al jugador en el juego.
 * Maneja movimiento, direcciones, estados (sneezing, kicking, dying) y animaciones.
 */
public class Player extends Entity {

    private Direction currentDirection;
    private Direction facingDirection;
    private boolean alive;
    private String characterType;

    // Estados de animación
    private boolean sneezing;
    private int sneezeTimer;
    private boolean kicking;
    private int kickTimer;
    private boolean dying;
    private int deathTimer;
    private boolean celebrating;

    // Constantes de duración de animaciones
    private static final int SNEEZE_DURATION = 500;
    private static final int KICK_DURATION = 400;
    private static final int DEATH_DURATION = 2000;

    /**
     * Constructor del jugador.
     *
     * @param initialPosition Posición inicial en el grid
     * @param characterType Tipo de personaje ("Chocolate", "Fresa", "Vainilla")
     */
    public Player(Point initialPosition, String characterType) {
        super(initialPosition);
        this.characterType = characterType;
        this.currentDirection = Direction.IDLE;
        this.facingDirection = Direction.DOWN;
        this.alive = true;
        this.sneezing = false;
        this.sneezeTimer = 0;
        this.kicking = false;
        this.kickTimer = 0;
        this.dying = false;
        this.deathTimer = 0;
        this.celebrating = false;
    }

    @Override
    public void update(int deltaTime) {
        updateSneeze(deltaTime);
        updateKick(deltaTime);
        updateDeath(deltaTime);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PLAYER;
    }

    @Override
    public EntitySnapshot createSnapshot() {
        return PlayerSnapshot.from(this);
    }

    // ==================== MOVIMIENTO ====================

    /**
     * Mueve al jugador en la dirección especificada.
     *
     * @param direction Dirección del movimiento
     */
    public void move(Direction direction) {
        if (!alive || sneezing || kicking || dying || celebrating) return;

        this.currentDirection = direction;

        if (direction != Direction.IDLE) {
            this.facingDirection = direction;
        }

        position.x += direction.getDeltaX();
        position.y += direction.getDeltaY();
    }

    /**
     * Detiene el movimiento del jugador.
     */
    public void stopMoving() {
        this.currentDirection = Direction.IDLE;
    }

    // ==================== ACCIONES ====================

    /**
     * Inicia la animación de estornudo (crear hielo).
     */
    public void startSneeze() {
        this.sneezing = true;
        this.sneezeTimer = SNEEZE_DURATION;
        this.currentDirection = Direction.IDLE;
    }

    /**
     * Inicia la animación de patada (romper hielo).
     */
    public void startKick() {
        this.kicking = true;
        this.kickTimer = KICK_DURATION;
        this.currentDirection = Direction.IDLE;
    }

    /**
     * Inicia la secuencia de muerte del jugador.
     */
    public void die() {
        this.dying = true;
        this.deathTimer = DEATH_DURATION;
        this.currentDirection = Direction.IDLE;
    }

    /**
     * Inicia la animación de celebración (victoria).
     */
    public void startCelebration() {
        this.celebrating = true;
        this.currentDirection = Direction.IDLE;
    }

    // ==================== ACTUALIZACIÓN DE ESTADOS ====================

    /**
     * Actualiza el estado de estornudo.
     */
    private void updateSneeze(int deltaTime) {
        if (sneezing) {
            sneezeTimer -= deltaTime;
            if (sneezeTimer <= 0) {
                sneezing = false;
                sneezeTimer = 0;
            }
        }
    }

    /**
     * Actualiza el estado de patada.
     */
    private void updateKick(int deltaTime) {
        if (kicking) {
            kickTimer -= deltaTime;
            if (kickTimer <= 0) {
                kicking = false;
                kickTimer = 0;
            }
        }
    }

    /**
     * Actualiza el estado de muerte.
     */
    private void updateDeath(int deltaTime) {
        if (dying) {
            deathTimer -= deltaTime;
            if (deathTimer <= 0) {
                finalizeDeath();
            }
        }
    }

    /**
     * Finaliza la muerte del jugador.
     */
    private void finalizeDeath() {
        this.alive = false;
        this.dying = false;
        this.active = false;
    }

    // ==================== GETTERS ====================

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public boolean isAlive() {
        return alive;
    }

    public String getCharacterType() {
        return characterType;
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

    /**
     * Verifica si el jugador está ocupado realizando una acción.
     */
    public boolean isBusy() {
        return sneezing || kicking || dying || celebrating;
    }
}