package domain.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa la configuración de enemigos del nivel.
 */
public class EnemyConfigDTO {

    private boolean randomPlacement;
    private List<EnemySpawnDTO> types;

    public EnemyConfigDTO() {
        this.types = new ArrayList<>();
    }

    public boolean isRandomPlacement() {
        return randomPlacement;
    }

    public void setRandomPlacement(boolean randomPlacement) {
        this.randomPlacement = randomPlacement;
    }

    public List<EnemySpawnDTO> getTypes() {
        return types;
    }

    public void setTypes(List<EnemySpawnDTO> types) {
        this.types = types;
    }
}
