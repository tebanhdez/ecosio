package com.ecosio.crawler;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WebCrawler {

    private final URI uri;
    private final int maxDepth;
    private final int timeOut;

    public WebCrawler(URI uri, int maxDepth, int timeOut) {
        this.uri = uri;
        this.maxDepth = maxDepth;
        this.timeOut = timeOut;
    }

    Set<String> outputSet = ConcurrentHashMap.newKeySet();
    Logger logger = Logger.getLogger(WebCrawler.class.getName());
    BlockingQueue<CompletableFuture<Void>> queue = new LinkedBlockingQueue<>();

    public void crawl() {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            Runnable taskProducer = () -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(new Worker(uri,executor, queue, maxDepth, outputSet), executor);
                queue.add(future);
            };

            Thread producerThread = new Thread(taskProducer);
            producerThread.start();

            Runnable taskConsumer = () -> consumerProcess(producerThread);

            Thread consumerThread = new Thread(taskConsumer);
            consumerThread.start();

            try {
                producerThread.join();
                consumerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.severe("Main thread interrupted.");
            }

            executor.shutdown();
            try {
                if (!executor.awaitTermination(timeOut, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }

        logger.info("All tasks completed and executor shutdown.");
        logger.info(outputSet.stream().sorted().collect(Collectors.joining(", ")));
        logger.info(String.format("Total collected links: %1$s", outputSet.size()));
    }

    private void consumerProcess(Thread producerThread) {
        while (true) {
            try {
                CompletableFuture<Void> future = queue.poll(1, TimeUnit.SECONDS);
                if (future != null) {
                    future.get();
                } else if (!producerThread.isAlive()) {
                    break;
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.severe(e.getMessage());
            }
        }
    }
}
