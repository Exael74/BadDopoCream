package domain;

import domain.dto.*;
import domain.entity.*;
import domain.service.GameLogic;
import domain.service.PersistenceService;
import domain.state.GameState;
import java.awt.Point;
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
            int numberOfPlayers) {
        this.gameState = new GameState(characterType, level, numberOfPlayers);

        // Set Player 2 character type if applicable
        if (gameState.getPlayer2() != null && characterTypeP2 != null) {
            Point pos = gameState.getPlayer2().getPosition();
            Player p2 = new Player(pos, characterTypeP2);
            gameState.setPlayer2(p2);
        }

        this.gameState.setPlayerNames(p1Name, p2Name);

        this.gameLogic = new GameLogic(gameState);
        this.persistenceService = new PersistenceService();
        this.lastUpdateTime = System.currentTimeMillis();
        this.paused = false;

        initializeLevel(level, numberOfPlayers);
    }

    public GameFacade(String characterType, int level, int numberOfPlayers) {
        this(characterType, null, "P1", "P2", level, numberOfPlayers);
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
        occupiedPositions.add(gameState.getPlayer().getPosition());

        // Agregar hielos predeterminados del nivel 1
        List<Point> predeterminedIcePositions = getPredeterminedIcePositionsLevel1();
        for (Point icePos : predeterminedIcePositions) {
            gameState.addIceBlock(new IceBlock(icePos, false));
            occupiedPositions.add(icePos);
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

        // Agregar 8 uvas
        for (int i = 0; i < 8; i++) {
            Point uvaPos = getRandomFreePosition(random, occupiedPositions);
            gameState.addFruit(new Fruit(uvaPos, FruitType.UVA));
            occupiedPositions.add(uvaPos);
        }

        // Agregar 8 plátanos
        for (int i = 0; i < 8; i++) {
            Point platanoPos = getRandomFreePosition(random, occupiedPositions);
            gameState.addFruit(new Fruit(platanoPos, FruitType.PLATANO));
            occupiedPositions.add(platanoPos);
        }
    }

    /**
     * Inicializa el nivel 2: 1 Maceta + 8 Piñas + 8 Plátanos.
     */
    private void initializeLevel2(int numberOfPlayers) {
        Random random = new Random();
        List<Point> occupiedPositions = new ArrayList<>();
        occupiedPositions.add(gameState.getPlayer().getPosition());

        // Patrón de hielo DIFERENTE para nivel 2
        List<Point> predeterminedIcePositions = getPredeterminedIcePositionsLevel2();
        for (Point icePos : predeterminedIcePositions) {
            gameState.addIceBlock(new IceBlock(icePos, false));
            occupiedPositions.add(icePos);
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

        // Agregar 8 piñas (se mueven aleatoriamente)
        for (int i = 0; i < 8; i++) {
            Point pinaPos = getRandomFreePosition(random, occupiedPositions);
            gameState.addFruit(new Fruit(pinaPos, FruitType.PIÑA));
            occupiedPositions.add(pinaPos);
        }

        // Agregar 8 plátanos (estáticos)
        for (int i = 0; i < 8; i++) {
            Point platanoPos = getRandomFreePosition(random, occupiedPositions);
            gameState.addFruit(new Fruit(platanoPos, FruitType.PLATANO));
            occupiedPositions.add(platanoPos);
        }
    }

    /**
     * Inicializa el nivel 3: 1 Calamar + 8 Piñas + 8 Cerezas.
     */
    private void initializeLevel3(int numberOfPlayers) {
        Random random = new Random();
        List<Point> occupiedPositions = new ArrayList<>();
        occupiedPositions.add(gameState.getPlayer().getPosition());

        // Patrón de hielo DIFERENTE para nivel 3
        List<Point> predeterminedIcePositions = getPredeterminedIcePositionsLevel3();
        for (Point icePos : predeterminedIcePositions) {
            gameState.addIceBlock(new IceBlock(icePos, false));
            occupiedPositions.add(icePos);
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

        // Agregar 8 piñas (se mueven aleatoriamente)
        for (int i = 0; i < 8; i++) {
            Point pinaPos = getRandomFreePosition(random, occupiedPositions);
            gameState.addFruit(new Fruit(pinaPos, FruitType.PIÑA));
            occupiedPositions.add(pinaPos);
        }

        // Agregar 8 cerezas (se teletransportan)
        for (int i = 0; i < 8; i++) {
            Point cerezaPos = getRandomFreePosition(random, occupiedPositions);
            gameState.addFruit(new Fruit(cerezaPos, FruitType.CEREZA));
            occupiedPositions.add(cerezaPos);
        }
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

        // Cruz central
        icePositions.add(new Point(6, 4));
        icePositions.add(new Point(6, 5));
        icePositions.add(new Point(6, 6));
        icePositions.add(new Point(6, 7));
        icePositions.add(new Point(6, 8));

        icePositions.add(new Point(4, 6));
        icePositions.add(new Point(5, 6));
        icePositions.add(new Point(7, 6));
        icePositions.add(new Point(8, 6));

        // Bloques en esquinas
        icePositions.add(new Point(1, 1));
        icePositions.add(new Point(2, 1));
        icePositions.add(new Point(1, 2));

        icePositions.add(new Point(10, 1));
        icePositions.add(new Point(11, 1));
        icePositions.add(new Point(11, 2));

        icePositions.add(new Point(1, 10));
        icePositions.add(new Point(1, 11));
        icePositions.add(new Point(2, 11));

        icePositions.add(new Point(10, 11));
        icePositions.add(new Point(11, 10));
        icePositions.add(new Point(11, 11));

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
        return gameState.getPlayer().isAlive();
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

        boolean p1Dead = !player.isDying() && !player.isAlive();
        boolean p2Dead = (player2 != null) && !player2.isDying() && !player2.isAlive();

        // In PvP/MvM, game over if ANY player dies.
        if (gameState.getNumberOfPlayers() == 2 || gameState.getNumberOfPlayers() == 0) {
            return p1Dead || p2Dead;
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

    