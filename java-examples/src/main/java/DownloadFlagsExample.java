import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class DownloadFlagsExample {
    private final static int BUFFER_SIZE = 4096;
    private final static String URL_BASE = "http://localhost:8080";
    private final static String LIST_PATH = "/";
    private final static String DOWNLOAD_DESTINATION = "/tmp/flags";
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final HttpClient httpClient = HttpClients.createDefault();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private CompletableFuture<String[]> listFlagsAsync(URI uri) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse response = httpClient.execute(new HttpGet(uri));
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("HTTP request fails with status code " +
                            response.getStatusLine().getStatusCode());
                }

                return objectMapper.readValue(response.getEntity().getContent(), String[].class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    private CompletableFuture<Void> downloadFlagAsync(URI uri) throws IOException {
        String suffix = Files.getFileExtension(uri.getPath());
        String nameWithoutExtension = Files.getNameWithoutExtension(uri.getPath());
        File tempFile =  File.createTempFile(nameWithoutExtension + "_", "." + suffix);
        tempFile.deleteOnExit();

        return CompletableFuture.runAsync(() -> {
            int n;
            byte[] buffer = new byte[BUFFER_SIZE];
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                HttpResponse response = httpClient.execute(new HttpGet(uri));
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("HTTP request fails with status code " +
                            response.getStatusLine().getStatusCode());
                }
                try (InputStream inputStream = response.getEntity().getContent()) {
                    while ((n = inputStream.read(buffer)) >= 0) {
                        outputStream.write(buffer, 0, n);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }, executor).whenComplete((res, exc) -> {
            if (exc != null) {
                log.error("download {} error", tempFile.getPath(), Throwables.getRootCause(exc));
            } else {
                log.info("downloaded to {}", tempFile.getPath());
                File destFile = new File(DOWNLOAD_DESTINATION + "/" + uri.getPath());
                if (destFile.exists()) {
                    if (destFile.delete()) {
                        log.info("deleted {} because it already exists", destFile.getPath());
                    } else {
                        log.info("unable to delete {}", destFile.getPath());
                        return;
                    }
                }

                if (tempFile.renameTo(destFile)) {
                    log.info("renamed to {}", destFile.getPath());
                } else {
                    log.error("unable to rename from {} to {}", tempFile.getPath(), destFile.getPath());
                }
            }
        });
    }

    private CompletableFuture<Void> startAsync() {
        return listFlagsAsync(URI.create(URL_BASE + LIST_PATH)).thenCompose(flags ->
        {
            log.info("flags: [{}]", String.join(", ", flags));
            try {
                List<URI> uris = Arrays.stream(flags)
                        .map(flag -> URL_BASE + "/" + flag)
                        .map(URI::create).collect(Collectors.toList());
                List<CompletableFuture<Void>> futures = new ArrayList<>(uris.size());
                for (URI uri: uris) {
                    futures.add(downloadFlagAsync(uri));
                }

                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void run() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            startAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            log.info("done, elapsed {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            executor.shutdown();
        }
    }

    public static void main(String[] args) { new DownloadFlagsExample().run(); }
}
