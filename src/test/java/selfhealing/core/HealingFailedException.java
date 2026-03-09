package selfhealing.core;

public class HealingFailedException extends RuntimeException {

    public HealingFailedException(String message) {
        super(message);
    }

    public HealingFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
