package io.yanmulin.codesnippets.spring.aop.target;

import java.io.EOFException;
import java.io.IOException;

public class DataSource {
    private String message;

    public DataSource(String message) {
        this.message = message;
    }

    public String read() throws IOException {
        if (message == null || message.isEmpty()) {
            throw new EOFException();
        }
        return message;
    }
}
