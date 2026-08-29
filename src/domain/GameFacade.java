package domain;

import domain.dto.*;
import domain.entity.enemy.Enemy;
import domain.entity.*;
import domain.service.GameLogic;
import domain.service.PersistenceService;
import domain.service.MapLoaderService;
import domain.service.MapParserService;
import exceptions.BadDopoException;
import domain.state.GameState;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.EnumMap;

/**
 * Fachada del dominio del juego.
 * Único punto de acceso desde la capa de presentación al dominio.
 * Coordina GameState y GameLogic, y provee una API limpia y simplificada.
 */
public class GameFacade {

    /** Número de fogatas que se generan cuando el nivel no indica otra cosa. */
    private static final int DEFAULT_FOGATA_COUNT = 2;

    private GameState gameState;
    private GameLogic gameLogic;
    private PersistenceService persistenceService;
    private MapLoaderService mapLoaderService;
    private MapParserService mapParserService;
    private LevelConfigurationDTO currentConfiguration; // Store configuration here
    private final Random spawnRandom = new Random();
    private long lastUpdateTime;
    private boolean isP2CPU; // Store this explicitly in Facade as well or rely on GameState
    private boolean paused;

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
        this.isP2CPU = isP2CPU;

        // Ensure configuration is initialized
        if (config != null) {
            this.currentConfiguration = config;
        } else {
            // Load defaults if not provided
            // We can't easily auto-load defaults without loading the level twice if we
            // aren't careful,
            // but initializeLevel will likely do it.
            // For now, let's allow it to be null and load lazily or during initializeLevel.
            // Actually, to support getters/setters BEFORE initialization, we might need to
            // load defaults immediately.
        }

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
        this.mapLoaderService = new MapLoaderService();
        this.mapParserService = new MapParserService();
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

    /**
     * Static helper to list saved games without an instance.
     */
    public static List<String> listSavedGames() {
        return new PersistenceService().getSavedGames();
    }

    /**
     * Factory method to load a game from a file.
     */
    public static GameFacade loadFromSave(String filename) throws BadDopoException {
        // Create a dummy facade to initialize services
        // Wait, constructor signature is (type, level, players).
        // Let's use the simplest one.
        GameFacade temp = new GameFacade("Placeholder", 1, 1);
        temp.loadGame(filename);
        return temp;
    }

    // ==================== REINICIO Y SALIDA ====================

    public void restartLevel() {
        int level = gameState.getLevel();
        int players = gameState.getNumberOfPlayers();
        String charType = gameState.getPlayer().getCharacterType();
        String charType2 = (gameState.getPlayer2() != null) ? gameState.getPlayer2().getCharacterType() : "Vainilla";
        String name1 = gameState.getPlayer().getName();
        String name2 = (gameState.getPlayer2() != null) ? gameState.getPlayer2().getName() : "P2";

        AIType aiType1 = gameState.getPlayer().getAIType();
        AIType aiType2 = (gameState.getPlayer2() != null) ? gameState.getPlayer2().getAIType() : null;
        boolean wasP2CPU = this.isP2CPU;
        LevelConfigurationDTO savedConfig = this.currentConfiguration;

        this.gameState = new GameState(charType, level, players);
        this.gameState.setP2CPU(wasP2CPU);
        this.isP2CPU = wasP2CPU;

        if (players == 2 || players == 0) {
            setPlayer2CharacterType(charType2);
        }
        this.gameState.setPlayerNames(name1, name2);

        if (aiType1 != null) gameState.getPlayer().setAIType(aiType1);
        if (aiType2 != null && gameState.getPlayer2() != null) gameState.getPlayer2().setAIType(aiType2);

        this.currentConfiguration = savedConfig;
        this.gameLogic = new GameLogic(gameState);
        this.paused = false;
        this.lastUpdateTime = System.currentTimeMillis();

        if (savedConfig != null) {
            initializeLevel(level, players, savedConfig);
        } else {
            initializeLevel(level, players);
        }
    }

    // ==================== INICIALIZACIÓN DE NIVELES ====================

