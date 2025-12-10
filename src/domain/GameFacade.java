package domain;

import domain.dto.*;
import domain.entity.*;
import domain.service.GameLogic;
import domain.service.PersistenceService;
import domain.state.GameState;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fachada del dominio del juego.
 * Único punto de acceso desde la capa de presentación al dominio.
 * Coordina GameState y GameLogic, y provee una API limpia y simplificada.
 */
public class GameFacade {

    private GameState gameState;
    private GameLogic gameLogic;
    private PersistenceService persistenceService;
    private long lastUpdateTime;
    private boolean paused;

    /**
     * Constructor de la fachada del juego.
     *
     * @param characterType   Tipo de personaje ("Chocolate", "Fresa", "Vainilla")
     * @param level           Nivel a jugar (1, 2, 3)
     * @param numberOfPlayers Número de jugadores (0=IA vs IA, 1=1P, 2=2P)
     */
    public GameFacade(String characterType, String characterTypeP2, String p1Name, String p2Name, int level,
            int numberOfPlayers, AIType aiTypeP1, AIType aiTypeP2, boolean isP2CPU) {
        this.gameState = new GameState(characterType, level, numberOfPlayers);
        this.gameState.setP2CPU(isP2CPU);

        // Set Player 2 character type if applicable
        if (gameState.getPlayer2() != null && characterTypeP2 != null) {
            Point pos = gameState.getPlayer2().getPosition();
            Player p2 = new Player(pos, characterTypeP2);
            gameState.setPlayer2(p2);
        }

        // Set AI Types for MvM (0 players) OR P1 vs Machine (2 players + isP2CPU)
        if (numberOfPlayers == 0) {
            // Machine vs Machine
            if (gameState.getPlayer() != null)
                gameState.getPlayer().setAIType(aiTypeP1);
            if (gameState.getPlayer2() != null)
                gameState.getPlayer2().setAIType(aiTypeP2);
        } else if (numberOfPlayers == 2 && isP2CPU) {
            // Player 1 vs Machine
            // P1 is human (no AI type set, or null)
            if (gameState.getPlayer2() != null) {
                gameState.getPlayer2().setAIType(aiTypeP2 != null ? aiTypeP2 : AIType.EXPERT); // Default to Expert if
                                                                                               // null
            }
        }

        this.gameState.setPlayerNames(p1Name, p2Name);

        this.gameLogic = new GameLogic(gameState);
        this.persistenceService = new PersistenceService();
        this.lastUpdateTime = System.currentTimeMillis();
        this.paused = false;

        initializeLevel(level, numberOfPlayers);
    }

    public GameFacade(String characterType, String characterTypeP2, String p1Name, String p2Name, int level,
            int numberOfPlayers, AIType aiTypeP1, AIType aiTypeP2) {
        this(characterType, characterTypeP2, p1Name, p2Name, level, numberOfPlayers, aiTypeP1, aiTypeP2, false);
    }

    public GameFacade(String characterType, int level, int numberOfPlayers) {
        this(characterType, null, "P1", "P2", level, numberOfPlayers, null, null, false);
    }

    /**
     * Sets the character type for Player 2.
     * Should be called immediately after construction if P2 type is known.
     */
    public void setPlayer2CharacterType(String characterType) {
        if (gameState.getPlayer2() != null) {
            // We need to recreate the player to set the type properly if it's immutable or
            // just set it
            // Player class doesn't have a setter for type usually, let's check.
            // Assuming we can just create a new Player or we need to add a setter.
            // Let's re-create P2 with the same position but new type.
            Point pos = gameState.getPlayer2().getPosition();
            Player p2 = new Player(pos, characterType);
            gameState.setPlayer2(p2);
        }
    }

    // ==================== CONTROL DE PAUSA ====================

