package com.ecosio.crawler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public class Main {
    static final Pattern pattern = Pattern.compile("^(http|https)://.*");
    public static void main(String[] args) throws URISyntaxException {
        if (args.length < 1) {
            System.out.println("Usage: java WebCrawler <domain> [depth] [time out]");
            return;
        }

        String domain = args[0];
        final String _domain = pattern.matcher(domain).matches() ? domain : "https://" + domain;
        final URI uri = new URI(_domain);
        int depth = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        int timeOut = args.length > 2 ? Integer.parseInt(args[2]) : 10;

        var crawler = new WebCrawler(uri, depth, timeOut);
        crawler.crawl();
    }
}
/*

public static class Main {

        static final Pattern pattern = Pattern.compile("^(http|https)://.*");

        public static void main(String[] args) throws URISyntaxException {
            if (args.length < 1) {
                System.out.println("Usage: java EcosioCrawler <domain> [depth] [time out]");
                return;
            }


            String domain = args[0]; // sacar el https a constante
            URI uri = new URI(pattern.matcher(domain).matches() ? domain : "https://" + domain); // o pasar la construccion a una linea abajo

            int depth = args.length > 1 ? Integer.parseInt(args[1]) : 1;
            int timeOut = args.length > 2 ? Integer.parseInt(args[2]) : 10;

            var crawler = new WebCrawler(uri, depth, timeOut);
            crawler.crawl();
        }
    }
 */