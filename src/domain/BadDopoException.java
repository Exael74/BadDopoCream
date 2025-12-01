package domain;

/**
 * Excepción personalizada para errores específicos del juego BadDopoCream.
 */
public class BadDopoException extends Exception {

    public BadDopoException(String message) {
        super(message);
    }

    public BadDopoException(String message, Throwable cause) {
        super(message, cause);
    }
}
