package domain.dto;

import java.awt.Point;

/**
 * Clase abstracta base para todos los snapshots de entidades.
 * Los snapshots son DTOs (Data Transfer Objects) que transfieren información
 * desde el dominio hacia la capa de presentación sin exponer las entidades
 * directamente.
 */
public abstract class EntitySnapshot {

    protected Point position;
    protected boolean active;
    protected String id;

    /**
     * Constructor protegido para uso de subclases.
     */
    protected EntitySnapshot() {
    }

    /**
     * Obtiene el tipo de snapshot.
     *
     * @return Tipo de snapshot
     */
    public abstract SnapshotType getType();

    // ==================== GETTERS COMUNES ====================

    /**
     * Obtiene la posición de la entidad.
     *
     * @return Copia de la posición
     */
    public Point getPosition() {
        return new Point(position);
    }

    /**
     * Verifica si la entidad está activa.
     *
     * @return true si está activa
     */
    public boolean isActive() {
        return active;
    }

    public String getId() {
        return id;
    }
}
