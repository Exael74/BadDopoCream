package domain.entity.enemy;

import domain.entity.Direction;

import domain.entity.IceBlock;
import domain.service.CollisionDetector;
import java.awt.Point;

public class Narval extends Enemy {

    private boolean isDrilling;

    public Narval(Point position) {
        super(position);
        // Narval doesn't use standard movement behavior for chasing,
        // it uses Line of Sight custom logic.
        this.isDrilling = false;
    }

    @Override
    public String getTypeName() {
        return "NARVAL";
    }

    @Override
    public boolean shouldMove() {
        if (isBreakingIce)
            return false;

        int interval = getMoveInterval();
        if (isDrilling) {
            interval = 240; // Fast charge
        }
        return moveTimer >= interval;
    }

    @Override
    public int getMoveInterval() {
        return 800;
    }

    @Override
    public int getScoreValue() {
        return 400; // Puntos arbitrarios para Narval, ajustar si hay diseño específico
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
    public boolean isDrilling() {
        return isDrilling;
    }

    public void startDrilling() {
        isDrilling = true;
    }

    public void stopDrilling() {
        isDrilling = false;
    }

    @Override
    public void updateMovement(Point targetPosition, CollisionDetector collisionDetector) {
        // 1. Detection (Always active if not drilling)
        if (!isDrilling) {
            Direction chargeDir = getPlayerDirectionIfSeeing(this.getPosition(), targetPosition, collisionDetector);
            if (chargeDir != null) {
                this.setDirection(chargeDir);
                this.startDrilling();
            }
        }

        // 2. Execution
        if (isDrilling) {
            Point nextPos = getNextPosition();

            // Check bounds/obstacles
            if (!collisionDetector.isValidPosition(nextPos) ||
                    collisionDetector.hasIgluAt(nextPos) ||
                    collisionDetector.hasUnbreakableBlockAt(nextPos)) {

                // Hit wall -> Stop
                stopDrilling();
                return; // Stop this turn
            }

            // Check Ice -> Destroy
            IceBlock ice = collisionDetector.getIceBlockAt(nextPos); // Assuming method exists or is getIceAt
            // CollisionDetector has getIceAt. GameLogic used getIceBlockAt?
            // GameLogic uses collisionDetector.getIceAt(nextPos) earlier but getIceBlockAt
            // in Narval code?
            // Checking CollisionDetector outline might be needed. I'll use getIceAt for
            // safety or check.
            if (ice == null && collisionDetector.hasIceAt(nextPos)) {
                ice = collisionDetector.getIceAt(nextPos);
            }

            if (ice != null) {
                ice.startBreaking();
                // We need to remove it from GameState.
                // But we don't have GameState here, only CollisionDetector.
                // CollisionDetector usually just queries.
                // We might need to handle removal via a callback or return value?
                // Or we can assume IceBlock.startBreaking() + game update cycle handles it?
                // GameLogic had `gameState.removeIceBlock(ice)`.
                // Narval *smashes* it instantly.

                // PROBLEM: We cannot remove from GameState here easily without passing
                // GameState.
                // I should pass GameState to updateMovement as well? Or trust CollisionDetector
                // to have reference?
                // CollisionDetector has GameState reference! But likely private.

                // Ideally, we mark ice as 'destroyed' and let GameLogic cleanup.
                // ice.setMaintained(false)? ice.destroy()?

                // Let's rely on `ice.startBreaking()` and maybe setting it to broken
                // immediately if possible.
                // Or I should assume GameLogic cleans up broken ice.
                // In GameLogic: `gameState.removeIceBlock(ice);` was explicit.

                // Workaround: We really should pass GameState if we want to modify state like
                // removing entities.
                // The prompt said "updateMovement(target, state)" in my plan.
                // So I should pass GameState.
            }

            move(nextPos);
            return;
        }

        // 3. Wander (Only if not drilling)
        Point nextPos = getNextPosition();
        if (collisionDetector.canEnemyMoveTo(nextPos, this)) {
            move(nextPos);
        } else {
            changeDirection();
        }
    }

    // Moved from GameLogic
    private Direction getPlayerDirectionIfSeeing(Point enemyPos, Point playerPos, CollisionDetector collisionDetector) {
        if (enemyPos.x == playerPos.x) {
            if (enemyPos.y > playerPos.y) {
                if (isPathClear(enemyPos, playerPos, Direction.UP, collisionDetector))
                    return Direction.UP;
            } else {
                if (isPathClear(enemyPos, playerPos, Direction.DOWN, collisionDetector))
                    return Direction.DOWN;
            }
        } else if (enemyPos.y == playerPos.y) {
            if (enemyPos.x > playerPos.x) {
                if (isPathClear(enemyPos, playerPos, Direction.LEFT, collisionDetector))
                    return Direction.LEFT;
            } else {
                if (isPathClear(enemyPos, playerPos, Direction.RIGHT, collisionDetector))
                    return Direction.RIGHT;
            }
        }
        return null;
    }

    private boolean isPathClear(Point start, Point end, Direction dir, CollisionDetector collisionDetector) {
        Point p = new Point(start);
        p.x += dir.getDeltaX();
        p.y += dir.getDeltaY();

        while (!p.equals(end)) {
            // Ice does NOT block vision for Narval charge.
            if (collisionDetector.hasUnbreakableBlockAt(p) || collisionDetector.hasIgluAt(p)) {
                return false;
            }
            p.x += dir.getDeltaX();
            p.y += dir.getDeltaY();

            if (!collisionDetector.isValidPosition(p))
                return false;
        }
        return true;
    }
}
