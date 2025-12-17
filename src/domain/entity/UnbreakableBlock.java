package domain.entity;

import java.awt.Point;
import java.io.Serializable;

/**
 * Entidad que representa un bloque irrompible (borde del mapa).
 */
public class UnbreakableBlock implements Serializable {

    private static final long serialVersionUID = 1L;

    private Point position;

    public UnbreakableBlock(Point position) {
        this.position = new Point(position);
    }

    public Point getPosition() {
        return new Point(position);
    }
}
