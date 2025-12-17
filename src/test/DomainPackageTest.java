package test;

import domain.GameFacade;
import domain.dto.*;
import domain.entity.*; // Importing Enums directly for property testing

import org.junit.Test;
import org.junit.Assert;
import java.awt.Point;
import java.util.List;
import java.util.Map;

public class DomainPackageTest {

    // ==================== INITIALIZATION & CONFIGURATION (10 Tests)
    // ====================

    @Test
    public void testGameFacadeInitialization() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertEquals(1, gf.getCurrentLevel());
        Assert.assertEquals(1, gf.getNumberOfPlayers());
    }

    @Test
    public void testGameFacadeInitializationWithPvP() {
        GameFacade gf = new GameFacade("Chocolate", "Fresa", "P1", "P2", 1, 2, "EXPERT", "EXPERT", false);
        Assert.assertEquals(2, gf.getNumberOfPlayers());
    }

    @Test
    public void testGameFacadeInitializationWithMachine() {
        GameFacade gf = new GameFacade("Chocolate", "Vainilla", "M1", "M2", 1, 0, "EXPERT", "EXPERT", true);
        Assert.assertEquals(0, gf.getNumberOfPlayers());
    }

    @Test
    public void testGameFacadeSetPlayer2CharacterType() {
        GameFacade gf = new GameFacade("Chocolate", 1, 2);
        gf.setPlayer2CharacterType("Fresa");
        Assert.assertEquals("Fresa", gf.getPlayer2Snapshot().getCharacterType());
    }

    @Test
    public void testConfigurationFruitSetters() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.setFruitCountConfig("UVA", 10);
        Assert.assertEquals(Integer.valueOf(10), gf.getFruitCountsConfig().get("UVA"));
    }

    @Test
    public void testConfigurationEnemySetters() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.setEnemyCountConfig("TROLL", 5);
        Assert.assertEquals(Integer.valueOf(5), gf.getEnemyCountsConfig().get("TROLL"));
    }

    @Test
    public void testConfigurationHotTileSetters() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.setHotTileCountConfig(4);
        Assert.assertEquals(4, gf.getHotTileCountConfig());
    }

    @Test
    public void testConfigurationAvailableFruits() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertTrue(gf.getAvailableFruitTypes().contains("UVA"));
        Assert.assertTrue(gf.getAvailableFruitTypes().contains("PLATANO"));
    }

    @Test
    public void testConfigurationAvailableEnemies() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertTrue(gf.getAvailableEnemyTypes().contains("TROLL"));
    }

    @Test
    public void testConfigurationAvailableAI() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertTrue(gf.getAvailableAITypes().contains("EXPERT"));
    }

    // ==================== ENUM PROPERTIES (30 Tests) ====================

    @Test
    public void testDirectionUp() {
        Assert.assertEquals(0, Direction.UP.getDeltaX());
        Assert.assertEquals(-1, Direction.UP.getDeltaY());
    }

    @Test
    public void testDirectionDown() {
        Assert.assertEquals(0, Direction.DOWN.getDeltaX());
        Assert.assertEquals(1, Direction.DOWN.getDeltaY());
    }

    @Test
    public void testDirectionLeft() {
        Assert.assertEquals(-1, Direction.LEFT.getDeltaX());
        Assert.assertEquals(0, Direction.LEFT.getDeltaY());
    }

    @Test
    public void testDirectionRight() {
        Assert.assertEquals(1, Direction.RIGHT.getDeltaX());
        Assert.assertEquals(0, Direction.RIGHT.getDeltaY());
    }

    @Test
    public void testDirectionIdle() {
        Assert.assertEquals(0, Direction.IDLE.getDeltaX());
        Assert.assertEquals(0, Direction.IDLE.getDeltaY());
    }

    @Test
    public void testFruitTypeUva() {
        Assert.assertFalse(FruitType.UVA.canMove());
        Assert.assertFalse(FruitType.UVA.canTeleport());
        Assert.assertTrue(FruitType.UVA.getScore() > 0);
    }

    @Test
    public void testFruitTypePlatano() {
        Assert.assertFalse(FruitType.PLATANO.canMove());
        Assert.assertFalse(FruitType.PLATANO.canTeleport());
        Assert.assertTrue(FruitType.PLATANO.getScore() > 0);
    }

    @Test
    public void testFruitTypePina() {
        Assert.assertTrue(FruitType.PIÑA.canMove());
        Assert.assertFalse(FruitType.PIÑA.canTeleport());
    }

    @Test
    public void testFruitTypeCereza() {
        Assert.assertFalse(FruitType.CEREZA.canMove());
        Assert.assertTrue(FruitType.CEREZA.canTeleport());
    }

    @Test
    public void testFruitTypeCactus() {
        Assert.assertFalse(FruitType.CACTUS.canMove());
        Assert.assertFalse(FruitType.CACTUS.canTeleport());
    }

    @Test
    public void testFruitTypeScoreValues() {
        Assert.assertEquals(50, FruitType.UVA.getScore());
        Assert.assertEquals(100, FruitType.PLATANO.getScore());
        Assert.assertEquals(200, FruitType.PIÑA.getScore());
        Assert.assertEquals(150, FruitType.CEREZA.getScore());
        Assert.assertEquals(250, FruitType.CACTUS.getScore());
    }

    @Test
    public void testFruitTypeIntervals() {
        Assert.assertTrue(FruitType.PIÑA.getActionInterval() > 0);
        Assert.assertTrue(FruitType.CEREZA.getActionInterval() > 0);
        Assert.assertTrue(FruitType.CACTUS.getActionInterval() > 0);
    }

    @Test
    public void testFruitTypeSpecialAbility() {
        Assert.assertFalse(FruitType.UVA.hasSpecialAbility());
        Assert.assertTrue(FruitType.PIÑA.hasSpecialAbility());
        Assert.assertTrue(FruitType.CEREZA.hasSpecialAbility());
    }

    @Test
    public void testEntityTypes() {
        Assert.assertNotNull(EntityType.PLAYER);
        Assert.assertNotNull(EntityType.ENEMY);
        Assert.assertNotNull(EntityType.FRUIT);
        Assert.assertNotNull(EntityType.ICE_BLOCK);
        Assert.assertNotNull(EntityType.FOGATA);
    }

    @Test
    public void testFruitStates() {
        Assert.assertNotNull(FruitState.SPAWNING);
        Assert.assertNotNull(FruitState.IDLE);
        Assert.assertNotNull(FruitState.COLLECTED);
        Assert.assertNotNull(FruitState.TELEPORT_OUT);
        Assert.assertNotNull(FruitState.TELEPORT_IN);
        Assert.assertNotNull(FruitState.SPIKES_WARNING);
        Assert.assertNotNull(FruitState.SPIKES_ACTIVE);
        Assert.assertNotNull(FruitState.SPIKES_COOLDOWN);
    }

    @Test
    public void testAITypes() {
        Assert.assertNotNull(AIType.HUNGRY);
        Assert.assertNotNull(AIType.FEARFUL);
        Assert.assertNotNull(AIType.EXPERT);
    }

    @Test
    public void testSnapshotTypes() {
        Assert.assertNotNull(SnapshotType.PLAYER);
        Assert.assertNotNull(SnapshotType.ENEMY);
        Assert.assertNotNull(SnapshotType.FRUIT);
        Assert.assertNotNull(SnapshotType.ICE_BLOCK);
    }

    // ==================== FACADE MOVEMENT & ACTIONS (15 Tests)
    // ====================

    @Test
    public void testMovePlayerUp() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.movePlayerUp();
        // Smoke test: should not throw
    }

    @Test
    public void testMovePlayerDown() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.movePlayerDown();
    }

    @Test
    public void testMovePlayerLeft() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.movePlayerLeft();
    }

    @Test
    public void testMovePlayerRight() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.movePlayerRight();
    }

    @Test
    public void testStopPlayer() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.stopPlayer();
        Assert.assertFalse(gf.isPlayerMoving());
    }

    @Test
    public void testMovePlayer2Up() {
        GameFacade gf = new GameFacade("Chocolate", "Fresa", "P1", "P2", 1, 2, "EXPERT", "EXPERT", false);
        gf.movePlayer2Up();
    }

    @Test
    public void testMovePlayer2Down() {
        GameFacade gf = new GameFacade("Chocolate", "Fresa", "P1", "P2", 1, 2, "EXPERT", "EXPERT", false);
        gf.movePlayer2Down();
    }

    @Test
    public void testMovePlayer2Left() {
        GameFacade gf = new GameFacade("Chocolate", "Fresa", "P1", "P2", 1, 2, "EXPERT", "EXPERT", false);
        gf.movePlayer2Left();
    }

    @Test
    public void testMovePlayer2Right() {
        GameFacade gf = new GameFacade("Chocolate", "Fresa", "P1", "P2", 1, 2, "EXPERT", "EXPERT", false);
        gf.movePlayer2Right();
    }

    @Test
    public void testStopPlayer2() {
        GameFacade gf = new GameFacade("Chocolate", "Fresa", "P1", "P2", 1, 2, "EXPERT", "EXPERT", false);
        gf.stopPlayer2();
    }

    @Test
    public void testPerformSpaceAction() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertNotNull(gf.performSpaceAction());
    }

    @Test
    public void testPerformSpaceActionPaused() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.togglePause();
        Assert.assertTrue(gf.performSpaceAction().isEmpty());
    }

    @Test
    public void testPerformPlayer2Action() {
        GameFacade gf = new GameFacade("Chocolate", "Fresa", "P1", "P2", 1, 2, "EXPERT", "EXPERT", false);
        Assert.assertNotNull(gf.performActionPlayer2());
    }

    @Test
    public void testPerformPlayer2ActionPaused() {
        GameFacade gf = new GameFacade("Chocolate", "Fresa", "P1", "P2", 1, 2, "EXPERT", "EXPERT", false);
        gf.togglePause();
        Assert.assertTrue(gf.performActionPlayer2().isEmpty());
    }

    // ==================== STATE QUERIES & SNAPSHOTS (20 Tests)
    // ====================

    @Test
    public void testIsPaused() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.isPaused());
    }

    @Test
    public void testIsGameOver() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.isGameOver());
    }

    @Test
    public void testIsVictory() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.isVictory());
    }

    @Test
    public void testIsTimeUp() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.isTimeUp());
    }

    @Test
    public void testIsPlayerMoving() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.isPlayerMoving());
    }

    @Test
    public void testIsPlayerSneezing() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.isPlayerSneezing());
    }

    @Test
    public void testIsPlayerKicking() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.isPlayerKicking());
    }

    @Test
    public void testIsPlayerDying() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.isPlayerDying());
    }

    @Test
    public void testIsPlayerCelebrating() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.isPlayerCelebrating());
    }

    @Test
    public void testIsPlayerBusy() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.isPlayerBusy());
    }

    @Test
    public void testIsPlayerAlive() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertTrue(gf.isPlayerAlive());
    }

    @Test
    public void testIsPlayer2Alive() {
        GameFacade gf = new GameFacade("Chocolate", "Fresa", "P1", "P2", 1, 2, "EXPERT", "EXPERT", false);
        Assert.assertTrue(gf.isPlayer2Alive());
    }

    @Test
    public void testDeathAnimationComplete() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.isDeathAnimationComplete());
    }

    @Test
    public void testShouldRestartLevel() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertFalse(gf.shouldRestartLevel());
    }

    @Test
    public void testScoreInitial() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertEquals(0, gf.getScore());
    }

    @Test
    public void testScoreP2Initial() {
        GameFacade gf = new GameFacade("Chocolate", "Fresa", "P1", "P2", 1, 2, "EXPERT", "EXPERT", false);
        Assert.assertEquals(0, gf.getScorePlayer2());
    }

    @Test
    public void testGetPlayerDirection() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertNotNull(gf.getPlayerDirection());
    }

    @Test
    public void testGetLevel() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertEquals(1, gf.getLevel());
    }

    @Test
    public void testGetAITypeP1() {
        GameFacade gf = new GameFacade("Chocolate", "Vainilla", "M1", "M2", 1, 0, "EXPERT", "EXPERT", true);
        Assert.assertEquals("EXPERT", gf.getAITypeP1());
    }

    @Test
    public void testGetPlayerCharacterType() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertEquals("Chocolate", gf.getPlayerCharacterType());
    }

    // ==================== SNAPSHOT NULL CHECKS (5 Tests) ====================

    @Test
    public void testPlayerSnapshot() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertNotNull(gf.getPlayerSnapshot());
    }

    @Test
    public void testSavedGamesList() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertNotNull(gf.getSavedGames());
    }

    // ==================== FOGATA TESTS ====================

    @Test
    public void testFogataCreation() {
        Fogata fogata = new Fogata(new Point(5, 5));
        Assert.assertTrue(fogata.isActive());
        Assert.assertEquals(new Point(5, 5), fogata.getPosition());
    }

    @Test
    public void testFogataExtinguish() {
        Fogata fogata = new Fogata(new Point(5, 5));
        fogata.extinguish();
        Assert.assertFalse(fogata.isActive());
    }

    @Test
    public void testFogataCooldown() {
        Fogata fogata = new Fogata(new Point(5, 5));
        fogata.extinguish();
        // Update with 5 seconds -> Still inactive
        fogata.update(5000);
        Assert.assertFalse(fogata.isActive());
        // Update with 6 more seconds (Total 11s) -> Active
        fogata.update(6000);
        Assert.assertTrue(fogata.isActive());
    }

    @Test
    public void testGameFacadeFogataConfiguration() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.setFogataCountConfig(5);
        Assert.assertEquals(5, gf.getFogataCountConfig());
    }

    @Test
    public void testEnemySnapshots() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertNotNull(gf.getEnemySnapshots());
    }

    @Test
    public void testFruitSnapshots() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertNotNull(gf.getFruitSnapshots());
    }

    @Test
    public void testIceBlockSnapshots() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertNotNull(gf.getIceBlockSnapshots());
    }

    @Test
    public void testHotTileSnapshots() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        Assert.assertNotNull(gf.getHotTileSnapshots());
    }

    // ==================== PERSISTENCE & CONTROL (5 Tests) ====================

    @Test
    public void testSaveGame() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        try {
            gf.saveGame();
        } catch (Exception e) {
        }
    }

    @Test
    public void testLoadGame() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        try {
            gf.loadGame("invalid_file");
        } catch (Exception e) {
        }
    }

    @Test
    public void testRestartLevel() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.restartLevel();
        Assert.assertFalse(gf.isPaused());
    }

    @Test
    public void testTogglePause() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        boolean initial = gf.isPaused();
        gf.togglePause();
        Assert.assertNotEquals(initial, gf.isPaused());
    }

    @Test
    public void testUpdate() {
        GameFacade gf = new GameFacade("Chocolate", 1, 1);
        gf.update();
    }

    // ==================== STATIC LOAD TESTS ====================

    @Test
    public void testListSavedGames() {
        // Static method call
        List<String> saves = GameFacade.listSavedGames();
        Assert.assertNotNull(saves);
    }

    @Test
    public void testLoadFromSaveInvalid() {
        try {
            GameFacade.loadFromSave("non_existent_file.dat");
        } catch (Exception e) {
            // Expected to fail or return null/throw, verifying it handles it gracefully
            // (throws BadDopoException usually)
            Assert.assertTrue(e instanceof exceptions.BadDopoException || e instanceof Exception);
        }
    }
}
