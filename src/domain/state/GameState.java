package domain.state;

import domain.entity.enemy.Enemy;
import domain.entity.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * Estado global del juego.
 * Contiene todas las entidades, configuración y estado temporal.
 */
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int GRID_SIZE = 13;
    private static final int TIME_LIMIT = 180000; // 3 minutos en milisegundos

    private Player player;
    private Player player2;
    // Estos campos forman parte del archivo de guardado, así que se declaran como
    // ArrayList (Serializable) y no como List: así se garantiza en compilación que
    // lo que se escribe al disco es serializable, en lugar de fallar al guardar.
    private ArrayList<Enemy> enemies;
    private ArrayList<Fruit> fruits;
    private ArrayList<List<Fruit>> pendingFruitWaves;
    private ArrayList<IceBlock> iceBlocks;
    private ArrayList<HotTile> hotTiles;
    private ArrayList<Fogata> fogatas;
    private Iglu iglu;
    private ArrayList<UnbreakableBlock> unbreakableBlocks;
    private boolean gameOver;
    private boolean victory;
    private int level;
    private int numberOfPlayers;
    private long timeRemaining;
    private boolean timeUp;
    private int score;
    private int scorePlayer2;
    private boolean p2CPU;
    private int totalFruitsSpawned;

    /**
     * Constructor del estado del juego.
     *
     * @param characterType   Tipo de personaje del jugador
     * @param level           Nivel actual
     * @param numberOfPlayers Número de jugadores (0=IA vs IA, 1=1P, 2=2P)
     */
    public GameState(String characterType, int level, int numberOfPlayers) {
        this.player = new Player(new Point(6, 6), characterType);
        if (numberOfPlayers == 2 || numberOfPlayers == 0) {
            // Player 2 will be properly initialized/positioned by GameFacade
            this.player2 = new Player(new Point(0, 0), "Vainilla");
        }
        this.enemies = new ArrayList<>();
        this.fruits = new ArrayList<>();
        this.pendingFruitWaves = new ArrayList<>();
        this.iceBlocks = new ArrayList<>();
        this.hotTiles = new ArrayList<>();
        this.fogatas = new ArrayList<>();
        this.iglu = null;
        this.unbreakableBlocks = new ArrayList<>();
        this.gameOver = false;
        this.victory = false;
        this.level = level;
        this.numberOfPlayers = numberOfPlayers;
        this.timeRemaining = TIME_LIMIT;
        this.timeUp = false;
        this.score = 0;
        this.scorePlayer2 = 0;
        this.p2CPU = false;
        this.totalFruitsSpawned = 0;
    }

    // ==================== GETTERS ESTÁTICOS ====================

    /**
     * Obtiene el tamaño del grid del juego.
     */
    public static int getGridSize() {
        return GRID_SIZE;
    }

    public static long getTimeLimit() {
        return TIME_LIMIT;
    }

    // ==================== GETTERS DE ENTIDADES ====================

    public Player getPlayer() {
        return player;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Fruit> getFruits() {
        return fruits;
    }

    public List<IceBlock> getIceBlocks() {
        return iceBlocks;
    }

    public List<HotTile> getHotTiles() {
        return hotTiles;
    }

    // ==================== GESTIÓN DE ENTIDADES ====================

    /**
     * Agrega un enemigo al juego.
     */
    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    /**
     * Agrega una fruta al juego.
     */
    public void addFruit(Fruit fruit) {
        fruits.add(fruit);
        totalFruitsSpawned++;
    }

    /**
     * Número total de frutas que han llegado a aparecer en la partida.
     * Sirve para distinguir "el jugador recolectó todo" de "el nivel nunca tuvo
     * frutas", que de otro modo se confundirían al comprobar la victoria.
     *
     * @return Cantidad acumulada de frutas generadas
     */
    public int getTotalFruitsSpawned() {
        return totalFruitsSpawned;
    }

    /**
     * Elimina todas las frutas y oleadas pendientes, dejando el contador de frutas
     * generadas a cero. Se usa al reconfigurar un nivel para que el estado quede
     * igual que en una partida recién creada.
     */
    public void resetFruits() {
        fruits.clear();
        pendingFruitWaves.clear();
        totalFruitsSpawned = 0;
    }

    /**
     * Agrega un bloque de hielo al juego.
     */
    public void addIceBlock(IceBlock iceBlock) {
        iceBlocks.add(iceBlock);
    }

    /**
     * Elimina un bloque de hielo del juego.
     */
    public void removeIceBlock(IceBlock iceBlock) {
        iceBlocks.remove(iceBlock);
    }

    /**
     * Agrega una baldosa caliente al juego.
     */
    public void addHotTile(HotTile hotTile) {
        hotTiles.add(hotTile);
    }

    public List<Fogata> getFogatas() {
        return fogatas;
    }

    public void addFogata(Fogata fogata) {
        fogatas.add(fogata);
    }

    public Iglu getIglu() {
        return iglu;
    }

    public void setIglu(Iglu iglu) {
        this.iglu = iglu;
    }

    public List<UnbreakableBlock> getUnbreakableBlocks() {
        return unbreakableBlocks;
    }

    public void addUnbreakableBlock(UnbreakableBlock block) {
        unbreakableBlocks.add(block);
    }

    // ==================== ESTADO DEL JUEGO ====================

    public boolean isGameOver() {
        return gameOver || timeUp;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isVictory() {
        return victory;
    }

    /**
     * Limpia todas las entidades del estado del juego.
     */
    public void clear() {
        enemies.clear();
        resetFruits();
        iceBlocks.clear();
        hotTiles.clear();
        fogatas.clear();
        unbreakableBlocks.clear();
        iglu = null;
    }

    public void setVictory(boolean victory) {
        this.victory = victory;
    }

    public int getLevel() {
        return level;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    // ==================== PUNTUACIÓN ====================

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public int getScorePlayer2() {
        return scorePlayer2;
    }

    public void addScorePlayer2(int points) {
        this.scorePlayer2 += points;
    }

    // ==================== TEMPORIZADOR ====================

    public long getTimeRemaining() {
        return timeRemaining;
    }

    /**
     * Actualiza el temporizador del juego.
     *
     * @param deltaTime Tiempo transcurrido en milisegundos
     */
    public void updateTime(int deltaTime) {
        if (!victory && !gameOver && !timeUp) {
            timeRemaining -= deltaTime;

            if (timeRemaining <= 0) {
                timeRemaining = 0;
                timeUp = true;
                domain.BadDopoLogger.logInfo("✗ ¡Se acabó el tiempo! - Game Over");
            }
        }
    }

    public boolean isTimeUp() {
        return timeUp;
    }

    /**
     * Obtiene el tiempo restante en formato MM:SS.
     *
     * @return Tiempo formateado
     */
    public String getFormattedTime() {
        long seconds = timeRemaining / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void setPlayerNames(String p1Name, String p2Name) {
        if (player != null && p1Name != null) {
            player.setName(p1Name);
        }
        if (player2 != null && p2Name != null) {
            player2.setName(p2Name);
        }
    }

    public List<List<Fruit>> getPendingFruitWaves() {
        return pendingFruitWaves;
    }

    public void addPendingFruitWave(List<Fruit> wave) {
        // Copia defensiva a ArrayList: garantiza que la oleada encolada es
        // serializable, sea cual sea la implementación de List que llegue.
        this.pendingFruitWaves.add(new ArrayList<>(wave));
    }

    public void addFruitWave(List<Fruit> wave) {
        // Alias mantenido por compatibilidad: ambas rutas encolan una oleada pendiente.
        addPendingFruitWave(wave);
    }

    public boolean isP2CPU() {
        return p2CPU;
    }

    public void setP2CPU(boolean p2CPU) {
        this.p2CPU = p2CPU;
    }
}