    /**
     * Inicializa el nivel especificado usando JSON.
     */
    private void initializeLevel(int level, int numberOfPlayers) {
        initializeLevelFromJSON(level, numberOfPlayers);
    }

    /**
     * Initialize level with custom configuration.
     */
    private void initializeLevel(int level, int numberOfPlayers, LevelConfigurationDTO config) {
        try {
            BadDopoLogger.logInfo(
                    "Iniciando nivel " + level + " con " + numberOfPlayers + " jugadores y config personalizada.");

            // 1. Setup Base Map Structure (Walls, Ice, Iglu) - Now from JSON
            setupMapStructure(level, numberOfPlayers);

            // 2. Spawn Configured Entities (Fruits, Enemies, HotTiles)
            // Use legacy method for backward compatibility
            spawnDynamicEntities(config);

        } catch (BadDopoException e) {
            BadDopoLogger.logError("Error al inicializar el nivel: " + e.getMessage(), e);
        } catch (Exception e) {
            BadDopoLogger.logError("Error inesperado al inicializar el nivel", e);
        }
    }

    /**
     * Initialize level using JSON configuration (new method).
     */
    private void initializeLevelFromJSON(int level, int numberOfPlayers) {
        try {
            BadDopoLogger.logInfo("Iniciando nivel " + level + " desde JSON con " + numberOfPlayers + " jugadores.");

            // Load level data from JSON
            LevelDataDTO levelData = mapLoaderService.loadLevel(level);

            // 1. Setup Map Structure from JSON
            mapParserService.applyMapLayout(gameState, levelData.getMapLayout(), numberOfPlayers);

            // 2. Spawn Dynamic Entities from JSON
            spawnDynamicEntitiesFromJSON(levelData);

            BadDopoLogger.logInfo("Nivel " + level + " inicializado completamente desde JSON");

        } catch (BadDopoException e) {
            BadDopoLogger.logError("Error al inicializar el nivel desde JSON: " + e.getMessage(), e);
        } catch (Exception e) {
            BadDopoLogger.logError("Error inesperado al inicializar el nivel desde JSON", e);
        }
    }

