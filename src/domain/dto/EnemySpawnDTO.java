package domain.dto;

/**
 * DTO que representa la configuración de spawn de un enemigo específico.
 */
public class EnemySpawnDTO {

    private String type;
    private int count;

    public EnemySpawnDTO() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
