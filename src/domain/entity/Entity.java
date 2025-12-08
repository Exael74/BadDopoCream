package domain.entity;

import domain.dto.EntitySnapshot;
import java.awt.Point;
import java.io.Serializable;

/**
 * Clase abstracta base para todas las entidades del juego.
 * Proporciona comportamiento común y define el contrato que deben cumplir
 * todas las entidades.
 */
public abstract class Entity implements Serializable {

    protected Point position;
    protected boolean active;
    protected String id;

    /**
     * Constructor base para todas las entidades.
     *
     * @param position Posición inicial de la entidad
     */
    public Entity(Point position) {
        this.position = new Point(position);
        this.active = true;
        this.id = java.util.UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    /**
     * Actualiza el estado de la entidad según el tiempo transcurrido.
     *
     * @param deltaTime Tiempo transcurrido en milisegundos desde la última
     *                  actualización
     */
    public abstract void update(int deltaTime);

    /**
     * Obtiene el tipo de entidad.
     *
     * @return Tipo de entidad
     */
    public abstract EntityType getEntityType();

    /**
     * Crea un snapshot (DTO) de esta entidad para transferir a la capa de
     * presentación.
     *
     * @return Snapshot de la entidad
     */
    public abstract EntitySnapshot createSnapshot();

    // ==================== GETTERS Y SETTERS COMUNES ====================

    /**
     * Obtiene la posición actual de la entidad.
     *
     * @return Copia de la posición
     */
    public Point getPosition() {
        return new Point(position);
    }

    /**
     * Establece la posición de la entidad.
     *
     * @param position Nueva posición
     */
    public void setPosition(Point position) {
        this.position = new Point(position);
    }

    /**
     * Verifica si la entidad está activa.
     *
     * @return true si está activa, false en caso contrario
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Establece el estado de actividad de la entidad.
     *
     * @param active Nuevo estado de actividad
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Mueve la entidad a una nueva posición.
     *
     * @param newPosition Nueva posición
     */
    public void moveTo(Point newPosition) {
        this.position = new Point(newPosition);
    }

    /**
     * Verifica si esta entidad está en la posición especificada.
     *
     * @param pos Posición a verificar
     * @return true si la entidad está en esa posición
     */
    public boolean isAt(Point pos) {
        return position.equals(pos);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Entity entity = (Entity) obj;
        return position.equals(entity.position);
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }
}