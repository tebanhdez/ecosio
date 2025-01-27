package com.ecosio.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Worker implements Runnable {

    private final URI uri;
    private final ExecutorService executor;
    private final BlockingQueue<CompletableFuture<Void>> queue;
    private final int depth;
    private final Set<String> outputSet;
    Logger logger = Logger.getLogger(Worker.class.getName());

    Worker(URI uri, ExecutorService executor, BlockingQueue<CompletableFuture<Void>> queue, int depth, Set<String> outputSet) {
        this.uri = uri;
        this.executor = executor;
        this.queue = queue;
        this.depth = depth;
        this.outputSet = outputSet;
    }

    @Override
    public void run() {

        if (depth <= 0) return;

        HttpResponse<String> response;

        try (HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()) {

            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return;

            String htmlContent = response.body();
            Set<String> links = extractLinks(htmlContent);
            for (String _link : links) {
                var link = _link.endsWith("/") ? _link.substring(0, _link.length() - 1) : _link;
                if (isSameHost(link) && !outputSet.contains(link)) {
                    outputSet.add(link);
                    CompletableFuture<Void> future = CompletableFuture.runAsync(new Worker(new URI(link), executor, queue, depth - 1, outputSet), executor);
                    queue.add(future);
                }
            }
        } catch (IOException | InterruptedException | URISyntaxException e) {
            logger.warning(e.getMessage());
        }
    }

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
}
