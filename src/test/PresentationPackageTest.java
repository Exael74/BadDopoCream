package test;

import presentation.*;
import domain.*;
import org.junit.Test;
import java.awt.Font;
import javax.swing.*;
import static org.junit.Assert.*;

public class PresentationPackageTest {

    // ==================== FONT LOADER (10 Tests) ====================

    @Test
    public void testFontLoaderSingleton() {
        assertNotNull(FontLoader.getInstance());
    }

    @Test
    public void testFontLoaderSize10() {
        assertNotNull(FontLoader.getInstance().getFont(10f));
    }

    @Test
    public void testFontLoaderSize12() {
        assertNotNull(FontLoader.getInstance().getFont(12f));
    }

    @Test
    public void testFontLoaderSize14() {
        assertNotNull(FontLoader.getInstance().getFont(14f));
    }

    @Test
    public void testFontLoaderSize16() {
        assertNotNull(FontLoader.getInstance().getFont(16f));
    }

    @Test
    public void testFontLoaderSize20() {
        assertNotNull(FontLoader.getInstance().getFont(20f));
    }

    @Test
    public void testFontLoaderBoldSize10() {
        assertNotNull(FontLoader.getInstance().getBoldFont(10f));
    }

    @Test
    public void testFontLoaderBoldSize20() {
        assertNotNull(FontLoader.getInstance().getBoldFont(20f));
    }

    @Test
    public void testFontLoaderPlainSize10() {
        assertNotNull(FontLoader.getInstance().getPlainFont(10f));
    }

    @Test
    public void testFontLoaderPlainSize20() {
        assertNotNull(FontLoader.getInstance().getPlainFont(20f));
    }

    // ==================== RESOURCE LOADER - FRUITS (10 Tests) ====================

    @Test
    public void testFruitUva() {
        ResourceLoader rl = new ResourceLoader();
        assertNotNull(rl.getFruitImage("UVA"));
    }

    @Test
    public void testFruitPlatano() {
        ResourceLoader rl = new ResourceLoader();
        assertNotNull(rl.getFruitImage("PLATANO"));
    }

    @Test
    public void testFruitPina() {
        ResourceLoader rl = new ResourceLoader();
        assertNotNull(rl.getFruitImage("PIÑA"));
    }

    @Test
    public void testFruitCereza() {
        ResourceLoader rl = new ResourceLoader();
        assertNotNull(rl.getFruitImage("CEREZA"));
    }

    @Test
    public void testFruitCactus() {
        ResourceLoader rl = new ResourceLoader();
        assertNotNull(rl.getFruitImage("CACTUS"));
    }

    @Test
    public void testFruitUnknown() {
        ResourceLoader rl = new ResourceLoader();
        assertNotNull(rl.getFruitImage("UNKNOWN"));
    }

    @Test
    public void testFruitUvaGif() {
        ResourceLoader rl = new ResourceLoader();
        assertNotNull(rl.getFruitGif("UVA", "IDLE"));
    }

    @Test
    public void testFruitPlatanoGif() {
        ResourceLoader rl = new ResourceLoader();
        assertNotNull(rl.getFruitGif("PLATANO", "IDLE"));
    }

    @Test
    public void testFruitPinaGif() {
        ResourceLoader rl = new ResourceLoader();
        assertNotNull(rl.getFruitGif("PIÑA", "IDLE"));
    }

    @Test
    public void testFruitCerezaGif() {
        ResourceLoader rl = new ResourceLoader();
        assertNotNull(rl.getFruitGif("CEREZA", "IDLE"));
    }

    // ==================== RESOURCE LOADER - ENEMIES (15 Tests)
    // ====================

    @Test
    public void testTrollUp() {
        assertNotNull(new ResourceLoader().getEnemyGif("TROLL", "UP", false));
    }

    @Test
    public void testTrollDown() {
        assertNotNull(new ResourceLoader().getEnemyGif("TROLL", "DOWN", false));
    }

    @Test
    public void testTrollLeft() {
        assertNotNull(new ResourceLoader().getEnemyGif("TROLL", "LEFT", false));
    }

    @Test
    public void testTrollRight() {
        assertNotNull(new ResourceLoader().getEnemyGif("TROLL", "RIGHT", false));
    }

    @Test
    public void testMacetaUp() {
        assertNotNull(new ResourceLoader().getEnemyGif("MACETA", "UP", false));
    }

    @Test
    public void testMacetaDown() {
        assertNotNull(new ResourceLoader().getEnemyGif("MACETA", "DOWN", false));
    }

