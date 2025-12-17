package domain.dto;

/**
 * DTO que contiene todos los datos de un nivel cargado desde JSON.
 * Incluye estructura del mapa, configuración de entidades y metadatos.
 */
public class LevelDataDTO {

    private int levelId;
    private String name;
    private long timeLimit;
    private int gridSize;
    private MapLayoutDTO mapLayout;
    private EnemyConfigDTO enemyConfig;
    private FruitConfigDTO fruitConfig;

    public LevelDataDTO() {
    }

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public MapLayoutDTO getMapLayout() {
        return mapLayout;
    }

    public void setMapLayout(MapLayoutDTO mapLayout) {
        this.mapLayout = mapLayout;
    }

    public EnemyConfigDTO getEnemyConfig() {
        return enemyConfig;
    }

    public void setEnemyConfig(EnemyConfigDTO enemyConfig) {
        this.enemyConfig = enemyConfig;
    }

    public FruitConfigDTO getFruitConfig() {
        return fruitConfig;
    }

    public void setFruitConfig(FruitConfigDTO fruitConfig) {
        this.fruitConfig = fruitConfig;
    }
}
