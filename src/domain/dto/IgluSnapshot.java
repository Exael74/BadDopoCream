package domain.dto;

import domain.entity.Iglu;
import java.awt.Point;
import java.io.Serializable;

/**
 * Snapshot inmutable del Iglú para la capa de presentación.
 */
public class IgluSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Point position;
    private final int width;
    private final int height;

    private IgluSnapshot(Point position, int width, int height) {
        this.position = new Point(position);
        this.width = width;
        this.height = height;
    }

    public static IgluSnapshot from(Iglu iglu) {
        if (iglu == null)
            return null;
        return new IgluSnapshot(iglu.getPosition(), iglu.getWidth(), iglu.getHeight());
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
}
