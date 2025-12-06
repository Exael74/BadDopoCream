package domain.service;

import domain.BadDopoException;
import domain.BadDopoLogger;
import domain.state.GameState;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Servicio de persistencia para guardar y cargar partidas.
 */
public class PersistenceService {

    private static final String SAVE_DIRECTORY = "saves";
    private static final String SAVE_EXTENSION = ".dat";

    public PersistenceService() {
        createSaveDirectory();
    }

    /**
     * Crea el directorio de guardado si no existe.
     */
    private void createSaveDirectory() {
        try {
            Path path = Paths.get(SAVE_DIRECTORY);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            BadDopoLogger.logError("Error al crear directorio de guardado", e);
        }
    }

    /**
     * Guarda el estado actual del juego.
     * El nombre del archivo incluye la fecha y hora.
     *
     * @param gameState Estado del juego a guardar
     * @return Nombre del archivo guardado
     * @throws BadDopoException Si hay error al guardar
     */
    public String saveGame(GameState gameState) throws BadDopoException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String filename = "save_" + timestamp + SAVE_EXTENSION;
        Path path = Paths.get(SAVE_DIRECTORY, filename);
        return saveGame(gameState, path.toFile());
    }

    /**
     * Guarda el estado actual del juego en un archivo específico.
     *
     * @param gameState Estado del juego a guardar
     * @param file      Archivo donde guardar
     * @return Nombre del archivo guardado
     * @throws BadDopoException Si hay error al guardar
     */
    public String saveGame(GameState gameState, File file) throws BadDopoException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(gameState);
            BadDopoLogger.logInfo("Partida guardada exitosamente: " + file.getName());
            return file.getName();
        } catch (IOException e) {
            throw new BadDopoException("Error al guardar la partida: " + e.getMessage());
        }
    }

    /**
     * Carga una partida guardada.
     *
     * @param filename Nombre del archivo a cargar
     * @return Estado del juego cargado
     * @throws BadDopoException Si hay error al cargar
     */
    public GameState loadGame(String filename) throws BadDopoException {
        Path path = Paths.get(SAVE_DIRECTORY, filename);
        if (!Files.exists(path)) {
            throw new BadDopoException("El archivo de guardado no existe: " + filename);
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            GameState gameState = (GameState) ois.readObject();
            BadDopoLogger.logInfo("Partida cargada exitosamente: " + filename);
            return gameState;
        } catch (IOException | ClassNotFoundException e) {
            throw new BadDopoException("Error al cargar la partida: " + e.getMessage());
        }
    }

    /**
     * Obtiene la lista de partidas guardadas disponibles.
     *
     * @return Lista de nombres de archivos
     */
    public List<String> getSavedGames() {
        try (Stream<Path> walk = Files.walk(Paths.get(SAVE_DIRECTORY))) {
            return walk.filter(Files::isRegularFile)
                    .map(x -> x.getFileName().toString())
                    .filter(f -> f.endsWith(SAVE_EXTENSION))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            BadDopoLogger.logError("Error al listar partidas guardadas", e);
            return new ArrayList<>();
        }
    }
}
