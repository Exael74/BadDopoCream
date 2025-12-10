package domain.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa la configuración de frutas del nivel.
 */
public class FruitConfigDTO {

    private boolean randomPlacement;
    private List<FruitWaveDTO> waves;

    public FruitConfigDTO() {
        this.waves = new ArrayList<>();
    }

    public boolean isRandomPlacement() {
        return randomPlacement;
    }

    public void setRandomPlacement(boolean randomPlacement) {
        this.randomPlacement = randomPlacement;
    }

    public List<FruitWaveDTO> getWaves() {
        return waves;
    }

    public void setWaves(List<FruitWaveDTO> waves) {
        this.waves = waves;
    }
}
