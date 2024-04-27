package io.yanmulin.codesnippets.spi.console;

import io.yanmulin.codesnippets.spi.Logger;

public class ConsoleLoggerImpl implements Logger {
    @Override
    public void log(String msg) {
        System.out.println("log: " + msg);
    }
}
