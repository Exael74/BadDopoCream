package domain.service;

import exceptions.BadDopoException;
import domain.BadDopoLogger;
import domain.dto.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio responsable de cargar niveles desde archivos JSON.
 * Mantiene el patrón MVC: solo trabaja con DTOs y archivos, sin conocer
 * GameState.
 */
public class MapLoaderService {

    private static final String LEVELS_PATH = "Resources/levels/";
    private Map<Integer, LevelDataDTO> levelCache;

    public MapLoaderService() {
        this.levelCache = new HashMap<>();
    }

    /**
     * Carga un nivel desde JSON. Usa caché para evitar parseo repetido.
     *
     * @param levelId ID del nivel (1, 2, 3, 4)
     * @return DTO con todos los datos del nivel
     * @throws BadDopoException si el archivo no existe o el JSON es inválido
     */
    public LevelDataDTO loadLevel(int levelId) throws BadDopoException {
        // Verificar caché
        if (levelCache.containsKey(levelId)) {
            BadDopoLogger.logInfo("Nivel " + levelId + " cargado desde caché");
            return levelCache.get(levelId);
        }

        // Cargar desde archivo
        String filename = LEVELS_PATH + "level_" + levelId + ".json";
        try {
            String jsonContent = readFile(filename);
            LevelDataDTO levelData = parseJSON(jsonContent);
            validateLevelData(levelData);

            // Guardar en caché
            levelCache.put(levelId, levelData);
            BadDopoLogger.logInfo("Nivel " + levelId + " cargado exitosamente desde " + filename);

            return levelData;

        } catch (IOException e) {
            throw BadDopoException.levelLoadError(filename, e);
        } catch (Exception e) {
            throw BadDopoException.jsonParseError(levelId, e.getMessage(), e);
        }
    }

    /**
     * Lee el contenido de un archivo como String.
     */
    private String readFile(String filename) throws IOException {
        Path path = Paths.get(filename);
        return new String(Files.readAllBytes(path));
    }

    /**
     * Parsea el contenido JSON y construye el DTO.
     */
    private LevelDataDTO parseJSON(String jsonContent) throws Exception {
        JSONObject root = new JSONObject(jsonContent);

        LevelDataDTO levelData = new LevelDataDTO();
        levelData.setLevelId(root.getInt("levelId"));
        levelData.setName(root.getString("name"));
        levelData.setTimeLimit(root.getLong("timeLimit"));
        levelData.setGridSize(root.getInt("gridSize"));

        // Parsear mapLayout
        if (root.has("mapLayout")) {
            levelData.setMapLayout(parseMapLayout(root.getJSONObject("mapLayout")));
        }

        // Parsear enemies
        if (root.has("enemies")) {
            levelData.setEnemyConfig(parseEnemyConfig(root.getJSONObject("enemies")));
        }

        // Parsear fruits
        if (root.has("fruits")) {
            levelData.setFruitConfig(parseFruitConfig(root.getJSONObject("fruits")));
        }

        return levelData;
    }

    /**
     * Parsea la sección mapLayout del JSON.
     */
    private MapLayoutDTO parseMapLayout(JSONObject mapLayoutObj) {
        MapLayoutDTO mapLayout = new MapLayoutDTO();

        // Parsear legend
        if (mapLayoutObj.has("legend")) {
            JSONObject legendObj = mapLayoutObj.getJSONObject("legend");
            Map<String, String> legend = new HashMap<>();
            for (String key : legendObj.keySet()) {
                legend.put(key, legendObj.getString(key));
            }
            mapLayout.setLegend(legend);
        }

        // Parsear grid
        if (mapLayoutObj.has("grid")) {
            JSONArray gridArray = mapLayoutObj.getJSONArray("grid");
            int rows = gridArray.length();
            int cols = rows > 0 ? gridArray.getJSONArray(0).length() : 0;

            String[][] grid = new String[rows][cols];
            for (int i = 0; i < rows; i++) {
                JSONArray rowArray = gridArray.getJSONArray(i);
                for (int j = 0; j < cols; j++) {
                    grid[i][j] = rowArray.getString(j);
                }
            }
            mapLayout.setGrid(grid);
            mapLayout.setGridSize(rows);
        }

        return mapLayout;
    }

