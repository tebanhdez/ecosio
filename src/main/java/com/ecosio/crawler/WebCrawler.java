package com.ecosio.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private Set<String> extractLinks(String htmlContent) {
        Set<String> links = new HashSet<>();
        String regex = "<a\\s+(?:[^>]*?\\s+)?href=\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(htmlContent);

        while (matcher.find()) {
            String link = matcher.group(1);
            if (link.startsWith("/")) {
                link = uri.getHost() + link;
            }
            links.add(link);
        }
        return links;
    }

    private boolean isSameHost(String url) {
        try {
            String _hostUri = uri.getHost();
            URI uri = new URI(url);
            return uri.getHost() != null && uri.getHost().endsWith(_hostUri);
        } catch (URISyntaxException e) {
            return false;
        }
    }
    public void crawl() {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            Runnable taskProducer = () -> {
                CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> traverseWebsite(uri, executor, queue, 0), executor);
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

    private Void traverseWebsite(URI uri, ExecutorService executor, BlockingQueue<CompletableFuture<Void>> queue, int depth) {
        if (depth < maxDepth) {
            HttpResponse<String> response;
            try (HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()) {
                HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    String htmlContent = response.body();
                    Set<String> links = extractLinks(htmlContent);
                    for (String _link : links) {
                        var link = _link.endsWith("/") ? _link.substring(0, _link.length() - 1) : _link;
                        if (isSameHost(link) && !outputSet.contains(link)) {
                            outputSet.add(link);
                            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                                try {
                                    return traverseWebsite(new URI(link), executor, queue, depth + 1);
                                } catch (URISyntaxException e) {
                                    return null;
                                }
                            }, executor);
                            queue.add(future);
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                logger.warning(e.getMessage());
            }
            return null;
        }
        return null;
    }
}