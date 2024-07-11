package io.yanmulin.codesnippets.examples.basic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Optional;

public class ProcessExamples {

    private void readAndPrint(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    public void run() {
        try {
            Process process = new ProcessBuilder().command("echo", "hello", "world").start();
            System.out.println("current pid: " + ProcessHandle.current().pid() + ", a new process started.");
            System.out.println();

            InputStreamReader stdoutInputStream = new InputStreamReader(process.getInputStream());
            BufferedReader stdoutReader = new BufferedReader(stdoutInputStream);
            InputStreamReader stderrInputStream = new InputStreamReader(process.getErrorStream());
            BufferedReader stderrReader = new BufferedReader(stderrInputStream);

            long pid = process.pid();
            Optional<ProcessHandle> parent = process.toHandle().parent();
            Optional<String> command = process.info().command();
            Optional<String[]> arguments = process.info().arguments();
            Optional<String> user = process.info().user();
            Optional<Instant> startInstant = process.info().startInstant();

            System.out.println("Process Info");
            System.out.println("* pid: " + pid);
            System.out.println("* parent pid: " + (parent.isPresent() ? parent.get().pid() : null));
            System.out.println("* command: " + command.orElse(null));
            System.out.println("* arguments: " + (arguments.isPresent() ? String.join(", ", arguments.get()) : null));
            System.out.println("* user: " + user.orElse(null));
            System.out.println("* startInstant: " + startInstant.orElse(null));
            System.out.println();

            process.onExit().thenAccept(p -> System.out.println("process " + p.pid() + " exited"));

            int exitCode = process.waitFor();
            System.out.println("Exited with code " + exitCode);
            System.out.println();

            System.out.println("Process stdout");
            readAndPrint(stdoutReader);
            System.out.println();

            System.out.println("Process stderr");
            readAndPrint(stderrReader);
            System.out.println();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ProcessExamples().run();
    }
}
