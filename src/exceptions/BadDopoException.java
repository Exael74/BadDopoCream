package exceptions;

/**
 * Excepción personalizada para el juego Bad Dopo Cream.
 * Centraliza todos los mensajes de error del dominio y presentación.
 */
public class BadDopoException extends Exception {

    // Constantes para mensajes fijos
    public static final String MSG_MAP_LAYOUT_NULL = "MapLayout o grid es null";
    public static final String MSG_LEGEND_NULL = "Legend es null";
    public static final String MSG_LEVEL_FILE_ERROR = "No se pudo leer el archivo de nivel: ";

    public BadDopoException(String message) {
        super(message);
    }

    public BadDopoException(String message, Throwable cause) {
        super(message, cause);
    }

    // Métodos de fábrica para excepciones comunes con parámetros

    public static BadDopoException saveError(String details) {
        return new BadDopoException("Error al guardar la partida: " + details);
    }

    public static BadDopoException saveFileNotFound(String filename) {
        return new BadDopoException("El archivo de guardado no existe: " + filename);
    }

    public static BadDopoException loadError(String details) {
        return new BadDopoException("Error al cargar la partida: " + details);
    }

    public static BadDopoException levelLoadError(String filename, Throwable cause) {
        return new BadDopoException(MSG_LEVEL_FILE_ERROR + filename, cause);
    }

    public static BadDopoException jsonParseError(int levelId, String details, Throwable cause) {
        return new BadDopoException("Error parseando JSON del nivel " + levelId + ": " + details, cause);
    }

    public static BadDopoException invalidLevelId(int levelId) {
        return new BadDopoException("levelId inválido: " + levelId);
    }

    public static BadDopoException invalidGridSize(int gridSize) {
        return new BadDopoException("gridSize inválido: " + gridSize);
    }

    public static BadDopoException gridMismatch(int gridSize) {
        return new BadDopoException("El grid no coincide con gridSize: " + gridSize);
    }

    public static BadDopoException rowSizeMismatch(int rowIndex, int rowLength) {
        return new BadDopoException("Fila " + rowIndex + " tiene tamaño incorrecto: " + rowLength);
    }

    public static BadDopoException mapLayoutNull() {
        return new BadDopoException(MSG_MAP_LAYOUT_NULL);
    }

    public static BadDopoException legendNull() {
        return new BadDopoException(MSG_LEGEND_NULL);
    }
}
