package io.yanmulin.codesnippets.examples.concurrency;

import java.util.Random;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class TravelAgency {

    private static String DEFAULT_AD = "default-ad";
    private static final int TIME_BUDGE_SECONDS = 5;
    private static final int ADS_COUNT = 6;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    Random random = new Random();

    class FetchAdsTask implements Callable<String> {

        int id;

        FetchAdsTask(int id) {
            this.id = id;
        }

        @Override
        public String call() throws Exception {
            int sleep = random.nextInt(4);
            switch (sleep) {
                case 0: throw new RuntimeException("runtime exception");
                case 1: throw new Exception("exception");
                default: SECONDS.sleep(sleep);
            }
            return "ads-" + id + "-" + sleep;
        }
    }

    public void renderAds() {
        long endMillis = System.currentTimeMillis() + TIME_BUDGE_SECONDS * 1000;

        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);

        for (int i=0;i<ADS_COUNT;i++) {
            completionService.submit(new FetchAdsTask(i));
        }

        for (int i=0;i<ADS_COUNT;i++) {
            String ads;
            try {
                long timeout = endMillis - System.currentTimeMillis();
                Future<String> future = completionService.poll(timeout, MILLISECONDS);
                if (future != null) {
                    ads = future.get();
                } else {
                    System.out.println("timeout");
                    ads = DEFAULT_AD;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                ads = DEFAULT_AD;
            } catch (ExecutionException e) {
                e.printStackTrace();
                ads = DEFAULT_AD;
            }
            System.out.println(ads);
        }
    }

    public static void main(String[] args) {
        new TravelAgency().renderAds();
    }

}
