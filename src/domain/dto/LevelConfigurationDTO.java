package domain.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for Level Configuration.
 * Holds user preferences for Fruits, Enemies, and Obstacles.
 */
public class LevelConfigurationDTO {

    private Map<String, Integer> fruitCounts;
    private Map<String, Integer> enemyCounts;
    private int hotTileCount;

    public LevelConfigurationDTO() {
        this.fruitCounts = new HashMap<>();
        this.enemyCounts = new HashMap<>();
        this.hotTileCount = 0;
    }

    public Map<String, Integer> getFruitCounts() {
        return fruitCounts;
    }

    public void setFruitCounts(Map<String, Integer> fruitCounts) {
        this.fruitCounts = fruitCounts;
    }

    public Map<String, Integer> getEnemyCounts() {
        return enemyCounts;
    }

    public void setEnemyCounts(Map<String, Integer> enemyCounts) {
        this.enemyCounts = enemyCounts;
    }

    public int getHotTileCount() {
        return hotTileCount;
    }

    public void setHotTileCount(int hotTileCount) {
        this.hotTileCount = hotTileCount;
    }

    public void addFruit(String type, int count) {
        fruitCounts.put(type, count);
    }

    public void addEnemy(String type, int count) {
        enemyCounts.put(type, count);
    }
}