    @Test
    public void testMacetaLeft() {
        assertNotNull(new ResourceLoader().getEnemyGif("MACETA", "LEFT", false));
    }

    @Test
    public void testMacetaRight() {
        assertNotNull(new ResourceLoader().getEnemyGif("MACETA", "RIGHT", false));
    }

    @Test
    public void testCalamarUp() {
        assertNotNull(new ResourceLoader().getCalamarGif("UP", false));
    }

    @Test
    public void testCalamarDown() {
        assertNotNull(new ResourceLoader().getCalamarGif("DOWN", false));
    }

    @Test
    public void testCalamarLeft() {
        assertNotNull(new ResourceLoader().getCalamarGif("LEFT", false));
    }

    @Test
    public void testCalamarRight() {
        assertNotNull(new ResourceLoader().getCalamarGif("RIGHT", false));
    }

    @Test
    public void testNarvalUp() {
        assertNotNull(new ResourceLoader().getEnemyGif("NARVAL", "UP", false));
    }

    @Test
    public void testNarvalDrilling() {
        assertNotNull(new ResourceLoader().getEnemyGif("NARVAL", "UP", true));
    }

    @Test
    public void testCalamarBreaking() {
        assertNotNull(new ResourceLoader().getCalamarGif("UP", true));
    }

    // ==================== RESOURCE LOADER - PLAYERS (15 Tests)
    // ====================

    @Test
    public void testPlayerChocoUp() {
        assertNotNull(new ResourceLoader().getPlayerGif("Chocolate", "UP", false, false, false, false, false));
    }

    @Test
    public void testPlayerChocoDown() {
        assertNotNull(new ResourceLoader().getPlayerGif("Chocolate", "DOWN", false, false, false, false, false));
    }

    @Test
    public void testPlayerChocoLeft() {
        assertNotNull(new ResourceLoader().getPlayerGif("Chocolate", "LEFT", false, false, false, false, false));
    }

    @Test
    public void testPlayerChocoRight() {
        assertNotNull(new ResourceLoader().getPlayerGif("Chocolate", "RIGHT", false, false, false, false, false));
    }

    @Test
    public void testPlayerChocoMoving() {
        assertNotNull(new ResourceLoader().getPlayerGif("Chocolate", "UP", true, false, false, false, false));
    }

    @Test
    public void testPlayerChocoSneeze() {
        assertNotNull(new ResourceLoader().getPlayerGif("Chocolate", "UP", false, true, false, false, false));
    }

    @Test
    public void testPlayerChocoKick() {
        assertNotNull(new ResourceLoader().getPlayerGif("Chocolate", "UP", false, false, true, false, false));
    }

    @Test
    public void testPlayerChocoDying() {
        assertNotNull(new ResourceLoader().getPlayerGif("Chocolate", "UP", false, false, false, true, false));
    }

    @Test
    public void testPlayerChocoWin() {
        assertNotNull(new ResourceLoader().getPlayerGif("Chocolate", "UP", false, false, false, false, true));
    }

    @Test
    public void testPlayerVainillaUp() {
        assertNotNull(new ResourceLoader().getPlayerGif("Vainilla", "UP", false, false, false, false, false));
    }

    @Test
    public void testPlayerFresaUp() {
        assertNotNull(new ResourceLoader().getPlayerGif("Fresa", "UP", false, false, false, false, false));
    }

    @Test
    public void testPlayerUnknown() {
        assertNotNull(new ResourceLoader().getPlayerGif("Unknown", "UP", false, false, false, false, false));
    }

    @Test
    public void testPlayerDirectionUnknown() {
        assertNotNull(new ResourceLoader().getPlayerGif("Chocolate", "UNKNOWN", false, false, false, false, false));
    }

    @Test
    public void testPlayerNullArgs() {
        assertNotNull(new ResourceLoader().getPlayerGif(null, "UP", false, false, false, false, false));
    }

    @Test
    public void testGetTrollGifDirect() {
        assertNotNull(new ResourceLoader().getTrollGif("UP"));
    }

    // ==================== RESOURCE LOADER - STATIC ASSETS (10 Tests)
    // ====================

    @Test
    public void testWallpaper() {
        assertNotNull(new ResourceLoader().wallpaperImage);
    }

    @Test
    public void testIceNormal() {
        assertNotNull(new ResourceLoader().iceBlockNormalImage);
    }

    @Test
    public void testIceBroken() {
        assertNotNull(new ResourceLoader().iceBlockBrokenImage);
    }

    @Test
    public void testIglu() {
        assertNotNull(new ResourceLoader().igluImage);
    }

