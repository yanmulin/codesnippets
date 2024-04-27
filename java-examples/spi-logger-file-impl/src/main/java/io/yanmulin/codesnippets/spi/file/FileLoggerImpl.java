package io.yanmulin.codesnippets.spi.file;

import io.yanmulin.codesnippets.spi.Logger;

import java.io.*;

public class FileLoggerImpl implements Logger {
    File file = new File("my.log");
    @Override
    public void log(String msg) {
        try (FileWriter fos = new FileWriter(file)) {
            fos.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
