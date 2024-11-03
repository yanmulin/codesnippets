package io.yanmulin.codesnippets.examples.concurrency.renderer;

public class SingleThreadRenderer extends AbstractRenderer {

    @Override
    public void render(CharSequence source) {
        renderText(source);
        for (String image: scanForImageInfo(source)) {
            downloadImage(image);
            renderImage(image);
        }
    }

    public static void main(String[] args) {
        long startMillis = System.currentTimeMillis();
        new SingleThreadRenderer().render("This is the first line.\n"
                + "The second line follows.\n"
                + "Next is an image.\n"
                + "<img>www.example.com/images/A</img>\n"
                + "Another image.\n"
                + "<img>www.example.com/images/B</img>\n");
        System.out.println("elapsed " + (System.currentTimeMillis() - startMillis) + " millis");
    }
}
