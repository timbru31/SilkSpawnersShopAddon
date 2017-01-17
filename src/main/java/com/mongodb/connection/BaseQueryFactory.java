package com.mongodb.connection;

public class BaseQueryFactory extends Exception {
    private static final long serialVersionUID = 551973567466799446L;

    public BaseQueryFactory() {
        super();
    }

    public BaseQueryFactory(String message) {
        super(message);
    }

    public BaseQueryFactory(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseQueryFactory(Throwable cause) {
        super(cause);
    }
}