    /**
     * Parsea la sección enemies del JSON.
     */
    private EnemyConfigDTO parseEnemyConfig(JSONObject enemiesObj) {
        EnemyConfigDTO config = new EnemyConfigDTO();
        config.setRandomPlacement(enemiesObj.optBoolean("randomPlacement", true));

        if (enemiesObj.has("types")) {
            JSONArray typesArray = enemiesObj.getJSONArray("types");
            List<EnemySpawnDTO> types = new ArrayList<>();

            for (int i = 0; i < typesArray.length(); i++) {
                JSONObject enemyObj = typesArray.getJSONObject(i);
                EnemySpawnDTO spawn = new EnemySpawnDTO();
                spawn.setType(enemyObj.getString("type"));
                spawn.setCount(enemyObj.getInt("count"));
                types.add(spawn);
            }
            config.setTypes(types);
        }

        return config;
    }

    /**
     * Parsea la sección fruits del JSON.
     */
    private FruitConfigDTO parseFruitConfig(JSONObject fruitsObj) {
        FruitConfigDTO config = new FruitConfigDTO();
        config.setRandomPlacement(fruitsObj.optBoolean("randomPlacement", true));

        if (fruitsObj.has("waves")) {
            JSONArray wavesArray = fruitsObj.getJSONArray("waves");
            List<FruitWaveDTO> waves = new ArrayList<>();

            for (int i = 0; i < wavesArray.length(); i++) {
                JSONObject waveObj = wavesArray.getJSONObject(i);
                FruitWaveDTO wave = new FruitWaveDTO();
                wave.setWaveNumber(waveObj.getInt("waveNumber"));
                wave.setSpawnOnStart(waveObj.getBoolean("spawnOnStart"));

                if (waveObj.has("fruits")) {
                    JSONArray fruitsArray = waveObj.getJSONArray("fruits");
                    List<FruitSpawnDTO> fruits = new ArrayList<>();

                    for (int j = 0; j < fruitsArray.length(); j++) {
                        JSONObject fruitObj = fruitsArray.getJSONObject(j);
                        FruitSpawnDTO spawn = new FruitSpawnDTO();
                        spawn.setType(fruitObj.getString("type"));
                        spawn.setCount(fruitObj.getInt("count"));
                        fruits.add(spawn);
                    }
                    wave.setFruits(fruits);
                }
                waves.add(wave);
            }
            config.setWaves(waves);
        }

        return config;
    }

    /**
     * Valida que los datos del nivel sean coherentes.
     */
    private void validateLevelData(LevelDataDTO data) throws BadDopoException {
        if (data.getLevelId() <= 0) {
            throw BadDopoException.invalidLevelId(data.getLevelId());
        }

        if (data.getGridSize() <= 0) {
            throw BadDopoException.invalidGridSize(data.getGridSize());
        }

        if (data.getMapLayout() != null) {
            String[][] grid = data.getMapLayout().getGrid();
            if (grid == null || grid.length != data.getGridSize()) {
                throw BadDopoException.gridMismatch(data.getGridSize());
            }

            // Verificar que todas las filas tengan el mismo tamaño
            for (int i = 0; i < grid.length; i++) {
                if (grid[i].length != data.getGridSize()) {
                    throw BadDopoException.rowSizeMismatch(i, grid[i].length);
                }
            }
        }

        BadDopoLogger.logInfo("Validación de nivel " + data.getLevelId() + " exitosa");
    }

    /**
     * Limpia la caché de niveles (útil para testing o recarga).
     */
    public void clearCache() {
        levelCache.clear();
        BadDopoLogger.logInfo("Caché de niveles limpiada");
    }
}
