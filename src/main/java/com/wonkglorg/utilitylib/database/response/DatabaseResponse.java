package com.wonkglorg.utilitylib.database.response;

@SuppressWarnings("unused")
public class DatabaseResponse {
    private final Exception exception;

    public DatabaseResponse(Exception exception) {
        this.exception = exception;
    }

    public boolean hasError() {
        return exception != null;
    }

    public Exception getException() {
        return exception;
    }
}
