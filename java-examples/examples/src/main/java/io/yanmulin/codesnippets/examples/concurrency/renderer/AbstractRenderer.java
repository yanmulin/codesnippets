package io.yanmulin.codesnippets.examples.concurrency.renderer;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

public abstract class AbstractRenderer {
    public abstract void render(CharSequence source);

    protected void renderText(CharSequence source) {
        int n = source.length();
        int lineStart = 0;
        for (int i=0;i<n;i++) {
            char c = source.charAt(i);
            if (c == '\n') {
                String line = source.subSequence(lineStart, i).toString();
                processTextLine(line);
                lineStart = i + 1;
            }
        }

        if (lineStart != n) {
            String line = source.subSequence(lineStart, n).toString();
            processTextLine(line);
        }
    }

    private void processTextLine(String line) {
        if (line.startsWith("<img>") && line.endsWith("</img>")) {
            System.out.println("##IMAGE PLACEHOLDER##");
        } else {
            System.out.println(line);
        }
    }

    protected void downloadImage(String image) {
        try {
            SECONDS.sleep(2);
        } catch (InterruptedException ignore) {}
    }

    protected void renderImage(String image) {
        System.out.println(image);
    }

    protected List<String> scanForImageInfo(CharSequence source) {
        int n = source.length();
        int lineStart = 0;
        List<String> images = new ArrayList<>();
        for (int i=0;i<n;i++) {
            char c = source.charAt(i);
            if (c == '\n') {
                String line = source.subSequence(lineStart, i).toString();
                processImageLine(images, line);
                lineStart = i + 1;
            }
        }

        if (lineStart != n) {
            String line = source.subSequence(lineStart, n).toString();
            processImageLine(images, line);
        }
        return images;
    }

    private void processImageLine(List<String> images, String line) {
        if (line.startsWith("<img>") && line.endsWith("</img>")) {
            images.add(line.substring("<img>".length(), line.length() - "</img>".length()));
        }
    }

    protected RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException) return (RuntimeException) t;
        else if (t instanceof Error) throw (Error) t;
        else throw new IllegalStateException("unknown error", t);
    }
}
