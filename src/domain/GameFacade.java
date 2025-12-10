package domain;

import domain.dto.*;
import domain.entity.*;
import domain.service.GameLogic;
import domain.service.PersistenceService;
import domain.service.MapLoaderService;
import domain.service.MapParserService;
import domain.state.GameState;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;

/**
 * Fachada del dominio del juego.
 * Único punto de acceso desde la capa de presentación al dominio.
 * Coordina GameState y GameLogic, y provee una API limpia y simplificada.
 */
public class GameFacade {

    private GameState gameState;
    private GameLogic gameLogic;
    private PersistenceService persistenceService;
    private MapLoaderService mapLoaderService;
    private MapParserService mapParserService;
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

    /**
     * Spawns entities based on LevelDataDTO from JSON.
     * TEMPORARILY DISABLED - Only loading map structure
     */
    private void spawnDynamicEntitiesFromJSON(LevelDataDTO levelData) {
        // DISABLED: Only loading map, not entities
        /*
         * // 1. Spawn Fruits from JSON waves
         * if (levelData.getFruitConfig() != null) {
         * for (FruitWaveDTO wave : levelData.getFruitConfig().getWaves()) {
         * List<Fruit> fruitWave = new ArrayList<>();
         * 
         * for (FruitSpawnDTO fruitSpawn : wave.getFruits()) {
         * try {
         * FruitType type = FruitType.valueOf(fruitSpawn.getType());
         * int count = fruitSpawn.getCount();
         * 
         * for (int i = 0; i < count; i++) {
         * Point position = findFreePosition();
         * if (position != null) {
         * fruitWave.add(new Fruit(position, type));
         * }
         * }
         * } catch (IllegalArgumentException e) {
         * BadDopoLogger.logError("Unknown Fruit Type in JSON: " + fruitSpawn.getType(),
         * e);
         * }
         * }
         * 
         * if (!fruitWave.isEmpty()) {
         * if (wave.isSpawnOnStart()) {
         * // Spawn immediately
         * for (Fruit f : fruitWave) {
         * gameState.addFruit(f);
         * }
         * } else {
         * // Add to pending waves
         * gameState.addPendingFruitWave(fruitWave);
         * }
         * }
         * }
         * }
         * 
         * // 2. Spawn Enemies from JSON
         * if (levelData.getEnemyConfig() != null) {
         * for (EnemySpawnDTO enemySpawn : levelData.getEnemyConfig().getTypes()) {
         * try {
         * EnemyType type = EnemyType.valueOf(enemySpawn.getType());
         * int count = enemySpawn.getCount();
         * 
         * for (int i = 0; i < count; i++) {
         * spawnEnemy(type);
         * }
         * } catch (IllegalArgumentException e) {
         * BadDopoLogger.logError("Unknown Enemy Type in JSON: " + enemySpawn.getType(),
         * e);
         * }
         * }
         * }
         */

        BadDopoLogger.logInfo("Mapa cargado desde JSON (entidades deshabilitadas temporalmente)");
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

    // --- Helper Methods for Entity Management and Collision ---

    private boolean hasIceAt(Point p) {
        return gameState.getIceBlocks().stream().anyMatch(ice -> ice.getPosition().equals(p));
    }

    private boolean hasHotTileAt(Point p) {
        return gameState.getHotTiles().stream().anyMatch(tile -> tile.getPosition().equals(p));
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
