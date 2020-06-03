package com.mongodb.connection;

/**
 * Custom exception class.
 *
 * @author timbru31
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class BaseQueryFactory extends Exception {
    private static final long serialVersionUID = -2138842311141557820L;

    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "PMD.CallSuperInConstructor", "PMD.UncommentedEmptyConstructor" })
    public BaseQueryFactory() {
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public BaseQueryFactory(final String message) {
        super(message);
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public BaseQueryFactory(final String message, final Throwable cause) {
        super(message, cause);
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public BaseQueryFactory(final Throwable cause) {
        super(cause);
    }
}
