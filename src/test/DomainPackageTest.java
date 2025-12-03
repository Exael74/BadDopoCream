package test;

import domain.*;
import domain.entity.*;
import domain.service.*;
import domain.state.GameState;
import org.junit.Test;
import org.junit.Assert;
import java.awt.Point;
import java.util.List;

public class DomainPackageTest {

    @Test
    public void testGameStateInitialValues() {
        GameState gs = new GameState("Chocolate", 1, 1);
        Assert.assertNotNull(gs.getPlayer());
        Assert.assertEquals(1, gs.getLevel());
        Assert.assertEquals(1, gs.getNumberOfPlayers());
        Assert.assertTrue(gs.getTimeRemaining() <= 180000);
    }

    @Test
    public void testAddEnemyFruitIce() {
        GameState gs = new GameState("Chocolate", 1, 1);
        Enemy e = new Enemy(new Point(1, 1), EnemyType.TROLL);
        Fruit f = new Fruit(new Point(2, 2), FruitType.UVA);
        IceBlock ice = new IceBlock(new Point(3, 3));
        gs.addEnemy(e);
        gs.addFruit(f);
        gs.addIceBlock(ice);
        Assert.assertTrue(gs.getEnemies().contains(e));
        Assert.assertTrue(gs.getFruits().contains(f));
        Assert.assertTrue(gs.getIceBlocks().contains(ice));
    }

    @Test
    public void testRemoveIceBlock() {
        GameState gs = new GameState("Chocolate", 1, 1);
        IceBlock ice = new IceBlock(new Point(4, 4));
        gs.addIceBlock(ice);
        Assert.assertTrue(gs.getIceBlocks().contains(ice));
        gs.removeIceBlock(ice);
        Assert.assertFalse(gs.getIceBlocks().contains(ice));
    }

    @Test
    public void testTimeUpdateCountsDown() {
        GameState gs = new GameState("Chocolate", 1, 1);
        long before = gs.getTimeRemaining();
        gs.updateTime(1000);
        Assert.assertTrue(gs.getTimeRemaining() < before);
    }

    @Test
    public void testPlayerMovementAndFacing() {
        Player p = new Player(new Point(5, 5), "Chocolate");
        p.move(Direction.RIGHT);
        Assert.assertEquals(Direction.RIGHT, p.getFacingDirection());
        p.move(Direction.IDLE);
        Assert.assertEquals(Direction.IDLE, p.getCurrentDirection());
    }

    @Test
    public void testPlayerSneezeAndKickStates() {
        Player p = new Player(new Point(6, 6), "Fresa");
        p.startSneeze();
        Assert.assertTrue(p.isSneezing());
        p.update(600);
        Assert.assertFalse(p.isSneezing());

        p.startKick();
        Assert.assertTrue(p.isKicking());
        p.update(500);
        Assert.assertFalse(p.isKicking());
    }

    @Test
    public void testPlayerDieFinalize() {
        Player p = new Player(new Point(6, 6), "Vainilla");
        p.die();
        Assert.assertTrue(p.isDying());
        p.update(2500);
        Assert.assertFalse(p.isDying());
        Assert.assertFalse(p.isAlive());
    }

    @Test
    public void testEnemyAssignBehaviorAndMovement() {
        Enemy t = new Enemy(new Point(1, 1), EnemyType.TROLL);
        Enemy m = new Enemy(new Point(2, 2), EnemyType.MACETA);
        Enemy c = new Enemy(new Point(3, 3), EnemyType.CALAMAR);
        Assert.assertNotNull(t.getType());
        t.changeDirection();
        Point next = t.getNextPosition();
        Assert.assertNotNull(next);
        m.chasePlayer(new Point(5, 5));
        c.chasePlayer(new Point(0, 0));
    }

    @Test
    public void testEnemyBreakIceAnimation() {
        Enemy c = new Enemy(new Point(4, 4), EnemyType.CALAMAR);
        c.startBreakIce();
        Assert.assertTrue(c.isBreakingIce());
        c.update(600);
        Assert.assertFalse(c.isBreakingIce());
    }

    @Test
    public void testCollisionDetectorValidPosition() {
        GameState gs = new GameState("Chocolate", 1, 1);
        CollisionDetector cd = new CollisionDetector(gs);
        Assert.assertTrue(cd.isValidPosition(new Point(0, 0)));
        Assert.assertFalse(cd.isValidPosition(new Point(-1, 0)));
    }

    @Test
    public void testCollisionDetectorEnemyCollision() {
        GameState gs = new GameState("Chocolate", 1, 1);
        Enemy e = new Enemy(gs.getPlayer().getPosition(), EnemyType.TROLL);
        gs.addEnemy(e);
        CollisionDetector cd = new CollisionDetector(gs);
        cd.checkCollisions();
        Assert.assertTrue(gs.isGameOver());
    }

    @Test
    public void testCollisionDetectorFruitCollection() {
        GameState gs = new GameState("Chocolate", 1, 1);
        Fruit f = new Fruit(new Point(6, 6), FruitType.UVA);
        gs.addFruit(f);
        CollisionDetector cd = new CollisionDetector(gs);
        cd.checkCollisions();
        Assert.assertTrue(f.isCollected());
        Assert.assertTrue(gs.getScore() > 0);
    }

