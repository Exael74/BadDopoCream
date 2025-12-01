package domain.entity;

import domain.dto.EntitySnapshot;
import domain.dto.IceBlockSnapshot;
import java.awt.Point;

/**
 * Entidad que representa un bloque de hielo en el juego.
 * Puede ser roto por el jugador o enemigos (Calamar).
 */
public class IceBlock extends Entity {

    private boolean breaking;
    private long breakStartTime;
    private boolean permanent;

    private static final long BREAK_ANIMATION_DURATION = 300;

    /**
     * Constructor de bloque de hielo no permanente.
     *
     * @param position Posición del bloque
     */
    public IceBlock(Point position) {
        this(position, false);
    }

    /**
     * Constructor de bloque de hielo.
     *
     * @param position Posición del bloque
     * @param permanent Si es permanente (no se puede romper)
     */
    public IceBlock(Point position, boolean permanent) {
        super(position);
        this.breaking = false;
        this.breakStartTime = 0;
        this.permanent = permanent;
    }

    @Override
    public void update(int deltaTime) {
        // No requiere actualización por frame, solo por tiempo real
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ICE_BLOCK;
    }

    @Override
    public EntitySnapshot createSnapshot() {
        return IceBlockSnapshot.from(this);
    }

    // ==================== RUPTURA ====================

    /**
     * Inicia la secuencia de ruptura del bloque.
     */
    public void startBreaking() {
        if (!breaking && !permanent) {
            breaking = true;
            breakStartTime = System.currentTimeMillis();
        }
    }

    /**
     * Verifica si el bloque ha terminado de romperse.
     */
    public boolean isFullyBroken() {
        if (!breaking || permanent) return false;
        return System.currentTimeMillis() - breakStartTime >= BREAK_ANIMATION_DURATION;
    }

    /**
     * Obtiene el progreso de ruptura (0-100).
     */
    public int getBreakProgress() {
        if (!breaking || permanent) return 0;
        long elapsed = System.currentTimeMillis() - breakStartTime;
        return (int) Math.min(100, (elapsed * 100) / BREAK_ANIMATION_DURATION);
    }

    // ==================== GETTERS ====================

    public boolean isBreaking() {
        return breaking;
    }

    public boolean isPermanent() {
        return permanent;
    }
}