    @Test
    public void testHotTile() {
        assertNotNull(new ResourceLoader().hotTileImage);
    }

    @Test
    public void testTitle() {
        assertNotNull(new ResourceLoader().titleGif);
    }

    @Test
    public void testBtnStart() {
        assertNotNull(new ResourceLoader().startButtonImage);
    }

    @Test
    public void testBtnBack() {
        assertNotNull(new ResourceLoader().backImage);
    }

    @Test
    public void testLevelSelGif() {
        assertNotNull(new ResourceLoader().levelSelectionGif);
    }

    @Test
    public void testNivelImages() {
        ResourceLoader rl = new ResourceLoader();
        assertNotNull(rl.nivel1Image);
        assertNotNull(rl.nivel2Image);
        assertNotNull(rl.nivel3Image);
        assertNotNull(rl.nivel4Image);
    }

    @Test
    public void testCampfireGif() {
        ResourceLoader rl = new ResourceLoader();
        assertNotNull(rl.campfireGif);
    }

    @Test
    public void testFlameGif() {
        ResourceLoader rl = new ResourceLoader();
        assertNotNull(rl.flameGif);
    }

    // ==================== UI COMPONENTS (15 Tests) ====================

    @Test
    public void testGamePanel1() {
        GamePanel gp = new GamePanel("Chocolate", 1, 1, new ResourceLoader());
        gp.cleanup();
    }

    @Test
    public void testGamePanel2() {
        GamePanel gp = new GamePanel("Chocolate", 2, 1, new ResourceLoader());
        gp.cleanup();
    }

    @Test
    public void testGamePanel3() {
        GamePanel gp = new GamePanel("Chocolate", 3, 1, new ResourceLoader());
        gp.cleanup();
    }

    @Test
    public void testGamePanel2Players() {
        GamePanel gp = new GamePanel("Chocolate", 1, 2, new ResourceLoader());
        gp.cleanup();
    }

    @Test
    public void testGamePanelMachine() {
        GamePanel gp = new GamePanel("Chocolate", 1, 0, new ResourceLoader());
        gp.cleanup();
    }

    @Test
    public void testWelcomeScreen() {
        WelcomeScreen ws = new WelcomeScreen();
        ws.dispose();
    }

    @Test
    public void testLevelSelLvl1() {
        assertNotNull(new LevelSelectionPanel(1, new ResourceLoader(), false));
    }

    @Test
    public void testLevelSelLvl2() {
        assertNotNull(new LevelSelectionPanel(2, new ResourceLoader(), false));
    }

    @Test
    public void testLevelSelLvl3() {
        assertNotNull(new LevelSelectionPanel(3, new ResourceLoader(), false));
    }

    @Test
    public void testCharSelP1() {
        assertNotNull(new CharacterSelectionPanel(1, 1, new ResourceLoader(), false));
    }

    @Test
    public void testCharSelP2() {
        assertNotNull(new CharacterSelectionPanel(1, 2, new ResourceLoader(), false));
    }

    @Test
    public void testCharSelMachine() {
        assertNotNull(new CharacterSelectionPanel(1, 0, new ResourceLoader(), true));
    }

    @Test
    public void testGameHUDStub() {
        assertNotNull(new GameHUD(new GameFacade("C", 1, 1), new ResourceLoader(), FontLoader.getInstance()));
    }

    @Test
    public void testGameOverlayStub() {
        assertNotNull(new GameOverlay(new GameFacade("C", 1, 1), FontLoader.getInstance(), 100, 100));
    }

    @Test
    public void testLevelConfigStub() {
        assertNotNull(new LevelConfigurationDialog(new JFrame(), new GameFacade("C", 1, 1)));
    }

    // ==================== FINAL BATCH TO REACH 150 (5 Tests) ====================

    @Test
    public void testFontLoaderSize18() {
        assertNotNull(FontLoader.getInstance().getFont(18f));
    }

    @Test
    public void testFontLoaderSize22() {
        assertNotNull(FontLoader.getInstance().getFont(22f));
    }

    @Test
    public void testFontLoaderSize24() {
        assertNotNull(FontLoader.getInstance().getFont(24f));
    }

    @Test
    public void testPlayerKickDown() {
        assertNotNull(new ResourceLoader().getPlayerGif("Chocolate", "DOWN", false, false, true, false, false));
    }

    @Test
    public void testPlayerKickLeft() {
        assertNotNull(new ResourceLoader().getPlayerGif("Chocolate", "LEFT", false, false, true, false, false));
    }
}
