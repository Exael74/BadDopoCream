package domain.entity.enemy;

import domain.behavior.MovementBehavior;
import domain.dto.EnemySnapshot;
import domain.dto.EntitySnapshot;
import domain.entity.Direction;

import domain.entity.Entity;
import domain.entity.EntityType;
import domain.service.CollisionDetector;
import java.awt.Point;
import java.util.Random;

/**
 * Entidad abstracta que representa un enemigo en el juego.
 * Base para la jerarquía escalable de enemigos.
 */
public abstract class Enemy extends Entity {

    // protected EnemyType type; // Removed
    protected Direction currentDirection;
    protected int moveTimer;
    protected Random random;
    protected boolean controlledByPlayer;
    protected Point targetPosition;
    protected int stuckCounter;
    protected Point lastPosition;

    // Estrategia de movimiento (Strategy Pattern)
    protected MovementBehavior movementBehavior;

    // Animación de romper hielo
    protected boolean isBreakingIce;
    protected int breakIceTimer;
    protected static final int BREAK_ICE_DURATION = 500;

    /**
     * Constructor base.
     */
    public Enemy(Point position) {
        super(position);
        // this.type = type; // Removed
        this.random = new Random();
        this.currentDirection = getRandomDirection();
        this.moveTimer = 0;
        this.controlledByPlayer = false;
        this.targetPosition = null;
        this.stuckCounter = 0;
        this.lastPosition = new Point(position);
        this.isBreakingIce = false;
        this.breakIceTimer = 0;
    }

    /**
     * Actualiza la lógica de movimiento específica del enemigo.
     * Reemplaza a los métodos processXMovement de GameLogic.
     */
    public abstract void updateMovement(Point targetPosition, CollisionDetector collisionDetector);

    @Override
    public void update(int deltaTime) {
        if (!controlledByPlayer) {
            if (!isBreakingIce) {
                moveTimer += deltaTime;
            }
        }

        if (isBreakingIce) {
            breakIceTimer += deltaTime;
            if (breakIceTimer >= BREAK_ICE_DURATION) {
                isBreakingIce = false;
                breakIceTimer = 0;
                moveTimer = 0;
            }
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ENEMY;
    }

    @Override
    public EntitySnapshot createSnapshot() {
        return EnemySnapshot.from(this); // Necesitaremos actualizar EnemySnapshot o usar casting/adapter si Enemy
                                         // cambia de paquete
        // Nota: EnemySnapshot espera domain.entity.Enemy. Tendremos que arreglar esto.
    }

    // ==================== MOVIMIENTO COMÚN ====================

    public Point getNextPosition() {
        Point next = new Point(position);
        next.x += currentDirection.getDeltaX();
        next.y += currentDirection.getDeltaY();
        return next;
    }

    public void chasePlayer(Point playerPosition) {
        this.targetPosition = new Point(playerPosition);
        if (movementBehavior != null) {
            this.currentDirection = movementBehavior.calculateDirection(
                    position, playerPosition, stuckCounter, random);
        }
    }

    public void move(Point newPosition) {
        if (position.equals(lastPosition)) {
            stuckCounter++;
        } else {
            stuckCounter = 0;
            lastPosition = new Point(position);
        }

        this.position = new Point(newPosition);
        this.moveTimer = 0;
    }

    public void reverseDirection() {
        currentDirection = currentDirection.getOpposite();
        this.moveTimer = 0;
        stuckCounter++;
    }

    public void changeDirection() {
        currentDirection = getRandomDirection();
        this.moveTimer = 0;
        stuckCounter++;
    }

    public void processDefaultMovement(CollisionDetector collisionDetector) {
        Point nextPos = getNextPosition();

        if (collisionDetector.isValidPosition(nextPos) &&
                !collisionDetector.isPositionBlocked(nextPos) &&
                !collisionDetector.hasOtherEnemyAt(nextPos, this)) { // "this" is Entity, might need override in
                                                                     // CollisionDetector to accept
                                                                     // domain.entity.enemy.Enemy
            move(nextPos);
        } else {
            changeDirection();
        }
    }

    public void setDirection(Direction direction) {
        this.currentDirection = direction;
    }

    protected Direction getRandomDirection() {
        Direction[] directions = { Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT };
        return directions[random.nextInt(directions.length)];
    }

    // ==================== ABSTRACT CONFIGURATION ====================

    /**
     * @return Intervalo de movimiento en milisegundos.
     */
    public abstract int getMoveInterval();

    /**
     * @return Puntos que otorga al morir.
     */
    public abstract int getScoreValue();

    /**
     * @return true si puede romper hielo.
     */
    public boolean canBreakIce() {
        return false;
    }

    /**
     * @return true si debe perseguir al jugador.
     */
    public boolean shouldChasePlayer() {
        return false;
    }

    // ==================== ACCIONES ====================

    public void startBreakIce() {
        if (canBreakIce()) {
            this.isBreakingIce = true;
            this.breakIceTimer = 0;
        }
    }

    // ==================== ESTADO ====================

    public boolean shouldMove() {
        if (isBreakingIce)
            return false;

        return moveTimer >= getMoveInterval();
    }

    public void resetStuckCounter() {
        this.stuckCounter = 0;
    }

    // ==================== GETTERS Y SETTERS ====================

    /**
     * @return El nombre del tipo de enemigo (ej. "TROLL", "MACETA").
     */
    public abstract String getTypeName();

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public int getMoveTimer() {
        return moveTimer;
    }

    public boolean isControlledByPlayer() {
        return controlledByPlayer;
    }

    public void setControlledByPlayer(boolean controlled) {
        this.controlledByPlayer = controlled;
    }

    public Point getTargetPosition() {
        return targetPosition;
    }

    public int getStuckCounter() {
        return stuckCounter;
    }

    public boolean isBreakingIce() {
        return isBreakingIce;
    }

    // Abstract hook for drilling (only Narval uses it, but base Enemy had
    // accessors)
    public boolean isDrilling() {
        return false;
    }
}
