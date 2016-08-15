package org.json;

public class HTTPTokenException extends Exception {

    private static final long serialVersionUID = 551973567466799446L;

    public HTTPTokenException() {
        super();
    }

    public HTTPTokenException(String message) {
        super(message);
    }

    public HTTPTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTTPTokenException(Throwable cause) {
        super(cause);
    }
}
