package io.yanmulin.codesnippets.spi.console;

import io.yanmulin.codesnippets.spi.Logger;
import io.yanmulin.codesnippets.spi.LoggerProvider;

public class ConsoleLoggerProvider implements LoggerProvider {
    @Override
    public Logger create() {
        return new ConsoleLoggerImpl();
    }
}
