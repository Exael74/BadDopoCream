package domain;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Utilidad de logging para el juego BadDopoCream.
 * Registra errores y eventos importantes en un archivo de log.
 */
public class BadDopoLogger {

    private static final Logger logger = Logger.getLogger("BadDopoCream");
    private static FileHandler fileHandler;

    static {
        try {
            // Configurar el handler de archivo
            fileHandler = new FileHandler("baddopo.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("No se pudo inicializar el sistema de logging: " + e.getMessage());
        }
    }

    /**
     * Registra un mensaje de información.
     *
     * @param message Mensaje a registrar
     */
    public static void logInfo(String message) {
        logger.info(message);
    }

    /**
     * Registra un error con su excepción.
     *
     * @param message Mensaje de error
     * @param e       Excepción ocurrida
     */
    public static void logError(String message, Throwable e) {
        logger.log(Level.SEVERE, message, e);
    }

    /**
     * Registra un error grave.
     *
     * @param message Mensaje de error
     */
    public static void logSevere(String message) {
        logger.severe(message);
    }
}
