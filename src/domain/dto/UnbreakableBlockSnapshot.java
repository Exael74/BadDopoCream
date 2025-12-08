package domain.dto;

import domain.entity.UnbreakableBlock;
import java.awt.Point;
import java.io.Serializable;

/**
 * Snapshot inmutable de bloque irrompible para la capa de presentación.
 */
public class UnbreakableBlockSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Point position;

    private UnbreakableBlockSnapshot(Point position) {
        this.position = new Point(position);
    }

    public static UnbreakableBlockSnapshot from(UnbreakableBlock block) {
        return new UnbreakableBlockSnapshot(block.getPosition());
    }

    public Point getPosition() {
        return new Point(position);
    }
}
