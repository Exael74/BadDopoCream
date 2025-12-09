package domain.entity;

import domain.behavior.ChaseMovement;
import domain.behavior.MovementBehavior;
import domain.behavior.RandomMovement;
import domain.dto.EnemySnapshot;
import domain.dto.EntitySnapshot;
import java.awt.Point;
import java.util.Random;

/**
 * Entidad que representa un enemigo en el juego.
 * Diferentes tipos de enemigos tienen diferentes comportamientos de movimiento.
 */
public class Enemy extends Entity {

    private EnemyType type;
    private Direction currentDirection;
    private int moveTimer;
    private Random random;
    private boolean controlledByPlayer;
    private Point targetPosition;
    private int stuckCounter;
    private Point lastPosition;

    // Animación de romper hielo (solo Calamar)
    private boolean isBreakingIce;
    private int breakIceTimer;
    private static final int BREAK_ICE_DURATION = 500;

    // Animacion de taladrar (solo Narval)
    private boolean isDrilling;

    // Estrategia de movimiento
    private MovementBehavior movementBehavior;

    /**
     * Constructor del enemigo.
     *
     * @param position Posición inicial
     * @param type     Tipo de enemigo
     */
    public Enemy(Point position, EnemyType type) {
        super(position);
        this.type = type;
        this.random = new Random();
        this.currentDirection = getRandomDirection();
        this.moveTimer = 0;
        this.controlledByPlayer = false;
        this.targetPosition = null;
        this.stuckCounter = 0;
        this.lastPosition = new Point(position);
        this.isBreakingIce = false;
        this.breakIceTimer = 0;
        this.isDrilling = false;

        // Asignar comportamiento según tipo
        assignMovementBehavior();
    }

    /**
     * Asigna la estrategia de movimiento según el tipo de enemigo.
     */
    private void assignMovementBehavior() {
        if (type == EnemyType.TROLL) {
            this.movementBehavior = new RandomMovement();
        } else {
            // MACETA y CALAMAR usan persecución
            this.movementBehavior = new ChaseMovement();
        }
    }

    @Override
    public void update(int deltaTime) {
        if (!controlledByPlayer) {
            moveTimer += deltaTime;
        }

        // Actualizar animación de romper hielo
        if (isBreakingIce) {
            breakIceTimer += deltaTime;
            if (breakIceTimer >= BREAK_ICE_DURATION) {
                isBreakingIce = false;
                breakIceTimer = 0;
            }
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ENEMY;
    }

    @Override
    public EntitySnapshot createSnapshot() {
        return EnemySnapshot.from(this);
    }

    // ==================== MOVIMIENTO ====================

    /**
     * Calcula la siguiente posición según la dirección actual.
     */
    public Point getNextPosition() {
        Point next = new Point(position);
        next.x += currentDirection.getDeltaX();
        next.y += currentDirection.getDeltaY();
        return next;
    }

    /**
     * Calcula la dirección para perseguir al jugador.
     * Delega el cálculo a la estrategia de movimiento.
     *
     * @param playerPosition Posición del jugador
     */
    public void chasePlayer(Point playerPosition) {
        this.targetPosition = new Point(playerPosition);
        this.currentDirection = movementBehavior.calculateDirection(
                position, playerPosition, stuckCounter, random);
    }

    /**
     * Mueve el enemigo a la nueva posición.
     *
     * @param newPosition Nueva posición
     */
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

    /**
     * Invierte la dirección actual del enemigo.
     */
    public void reverseDirection() {
        currentDirection = currentDirection.getOpposite();
        this.moveTimer = 0;
        stuckCounter++;
    }

    /**
     * Cambia a una dirección aleatoria.
     */
    public void changeDirection() {
        currentDirection = getRandomDirection();
        this.moveTimer = 0;
        stuckCounter++;
    }

    /**
     * Establece la dirección del enemigo.
     */
    public void setDirection(Direction direction) {
        this.currentDirection = direction;
    }

    /**
     * Obtiene una dirección aleatoria (excluyendo IDLE).
     */
    private Direction getRandomDirection() {
        Direction[] directions = { Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT };
        return directions[random.nextInt(directions.length)];
    }

    // ==================== ACCIONES ====================

    /**
     * Inicia la animación de romper hielo (solo Calamar/Narval).
     */
    public void startBreakIce() {
        if (type.canBreakIce()) {
            this.isBreakingIce = true;
            this.breakIceTimer = 0;
        }
    }

    public void startDrilling() {
        if (type == EnemyType.NARVAL) {
            this.isDrilling = true;
        }
    }

    public void stopDrilling() {
        this.isDrilling = false;
    }

    public boolean isDrilling() {
        return isDrilling;
    }

    // ==================== ESTADO ====================

    /**
     * Verifica si el enemigo debe moverse según su intervalo.
     */
    public boolean shouldMove() {
        int interval = type.getMoveInterval();
        if (isDrilling) {
            interval = 120; // Fast charge (halved form 60)
        }
        return moveTimer >= interval;
    }

    /**
     * Resetea el contador de atasco.
     */
    public void resetStuckCounter() {
        this.stuckCounter = 0;
    }

    // ==================== GETTERS Y SETTERS ====================

    public EnemyType getType() {
        return type;
    }

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
}