package it.unibo.mvc;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    
    private static final String fileName = "src/main/resources/config.yml";
    private static final String output_path = System.getProperty("user.home") + File.separator + "output.txt";

    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     * @throws IOException 
     */
    public DrawNumberApp(final DrawNumberView... views) throws IOException {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }

        this.model = parse(fileName);
    }

    public DrawNumberImpl parse(String fileName) throws IOException{
        try {
            BufferedReader r = new BufferedReader(new FileReader(new File(fileName)));
            String line;
            int min = DrawNumberApp.MIN;
            int max = DrawNumberApp.MAX;
            int attemps = DrawNumberApp.ATTEMPTS;
            while((line = r.readLine()) != null){
                final String[] split = line.split(": ");
                switch (split[0]) {
                    case "minimum":
                        min = Integer.valueOf(split[1]); 
                        break;
                    case "maximum":
                        max = Integer.valueOf(split[1]); 
                        break;
                    case "attemps":
                        attemps = Integer.valueOf(split[1]);
                        break;
                }
            }
            r.close();
            return new DrawNumberImpl(min, max, attemps);
        } catch (final FileNotFoundException e){
            return new DrawNumberImpl(MIN, MAX, ATTEMPTS);
        }
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws IOException 
     */
    public static void main(final String... args) throws IOException {
        new DrawNumberApp(new DrawNumberViewImpl(), new DrawNumberViewImpl(), new PrintStreamView(System.out), new PrintStreamView(output_path));
    }

}
