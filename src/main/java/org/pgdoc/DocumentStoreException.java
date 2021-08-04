package org.pgdoc;

public class DocumentStoreException extends RuntimeException {
    public DocumentStoreException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}
