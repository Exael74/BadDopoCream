package domain.dto;

import domain.entity.HotTile;
import java.awt.Point;
import java.io.Serializable;

/**
 * Snapshot inmutable de una baldosa caliente para la capa de presentación.
 */
public class HotTileSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Point position;

    private HotTileSnapshot(Point position) {
        this.position = new Point(position);
    }

    /**
     * Crea un snapshot desde una baldosa caliente.
     *
     * @param hotTile Baldosa caliente
     * @return Snapshot inmutable
     */
    public static HotTileSnapshot from(HotTile hotTile) {
        return new HotTileSnapshot(hotTile.getPosition());
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
