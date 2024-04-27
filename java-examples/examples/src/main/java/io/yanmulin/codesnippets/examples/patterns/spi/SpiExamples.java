package io.yanmulin.codesnippets.examples.patterns.spi;

import io.yanmulin.codesnippets.spi.Logger;
import io.yanmulin.codesnippets.spi.LoggerProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class SpiExamples {
    public void allProviders() {
        List<LoggerProvider> services = new ArrayList<>();
        ServiceLoader<LoggerProvider> loader = ServiceLoader.load(LoggerProvider.class);
        loader.forEach(services::add);
        System.out.println(services.stream().map(Object::getClass).collect(Collectors.toList()));
    }

    private LoggerProvider provider(String name) {
        ServiceLoader<LoggerProvider> loader = ServiceLoader.load(LoggerProvider.class);
        Iterator<LoggerProvider> iterator = loader.iterator();
        while (iterator.hasNext()) {
            LoggerProvider next = iterator.next();
            if (name.equals(next.getClass().getName())) {
                return next;
            }
        }
        return null;
    }

    public void logToConsole() {
        LoggerProvider provider = provider("io.yanmulin.codesnippets.spi.console.ConsoleLoggerProvider");
        if (provider == null) {
            System.out.println("console provider not found");
            return;
        }

        Logger logger = provider.create();
        logger.log("hello");
    }

    // add spi-logger-file and spi-logger-console jars to classpath before running
    public static void main(String[] args) {
        new SpiExamples().allProviders();
        new SpiExamples().logToConsole();
    }
}
