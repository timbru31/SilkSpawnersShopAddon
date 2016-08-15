package de.dustplanet.silkspawnersshopaddon;

public class InvalidAmountException extends Exception {
    private static final long serialVersionUID = -8178693365567126589L;

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
