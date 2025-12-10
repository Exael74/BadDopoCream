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
import java.util.Map;
import java.util.HashMap;

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
    /**
     * Constructor de la fachada del juego.
     *
     * @param characterType   Tipo de personaje ("Chocolate", "Fresa", "Vainilla")
     * @param level           Nivel a jugar (1, 2, 3)
     * @param numberOfPlayers Número de jugadores (0=IA vs IA, 1=1P, 2=2P)
     * @param aiTypeP1        Tipo de IA para P1 (String)
     * @param aiTypeP2        Tipo de IA para P2 (String)
     */
    public GameFacade(String characterType, String characterTypeP2, String p1Name, String p2Name, int level,
            int numberOfPlayers, String aiTypeP1, String aiTypeP2, boolean isP2CPU) {
        this(characterType, characterTypeP2, p1Name, p2Name, level, numberOfPlayers, aiTypeP1, aiTypeP2, isP2CPU, null);
    }

    public GameFacade(String characterType, String characterTypeP2, String p1Name, String p2Name, int level,
            int numberOfPlayers, String aiTypeP1, String aiTypeP2, boolean isP2CPU,
            domain.dto.LevelConfigurationDTO config) {
        this.gameState = new GameState(characterType, level, numberOfPlayers);
        this.gameState.setP2CPU(isP2CPU);

        // Convert Strings to Enums
        AIType type1 = parseAIType(aiTypeP1);
        AIType type2 = parseAIType(aiTypeP2);

        // Set Player 2 character type if applicable
        if (gameState.getPlayer2() != null && characterTypeP2 != null) {
            Point pos = gameState.getPlayer2().getPosition();
            Player p2 = new Player(pos, characterTypeP2);
            gameState.setPlayer2(p2);
        }

        // Set AI Types
        if (numberOfPlayers == 0) {
            // Machine vs Machine
            if (gameState.getPlayer() != null)
                gameState.getPlayer().setAIType(type1);
            if (gameState.getPlayer2() != null)
                gameState.getPlayer2().setAIType(type2);
        } else if (numberOfPlayers == 2 && isP2CPU) {
            // Player 1 vs Machine
            if (gameState.getPlayer2() != null) {
                gameState.getPlayer2().setAIType(type2 != null ? type2 : AIType.EXPERT);
            }
        }

        this.gameState.setPlayerNames(p1Name, p2Name);

        this.gameLogic = new GameLogic(gameState);
        this.persistenceService = new PersistenceService();
        this.lastUpdateTime = System.currentTimeMillis();
        this.paused = false;

        // Initialize Level with Config if provided, otherwise default
        if (config != null) {
            initializeLevel(level, numberOfPlayers, config);
        } else {
            initializeLevel(level, numberOfPlayers);
        }
    }

    public GameFacade(String characterType, String characterTypeP2, String p1Name, String p2Name, int level,
            int numberOfPlayers, String aiTypeP1, String aiTypeP2) {
        this(characterType, characterTypeP2, p1Name, p2Name, level, numberOfPlayers, aiTypeP1, aiTypeP2, false, null);
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
    /**
     * Inicializa el nivel especificado usando configuración predeterminada.
     */
    private void initializeLevel(int level, int numberOfPlayers) {
        initializeLevel(level, numberOfPlayers, getDefaultConfiguration(level));
    }

    /**
     * Initialize level with custom configuration.
     */
    private void initializeLevel(int level, int numberOfPlayers, LevelConfigurationDTO config) {
        try {
            BadDopoLogger.logInfo(
                    "Iniciando nivel " + level + " con " + numberOfPlayers + " jugadores y config personalizada.");

            // 1. Setup Base Map Structure (Walls, Ice, Iglu) - Preserving Level Design
            setupMapStructure(level, numberOfPlayers);

            // 2. Spawn Configured Entities (Fruits, Enemies, HotTiles)
            spawnDynamicEntities(config);

        } catch (BadDopoException e) {
            BadDopoLogger.logError("Error al inicializar el nivel: " + e.getMessage(), e);
        } catch (Exception e) {
            BadDopoLogger.logError("Error inesperado al inicializar el nivel", e);
        }
    }

    /**
     * Sets up the static map elements (Walls, Iglu, Default Ice) based on Level ID.
     */
    private void setupMapStructure(int level, int numberOfPlayers) throws BadDopoException {
        switch (level) {
            case 1:
                setupLevel1Structure(numberOfPlayers);
                break;
            case 2:
                initializeLevel2StructureOnly(numberOfPlayers);
                break;
            case 3:
                initializeLevel3StructureOnly(numberOfPlayers);
                break;
            case 4:
                initializeLevel4StructureOnly(numberOfPlayers);
                break;
            default:
                throw new BadDopoException("Nivel inválido: " + level);
        }
    }

    /**
     * Spawns entities based on the configuration.
     */
    private void spawnDynamicEntities(LevelConfigurationDTO config) {
        // 1. Spawn Fruits (Ordered Waves: Uvas -> Platanos -> Piña -> Cactus)
        // Store counts first
        Map<FruitType, Integer> counts = new java.util.EnumMap<>(FruitType.class);
        for (Map.Entry<String, Integer> entry : config.getFruitCounts().entrySet()) {
            try {
                FruitType type = FruitType.valueOf(entry.getKey());
                counts.put(type, entry.getValue());
            } catch (IllegalArgumentException e) {
                BadDopoLogger.logError("Unknown Fruit Type in Config: " + entry.getKey(), e);
            }
        }

        // Define strict wave order
        FruitType[] waveOrder = { FruitType.UVA, FruitType.PLATANO, FruitType.PIÑA, FruitType.CACTUS,
                FruitType.CEREZA };

        // Populate Pending Waves
        for (FruitType type : waveOrder) {
            if (counts.containsKey(type)) {
                int count = counts.get(type);
                if (count > 0) {
                    List<Fruit> wave = new ArrayList<>();
                    // We PRE-CALCULATE positions? No, avoiding overlaps is hard if we pre-calc.
                    // But GameState expects List<Fruit>.
                    // A better approach for MVC:
                    // Add a "PendingWave" concept to State? No, keep it simple.
                    // We will generate the Fruit objects but their position might need to be
                    // re-validated on spawn?
                    // Or simpler: Just store the Types and Counts in GameState?
                    // The requirement "pendingFruitWaves" is List<List<Fruit>>.
                    // Let's try to generate them with temporary positions or find positions now.
                    // Finding positions now is safer provided the map layout is static.

                    for (int i = 0; i < count; i++) {
                        Point position = findFreePosition();
                        if (position != null) {
                            wave.add(new Fruit(position, type));
                        }
                    }
                    if (!wave.isEmpty()) {
                        gameState.addPendingFruitWave(wave);
                    }
                }
            }
        }

        // Spawn First Wave Immediately
        if (!gameState.getPendingFruitWaves().isEmpty()) {
            List<Fruit> firstWave = gameState.getPendingFruitWaves().remove(0);
            for (Fruit f : firstWave) {
                gameState.addFruit(f);
            }
        }

        // 2. Spawn Enemies
        for (Map.Entry<String, Integer> entry : config.getEnemyCounts().entrySet()) {
            String enemyTypeStr = entry.getKey();
            int count = entry.getValue();
            try {
                EnemyType type = EnemyType.valueOf(enemyTypeStr);
                for (int i = 0; i < count; i++) {
                    spawnEnemy(type);
                }
            } catch (IllegalArgumentException e) {
                BadDopoLogger.logError("Unknown Enemy Type in Config: " + enemyTypeStr, e);
            }
        }

        // 3. Spawn Hot Tiles
        initializeHotTiles(config.getHotTileCount());
    }

    // Helper for Spawning specific fruit type
    private void spawnRandomFruit(FruitType type) {
        Point position = findFreePosition();
        if (position != null) {
            Fruit fruit = new Fruit(position, type); // Corrected Order
            gameState.addFruit(fruit);
        }
    }

    // Helper for Spawning specific enemy type
    private void spawnEnemy(EnemyType type) {
        Point position = findFreePosition();
        if (position != null) {
            Enemy enemy = new Enemy(position, type); // Corrected Instantiation
            gameState.addEnemy(enemy);
        }
    }

    /**
     * Finds a random free position in the grid (not occupied by walls, ice, etc).
     */
    private Point findFreePosition() {
        Random random = new Random();
        int attempts = 0;
        while (attempts < 100) {
            int x = random.nextInt(GameState.getGridSize());
            int y = random.nextInt(GameState.getGridSize());
            Point p = new Point(x, y);

            // Check collision with walls, iglu, ice, other entities
            // For simplicity, checking if GameState says it's empty
            // But GameState might not have a unified "isOccupied" for strictly spawning.
            // We'll check basic constraints.

            if (gameState.getIglu() != null && gameState.getIglu().collidesWith(p))
                continue;
            if (isWall(p))
                continue;
            if (hasIceAt(p))
                continue;
            if (hasHotTileAt(p))
                continue;
            // Check fruits/enemies? Ideally yes, but for now simple check.

            return p;
        }
        return null;
    }

    private boolean isWall(Point p) {
        // Simple border check if walls are only borders
        int s = GameState.getGridSize();
        return p.x == 0 || p.x == s - 1 || p.y == 0 || p.y == s - 1;
        // Also check Unbreakable blocks list if needed.
    }

    private void initializeHotTiles(int count) {
        for (int i = 0; i < count; i++) {
            Point pos = findFreePosition();
            if (pos != null) {
                gameState.addHotTile(new HotTile(pos));
            }
        }
    }

    private void clearSpaceForPlayers(int numberOfPlayers) {
        // Clear ice/entities around 1,1
        Point p1 = new Point(1, 1);
        removeIceAt(p1);
        gameState.getPlayer().setPosition(p1);

        if (numberOfPlayers == 2 || numberOfPlayers == 0) {
            if (gameState.getPlayer2() != null) {
                Point p2 = findFreePosition();
                if (p2 == null)
                    p2 = new Point(11, 11);
                removeIceAt(p2);
                gameState.getPlayer2().setPosition(p2);
            }
        }
    }

    // --- Helper Methods for Entity Management and Collision ---

    private void removeIceAt(Point p) {
        // Safe removal using iterator or removeIf
        gameState.getIceBlocks().removeIf(ice -> ice.getPosition().equals(p));
    }

    private boolean hasIceAt(Point p) {
        return gameState.getIceBlocks().stream().anyMatch(ice -> ice.getPosition().equals(p));
    }

    private boolean hasHotTileAt(Point p) {
        return gameState.getHotTiles().stream().anyMatch(tile -> tile.getPosition().equals(p));
    }

    // --- Structure Helpers ---

    private void initializeCentralIglu() {
        gameState.setIglu(new Iglu(new Point(5, 5)));
    }

    private void initializeBorders() {
        for (int x = 0; x < GameState.getGridSize(); x++) {
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(x, 0))); // Top
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(x, GameState.getGridSize() - 1))); // Bottom
        }
        for (int y = 1; y < GameState.getGridSize() - 1; y++) {
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(0, y))); // Left
            gameState.addUnbreakableBlock(new UnbreakableBlock(new Point(GameState.getGridSize() - 1, y))); // Right
        }
    }

    private void initializeIcePatternLevel1() {
        List<Point> predeterminedIcePositions = getPredeterminedIcePositionsLevel1();
        for (Point icePos : predeterminedIcePositions) {
            if (gameState.getIglu() != null && !gameState.getIglu().collidesWith(icePos)) {
                gameState.addIceBlock(new IceBlock(icePos, false));
            }
        }
    }

    /**
     * Initializes the structure (walls, iglu, ice) for Level 1.
     */
    private void setupLevel1Structure(int numberOfPlayers) {
        gameState.clear(); // Ensure clear call works (or implement it if missing)
        // If gameState.clear() is undefined, we need to clear lists manually.
        // Assuming clear() might be missing based on lint "The method clear() is
        // undefined for the type GameState"
        gameState.getFruits().clear();
        gameState.getEnemies().clear();
        gameState.getIceBlocks().clear();
        gameState.getHotTiles().clear();
        gameState.getUnbreakableBlocks().clear();

        initializeBorders();
        initializeCentralIglu();
        // Clear space MUST happen after or before?
        // If we clear space, we remove ice. So ice must be there.
        initializeIcePatternLevel1();
        clearSpaceForPlayers(numberOfPlayers);
    }

    // Alias Helpers
    private void initializeLevel2StructureOnly(int n) {
        // CLEANUP
        gameState.clear();
        gameState.getFruits().clear();
        gameState.getEnemies().clear();
        gameState.getIceBlocks().clear();
        gameState.getHotTiles().clear();
        gameState.getUnbreakableBlocks().clear();

        initializeBorders();
        initializeCentralIglu();
        initializeIcePatternLevel2();
        clearSpaceForPlayers(n);
    }

    private void initializeLevel3StructureOnly(int n) {
        // CLEANUP
        gameState.clear();
        gameState.getFruits().clear();
        gameState.getEnemies().clear();
        gameState.getIceBlocks().clear();
        gameState.getHotTiles().clear();
        gameState.getUnbreakableBlocks().clear();

        initializeBorders();
        initializeCentralIglu();
        initializeIcePatternLevel3();
        clearSpaceForPlayers(n);
    }

    private void initializeLevel4StructureOnly(int n) {
        // CLEANUP
        gameState.clear();
        gameState.getFruits().clear();
        gameState.getEnemies().clear();
        gameState.getIceBlocks().clear();
        gameState.getHotTiles().clear();
        gameState.getUnbreakableBlocks().clear();

        initializeBorders();
        initializeCentralIglu();
        initializeIcePatternLevel4();
        clearSpaceForPlayers(n);
    }

    private void initializeLevel1(int numberOfPlayers) {
        Random random = new Random();
        List<Point> occupiedPositions = new ArrayList<>();

        // Fix Player 1 Position (Move out of center)
        // Handled by clearSpaceForPlayers
        // gameState.getPlayer().setPosition(new Point(1, 1));
        // occupiedPositions.add(gameState.getPlayer().getPosition());

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
        return icePositions;
    }

    /**
     * Level 2: Cross Pattern (Diagonal Ice)
     */
    private void initializeIcePatternLevel2() {
        for (Point p : getPredeterminedIcePositionsLevel2()) {
            if (gameState.getIglu() != null && !gameState.getIglu().collidesWith(p)) {
                gameState.addIceBlock(new IceBlock(p, false));
            }
        }
    }

    private List<Point> getPredeterminedIcePositionsLevel2() {
        List<Point> list = new ArrayList<>();
        int size = GameState.getGridSize();
        for (int i = 2; i < size - 2; i++) {
            // X pattern
            list.add(new Point(i, i));
            list.add(new Point(i, size - 1 - i));
        }
        return list;
    }

    /**
     * Level 3: Horizontal Stripes
     */
    private void initializeIcePatternLevel3() {
        for (Point p : getPredeterminedIcePositionsLevel3()) {
            if (gameState.getIglu() != null && !gameState.getIglu().collidesWith(p)) {
                gameState.addIceBlock(new IceBlock(p, false));
            }
        }
    }

    private List<Point> getPredeterminedIcePositionsLevel3() {
        List<Point> list = new ArrayList<>();
        int size = GameState.getGridSize();
        // Rows 3, 5, 7, 9
        for (int y = 3; y < size - 2; y += 2) {
            for (int x = 2; x < size - 2; x++) {
                if (x % 3 != 0) { // Leave gaps
                    list.add(new Point(x, y));
                }
            }
        }
        return list;
    }

    /**
     * Level 4: Dense Field / Rings
     */
    private void initializeIcePatternLevel4() {
        for (Point p : getPredeterminedIcePositionsLevel4()) {
            if (gameState.getIglu() != null && !gameState.getIglu().collidesWith(p)) {
                gameState.addIceBlock(new IceBlock(p, false));
            }
        }
    }

    private List<Point> getPredeterminedIcePositionsLevel4() {
        List<Point> list = new ArrayList<>();
        int size = GameState.getGridSize();
        // Ring 1 (Inner) - Avoid Iglu
        // Ring 2 (Outer)
        for (int x = 2; x < size - 2; x++) {
            list.add(new Point(x, 2));
            list.add(new Point(x, size - 3));
        }
        for (int y = 2; y < size - 2; y++) {
            list.add(new Point(2, y));
            list.add(new Point(size - 3, y));
        }
        // Random scattered inside remaining space
        list.add(new Point(3, 3));
        list.add(new Point(9, 3));
        list.add(new Point(3, 9));
        list.add(new Point(9, 9));

        return list;
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
        if (isTimeUp()) {
            return true;
        }

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

    // ==================== DECOUPLING HELPERS (AIType) ====================

    /**
     * Obtiene los tipos de IA disponibles como Strings.
     */
    public List<String> getAvailableAITypes() {
        List<String> types = new ArrayList<>();
        for (AIType type : AIType.values()) {
            types.add(type.name());
        }
        return types;
    }

    /**
     * Parsea un String a AIType de forma segura.
     */
    private AIType parseAIType(String typeName) {
        if (typeName == null)
            return null; // Allow null to mean "no change" or "default" depending on context
        try {
            return AIType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            BadDopoLogger.logError("Invalid AI Type: " + typeName + ", defaulting to EXPERT", e);
            return AIType.EXPERT;
        }

    }

    // ==================== LEVEL CONFIGURATION SUPPORT ====================

    /**
     * @return List of available fruit types (Strings).
     */
    public List<String> getAvailableFruitTypes() {
        List<String> types = new ArrayList<>();
        for (FruitType type : FruitType.values()) {
            types.add(type.name());
        }
        return types;
    }

    /**
     * @return List of available enemy types (Strings).
     */
    public List<String> getAvailableEnemyTypes() {
        List<String> types = new ArrayList<>();
        for (EnemyType type : EnemyType.values()) {
            types.add(type.name());
        }
        return types;
    }

    /**
     * Gets the default configuration for a given level.
     */
    public LevelConfigurationDTO getDefaultConfiguration(int level) {
        LevelConfigurationDTO config = new LevelConfigurationDTO();

        // Defaults now strictly ZERO per user request
        switch (level) {
            // Cases kept for structure if we want metadata later, but values are 0.
            case 1:
            case 2:
            case 3:
            case 4:
            default:
                // All values explicitly 0 (default int is 0)
                // config.addFruit("UVA", 0); // Implicit
                break;
        }
        return config;
    }

}
