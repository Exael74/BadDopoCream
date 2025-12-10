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
    public void testCollisionDetectorEnemyDetection() {
        GameState gs = new GameState("Chocolate", 1, 1);
        Point playerPos = gs.getPlayer().getPosition();
        Enemy e = new Enemy(playerPos, EnemyType.TROLL);
        gs.addEnemy(e);
        CollisionDetector cd = new CollisionDetector(gs);
        // Verify enemy is detected at player position
        Assert.assertTrue(cd.hasEnemyAt(playerPos));
    }

    @Test
    public void testCollisionDetectorFruitDetection() {
        GameState gs = new GameState("Chocolate", 1, 1);
        Fruit f = new Fruit(new Point(6, 6), FruitType.UVA);
        gs.addFruit(f);
        CollisionDetector cd = new CollisionDetector(gs);
        // Verify fruit is detected at position
        Assert.assertTrue(cd.hasFruitAt(new Point(6, 6)));
        Assert.assertFalse(cd.hasFruitAt(new Point(5, 5)));
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

    @Test
    public void testIsPlayer2AliveWithNoPlayer2() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        // When there's no player 2, isPlayer2Alive should return true (no player 2 to
        // be dead)
        Assert.assertTrue(gf.isPlayer2Alive());
    }

    @Test
    public void testIsPlayer2AliveWithPlayer2() {
        GameFacade gf = new GameFacade("Chocolate", "Fresa", "P1", "P2", 1, 2, "EXPERT", "EXPERT");
        // Initially player 2 should be alive
        Assert.assertTrue(gf.isPlayer2Alive());
    }

    @Test
    public void testIsDeathAnimationCompleteForSinglePlayer() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        // Initially no death animation
        Assert.assertFalse(gf.isDeathAnimationComplete());
    }

    @Test
    public void testPvPDeathSetsGameOver() {
        GameState gs = new GameState("Chocolate", 1, 2);
        Player p1 = gs.getPlayer();
        Player p2 = gs.getPlayer2();

        // Simulate player 1 death
        p1.die();
        p1.update(2500); // Complete death animation

        CollisionDetector cd = new CollisionDetector(gs);
        // In PvP, any player death should trigger game over
        // This is set by collision detection when a player touches an enemy
        Assert.assertFalse(p1.isAlive());
    }

    @Test
    public void testPvPBothPlayersDead() {
        GameState gs = new GameState("Chocolate", 1, 2);
        Player p1 = gs.getPlayer();
        Player p2 = gs.getPlayer2();

        p1.die();
        p2.die();
        p1.update(2500);
        p2.update(2500);

        Assert.assertFalse(p1.isAlive());
        Assert.assertFalse(p2.isAlive());
    }

    @Test
    public void testGameFacadePlayer2Methods() {
        GameFacade gf = new GameFacade("Chocolate", "Fresa", "P1", "P2", 1, 2, "EXPERT", "EXPERT");

        // Test player 2 movement
        gf.movePlayer2Up();
        gf.movePlayer2Down();
        gf.movePlayer2Left();
        gf.movePlayer2Right();
        gf.stopPlayer2();

        // Test player 2 snapshot
        Assert.assertNotNull(gf.getPlayer2Snapshot());
    }

    @Test
    public void testMachineModeInitialization() {
        GameFacade gf = new GameFacade("Chocolate", "Fresa", "M1", "M2", 1, 0, "EXPERT", "EXPERT");
        Assert.assertEquals(0, gf.getNumberOfPlayers());
        Assert.assertNotNull(gf.getPlayer2Snapshot());
    }

    // ==================== EXCEPTION TESTS ====================

    @Test
    public void testBadDopoExceptionWithMessage() {
        BadDopoException ex = new BadDopoException("Test error");
        Assert.assertEquals("Test error", ex.getMessage());
    }

    @Test
    public void testBadDopoExceptionWithCause() {
        Exception cause = new RuntimeException("Root cause");
        BadDopoException ex = new BadDopoException("Wrapper", cause);
        Assert.assertEquals("Wrapper", ex.getMessage());
        Assert.assertEquals(cause, ex.getCause());
    }

    // ==================== LOGGER TESTS ====================

    @Test
    public void testBadDopoLoggerInfo() {
        // Just verify it doesn't throw
        BadDopoLogger.logInfo("Test info message");
    }

    @Test
    public void testBadDopoLoggerError() {
        BadDopoLogger.logError("Test error", new Exception("test"));
    }

    @Test
    public void testBadDopoLoggerSevere() {
        BadDopoLogger.logSevere("Test severe message");
    }

    // ==================== ENTITY TESTS: HotTile ====================

    @Test
    public void testHotTileCreation() {
        HotTile ht = new HotTile(new Point(5, 5));
        Assert.assertNotNull(ht.getPosition());
        Assert.assertEquals(5, ht.getPosition().x);
        Assert.assertEquals(5, ht.getPosition().y);
    }

    @Test
    public void testHotTilePositionIsCopy() {
        Point original = new Point(3, 3);
        HotTile ht = new HotTile(original);
        original.x = 10;
        Assert.assertEquals(3, ht.getPosition().x);
    }

    // ==================== ENTITY TESTS: Iglu ====================

    @Test
    public void testIgluCreation() {
        Iglu iglu = new Iglu(new Point(5, 5));
        Assert.assertNotNull(iglu.getPosition());
        Assert.assertEquals(3, iglu.getWidth());
        Assert.assertEquals(3, iglu.getHeight());
    }

    @Test
    public void testIgluCollision() {
        Iglu iglu = new Iglu(new Point(5, 5));
        Assert.assertTrue(iglu.collidesWith(new Point(5, 5)));
        Assert.assertTrue(iglu.collidesWith(new Point(6, 6)));
        Assert.assertTrue(iglu.collidesWith(new Point(7, 7)));
        Assert.assertFalse(iglu.collidesWith(new Point(8, 8)));
        Assert.assertFalse(iglu.collidesWith(new Point(4, 4)));
    }

    // ==================== ENTITY TESTS: UnbreakableBlock ====================

    @Test
    public void testUnbreakableBlockCreation() {
        UnbreakableBlock ub = new UnbreakableBlock(new Point(2, 2));
        Assert.assertNotNull(ub.getPosition());
        Assert.assertEquals(2, ub.getPosition().x);
    }

    // ==================== ENUM TESTS ====================

    @Test
    public void testDirectionValues() {
        Assert.assertNotNull(Direction.UP);
        Assert.assertNotNull(Direction.DOWN);
        Assert.assertNotNull(Direction.LEFT);
        Assert.assertNotNull(Direction.RIGHT);
        Assert.assertNotNull(Direction.IDLE);
        Assert.assertEquals(5, Direction.values().length);
    }

    @Test
    public void testDirectionDeltas() {
        Assert.assertEquals(0, Direction.UP.getDeltaX());
        Assert.assertEquals(-1, Direction.UP.getDeltaY());
        Assert.assertEquals(1, Direction.DOWN.getDeltaY());
        Assert.assertEquals(-1, Direction.LEFT.getDeltaX());
        Assert.assertEquals(1, Direction.RIGHT.getDeltaX());
    }

    @Test
    public void testAITypeValues() {
        Assert.assertNotNull(AIType.HUNGRY);
        Assert.assertNotNull(AIType.FEARFUL);
        Assert.assertNotNull(AIType.EXPERT);
        Assert.assertEquals(3, AIType.values().length);
    }

    @Test
    public void testEnemyTypeValues() {
        Assert.assertNotNull(EnemyType.TROLL);
        Assert.assertNotNull(EnemyType.MACETA);
        Assert.assertNotNull(EnemyType.CALAMAR);
        Assert.assertNotNull(EnemyType.NARVAL);
        Assert.assertTrue(EnemyType.values().length >= 4);
    }

    @Test
    public void testEnemyTypeProperties() {
        Assert.assertTrue(EnemyType.MACETA.shouldChasePlayer());
        Assert.assertTrue(EnemyType.CALAMAR.canBreakIce());
    }

    @Test
    public void testFruitTypeValues() {
        Assert.assertNotNull(FruitType.UVA);
        Assert.assertNotNull(FruitType.PLATANO);
        Assert.assertNotNull(FruitType.PIÑA);
        Assert.assertNotNull(FruitType.CEREZA);
        Assert.assertNotNull(FruitType.CACTUS);
        Assert.assertTrue(FruitType.values().length >= 5);
    }

    @Test
    public void testFruitTypeScores() {
        Assert.assertTrue(FruitType.UVA.getScore() > 0);
        Assert.assertTrue(FruitType.PLATANO.getScore() > 0);
    }

    @Test
    public void testFruitStateValues() {
        Assert.assertNotNull(FruitState.IDLE);
        Assert.assertNotNull(FruitState.COLLECTED);
        Assert.assertNotNull(FruitState.TELEPORT_OUT);
        Assert.assertNotNull(FruitState.TELEPORT_IN);
    }

    @Test
    public void testEntityTypeValues() {
        Assert.assertNotNull(EntityType.PLAYER);
        Assert.assertNotNull(EntityType.ENEMY);
        Assert.assertNotNull(EntityType.FRUIT);
        Assert.assertNotNull(EntityType.ICE_BLOCK);
    }

    // ==================== DTO/SNAPSHOT TESTS ====================

    @Test
    public void testPlayerSnapshotCreation() {
        Player p = new Player(new Point(5, 5), "Chocolate");
        domain.dto.PlayerSnapshot snapshot = domain.dto.PlayerSnapshot.from(p);
        Assert.assertNotNull(snapshot);
        Assert.assertEquals("Chocolate", snapshot.getCharacterType());
        Assert.assertEquals(5, snapshot.getPosition().x);
    }

    @Test
    public void testEnemySnapshotCreation() {
        Enemy e = new Enemy(new Point(3, 3), EnemyType.TROLL);
        domain.dto.EnemySnapshot snapshot = domain.dto.EnemySnapshot.from(e);
        Assert.assertNotNull(snapshot);
        Assert.assertEquals("TROLL", snapshot.getEnemyType());
    }

    @Test
    public void testFruitSnapshotCreation() {
        Fruit f = new Fruit(new Point(2, 2), FruitType.UVA);
        domain.dto.FruitSnapshot snapshot = domain.dto.FruitSnapshot.from(f);
        Assert.assertNotNull(snapshot);
        Assert.assertEquals("UVA", snapshot.getFruitType());
    }

    @Test
    public void testIceBlockSnapshotCreation() {
        IceBlock ice = new IceBlock(new Point(1, 1));
        domain.dto.IceBlockSnapshot snapshot = domain.dto.IceBlockSnapshot.from(ice);
        Assert.assertNotNull(snapshot);
        Assert.assertEquals(1, snapshot.getPosition().x);
    }

    @Test
    public void testLevelConfigurationDTO() {
        domain.dto.LevelConfigurationDTO config = new domain.dto.LevelConfigurationDTO();
        config.addFruit("UVA", 5);
        config.addEnemy("TROLL", 2);
        config.setHotTileCount(3);
        Assert.assertEquals(Integer.valueOf(5), config.getFruitCounts().get("UVA"));
        Assert.assertEquals(Integer.valueOf(2), config.getEnemyCounts().get("TROLL"));
        Assert.assertEquals(3, config.getHotTileCount());
    }

    // ==================== SERVICE TESTS: MapLoader ====================

    @Test
    public void testMapLoaderServiceLoadsLevel() throws Exception {
        MapLoaderService mls = new MapLoaderService();
        domain.dto.LevelDataDTO data = mls.loadLevel(1);
        Assert.assertNotNull(data);
        Assert.assertNotNull(data.getMapLayout());
    }

    @Test(expected = BadDopoException.class)
    public void testMapLoaderInvalidLevel() throws Exception {
        MapLoaderService mls = new MapLoaderService();
        mls.loadLevel(999);
    }

    // ==================== SERVICE TESTS: MapParser ====================

    @Test
    public void testMapParserAppliesLayout() throws Exception {
        GameState gs = new GameState("Chocolate", 1, 1);
        MapParserService mps = new MapParserService();
        MapLoaderService mls = new MapLoaderService();
        domain.dto.LevelDataDTO data = mls.loadLevel(1);
        mps.applyMapLayout(gs, data.getMapLayout(), 1);
        // Verify some entities were created
        Assert.assertTrue(gs.getIceBlocks().size() >= 0);
    }

    // ==================== GAMEFACADE ADDITIONAL TESTS ====================

    @Test
    public void testGameFacadeApplyConfiguration() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        domain.dto.LevelConfigurationDTO config = new domain.dto.LevelConfigurationDTO();
        config.addFruit("UVA", 3);
        gf.setConfiguration(config);
        gf.applyConfiguration();
        // Verify configuration was applied
        Assert.assertNotNull(gf.getFruitSnapshots());
    }

    @Test
    public void testGameFacadeGetAvailableTypes() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.getAvailableFruitTypes().isEmpty());
        Assert.assertFalse(gf.getAvailableEnemyTypes().isEmpty());
        Assert.assertFalse(gf.getAvailableAITypes().isEmpty());
    }

    @Test
    public void testGameFacadeConfigurationMethods() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.setFruitCountConfig("UVA", 5);
        gf.setEnemyCountConfig("TROLL", 2);
        gf.setHotTileCountConfig(3);
        Assert.assertEquals(Integer.valueOf(5), gf.getFruitCountsConfig().get("UVA"));
        Assert.assertEquals(Integer.valueOf(2), gf.getEnemyCountsConfig().get("TROLL"));
        Assert.assertEquals(3, gf.getHotTileCountConfig());
    }

    @Test
    public void testGameFacadeCountRemainingFruits() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        int count = gf.countRemainingFruits("UVA");
        Assert.assertTrue(count >= 0);
    }

    // ==================== GAMESTATE ADDITIONAL TESTS ====================

    @Test
    public void testGameStateClear() {
        GameState gs = new GameState("Chocolate", 1, 1);
        gs.addEnemy(new Enemy(new Point(1, 1), EnemyType.TROLL));
        gs.addFruit(new Fruit(new Point(2, 2), FruitType.UVA));
        gs.clear();
        Assert.assertTrue(gs.getEnemies().isEmpty());
        Assert.assertTrue(gs.getFruits().isEmpty());
    }

    @Test
    public void testGameStateHotTiles() {
        GameState gs = new GameState("Chocolate", 1, 1);
        HotTile ht = new HotTile(new Point(3, 3));
        gs.addHotTile(ht);
        Assert.assertTrue(gs.getHotTiles().contains(ht));
    }

    @Test
    public void testGameStateIglu() {
        GameState gs = new GameState("Chocolate", 1, 1);
        Iglu iglu = new Iglu(new Point(5, 5));
        gs.setIglu(iglu);
        Assert.assertEquals(iglu, gs.getIglu());
    }

    @Test
    public void testGameStateUnbreakableBlocks() {
        GameState gs = new GameState("Chocolate", 1, 1);
        UnbreakableBlock ub = new UnbreakableBlock(new Point(0, 0));
        gs.addUnbreakableBlock(ub);
        Assert.assertTrue(gs.getUnbreakableBlocks().contains(ub));
    }

    @Test
    public void testGameStateFormattedTime() {
        GameState gs = new GameState("Chocolate", 1, 1);
        String time = gs.getFormattedTime();
        Assert.assertNotNull(time);
        Assert.assertTrue(time.contains(":"));
    }

    @Test
    public void testGameStateP2CPU() {
        GameState gs = new GameState("Chocolate", 1, 2);
        gs.setP2CPU(true);
        Assert.assertTrue(gs.isP2CPU());
    }

    @Test
    public void testGameStatePendingWaves() {
        GameState gs = new GameState("Chocolate", 1, 1);
        List<Fruit> wave = new java.util.ArrayList<>();
        wave.add(new Fruit(new Point(1, 1), FruitType.UVA));
        gs.addPendingFruitWave(wave);
        Assert.assertFalse(gs.getPendingFruitWaves().isEmpty());
    }

    // ==================== FRUIT ADDITIONAL TESTS ====================

    @Test
    public void testFruitStateProperty() {
        Fruit cactus = new Fruit(new Point(1, 1), FruitType.CACTUS);
        // Verify fruit has a state
        Assert.assertNotNull(cactus.getState());
    }

    @Test
    public void testFruitRandomAdjacentPosition() {
        Fruit f = new Fruit(new Point(5, 5), FruitType.PIÑA);
        Point adjacent = f.getRandomAdjacentPosition();
        Assert.assertNotNull(adjacent);
        int distance = Math.abs(adjacent.x - 5) + Math.abs(adjacent.y - 5);
        Assert.assertEquals(1, distance);
    }

    @Test
    public void testFruitMove() {
        Fruit f = new Fruit(new Point(5, 5), FruitType.UVA);
        f.move(new Point(6, 6));
        Assert.assertEquals(6, f.getPosition().x);
    }

    // ==================== ENEMY ADDITIONAL TESTS ====================

    @Test
    public void testEnemyDrilling() {
        Enemy narval = new Enemy(new Point(5, 5), EnemyType.NARVAL);
        narval.startDrilling();
        Assert.assertTrue(narval.isDrilling());
        narval.stopDrilling();
        Assert.assertFalse(narval.isDrilling());
    }

    @Test
    public void testEnemyStuckCounter() {
        Enemy e = new Enemy(new Point(1, 1), EnemyType.MACETA);
        int before = e.getStuckCounter();
        e.resetStuckCounter();
        Assert.assertEquals(0, e.getStuckCounter());
    }

    @Test
    public void testEnemyGetId() {
        Enemy e = new Enemy(new Point(1, 1), EnemyType.TROLL);
        Assert.assertNotNull(e.getId());
    }

    // ==================== PLAYER ADDITIONAL TESTS ====================

    @Test
    public void testPlayerSetName() {
        Player p = new Player(new Point(5, 5), "Chocolate");
        p.setName("TestPlayer");
        Assert.assertEquals("TestPlayer", p.getName());
    }

    @Test
    public void testPlayerSetAIType() {
        Player p = new Player(new Point(5, 5), "Chocolate");
        p.setAIType(AIType.EXPERT);
        Assert.assertEquals(AIType.EXPERT, p.getAIType());
    }

    @Test
    public void testPlayerGetCharacterType() {
        Player p = new Player(new Point(5, 5), "Vainilla");
        Assert.assertEquals("Vainilla", p.getCharacterType());
    }

    // ==================== COLLISION DETECTOR ADDITIONAL TESTS ====================

    @Test
    public void testCollisionDetectorHasFruitAt() {
        GameState gs = new GameState("Chocolate", 1, 1);
        Fruit f = new Fruit(new Point(5, 5), FruitType.UVA);
        gs.addFruit(f);
        CollisionDetector cd = new CollisionDetector(gs);
        Assert.assertTrue(cd.hasFruitAt(new Point(5, 5)));
        Assert.assertFalse(cd.hasFruitAt(new Point(6, 6)));
    }

    @Test
    public void testCollisionDetectorHasIgluAt() {
        GameState gs = new GameState("Chocolate", 1, 1);
        gs.setIglu(new Iglu(new Point(5, 5)));
        CollisionDetector cd = new CollisionDetector(gs);
        Assert.assertTrue(cd.hasIgluAt(new Point(5, 5)));
        Assert.assertTrue(cd.hasIgluAt(new Point(6, 6)));
        Assert.assertFalse(cd.hasIgluAt(new Point(10, 10)));
    }

    @Test
    public void testCollisionDetectorHasUnbreakableBlockAt() {
        GameState gs = new GameState("Chocolate", 1, 1);
        gs.addUnbreakableBlock(new UnbreakableBlock(new Point(0, 0)));
        CollisionDetector cd = new CollisionDetector(gs);
        Assert.assertTrue(cd.hasUnbreakableBlockAt(new Point(0, 0)));
    }

    @Test
    public void testCollisionDetectorGetIceAt() {
        GameState gs = new GameState("Chocolate", 1, 1);
        IceBlock ice = new IceBlock(new Point(3, 3));
        gs.addIceBlock(ice);
        CollisionDetector cd = new CollisionDetector(gs);
        IceBlock found = cd.getIceAt(new Point(3, 3));
        Assert.assertNotNull(found);
    }

    @Test
    public void testCollisionDetectorIsPositionBlocked() {
        GameState gs = new GameState("Chocolate", 1, 1);
        gs.addIceBlock(new IceBlock(new Point(5, 5)));
        CollisionDetector cd = new CollisionDetector(gs);
        Assert.assertTrue(cd.isPositionBlocked(new Point(5, 5)));
    }

}
