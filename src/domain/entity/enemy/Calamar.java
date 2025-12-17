package domain.entity.enemy;

import domain.behavior.ChaseMovement;

import domain.entity.IceBlock;
import domain.service.CollisionDetector;
import java.awt.Point;

public class Calamar extends Enemy {

    public Calamar(Point position) {
        super(position);
        this.movementBehavior = new ChaseMovement();
    }

    @Override
    public String getTypeName() {
        return "CALAMAR";
    }

    @Override
    public int getMoveInterval() {
        return 700;
    }

    @Override
    public int getScoreValue() {
        return 300;
    }

    @Override
    public boolean canBreakIce() {
        return true;
    }

    @Override
    public boolean shouldChasePlayer() {
        return true;
    }

    @Override
    public void updateMovement(Point targetPosition, CollisionDetector collisionDetector) {
        chasePlayer(targetPosition);
        Point nextPos = getNextPosition();

        // Si hay hielo en el camino, romperlo
        if (collisionDetector.isValidPosition(nextPos) && collisionDetector.hasIceAt(nextPos)) {
            IceBlock ice = collisionDetector.getIceAt(nextPos);
            if (ice != null) {
                ice.startBreaking();
                startBreakIce();
                domain.BadDopoLogger.logInfo("✓ Calamar IA rompió hielo automáticamente");
            }
        }
        // Si no hay hielo, intentar moverse
        else if (!tryMove(collisionDetector)) {
            changeDirection();
        }
    }
}
