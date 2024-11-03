package io.yanmulin.codesnippets.examples.concurrency.renderer;

import java.util.List;
import java.util.concurrent.*;

public class ConcurrentRenderer extends AbstractRenderer {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    public void render(CharSequence source) {
        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
        List<String> images = scanForImageInfo(source);
        for (String image: images) {
            completionService.submit(() -> downloadImage(image), image);
        }

        renderText(source);
        for (int i=0;i<images.size();i++) {

            try {
                Future<String> future = completionService.take();
                renderImage(future.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw launderThrowable(e.getCause());
            }
        }
    }

    public static void main(String[] args) {
        long startMillis = System.currentTimeMillis();
        new ConcurrentRenderer().render("This is the first line.\n"
                + "The second line follows.\n"
                + "Next is an image.\n"
                + "<img>www.example.com/images/A</img>\n"
                + "Another image.\n"
                + "<img>www.example.com/images/B</img>\n");
        System.out.println("elapsed " + (System.currentTimeMillis() - startMillis) + " millis");
    }
}