    /**
     * Sets up the static map elements (Walls, Iglu, Default Ice) based on Level ID.
     * Now loads from JSON instead of hardcoded methods.
     */
    private void setupMapStructure(int level, int numberOfPlayers) throws BadDopoException {
        try {
            LevelDataDTO levelData = mapLoaderService.loadLevel(level);
            mapParserService.applyMapLayout(gameState, levelData.getMapLayout(), numberOfPlayers);
            BadDopoLogger.logInfo("Estructura del mapa nivel " + level + " cargada desde JSON");
        } catch (BadDopoException e) {
            BadDopoLogger.logError("Error cargando estructura del mapa desde JSON: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Spawns entities based on the configuration from JSON.
     */
    private void spawnDynamicEntities(LevelConfigurationDTO config) {
        // Legacy method - convert to use JSON if available
        // This is called when using old constructor without JSON

        // 1. Spawn Fruits (Ordered Waves)
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
                // Simple validation - try to create one dummy (or just rely on factory throw)
                // Better: just spawn. Factory throws if invalid.
                for (int i = 0; i < count; i++) {
                    spawnEnemy(enemyTypeStr);
                }
            } catch (IllegalArgumentException e) {
                BadDopoLogger.logError("Unknown Enemy Type in Config: " + enemyTypeStr, e);
            }
        }

        // 3. Spawn Hot Tiles
        initializeHotTiles(config.getHotTileCount());

        // 4. Spawn Fogatas
        initializeFogatas(config.getFogataCount());
    }

    private void spawnDynamicEntitiesFromJSON(LevelDataDTO levelData) {
        BadDopoLogger.logInfo("Generando entidades desde JSON...");

        // 1. Spawn Fruits from waves
        if (levelData.getFruitConfig() != null) {
            for (FruitWaveDTO wave : levelData.getFruitConfig().getWaves()) {
                List<Fruit> waveFruits = new ArrayList<>();
                for (FruitSpawnDTO spawn : wave.getFruits()) {
                    try {
                        FruitType type = FruitType.valueOf(spawn.getType());
                        for (int i = 0; i < spawn.getCount(); i++) {
                            Point pos = findFreePosition();
                            if (pos != null) {
                                waveFruits.add(new Fruit(pos, type));
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        BadDopoLogger.logError("Unknown Fruit Type in JSON: " + spawn.getType(), e);
                    }
                }
                if (!waveFruits.isEmpty()) {
                    if (wave.isSpawnOnStart()) {
                        for (Fruit f : waveFruits) {
                            gameState.addFruit(f);
                        }
                    } else {
                        gameState.addPendingFruitWave(waveFruits);
                    }
                }
            }
        }

        // 2. Spawn Enemies
        if (levelData.getEnemyConfig() != null) {
            for (EnemySpawnDTO spawn : levelData.getEnemyConfig().getTypes()) {
                String type = spawn.getType();
                for (int i = 0; i < spawn.getCount(); i++) {
                    spawnEnemy(type);
                }
            }
        }

        // 3. Hot tiles from map layout already handled by MapParserService
        // 4. Fogatas - use default count for now
        initializeFogatas(DEFAULT_FOGATA_COUNT);
    }

    // Helper for Spawning specific enemy type
    private void spawnEnemy(String type) {
        Point position = findFreePosition();
        if (position == null) {
            return;
        }
        try {
            domain.entity.enemy.Enemy enemy = domain.entity.enemy.EnemyFactory.createEnemy(position, type);
            gameState.addEnemy(enemy);
        } catch (IllegalArgumentException e) {
            // Un tipo desconocido no debe abortar la generación del resto de entidades
            BadDopoLogger.logError("Tipo de enemigo desconocido: " + type, e);
        }
    }

    /**
     * Busca una casilla libre del tablero para colocar una entidad nueva.
     * Recorre todas las casillas y elige una al azar entre las disponibles, de modo
     * que devuelve null únicamente cuando el tablero está realmente lleno.
     *
     * @return Una posición libre aleatoria, o null si no queda ninguna
     */
    private Point findFreePosition() {
        int gridSize = GameState.getGridSize();
        List<Point> freePositions = new ArrayList<>();

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                Point p = new Point(x, y);
                if (isFreeForSpawn(p)) {
                    freePositions.add(p);
                }
            }
        }

        if (freePositions.isEmpty()) {
            BadDopoLogger.logInfo("No queda ninguna casilla libre para generar una entidad");
            return null;
        }

        return freePositions.get(spawnRandom.nextInt(freePositions.size()));
    }

    /**
     * Indica si una casilla está completamente libre para generar una entidad.
     */
    private boolean isFreeForSpawn(Point p) {
        if (gameState.getIglu() != null && gameState.getIglu().collidesWith(p))
            return false;
        return !isWall(p)
                && !hasIceAt(p)
                && !hasHotTileAt(p)
                && !hasFogataAt(p)
                && !hasEnemyAt(p)
                && !hasFruitAt(p)
                && !hasPlayerAt(p);
    }

    private boolean isWall(Point p) {
        int s = GameState.getGridSize();
        if (p.x == 0 || p.x == s - 1 || p.y == 0 || p.y == s - 1) return true;
        for (UnbreakableBlock b : gameState.getUnbreakableBlocks()) {
            if (b.getPosition().equals(p)) return true;
        }
        return false;
    }

    private void updateFogatas(int dt) {
        for (Fogata f : gameState.getFogatas()) {
            f.update(dt);
        }
    }

    private Fogata getFogataAt(Point p) {
        for (Fogata f : gameState.getFogatas()) {
            if (f.getPosition().equals(p))
                return f;
        }
        return null;
    }

    private void initializeHotTiles(int count) {
        for (int i = 0; i < count; i++) {
            Point pos = findFreePosition();
            if (pos != null) {
                gameState.addHotTile(new HotTile(pos));
            }
        }
    }

    private void initializeFogatas(int count) {
        for (int i = 0; i < count; i++) {
            Point pos = findFreePosition();
            if (pos != null) {
                gameState.addFogata(new Fogata(pos));
            }
        }
    }

    // --- Helper Methods for Entity Management and Collision ---

    private boolean hasIceAt(Point p) {
        return gameState.getIceBlocks().stream().anyMatch(ice -> ice.getPosition().equals(p));
    }

    private boolean hasHotTileAt(Point p) {
        return gameState.getHotTiles().stream().anyMatch(tile -> tile.getPosition().equals(p));
    }

    private boolean hasFogataAt(Point p) {
        return gameState.getFogatas().stream().anyMatch(f -> f.getPosition().equals(p));
    }

    private boolean hasEnemyAt(Point p) {
        return gameState.getEnemies().stream().anyMatch(e -> e.isActive() && e.getPosition().equals(p));
    }

    private boolean hasFruitAt(Point p) {
        return gameState.getFruits().stream().anyMatch(f -> f.isActive() && !f.isCollected() && f.getPosition().equals(p));
    }

    private boolean hasPlayerAt(Point p) {
        if (gameState.getPlayer() != null && gameState.getPlayer().getPosition().equals(p)) return true;
        if (gameState.getPlayer2() != null && gameState.getPlayer2().getPosition().equals(p)) return true;
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

    public List<FogataSnapshot> getFogataSnapshots() {
        List<FogataSnapshot> snapshots = new ArrayList<>();
        for (Fogata f : gameState.getFogatas()) {
            snapshots.add(FogataSnapshot.from(f));
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
        return gameState.getPlayer2() != null && gameState.getPlayer2().isAlive();
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

    public String getAITypeP1() {
        if (gameState.getPlayer() != null && gameState.getPlayer().getAIType() != null) {
            return gameState.getPlayer().getAIType().toString();
        }
        return "EXPERT"; // Default
    }

    public void setFogataCountConfig(int count) {
        if (currentConfiguration == null)
            currentConfiguration = new LevelConfigurationDTO();
        currentConfiguration.setFogataCount(count);
    }

    public int getFogataCountConfig() {
        return (currentConfiguration != null) ? currentConfiguration.getFogataCount() : 0;
    }

    public String getAITypeP2() {
        if (gameState.getPlayer2() != null && gameState.getPlayer2().getAIType() != null) {
            return gameState.getPlayer2().getAIType().toString();
        }
        return "EXPERT"; // Default
    }

    // ==================== DEBUG / TESTS ====================

    public GameState getGameState() {
        return gameState;
    }

    // ==================== CONFIGURACIÓN DE NIVEL ====================

    /**
     * Obtiene la configuración actual del nivel.
     */
    public LevelConfigurationDTO getConfiguration() {
        return this.currentConfiguration;
    }

    /**
     * Establece la configuración actual.
     */
    public void setConfiguration(LevelConfigurationDTO config) {
        this.currentConfiguration = config;
    }

    /**
     * Configura la cantidad de frutas de un tipo específico.
     */
    public void setFruitCountConfig(String type, int count) {
        if (currentConfiguration == null)
            currentConfiguration = new LevelConfigurationDTO();
        currentConfiguration.addFruit(type, count);
    }

    /**
     * Configura la cantidad de enemigos de un tipo específico.
     */
    public void setEnemyCountConfig(String type, int count) {
        if (currentConfiguration == null)
            currentConfiguration = new LevelConfigurationDTO();
        currentConfiguration.addEnemy(type, count);
    }

    /**
     * Configura la cantidad de baldosas calientes.
     */
    public void setHotTileCountConfig(int count) {
        if (currentConfiguration == null)
            currentConfiguration = new LevelConfigurationDTO();
        currentConfiguration.setHotTileCount(count);
    }

    public Map<String, Integer> getFruitCountsConfig() {
        if (currentConfiguration == null)
            currentConfiguration = new LevelConfigurationDTO();
        return currentConfiguration.getFruitCounts();
    }

    public Map<String, Integer> getEnemyCountsConfig() {
        if (currentConfiguration == null)
            currentConfiguration = new LevelConfigurationDTO();
        return currentConfiguration.getEnemyCounts();
    }

    public int getHotTileCountConfig() {
        if (currentConfiguration == null)
            currentConfiguration = new LevelConfigurationDTO();
        return currentConfiguration.getHotTileCount();
    }

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

    // ==================== TIPOS DISPONIBLES ====================

    public List<String> getAvailableFruitTypes() {
        // Return enum names
        List<String> types = new ArrayList<>();
        for (FruitType t : FruitType.values()) {
            types.add(t.toString());
        }
        return types;
    }

    public List<String> getAvailableEnemyTypes() {
        return java.util.Arrays.asList(domain.entity.enemy.EnemyFactory.getSupportedTypes());
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
            BadDopoLogger.logError("Invalid AI Type: " + typeName + ", default to EXPERT", e);
            return AIType.EXPERT;
        }

    }

    // ==================== MISSING METHODS RESTORED ====================

    /**
     * Applies the current configuration to the game state.
     * Often used after setting configuration via dialog.
     */
    public void applyConfiguration() {
        if (currentConfiguration != null) {
            // Clear existing logic if needed or just spawn new entities
            // Clear existing dynamic entities but PRESERVE map structure (walls, ice, iglu)
            // Reinicia frutas, oleadas pendientes y el contador de frutas generadas.
            gameState.resetFruits();

            gameState.getEnemies().clear();
            gameState.getHotTiles().clear();
            // Las fogatas también se regeneran desde la configuración; si no se limpian
            // aquí se acumulan sobre las creadas al inicializar el nivel.
            gameState.getFogatas().clear();

            // Setup entities again based on configuration
            spawnDynamicEntities(currentConfiguration);
        }
    }

    public boolean isP2CPU() {
        return this.isP2CPU;
    }

    public List<String> getUniqueFruitTypes() {
        // Return enum names that are distinct?
        // Actually just all available fruit types
        return getAvailableFruitTypes();
    }

    /**
     * Carga la configuración por defecto para un nivel.
     * Útil para inicializar el diálogo de configuración.
     */
    public LevelConfigurationDTO getDefaultConfiguration(int levelId) {
        LevelConfigurationDTO config = new LevelConfigurationDTO();
        try {
            LevelDataDTO data = mapLoaderService.loadLevel(levelId);

            // Frutas: se suman las cantidades de todas las oleadas del nivel.
            if (data.getFruitConfig() != null && data.getFruitConfig().getWaves() != null) {
                for (FruitWaveDTO wave : data.getFruitConfig().getWaves()) {
                    if (wave.getFruits() == null)
                        continue;
                    for (FruitSpawnDTO spawn : wave.getFruits()) {
                        int previous = config.getFruitCounts().getOrDefault(spawn.getType(), 0);
                        config.addFruit(spawn.getType(), previous + spawn.getCount());
                    }
                }
            }

            // Enemigos definidos por el nivel.
            if (data.getEnemyConfig() != null && data.getEnemyConfig().getTypes() != null) {
                for (EnemySpawnDTO spawn : data.getEnemyConfig().getTypes()) {
                    int previous = config.getEnemyCounts().getOrDefault(spawn.getType(), 0);
                    config.addEnemy(spawn.getType(), previous + spawn.getCount());
                }
            }

            // Baldosas calientes: las que el propio mapa dibuja con la letra "H".
            config.setHotTileCount(countHotTilesInLayout(data.getMapLayout()));
            config.setFogataCount(DEFAULT_FOGATA_COUNT);

        } catch (Exception e) {
            BadDopoLogger.logError("Error loading default config", e);
        }
        return config;
    }

    /**
     * Cuenta las baldosas calientes declaradas en el mapa del nivel.
     */
    private int countHotTilesInLayout(MapLayoutDTO layout) {
        if (layout == null || layout.getGrid() == null) {
            return 0;
        }
        int count = 0;
        for (String[] row : layout.getGrid()) {
            if (row == null)
                continue;
            for (String cell : row) {
                if ("H".equals(cell)) {
                    count++;
                }
            }
        }
        return count;
    }
}
