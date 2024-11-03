package io.yanmulin.codesnippets.examples.concurrency.renderer;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureRenderer extends AbstractRenderer {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    public void render(CharSequence source) {
        Future<List<String>> future = executor.submit(() -> {
            List<String> images = scanForImageInfo(source);
            for (String image : images) {
                downloadImage(image);
            }
            return images;
        });
        renderText(source);
        try {
            future.get();
        } catch (ExecutionException e) {
            throw launderThrowable(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
        }
    }

    public static void main(String[] args) {
        long startMillis = System.currentTimeMillis();
        new FutureRenderer().render("This is the first line.\n"
                + "The second line follows.\n"
                + "Next is an image.\n"
                + "<img>www.example.com/images/A</img>\n"
                + "Another image.\n"
                + "<img>www.example.com/images/B</img>\n");
        System.out.println("elapsed " + (System.currentTimeMillis() - startMillis) + " millis");
    }
}