    @Test
    public void testGameLogicSneezeCreatesIce() {
        GameState gs = new GameState("Chocolate", 1, 1);
        GameLogic gl = new GameLogic(gs);
        java.util.List<Point> created = gl.performIceSneeze();
        Assert.assertNotNull(created);
        Assert.assertTrue(gs.getIceBlocks().size() >= created.size());
    }

    @Test
    public void testGameLogicKickBreaksIce() {
        GameState gs = new GameState("Chocolate", 1, 1);
        Point p = gs.getPlayer().getPosition();
        Point ahead = new Point(p.x, Math.max(0, p.y - 1));
        IceBlock ice = new IceBlock(ahead);
        gs.addIceBlock(ice);
        GameLogic gl = new GameLogic(gs);
        java.util.List<Point> broken = gl.performIceKick();
        Assert.assertNotNull(broken);
    }

    @Test
    public void testAIControllerBasicUpdate() {
        GameState gs = new GameState("Chocolate", 1, 0);
        GameLogic gl = new GameLogic(gs);
        AIController ai = new AIController(gs, gl);
        ai.updateAI(500);
        Assert.assertNotNull(gs.getPlayer());
    }

    @Test
    public void testFindClosestFruitUtility() {
        GameState gs = new GameState("Chocolate", 1, 1);
        gs.addFruit(new Fruit(new Point(0, 0), FruitType.UVA));
        gs.addFruit(new Fruit(new Point(12, 12), FruitType.CEREZA));
        AIController ai = new AIController(gs, new GameLogic(gs));
        ai.updateAI(500);
    }

    @Test
    public void testPersistenceSaveLoadRoundtrip() throws Exception {
        GameState gs = new GameState("Chocolate", 2, 1);
        PersistenceService ps = new PersistenceService();
        String name = ps.saveGame(gs);
        Assert.assertNotNull(name);
        GameState loaded = ps.loadGame(name);
        Assert.assertNotNull(loaded);
        Assert.assertEquals(gs.getLevel(), loaded.getLevel());
    }

    @Test
    public void testEntityEqualsHash() {
        Entity e1 = new Enemy(new Point(1, 2), EnemyType.TROLL);
        Entity e2 = new Enemy(new Point(1, 2), EnemyType.MACETA);
        Assert.assertEquals(e1, e2);
        Assert.assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    public void testGameFacadeInitAndGetters() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertEquals(1, gf.getCurrentLevel());
        Assert.assertEquals(1, gf.getNumberOfPlayers());
        Assert.assertFalse(gf.isPaused());
    }

    @Test
    public void testFacadeMovementCommands() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.movePlayerUp();
        gf.movePlayerLeft();
        gf.stopPlayer();
    }

    @Test
    public void testFacadeSnapshotsNonNull() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertNotNull(gf.getPlayerSnapshot());
        Assert.assertNotNull(gf.getEnemySnapshots());
        Assert.assertNotNull(gf.getFruitSnapshots());
        Assert.assertNotNull(gf.getIceBlockSnapshots());
    }

    @Test
    public void testCountRemainingFruitsAndUniqueTypes() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        java.util.List<String> types = gf.getUniqueFruitTypes();
        Assert.assertNotNull(types);
    }

    @Test
    public void testTogglePause() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        boolean before = gf.isPaused();
        gf.togglePause();
        Assert.assertNotEquals(before, gf.isPaused());
    }

    @Test
    public void testShouldRestartLevel() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.shouldRestartLevel());
    }

    @Test
    public void testGameStateScoreManipulation() {
        GameState gs = new GameState("Chocolate", 1, 1);
        gs.addScore(10);
        Assert.assertEquals(10, gs.getScore());
        gs.addScorePlayer2(5);
        Assert.assertEquals(5, gs.getScorePlayer2());
    }

    @Test
    public void testIceBlockProperties() {
        IceBlock ice = new IceBlock(new Point(2, 3));
        Assert.assertFalse(ice.isPermanent());
        ice.startBreaking();
        Assert.assertTrue(ice.isBreaking());
    }

    @Test
    public void testFruitPropertiesAndCollect() {
        Fruit f = new Fruit(new Point(7, 7), FruitType.PIÑA);
        Assert.assertFalse(f.isCollected());
        f.collect();
        Assert.assertTrue(f.isCollected());
    }

    @Test
    public void testEnemyControlledFlag() {
        Enemy e = new Enemy(new Point(1, 1), EnemyType.MACETA);
        Assert.assertFalse(e.isControlledByPlayer());
        e.setControlledByPlayer(true);
        Assert.assertTrue(e.isControlledByPlayer());
    }

    @Test
    public void testEntityMoveToAndIsAt() {
        Enemy e = new Enemy(new Point(1, 1), EnemyType.TROLL);
        e.moveTo(new Point(5, 5));
        Assert.assertTrue(e.isAt(new Point(5, 5)));
    }

    @Test
    public void testPlayerBusyStateDuringCelebration() {
        Player p = new Player(new Point(6, 6), "Chocolate");
        p.startCelebration();
        Assert.assertTrue(p.isBusy());
    }

    @Test
    public void testCollisionDetectorIsPlayerAt() {
        GameState gs = new GameState("Chocolate", 1, 1);
        CollisionDetector cd = new CollisionDetector(gs);
        Assert.assertTrue(cd.isPlayerAt(gs.getPlayer().getPosition()));
    }

}
