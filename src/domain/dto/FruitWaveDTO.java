package domain.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa una oleada de frutas.
 */
public class FruitWaveDTO {

    private int waveNumber;
    private boolean spawnOnStart;
    private List<FruitSpawnDTO> fruits;

    public FruitWaveDTO() {
        this.fruits = new ArrayList<>();
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public void setWaveNumber(int waveNumber) {
        this.waveNumber = waveNumber;
    }

    public boolean isSpawnOnStart() {
        return spawnOnStart;
    }

    public void setSpawnOnStart(boolean spawnOnStart) {
        this.spawnOnStart = spawnOnStart;
    }

    public List<FruitSpawnDTO> getFruits() {
        return fruits;
    }

    public void setFruits(List<FruitSpawnDTO> fruits) {
        this.fruits = fruits;
    }
}
