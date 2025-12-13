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
        Point nextPos = getNextPosition();

        if (collisionDetector.isValidPosition(nextPos) &&
                !collisionDetector.isPositionBlocked(nextPos) &&
                !collisionDetector.hasOtherEnemyAt(nextPos, this)) { // Assuming cast works or fix later
            move(nextPos);
        } else {
            boolean moved = false;
            int attempts = 0;

            while (!moved && attempts < 4) {
                // Force re-calculation (ChaseMovement handles randomness so calling again might
                // yield diff result?
                // Actually ChaseBehavior uses random for "close" or "alternative".
                // Calling chasePlayer again updates currentDirection.

                chasePlayer(targetPosition);
                nextPos = getNextPosition();

                if (collisionDetector.isValidPosition(nextPos) &&
                        !collisionDetector.isPositionBlocked(nextPos) &&
                        !collisionDetector.hasOtherEnemyAt(nextPos, this)) {
                    move(nextPos);
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