    public void togglePause() {
        this.paused = !this.paused;
        // Resetear lastUpdateTime al reanudar para evitar saltos grandes de tiempo
        if (!paused) {
            this.lastUpdateTime = System.currentTimeMillis();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    // ==================== PERSISTENCIA ====================

    public void saveGame() throws BadDopoException {
        persistenceService.saveGame(gameState);
    }

    public void saveGame(File file) throws BadDopoException {
        persistenceService.saveGame(gameState, file);
    }

    public void loadGame(String filename) throws BadDopoException {
        GameState loadedState = persistenceService.loadGame(filename);
        if (loadedState != null) {
            this.gameState = loadedState;
            // Re-inicializar GameLogic con el nuevo estado
            this.gameLogic = new GameLogic(this.gameState);
            this.paused = false; // Reanudar al cargar
            this.lastUpdateTime = System.currentTimeMillis();
        }
    }

    public List<String> getSavedGames() {
        return persistenceService.getSavedGames();
    }

    // ==================== REINICIO Y SALIDA ====================

    public void restartLevel() {
        // Reiniciar con la misma configuración
        String charType = gameState.getPlayer().getCharacterType();
        int level = gameState.getLevel();
        int players = gameState.getNumberOfPlayers();

        // Preserve P2 type if exists
        String charType2 = (gameState.getPlayer2() != null) ? gameState.getPlayer2().getCharacterType() : "Vainilla";
        String name1 = gameState.getPlayer().getName();
        String name2 = (gameState.getPlayer2() != null) ? gameState.getPlayer2().getName() : "P2";

        this.gameState = new GameState(charType, level, players);
        if (players == 2 || players == 0) {
            setPlayer2CharacterType(charType2);
        }
        this.gameState.setPlayerNames(name1, name2);

        this.gameLogic = new GameLogic(gameState);
        this.paused = false;
        this.lastUpdateTime = System.currentTimeMillis();

        initializeLevel(level, players);
    }

    // ==================== INICIALIZACIÓN DE NIVELES ====================

    /**
     * Inicializa el nivel especificado.
     */
    private void initializeLevel(int level, int numberOfPlayers) {
        try {
            BadDopoLogger.logInfo("Iniciando nivel " + level + " con " + numberOfPlayers + " jugadores.");
            switch (level) {
                case 1:
                    initializeLevel1(numberOfPlayers);
                    break;
                case 2:
                    initializeLevel2(numberOfPlayers);
                    break;
                case 3:
                    initializeLevel3(numberOfPlayers);
                    break;
                case 4:
                    initializeLevel4(numberOfPlayers);
                    break;
                default:
                    throw new BadDopoException("Nivel inválido: " + level);
            }
        } catch (BadDopoException e) {
            BadDopoLogger.logError("Error al inicializar el nivel: " + e.getMessage(), e);
            System.err.println(e.getMessage());
        } catch (Exception e) {
            BadDopoLogger.logError("Error inesperado al inicializar el nivel", e);
            e.printStackTrace();
        }
    }

    /**
     * Inicializa el nivel 1: 2 Trolls + 8 Uvas + 8 Plátanos.
     */
    private void initializeLevel1(int numberOfPlayers) {
        Random random = new Random();
        List<Point> occupiedPositions = new ArrayList<>();

        // Fix Player 1 Position (Move out of center)
        gameState.getPlayer().setPosition(new Point(1, 1));
        occupiedPositions.add(gameState.getPlayer().getPosition());

        // Create Central Iglu
        gameState.setIglu(new Iglu(new Point(5, 5))); // 3x3 Iglu at center

        // Add Unbreakable Blocks at borders
        for (int x = 0; x < GameState.getGridSize(); x++) {
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(x, 0))); // Top
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(x, GameState.getGridSize() - 1))); // Bottom
            occupiedPositions.add(new Point(x, 0));
            occupiedPositions.add(new Point(x, GameState.getGridSize() - 1));
        }
        for (int y = 1; y < GameState.getGridSize() - 1; y++) {
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(0, y))); // Left
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(GameState.getGridSize() - 1, y))); // Right
            occupiedPositions.add(new Point(0, y));
            occupiedPositions.add(new Point(GameState.getGridSize() - 1, y));
        }

        // Agregar hielos predeterminados del nivel 1 (Avoid center and borders)
        List<Point> predeterminedIcePositions = getPredeterminedIcePositionsLevel1();
        for (Point icePos : predeterminedIcePositions) {
            if (!gameState.getIglu().collidesWith(icePos)) {
                gameState.addIceBlock(new IceBlock(icePos, false));
                occupiedPositions.add(icePos);
            }
        }

        // Agregar baldosas calientes (Nivel 1: pocas) - AVOID ICE positions per user
        List<Point> hotTilePositions = List.of(new Point(2, 2), new Point(10, 10), new Point(2, 10), new Point(10, 2));
        for (Point pos : hotTilePositions) {
            if (!isPositionOccupied(pos, occupiedPositions) && !gameState.getIglu().collidesWith(pos)) {
                gameState.addHotTile(new HotTile(pos));
                occupiedPositions.add(pos);
            }
        }

        // Spawn Player 2 if applicable (2 Players OR Machine vs Machine)
        if ((numberOfPlayers == 2 || numberOfPlayers == 0) && gameState.getPlayer2() != null) {
            Point p2Pos = getRandomFreePosition(random, occupiedPositions);
            gameState.getPlayer2().setPosition(p2Pos);
            occupiedPositions.add(p2Pos);
        }

        // Agregar 2 trolls
        for (int i = 0; i < 2; i++) {
            Point trollPos = getRandomFreePosition(random, occupiedPositions);
            Enemy troll = new Enemy(trollPos, EnemyType.TROLL);
            gameState.addEnemy(troll);
            occupiedPositions.add(trollPos);
        }

        // Wave 1: 16 Uvas (Initial)
        for (int i = 0; i < 16; i++) {
            Point uvaPos = getRandomFreePosition(random, occupiedPositions);
            gameState.addFruit(new Fruit(uvaPos, FruitType.UVA));
            occupiedPositions.add(uvaPos);
        }

        // Wave 2: 16 Plátanos (Pending)
        List<Fruit> wave2 = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            Point platanoPos = getRandomFreePosition(random, occupiedPositions);
            wave2.add(new Fruit(platanoPos, FruitType.PLATANO));
            occupiedPositions.add(platanoPos);
        }
        gameState.addFruitWave(wave2);
    }

    /**
     * Inicializa el nivel 2: 1 Maceta + 8 Piñas + 8 Plátanos.
     */
    private void initializeLevel2(int numberOfPlayers) {
        Random random = new Random();
        List<Point> occupiedPositions = new ArrayList<>();

        // Fix Player 1 Position
        gameState.getPlayer().setPosition(new Point(1, 1));
        occupiedPositions.add(gameState.getPlayer().getPosition());

        // Create Central Iglu
        gameState.setIglu(new Iglu(new Point(5, 5)));

        // Add Unbreakable Blocks at borders
        for (int x = 0; x < GameState.getGridSize(); x++) {
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(x, 0)));
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(x, GameState.getGridSize() - 1)));
            occupiedPositions.add(new Point(x, 0));
            occupiedPositions.add(new Point(x, GameState.getGridSize() - 1));
        }
        for (int y = 1; y < GameState.getGridSize() - 1; y++) {
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(0, y)));
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(GameState.getGridSize() - 1, y)));
            occupiedPositions.add(new Point(0, y));
            occupiedPositions.add(new Point(GameState.getGridSize() - 1, y));
        }

        // Patrón de hielo DIFERENTE para nivel 2 (Avoid center/borders)
        List<Point> predeterminedIcePositions = getPredeterminedIcePositionsLevel2();
        for (Point icePos : predeterminedIcePositions) {
            if (!gameState.getIglu().collidesWith(icePos)) {
                gameState.addIceBlock(new IceBlock(icePos, false));
                occupiedPositions.add(icePos);
            }
        }

        // Agregar baldosas calientes (Nivel 2: moderadas)
        List<Point> hotTilePositions = List.of(
                new Point(4, 4), new Point(8, 8), new Point(4, 8), new Point(8, 4),
                new Point(6, 2), new Point(6, 10)); // Some of these might collide with Iglu (4,4 - 8,8).

        // Filter hot tiles that collide with Iglu
        for (Point pos : hotTilePositions) {
            if (!gameState.getIglu().collidesWith(pos) && !isPositionOccupied(pos, occupiedPositions)) {
                gameState.addHotTile(new HotTile(pos));
                occupiedPositions.add(pos);
            }
        }

        // Spawn Player 2 if applicable (2 Players OR Machine vs Machine)
        if ((numberOfPlayers == 2 || numberOfPlayers == 0) && gameState.getPlayer2() != null) {
            Point p2Pos = getRandomFreePosition(random, occupiedPositions);
            gameState.getPlayer2().setPosition(p2Pos);
            occupiedPositions.add(p2Pos);
        }

        // Agregar 1 maceta
        Point macetaPos = getRandomFreePosition(random, occupiedPositions);
        Enemy maceta = new Enemy(macetaPos, EnemyType.MACETA);
        gameState.addEnemy(maceta);
        occupiedPositions.add(macetaPos);

        // Wave 1: 16 Piñas (Initial)
        for (int i = 0; i < 16; i++) {
            Point pinaPos = getRandomFreePosition(random, occupiedPositions);
            gameState.addFruit(new Fruit(pinaPos, FruitType.PIÑA));
            occupiedPositions.add(pinaPos);
        }

        // Wave 2: 16 Plátanos (Pending)
        List<Fruit> wave2 = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            Point platanoPos = getRandomFreePosition(random, occupiedPositions);
            wave2.add(new Fruit(platanoPos, FruitType.PLATANO));
            occupiedPositions.add(platanoPos);
        }
        gameState.addFruitWave(wave2);
    }

    /**
     * Inicializa el nivel 3: 1 Calamar + 8 Piñas + 8 Cerezas.
     */
    private void initializeLevel3(int numberOfPlayers) {
        Random random = new Random();
        List<Point> occupiedPositions = new ArrayList<>();

        // Fix Player 1 Position
        gameState.getPlayer().setPosition(new Point(1, 1));
        occupiedPositions.add(gameState.getPlayer().getPosition());

        // Create Central Iglu
        gameState.setIglu(new Iglu(new Point(5, 5)));

        // Add Unbreakable Blocks at borders
        for (int x = 0; x < GameState.getGridSize(); x++) {
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(x, 0)));
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(x, GameState.getGridSize() - 1)));
            occupiedPositions.add(new Point(x, 0));
            occupiedPositions.add(new Point(x, GameState.getGridSize() - 1));
        }
        for (int y = 1; y < GameState.getGridSize() - 1; y++) {
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(0, y)));
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(GameState.getGridSize() - 1, y)));
            occupiedPositions.add(new Point(0, y));
            occupiedPositions.add(new Point(GameState.getGridSize() - 1, y));
        }

        // Patrón de hielo DIFERENTE para nivel 3
        List<Point> predeterminedIcePositions = getPredeterminedIcePositionsLevel3();
        for (Point icePos : predeterminedIcePositions) {
            if (!gameState.getIglu().collidesWith(icePos) && !isPositionOccupied(icePos, occupiedPositions)) {
                gameState.addIceBlock(new IceBlock(icePos, false));
                occupiedPositions.add(icePos);
            }
        }

        // Agregar baldosas calientes (Nivel 3: muchas)
        List<Point> hotTilePositions = List.of(
                new Point(2, 2), new Point(10, 10), new Point(2, 10), new Point(10, 2),
                new Point(6, 4), new Point(6, 8), new Point(4, 6), new Point(8, 6)); // 6,4 and 6,8 collide with Iglu

        for (Point pos : hotTilePositions) {
            if (!gameState.getIglu().collidesWith(pos) && !isPositionOccupied(pos, occupiedPositions)) {
                gameState.addHotTile(new HotTile(pos));
                occupiedPositions.add(pos);
            }
        }

        // Spawn Player 2 if applicable (2 Players OR Machine vs Machine)
        if ((numberOfPlayers == 2 || numberOfPlayers == 0) && gameState.getPlayer2() != null) {
            Point p2Pos = getRandomFreePosition(random, occupiedPositions);
            gameState.getPlayer2().setPosition(p2Pos);
            occupiedPositions.add(p2Pos);
        }

        // Agregar 1 calamar
        Point calamarPos = getRandomFreePosition(random, occupiedPositions);
        Enemy calamar = new Enemy(calamarPos, EnemyType.CALAMAR);
        gameState.addEnemy(calamar);
        occupiedPositions.add(calamarPos);

        // Wave 1: 16 Piñas (Initial) - Removing this block as Level 4 logic below
        // handles it
        // and Level 3 logic handles its own. This was a legacy block that might
        // interfere.

        // Initialize specific level logic
        initializeLevelSpecifics(gameState.getLevel(), occupiedPositions);
    }

    private void initializeLevelSpecifics(int level, List<Point> occupiedPositions) {
        Random random = new Random();

        switch (level) {
            case 1:
                // Level 1: Uvas (Initial), Platano (Pending)
                for (int i = 0; i < 20; i++) { // 20 Uvas
                    Point pos = gameLogic.findRandomEmptyPosition();
                    if (pos != null) {
                        gameState.addFruit(new Fruit(pos, FruitType.UVA));
                        occupiedPositions.add(pos);
                    }
                }
                List<Fruit> wave1_2 = new ArrayList<>();
                for (int i = 0; i < 5; i++) { // 5 Platanos
                    Point pos = gameLogic.findRandomEmptyPosition();
                    if (pos != null)
                        wave1_2.add(new Fruit(pos, FruitType.PLATANO));
                }
                gameState.addPendingFruitWave(wave1_2);
                break;

            case 2:
                // Level 2: Platano (Initial), Piña (Pending)
                for (int i = 0; i < 15; i++) { // 15 Platanos
                    Point pos = gameLogic.findRandomEmptyPosition();
                    if (pos != null) {
                        gameState.addFruit(new Fruit(pos, FruitType.PLATANO));
                        occupiedPositions.add(pos);
                    }
                }
                List<Fruit> wave2_2 = new ArrayList<>();
                for (int i = 0; i < 5; i++) { // 5 Piñas
                    Point pos = gameLogic.findRandomEmptyPosition();
                    if (pos != null)
                        wave2_2.add(new Fruit(pos, FruitType.PIÑA));
                }
                gameState.addPendingFruitWave(wave2_2);
                break;

            case 3:
                // Level 3: Piña (Initial), Cereza (Pending)
                for (int i = 0; i < 15; i++) { // 15 Piñas
                    Point pos = gameLogic.findRandomEmptyPosition();
                    if (pos != null) {
                        gameState.addFruit(new Fruit(pos, FruitType.PIÑA));
                        occupiedPositions.add(pos);
                    }
                }
                List<Fruit> wave3_2 = new ArrayList<>();
                for (int i = 0; i < 5; i++) { // 5 Cerezas
                    Point pos = gameLogic.findRandomEmptyPosition();
                    if (pos != null)
                        wave3_2.add(new Fruit(pos, FruitType.CEREZA));
                }
                gameState.addPendingFruitWave(wave3_2);
                break;

            case 4:
                // Level 4: Piña (Initial), Cereza (Pending), Cactus (Pending)
                for (int i = 0; i < 10; i++) { // 10 Piñas
                    Point pos = gameLogic.findRandomEmptyPosition();
                    if (pos != null) {
                        gameState.addFruit(new Fruit(pos, FruitType.PIÑA));
                        occupiedPositions.add(pos);
                    }
                }
                List<Fruit> wave4_2 = new ArrayList<>();
                for (int i = 0; i < 5; i++) { // 5 Cerezas
                    Point pos = gameLogic.findRandomEmptyPosition();
                    if (pos != null)
                        wave4_2.add(new Fruit(pos, FruitType.CEREZA));
                }
                gameState.addPendingFruitWave(wave4_2);

                List<Fruit> wave4_3 = new ArrayList<>();
                for (int i = 0; i < 4; i++) { // 4 Cactus
                    Point pos = gameLogic.findRandomEmptyPosition();
                    if (pos != null)
                        wave4_3.add(new Fruit(pos, FruitType.CACTUS));
                }
                gameState.addPendingFruitWave(wave4_3);
                System.out.println("DEBUG: Level 4 Initialized. Piñas: " + 10 + ", Pending Wave 1 (Cerezas): "
                        + wave4_2.size() + ", Pending Wave 2 (Cactus): " + wave4_3.size());
                break;
        }
    }

    /**
     * Inicializa el nivel 4: 1 Narval + Piñas (Inicial) + Cerezas (Ola 1).
     */
    private void initializeLevel4(int numberOfPlayers) {
        Random random = new Random();
        List<Point> occupiedPositions = new ArrayList<>();

        // Fix Player 1 Position
        gameState.getPlayer().setPosition(new Point(1, 1));
        occupiedPositions.add(gameState.getPlayer().getPosition());

        // Create Central Iglu
        gameState.setIglu(new Iglu(new Point(5, 5)));

        // Add Unbreakable Blocks at borders
        for (int x = 0; x < GameState.getGridSize(); x++) {
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(x, 0)));
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(x, GameState.getGridSize() - 1)));
            occupiedPositions.add(new Point(x, 0));
            occupiedPositions.add(new Point(x, GameState.getGridSize() - 1));
        }
        for (int y = 1; y < GameState.getGridSize() - 1; y++) {
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(0, y)));
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(GameState.getGridSize() - 1, y)));
            occupiedPositions.add(new Point(0, y));
            occupiedPositions.add(new Point(GameState.getGridSize() - 1, y));
        }

        // Patrón de hielo (Unique Level 4 Pattern)
        List<Point> predeterminedIcePositions = getPredeterminedIcePositionsLevel4();
        for (Point icePos : predeterminedIcePositions) {
            if (!gameState.getIglu().collidesWith(icePos) && !isPositionOccupied(icePos, occupiedPositions)) {
                gameState.addIceBlock(new IceBlock(icePos, false));
                occupiedPositions.add(icePos);
            }
        }

        // Agregar baldosas calientes (Muchas, evitando Iglú y bloqueados)
        // Added (1, 6) and (11, 6) for 8 total
        List<Point> hotTilePositions = List.of(
                new Point(3, 3), new Point(9, 3), new Point(3, 9), new Point(9, 9),
                new Point(6, 2), new Point(6, 10),
                new Point(1, 6), new Point(11, 6));

        for (Point pos : hotTilePositions) {
            if (!gameState.getIglu().collidesWith(pos) && !isPositionOccupied(pos, occupiedPositions)) {
                gameState.addHotTile(new HotTile(pos));
                occupiedPositions.add(pos);
            }
        }

        // Spawn Player 2 if applicable
        if ((numberOfPlayers == 2 || numberOfPlayers == 0) && gameState.getPlayer2() != null) {
            Point p2Pos = getRandomFreePosition(random, occupiedPositions);
            gameState.getPlayer2().setPosition(p2Pos);
            occupiedPositions.add(p2Pos);
        }

        // Agregar 1 Narval (Exclusivo Nivel 4)
        Point narvalPos = getRandomFreePosition(random, occupiedPositions);
        Enemy narval = new Enemy(narvalPos, EnemyType.NARVAL);
        gameState.addEnemy(narval);
        occupiedPositions.add(narvalPos);

        // Initialize specific level logic (Fruits & Waves: Pineapple -> Cherry ->
        // Cactus)
        initializeLevelSpecifics(4, occupiedPositions);
    }

    // ==================== PATRONES DE HIELO ====================

    /**
     * Obtiene las posiciones de hielo predeterminadas para el nivel 1.
     * Patrón: Simétrico con bloques en esquinas.
     */
    private List<Point> getPredeterminedIcePositionsLevel1() {
        List<Point> icePositions = new ArrayList<>();

        // Patrón simétrico
        icePositions.add(new Point(2, 3));
        icePositions.add(new Point(3, 3));
        icePositions.add(new Point(4, 3));

        icePositions.add(new Point(8, 3));
        icePositions.add(new Point(9, 3));
        icePositions.add(new Point(10, 3));

        icePositions.add(new Point(2, 6));
        icePositions.add(new Point(2, 7));
        icePositions.add(new Point(2, 8));

        icePositions.add(new Point(10, 6));
        icePositions.add(new Point(10, 7));
        icePositions.add(new Point(10, 8));

        icePositions.add(new Point(2, 9));
        icePositions.add(new Point(3, 9));
        icePositions.add(new Point(4, 9));

        icePositions.add(new Point(8, 9));
        icePositions.add(new Point(9, 9));
        icePositions.add(new Point(10, 9));

        icePositions.add(new Point(5, 5));
        icePositions.add(new Point(7, 5));
        icePositions.add(new Point(5, 7));
        icePositions.add(new Point(7, 7));

        return icePositions;
    }

    /**
     * Obtiene las posiciones de hielo predeterminadas para el nivel 2.
     * Patrón: Cruz central + bloques en esquinas.
     */
    private List<Point> getPredeterminedIcePositionsLevel2() {
        List<Point> icePositions = new ArrayList<>();

        // Cruz central (alrededor del Iglú de 3x3 que está en 5,5)
        // Iglu covers 5,5 to 7,7. We place ice around it.

        // Arriba del Iglu
        icePositions.add(new Point(5, 4));
        icePositions.add(new Point(6, 4));
        icePositions.add(new Point(7, 4));

        // Abajo del Iglu
        icePositions.add(new Point(5, 8));
        icePositions.add(new Point(6, 8));
        icePositions.add(new Point(7, 8));

        // Izquierda del Iglu
        icePositions.add(new Point(4, 5));
        icePositions.add(new Point(4, 6));
        icePositions.add(new Point(4, 7));

        // Derecha del Iglu
        icePositions.add(new Point(8, 5));
        icePositions.add(new Point(8, 6));
        icePositions.add(new Point(8, 7));

        // Bloques en esquinas (Evitando Spawn 1,1)
        // Top-Left
        icePositions.add(new Point(2, 1));
        icePositions.add(new Point(3, 1));
        icePositions.add(new Point(1, 2));
        icePositions.add(new Point(1, 3));

        // Top-Right
        icePositions.add(new Point(9, 1));
        icePositions.add(new Point(10, 1));
        icePositions.add(new Point(11, 1));
        icePositions.add(new Point(11, 2));
        icePositions.add(new Point(11, 3));

        // Bottom-Left
        icePositions.add(new Point(1, 9));
        icePositions.add(new Point(1, 10));
        icePositions.add(new Point(1, 11));
        icePositions.add(new Point(2, 11));
        icePositions.add(new Point(3, 11));

        // Bottom-Right
        icePositions.add(new Point(9, 11));
        icePositions.add(new Point(10, 11));
        icePositions.add(new Point(11, 11));
        icePositions.add(new Point(11, 10));
        icePositions.add(new Point(11, 9));

        // Extra scattered blocks for complexity
        icePositions.add(new Point(3, 3));
        icePositions.add(new Point(9, 3));
        icePositions.add(new Point(3, 9));
        icePositions.add(new Point(9, 9));

        return icePositions;
    }

    /**
     * Obtiene las posiciones de hielo predeterminadas para el nivel 3.
     * Patrón: Laberinto con líneas horizontales y verticales.
     */
    private List<Point> getPredeterminedIcePositionsLevel3() {
        List<Point> icePositions = new ArrayList<>();

        // Líneas horizontales
        icePositions.add(new Point(1, 3));
        icePositions.add(new Point(2, 3));
        icePositions.add(new Point(3, 3));
        icePositions.add(new Point(4, 3));

        icePositions.add(new Point(8, 3));
        icePositions.add(new Point(9, 3));
        icePositions.add(new Point(10, 3));
        icePositions.add(new Point(11, 3));

        icePositions.add(new Point(1, 9));
        icePositions.add(new Point(2, 9));
        icePositions.add(new Point(3, 9));
        icePositions.add(new Point(4, 9));

        icePositions.add(new Point(8, 9));
        icePositions.add(new Point(9, 9));
        icePositions.add(new Point(10, 9));
        icePositions.add(new Point(11, 9));

        // Columnas verticales
        icePositions.add(new Point(3, 5));
        icePositions.add(new Point(3, 6));
        icePositions.add(new Point(3, 7));

        icePositions.add(new Point(9, 5));
        icePositions.add(new Point(9, 6));
        icePositions.add(new Point(9, 7));

        // Centro
        icePositions.add(new Point(6, 6));

        return icePositions;
    }

    /**
     * Obtiene las posiciones de hielo predeterminadas para el nivel 4.
     * Patrón: Diamante / Diagonal.
     */
    private List<Point> getPredeterminedIcePositionsLevel4() {
        List<Point> icePositions = new ArrayList<>();

        // Diagonales desde las esquinas hacia el centro
        icePositions.add(new Point(2, 2));
        icePositions.add(new Point(3, 3));
        icePositions.add(new Point(9, 3));
        icePositions.add(new Point(10, 2));

        icePositions.add(new Point(2, 10));
        icePositions.add(new Point(3, 9));
        icePositions.add(new Point(9, 9));
        icePositions.add(new Point(10, 10));

        // Bloques centrales rodeando el Iglú
        icePositions.add(new Point(6, 3));
        icePositions.add(new Point(6, 9));
        icePositions.add(new Point(3, 6));
        icePositions.add(new Point(9, 6));

        // Esquinas internas
        icePositions.add(new Point(4, 4));
        icePositions.add(new Point(8, 4));
        icePositions.add(new Point(4, 8));
        icePositions.add(new Point(8, 8));

        return icePositions;
    }

    // ==================== UTILIDADES PRIVADAS ====================

    /**
     * Obtiene una posición libre aleatoria en el grid.
     */
    private Point getRandomFreePosition(Random random, List<Point> occupiedPositions) {
        Point newPosition;
        do {
            int x = random.nextInt(GameState.getGridSize());
            int y = random.nextInt(GameState.getGridSize());
            newPosition = new Point(x, y);
        } while (isPositionOccupied(newPosition, occupiedPositions));

        return newPosition;
    }

    /**
     * Verifica si una posición está ocupada.
     */
    private boolean isPositionOccupied(Point position, List<Point> occupiedPositions) {
        if (gameState.getIglu() != null && gameState.getIglu().collidesWith(position)) {
            return true;
        }
        for (Point occupied : occupiedPositions) {
            if (occupied.x == position.x && occupied.y == position.y) {
                return true;
            }
        }
        return false;
    }

    // ==================== COMANDOS DE MOVIMIENTO P1 ====================

    public void movePlayerUp() {
        if (!paused)
            gameLogic.movePlayer(Direction.UP);
    }

    public void movePlayerDown() {
        if (!paused)
            gameLogic.movePlayer(Direction.DOWN);
    }

    public void movePlayerLeft() {
        if (!paused)
            gameLogic.movePlayer(Direction.LEFT);
    }

    public void movePlayerRight() {
        if (!paused)
            gameLogic.movePlayer(Direction.RIGHT);
    }

    public void stopPlayer() {
        if (!paused)
            gameLogic.stopPlayer();
    }

    // ==================== COMANDOS DE MOVIMIENTO P2 ====================

    public void movePlayer2Up() {
        if (!paused)
            gameLogic.movePlayer2(Direction.UP);
    }

    public void movePlayer2Down() {
        if (!paused)
            gameLogic.movePlayer2(Direction.DOWN);
    }

    public void movePlayer2Left() {
        if (!paused)
            gameLogic.movePlayer2(Direction.LEFT);
    }

    public void movePlayer2Right() {
        if (!paused)
            gameLogic.movePlayer2(Direction.RIGHT);
    }

    public void stopPlayer2() {
        if (!paused)
            gameLogic.stopPlayer2();
    }

    // ==================== ACCIONES DEL JUGADOR ====================

    /**
     * Ejecuta la acción del jugador 1.
     */
    public List<Point> performSpaceAction() {
        if (paused)
            return new ArrayList<>();
        return gameLogic.performSpaceAction();
    }

    /**
     * Ejecuta la acción del jugador 2.
     */
    public List<Point> performActionPlayer2() {
        if (paused)
            return new ArrayList<>();
        return gameLogic.performActionPlayer2();
    }

    // ==================== ACTUALIZACIÓN DEL JUEGO ====================

    /**
     * Actualiza el estado completo del juego.
     * Debe llamarse en cada frame del gameloop.
     */
    public void update() {
        long currentTime = System.currentTimeMillis();
        int deltaTime = (int) (currentTime - lastUpdateTime);
        lastUpdateTime = currentTime;

        if (!paused) {
            gameLogic.update(deltaTime);
        }
    }

    // ==================== SNAPSHOTS PARA PRESENTACIÓN ====================

    /**
     * Obtiene snapshot del jugador para renderizado.
     *
     * @return Snapshot del jugador
     */
    public PlayerSnapshot getPlayerSnapshot() {
        return PlayerSnapshot.from(gameState.getPlayer());
    }

    /**
     * Obtiene snapshot del jugador 2 para renderizado.
     *
     * @return Snapshot del jugador 2 o null
     */
    public PlayerSnapshot getPlayer2Snapshot() {
        if (gameState.getPlayer2() != null) {
            return PlayerSnapshot.from(gameState.getPlayer2());
        }
        return null;
    }

    /**
     * Obtiene snapshots de todos los enemigos para renderizado.
     *
     * @return Lista de snapshots de enemigos
     */
    public List<EnemySnapshot> getEnemySnapshots() {
        List<EnemySnapshot> snapshots = new ArrayList<>();
        for (Enemy enemy : gameState.getEnemies()) {
            snapshots.add(EnemySnapshot.from(enemy));
        }
        return snapshots;
    }

    /**
     * Obtiene snapshots de todas las frutas para renderizado.
     *
     * @return Lista de snapshots de frutas
     */
    public List<FruitSnapshot> getFruitSnapshots() {
        List<FruitSnapshot> snapshots = new ArrayList<>();
        for (Fruit fruit : gameState.getFruits()) {
            snapshots.add(FruitSnapshot.from(fruit));
        }
        return snapshots;
    }

    /**
     * Obtiene snapshots de todos los bloques de hielo para renderizado.
     *
     * @return Lista de snapshots de bloques de hielo
     */
    public List<IceBlockSnapshot> getIceBlockSnapshots() {
        List<IceBlockSnapshot> snapshots = new ArrayList<>();
        for (IceBlock ice : gameState.getIceBlocks()) {
            snapshots.add(IceBlockSnapshot.from(ice));
        }
        return snapshots;
    }

    /**
     * Obtiene snapshots de todas las baldosas calientes para renderizado.
     *
     * @return Lista de snapshots de baldosas calientes
     */
    public List<HotTileSnapshot> getHotTileSnapshots() {
        List<HotTileSnapshot> snapshots = new ArrayList<>();
        for (HotTile tile : gameState.getHotTiles()) {
            snapshots.add(HotTileSnapshot.from(tile));
        }
        return snapshots;
    }

    public IgluSnapshot getIgluSnapshot() {
        return IgluSnapshot.from(gameState.getIglu());
    }

    public List<UnbreakableBlockSnapshot> getUnbreakableBlockSnapshots() {
        List<UnbreakableBlockSnapshot> snapshots = new ArrayList<>();
        for (UnbreakableBlock block : gameState.getUnbreakableBlocks()) {
            snapshots.add(UnbreakableBlockSnapshot.from(block));
        }
        return snapshots;
    }

    // ==================== CONSULTAS DEL ESTADO DEL JUGADOR ====================

    public Point getPlayerPosition() {
        return gameState.getPlayer().getPosition();
    }

    public String getPlayerDirection() {
        Direction dir = gameState.getPlayer().getCurrentDirection();
        if (dir == Direction.IDLE) {
            dir = gameState.getPlayer().getFacingDirection();
        }
        return dir.toString();
    }

    public boolean isPlayerMoving() {
        return gameState.getPlayer().getCurrentDirection() != Direction.IDLE;
    }

    public boolean isPlayerSneezing() {
        return gameState.getPlayer().isSneezing();
    }

    public boolean isPlayerKicking() {
        return gameState.getPlayer().isKicking();
    }

    public boolean isPlayerDying() {
        return gameState.getPlayer().isDying();
    }

    public boolean isPlayerCelebrating() {
        return gameState.getPlayer().isCelebrating();
    }

    public boolean isPlayerAlive() {
        return gameState.getPlayer() != null && gameState.getPlayer().isAlive();
    }

    public boolean isPlayer2Alive() {
        return gameState.getPlayer2() == null || gameState.getPlayer2().isAlive();
    }

    /**
     * Verifica si la animación de muerte ha terminado completamente.
     *
     * @return true si la animación terminó
     */
    public boolean isDeathAnimationComplete() {
        Player player = gameState.getPlayer();
        Player player2 = gameState.getPlayer2();

        boolean p1Dead = (player != null) && !player.isDying() && !player.isAlive();
        boolean p2Dead = (player2 != null) && !player2.isDying() && !player2.isAlive();

        // In PvP/MvM, game over only if BOTH players die (GameLogic ensures isGameOver
        // is set).
        // Here we ensure we wait for BOTH animations to complete before showing
        // summary.
        if (gameState.getNumberOfPlayers() == 2 || gameState.getNumberOfPlayers() == 0) {
            return p1Dead && p2Dead;
        }
        return p1Dead;
    }

    /**
     * Verifica si el jugador está ocupado y no puede realizar acciones.
     *
     * @return true si está ocupado
     */
    public boolean isPlayerBusy() {
        return gameState.getPlayer().isBusy();
    }

    public String getPlayerCharacterType() {
        return gameState.getPlayer().getCharacterType();
    }

    // ==================== CONSULTAS DEL ESTADO DEL JUEGO ====================

    public boolean isGameOver() {
        return gameState.isGameOver();
    }

    public boolean isVictory() {
        return gameState.isVictory();
    }

    /**
     * Verifica si se debe reiniciar el nivel.
     *
     * @return true si el tiempo se acabó O si el jugador murió completamente
     */
    public boolean shouldRestartLevel() {
        return gameState.isTimeUp() ||
                (gameState.isGameOver() && isDeathAnimationComplete());
    }

    public int getLevel() {
        return gameState.getLevel();
    }

    public int getNumberOfPlayers() {
        return gameState.getNumberOfPlayers();
    }

    public int getCurrentLevel() {
        return gameState.getLevel();
    }

    // ==================== TEMPORIZADOR ====================

    public String getFormattedTime() {
        return gameState.getFormattedTime();
    }

    public long getTimeRemaining() {
        return gameState.getTimeRemaining();
    }

    public boolean isTimeUp() {
        return gameState.isTimeUp();
    }

    public int getScore() {
        return gameState.getScore();
    }

    public int getScorePlayer2() {
        return gameState.getScorePlayer2();
    }

    // ==================== CONTEO DE FRUTAS ====================

    /**
     * Cuenta frutas restantes por tipo.
     *
     * @param fruitType Tipo de fruta como String
     * @return Cantidad de frutas restantes de ese tipo
     */
    public int countRemainingFruits(String fruitType) {
        int count = 0;
        for (Fruit fruit : gameState.getFruits()) {
            if (!fruit.isCollected() && fruit.getType().toString().equals(fruitType)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Obtiene tipos únicos de frutas en el nivel.
     *
     * @return Lista de nombres de tipos de frutas
     */
    public List<String> getUniqueFruitTypes() {
        List<String> types = new ArrayList<>();
        for (Fruit fruit : gameState.getFruits()) {
            String type = fruit.getType().toString();
            if (!types.contains(type)) {
                types.add(type);
            }
        }
        return types;
    }

}
