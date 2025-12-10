package domain.service;

import domain.BadDopoException;
import domain.BadDopoLogger;
import domain.dto.MapLayoutDTO;
import domain.entity.*;
import domain.state.GameState;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio responsable de parsear la matriz visual del mapa y aplicarla al
 * GameState.
 * Convierte símbolos de la leyenda en entidades del juego.
 */
public class MapParserService {

    /**
     * Aplica el layout del mapa al GameState.
     * Limpia el estado actual y crea entidades según la matriz visual.
     *
     * @param state           GameState a modificar
     * @param layout          Estructura del mapa desde JSON
     * @param numberOfPlayers Número de jugadores (para posicionar spawns)
     * @throws BadDopoException si hay errores en el layout
     */
    public void applyMapLayout(GameState state, MapLayoutDTO layout, int numberOfPlayers) throws BadDopoException {
        if (layout == null || layout.getGrid() == null) {
            throw new BadDopoException("MapLayout o grid es null");
        }

        String[][] grid = layout.getGrid();
        Map<String, String> legend = layout.getLegend();

        if (legend == null) {
            throw new BadDopoException("Legend es null");
        }

        // Limpiar estado actual
        state.getIceBlocks().clear();
        state.getHotTiles().clear();
        state.getUnbreakableBlocks().clear();

        BadDopoLogger.logInfo("Parseando mapa de " + grid.length + "x" + grid[0].length);

        // Parsear grid
        Point player1Spawn = null;
        Point player2Spawn = null;
        Point igluCenter = null;

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                String symbol = grid[row][col];
                String type = legend.get(symbol);

                if (type == null) {
                    BadDopoLogger.logError("Símbolo desconocido en grid[" + row + "][" + col + "]: " + symbol, null);
                    continue;
                }

                Point position = new Point(col, row);

                switch (type) {
                    case "empty":
                        // No hacer nada
                        break;

                    case "ice":
                        state.addIceBlock(new IceBlock(position, false));
                        break;

                    case "ice_permanent":
                        state.addIceBlock(new IceBlock(position, true));
                        break;

                    case "wall":
                        state.addUnbreakableBlock(new UnbreakableBlock(position));
                        break;

                    case "hot_tile":
                        state.addHotTile(new HotTile(position));
                        break;

                    case "iglu":
                        // El iglú es 3x3, solo guardamos el centro una vez
                        if (igluCenter == null) {
                            igluCenter = position;
                        }
                        break;

                    case "player1_spawn":
                        player1Spawn = position;
                        break;

                    case "player2_spawn":
                        player2Spawn = position;
                        break;

                    default:
                        BadDopoLogger.logError("Tipo desconocido en leyenda: " + type, null);
                        break;
                }
            }
        }

        // Aplicar iglú
        if (igluCenter != null) {
            state.setIglu(new Iglu(igluCenter));
            BadDopoLogger.logInfo("Iglú creado en " + igluCenter);
        }

        // Aplicar spawns de jugadores
        if (player1Spawn != null && state.getPlayer() != null) {
            state.getPlayer().setPosition(player1Spawn);
            BadDopoLogger.logInfo("Player 1 spawn: " + player1Spawn);
        }

        if (player2Spawn != null && state.getPlayer2() != null && (numberOfPlayers == 2 || numberOfPlayers == 0)) {
            state.getPlayer2().setPosition(player2Spawn);
            BadDopoLogger.logInfo("Player 2 spawn: " + player2Spawn);
        }

        BadDopoLogger.logInfo("Mapa aplicado: " + state.getIceBlocks().size() + " hielos, " +
                state.getUnbreakableBlocks().size() + " paredes, " +
                state.getHotTiles().size() + " baldosas calientes");
    }

    /**
     * Encuentra la posición de spawn de un jugador en el grid.
     * Útil para validación o búsqueda manual.
     */
    public Point findPlayerSpawn(String[][] grid, Map<String, String> legend, String playerMarker) {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                String symbol = grid[row][col];
                String type = legend.get(symbol);
                if (playerMarker.equals(type)) {
                    return new Point(col, row);
                }
            }
        }
        return null;
    }

    /**
     * Extrae todas las posiciones de hielo del grid.
     * Útil para análisis o debugging.
     */
    public List<Point> extractIcePositions(String[][] grid, Map<String, String> legend) {
        List<Point> icePositions = new ArrayList<>();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                String symbol = grid[row][col];
                String type = legend.get(symbol);
                if ("ice".equals(type) || "ice_permanent".equals(type)) {
                    icePositions.add(new Point(col, row));
                }
            }
        }
        return icePositions;
    }
}
