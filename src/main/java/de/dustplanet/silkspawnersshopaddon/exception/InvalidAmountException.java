package de.dustplanet.silkspawnersshopaddon.exception;

/**
 * Custom exception for an invalid amount value.
 *
 * @author timbru31
 */
public class InvalidAmountException extends Exception {
    private static final long serialVersionUID = 1308980719989527011L;

    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "PMD.AvoidDuplicateLiterals", "PMD.CallSuperInConstructor",
            "PMD.UncommentedEmptyConstructor" })
    public InvalidAmountException() {
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public InvalidAmountException(final String message) {
        super(message);
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public InvalidAmountException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public InvalidAmountException(final Throwable cause) {
        super(cause);
    }
}
