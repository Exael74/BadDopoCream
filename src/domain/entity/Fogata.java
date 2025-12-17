package domain.entity;

import java.awt.Point;
import java.io.Serializable;

/**
 * Entidad que representa una Fogata.
 * - Activa: Mata al jugador al contacto.
 * - Inactiva (Apagada): No hace daño. Se apaga al ponerle hielo encima.
 * - Se reactiva automáticamente después de 10 segundos.
 */
public class Fogata implements Serializable {
    private static final long serialVersionUID = 1L;

    private Point position;
    private boolean active;
    private int cooldownTimer;
    private static final int COOLDOWN_DURATION = 10000; // 10 segundos

    public Fogata(Point position) {
        this.position = new Point(position);
        this.active = true;
        this.cooldownTimer = 0;
    }

    public void update(int deltaTime) {
        if (!active) {
            cooldownTimer += deltaTime;
            if (cooldownTimer >= COOLDOWN_DURATION) {
                active = true;
                cooldownTimer = 0;
            }
        }
    }

    public void extinguish() {
        this.active = false;
        this.cooldownTimer = 0;
    }

    public Point getPosition() {
        return new Point(position);
    }

    public boolean isActive() {
        return active;
    }
}
