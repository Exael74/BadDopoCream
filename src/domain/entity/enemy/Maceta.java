package domain.entity.enemy;

import domain.behavior.ChaseMovement;

import domain.service.CollisionDetector;
import java.awt.Point;

public class Maceta extends Enemy {

    public Maceta(Point position) {
        super(position);
        this.movementBehavior = new ChaseMovement();
    }

    @Override
    public String getTypeName() {
        return "MACETA";
    }

    @Override
    public int getMoveInterval() {
        return 600;
    }

    @Override
    public int getScoreValue() {
        return 200;
    }

    @Override
    public boolean shouldChasePlayer() {
        return true;
    }

    @Override
    public void updateMovement(Point targetPosition, CollisionDetector collisionDetector) {
        chasePlayer(targetPosition);

        if (tryMove(collisionDetector)) {
            // Moved successfully
        } else {
            boolean moved = false;
            int attempts = 0;

            while (!moved && attempts < 4) {
                chasePlayer(targetPosition);
                // tryMove uses getNextPosition internaly based on new direction from
                // chasePlayer
                if (tryMove(collisionDetector)) {
                    resetStuckCounter();
                    moved = true;
                } else {
                    changeDirection(); // Random change
                    attempts++;
                }
            }

            if (!moved) {
                resetStuckCounter();
            }
        }
    }
}
