package domain.entity.enemy;

import domain.service.CollisionDetector;
import java.awt.Point;

public class Troll extends Enemy {

    public Troll(Point position) {
        super(position);
    }

    @Override
    public String getTypeName() {
        return "TROLL";
    }

    @Override
    public int getMoveInterval() {
        return 1000;
    }

    @Override
    public int getScoreValue() {
        return 100;
    }

    @Override
    public void updateMovement(Point targetPosition, CollisionDetector collisionDetector) {
        // Trolls just wander by default (or RandomBehavior determines direction,
        // but default movement simply moves forward or changes direction)

        // Actually, logic in GameLogic just called processDefaultMovement for
        // Non-Chasers.
        // Troll is Non-Chaser.
        // So we use processDefaultMovement which uses currentDirection.

        // Wait, RandomMovement behavior `calculateDirection` chooses a random
        // direction.
        // But `processDefaultMovement` moves in current direction until blocked, THEN
        // changes direction.
        // Let's verify loop in GameLogic.
        // processEnemyMovement checks `shouldChasePlayer`. TROLL returns false. call
        // processDefaultMovement.

        processDefaultMovement(collisionDetector);
    }
}
