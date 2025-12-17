package domain.dto;

import java.util.Map;

/**
 * DTO que representa la estructura visual del mapa.
 * Contiene la leyenda de símbolos y la matriz del grid.
 */
public class MapLayoutDTO {

    private Map<String, String> legend;
    private String[][] grid;
    private int gridSize;

    public MapLayoutDTO() {
    }

    public Map<String, String> getLegend() {
        return legend;
    }

    public void setLegend(Map<String, String> legend) {
        this.legend = legend;
    }

    public String[][] getGrid() {
        return grid;
    }

    public void setGrid(String[][] grid) {
        this.grid = grid;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }
}
