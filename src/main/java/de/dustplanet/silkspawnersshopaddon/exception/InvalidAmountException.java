package de.dustplanet.silkspawnersshopaddon.exception;

public class InvalidAmountException extends Exception {
    private static final long serialVersionUID = 1308980719989527011L;

    public InvalidAmountException() {
        super();
    }

    public InvalidAmountException(String message) {
        super(message);
    }

    public InvalidAmountException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAmountException(Throwable cause) {
        super(cause);
    }
}
