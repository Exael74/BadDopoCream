package domain.dto;

import domain.entity.Fogata;
import java.awt.Point;
import java.io.Serializable;

public class FogataSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Point position;
    private final boolean active;

    public FogataSnapshot(Point position, boolean active) {
        this.position = position;
        this.active = active;
    }

    public static FogataSnapshot from(Fogata fogata) {
        return new FogataSnapshot(fogata.getPosition(), fogata.isActive());
    }

    public Point getPosition() {
        return position;
    }

    public boolean isActive() {
        return active;
    }
}
