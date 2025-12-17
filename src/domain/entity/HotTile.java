package domain.entity;

import java.awt.Point;
import java.io.Serializable;

/**
 * Entidad que representa una baldosa caliente en el juego.
 * Mata al jugador de helado al contacto, pero no afecta a los enemigos.
 * No se puede romper ni colocar hielo sobre ella.
 */
public class HotTile implements Serializable {

    private static final long serialVersionUID = 1L;

    private Point position;

    /**
     * Constructor de baldosa caliente.
     *
     * @param position Posición de la baldosa
     */
    public HotTile(Point position) {
        this.position = new Point(position);
    }

    /**
     * Obtiene la posición de la baldosa.
     *
     * @return Copia de la posición
     */
    public Point getPosition() {
        return new Point(position);
    }
}
