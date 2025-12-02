package test;

import domain.*;
import domain.entity.*;
import domain.service.*;
import domain.state.GameState;
import org.junit.Test;
import java.awt.Point;
import java.util.List;
import static org.junit.Assert.*;

public class DomainPackageTest {

    @Test
    public void testGameStateInitialValues() {
        GameState gs = new GameState("Chocolate", 1, 1);
        assertNotNull(gs.getPlayer());
        assertEquals(1, gs.getLevel());
        assertEquals(1, gs.getNumberOfPlayers());
        assertTrue(gs.getTimeRemaining() <= 180000);
    }

    @Test
    public void testAddEnemyFruitIce() {
        GameState gs = new GameState("Chocolate", 1, 1);
        Enemy e = new Enemy(new Point(1,1), EnemyType.TROLL);
        Fruit f = new Fruit(new Point(2,2), FruitType.UVA);
        IceBlock ice = new IceBlock(new Point(3,3));
        gs.addEnemy(e);
        gs.addFruit(f);
        gs.addIceBlock(ice);
        assertTrue(gs.getEnemies().contains(e));
        assertTrue(gs.getFruits().contains(f));
        assertTrue(gs.getIceBlocks().contains(ice));
    }

    @Test
    public void testRemoveIceBlock() {
        GameState gs = new GameState("Chocolate", 1, 1);
        IceBlock ice = new IceBlock(new Point(4,4));
        gs.addIceBlock(ice);
        assertTrue(gs.getIceBlocks().contains(ice));
        gs.removeIceBlock(ice);
        assertFalse(gs.getIceBlocks().contains(ice));
    }

    @Test
    public void testTimeUpdateCountsDown() {
        GameState gs = new GameState("Chocolate", 1, 1);
        long before = gs.getTimeRemaining();
        gs.updateTime(1000);
        assertTrue(gs.getTimeRemaining() < before);
    }

    @Test
    public void testPlayerMovementAndFacing() {
        Player p = new Player(new Point(5,5), "Chocolate");
        p.move(Direction.RIGHT);
        assertEquals(Direction.RIGHT, p.getFacingDirection());
        p.move(Direction.IDLE);
        assertEquals(Direction.IDLE, p.getCurrentDirection());
    }

    @Test
    public void testPlayerSneezeAndKickStates() {
        Player p = new Player(new Point(6,6), "Fresa");
        p.startSneeze();
        assertTrue(p.isSneezing());
        p.update(600);
        assertFalse(p.isSneezing());

        p.startKick();
        assertTrue(p.isKicking());
        p.update(500);
        assertFalse(p.isKicking());
    }

    @Test
    public void testPlayerDieFinalize() {
        Player p = new Player(new Point(6,6), "Vainilla");
        p.die();
        assertTrue(p.isDying());
        p.update(2500);
        assertFalse(p.isDying());
        assertFalse(p.isAlive());
    }

    @Test
    public void testEnemyAssignBehaviorAndMovement() {
        Enemy t = new Enemy(new Point(1,1), EnemyType.TROLL);
        Enemy m = new Enemy(new Point(2,2), EnemyType.MACETA);
        Enemy c = new Enemy(new Point(3,3), EnemyType.CALAMAR);
        assertNotNull(t.getType());
        t.changeDirection();
        Point next = t.getNextPosition();
        assertNotNull(next);
        m.chasePlayer(new Point(5,5));
        c.chasePlayer(new Point(0,0));
    }

    @Test
    public void testEnemyBreakIceAnimation() {
        Enemy c = new Enemy(new Point(4,4), EnemyType.CALAMAR);
        c.startBreakIce();
        assertTrue(c.isBreakingIce());
        c.update(600);
        assertFalse(c.isBreakingIce());
    }

    @Test
    public void testCollisionDetectorValidPosition() {
        GameState gs = new GameState("Chocolate", 1, 1);
        CollisionDetector cd = new CollisionDetector(gs);
        assertTrue(cd.isValidPosition(new Point(0,0)));
        assertFalse(cd.isValidPosition(new Point(-1,0)));
    }

    @Test
    public void testCollisionDetectorEnemyCollision() {
        GameState gs = new GameState("Chocolate",1,1);
        Enemy e = new Enemy(gs.getPlayer().getPosition(), EnemyType.TROLL);
        gs.addEnemy(e);
        CollisionDetector cd = new CollisionDetector(gs);
        cd.checkCollisions();
        assertTrue(gs.isGameOver());
    }

    @Test
    public void testCollisionDetectorFruitCollection() {
        GameState gs = new GameState("Chocolate",1,1);
        Fruit f = new Fruit(new Point(6,6), FruitType.UVA);
        gs.addFruit(f);
        CollisionDetector cd = new CollisionDetector(gs);
        cd.checkCollisions();
        assertTrue(f.isCollected());
        assertTrue(gs.getScore() > 0);
    }

    @Test
    public void testGameLogicSneezeCreatesIce() {
        GameState gs = new GameState("Chocolate",1,1);
        GameLogic gl = new GameLogic(gs);
        java.util.List<Point> created = gl.performIceSneeze();
        assertNotNull(created);
        assertTrue(gs.getIceBlocks().size() >= created.size());
    }

