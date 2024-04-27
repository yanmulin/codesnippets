package io.yanmulin.codesnippets.spi.file;

import io.yanmulin.codesnippets.spi.Logger;
import io.yanmulin.codesnippets.spi.LoggerProvider;

public class FileLoggerProvider implements LoggerProvider {
    @Override
    public Logger create() {
        return new FileLoggerImpl();
    }
}
