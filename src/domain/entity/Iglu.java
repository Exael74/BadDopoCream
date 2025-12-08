package domain.entity;

import java.awt.Point;
import java.io.Serializable;

/**
 * Entidad que representa un iglú central en el juego.
 * Estructura de 4x4 que actúa como obstáculo irrompible.
 */
public class Iglu implements Serializable {

    private static final long serialVersionUID = 1L;

    // Posición superior izquierda del iglú
    private Point position;
    private int width;
    private int height;

    /**
     * Constructor del iglú.
     *
     * @param position Posición superior izquierda
     */
    public Iglu(Point position) {
        this.position = new Point(position);
        this.width = 3; // 3 celdas de ancho
        this.height = 3; // 3 celdas de alto
    }

    public Point getPosition() {
        return new Point(position);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Verifica si una posición dada colisiona con el iglú.
     */
    public boolean collidesWith(Point p) {
        return p.x >= position.x && p.x < position.x + width &&
                p.y >= position.y && p.y < position.y + height;
    }
}