    @Test
    public void testGameLogicKickBreaksIce() {
        GameState gs = new GameState("Chocolate",1,1);
        Point p = gs.getPlayer().getPosition();
        Point ahead = new Point(p.x, Math.max(0, p.y-1));
        IceBlock ice = new IceBlock(ahead);
        gs.addIceBlock(ice);
        GameLogic gl = new GameLogic(gs);
        java.util.List<Point> broken = gl.performIceKick();
        assertNotNull(broken);
    }

    @Test
    public void testAIControllerBasicUpdate() {
        GameState gs = new GameState("Chocolate",1,0);
        GameLogic gl = new GameLogic(gs);
        AIController ai = new AIController(gs, gl);
        ai.updateAI(500);
        assertNotNull(gs.getPlayer());
    }

    @Test
    public void testFindClosestFruitUtility() {
        GameState gs = new GameState("Chocolate",1,1);
        gs.addFruit(new Fruit(new Point(0,0), FruitType.UVA));
        gs.addFruit(new Fruit(new Point(12,12), FruitType.CEREZA));
        AIController ai = new AIController(gs, new GameLogic(gs));
        ai.updateAI(500);
    }

    @Test
    public void testPersistenceSaveLoadRoundtrip() throws Exception {
        GameState gs = new GameState("Chocolate",2,1);
        PersistenceService ps = new PersistenceService();
        String name = ps.saveGame(gs);
        assertNotNull(name);
        GameState loaded = ps.loadGame(name);
        assertNotNull(loaded);
        assertEquals(gs.getLevel(), loaded.getLevel());
    }

    @Test
    public void testEntityEqualsHash() {
        Entity e1 = new Enemy(new Point(1,2), EnemyType.TROLL);
        Entity e2 = new Enemy(new Point(1,2), EnemyType.MACETA);
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    public void testGameFacadeInitAndGetters() {
        GameFacade gf = new GameFacade("Chocolate",1,1);
        assertEquals(1, gf.getCurrentLevel());
        assertEquals(1, gf.getNumberOfPlayers());
        assertFalse(gf.isPaused());
    }

    @Test
    public void testFacadeMovementCommands() {
        GameFacade gf = new GameFacade("Chocolate",1,1);
        gf.movePlayerUp();
        gf.movePlayerLeft();
        gf.stopPlayer();
    }

    @Test
    public void testFacadeSnapshotsNonNull() {
        GameFacade gf = new GameFacade("Chocolate",1,1);
        assertNotNull(gf.getPlayerSnapshot());
        assertNotNull(gf.getEnemySnapshots());
        assertNotNull(gf.getFruitSnapshots());
        assertNotNull(gf.getIceBlockSnapshots());
    }

    @Test
    public void testCountRemainingFruitsAndUniqueTypes() {
        GameFacade gf = new GameFacade("Chocolate",1,1);
        java.util.List<String> types = gf.getUniqueFruitTypes();
        assertNotNull(types);
    }

    @Test
    public void testTogglePause() {
        GameFacade gf = new GameFacade("Chocolate",1,1);
        boolean before = gf.isPaused();
        gf.togglePause();
        assertNotEquals(before, gf.isPaused());
    }

    @Test
    public void testShouldRestartLevel() {
        GameFacade gf = new GameFacade("Chocolate",1,1);
        assertFalse(gf.shouldRestartLevel());
    }

    @Test
    public void testGameStateScoreManipulation() {
        GameState gs = new GameState("Chocolate",1,1);
        gs.addScore(10);
        assertEquals(10, gs.getScore());
        gs.addScorePlayer2(5);
        assertEquals(5, gs.getScorePlayer2());
    }

    @Test
    public void testIceBlockProperties() {
        IceBlock ice = new IceBlock(new Point(2,3));
        assertFalse(ice.isPermanent());
        ice.startBreaking();
        assertTrue(ice.isBreaking());
    }

    @Test
    public void testFruitPropertiesAndCollect() {
        Fruit f = new Fruit(new Point(7,7), FruitType.PIÑA);
        assertFalse(f.isCollected());
        f.collect();
        assertTrue(f.isCollected());
    }

    @Test
    public void testEnemyControlledFlag() {
        Enemy e = new Enemy(new Point(1,1), EnemyType.MACETA);
        assertFalse(e.isControlledByPlayer());
        e.setControlledByPlayer(true);
        assertTrue(e.isControlledByPlayer());
    }

    @Test
    public void testEntityMoveToAndIsAt() {
        Enemy e = new Enemy(new Point(1,1), EnemyType.TROLL);
        e.moveTo(new Point(5,5));
        assertTrue(e.isAt(new Point(5,5)));
    }

    @Test
    public void testPlayerBusyStateDuringCelebration() {
        Player p = new Player(new Point(6,6), "Chocolate");
        p.startCelebration();
        assertTrue(p.isBusy());
    }

    @Test
    public void testCollisionDetectorIsPlayerAt() {
        GameState gs = new GameState("Chocolate",1,1);
        CollisionDetector cd = new CollisionDetector(gs);
        assertTrue(cd.isPlayerAt(gs.getPlayer().getPosition()));
    }

}
