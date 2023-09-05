package info.kgeorgiy.ja.kadochnikova.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
    }
    public void close() {
        downloaders.shutdown();
        extractors.shutdown();
    }
    public Result download(String url, int depth) {
        List<String> tab = Collections.synchronizedList(new ArrayList<>());
        Map<String, IOException> tabs = new ConcurrentHashMap<>();
        Set<String> processedPages = ConcurrentHashMap.newKeySet();
        Queue<String> queue = new ConcurrentLinkedQueue<>();

        queue.add(url);
        processedPages.add(url);

        for (int i = 0; i < depth; i++) {
            ConcurrentLinkedQueue<CompletableFuture<List<?>>> queues = downloads(queue, tab, tabs);
            queue = urls(queues, processedPages);
        }

        return new Result(new ArrayList<>(tab), tabs);
    }

    private ConcurrentLinkedQueue<CompletableFuture<List<?>>> downloads(Queue<String> url, List<String> tab, Map<String, IOException> pages) {
        return url.stream()
                .map(urls -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Document report = downloader.download(urls);
                        if (report != null) {
                            tab.add(urls);
                        }
                        return report;
                    } catch (IOException e) {
                        pages.put(urls, e);
                        return null;
                    }
                }, downloaders))
                .map(documentFuture -> documentFuture.thenApplyAsync(report -> {
                    if (report != null) {
                        try {
                            return report.extractLinks();
                        } catch (IOException e) {
                            return Collections.emptyList();
                        }
                    } else {
                        return Collections.emptyList();
                    }
                }, extractors))
                .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }


    private Queue<String> urls(ConcurrentLinkedQueue<CompletableFuture<List<?>>> queue, Set<String> tabs) {
        Queue<String> url = new ConcurrentLinkedQueue<>();

        queue.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .distinct()
                .filter(link -> !tabs.contains(link))
                .forEach(link -> {
                    tabs.add((String) link);
                    url.add((String) link);
                });

        return url;
    }

    public static void main(String[] args) {
        if (args == null || args.length < 1 || args.length > 5) {
            System.out.println("Incorrect number of args");
            return;
        }
        String url = args[0];
        int depth, downloaders, extractors, perHost;
        depth = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        downloaders = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        extractors = args.length > 3 ? Integer.parseInt(args[3]) : 1;
        perHost = args.length > 4 ? Integer.parseInt(args[4]) : 1;

        try (WebCrawler webCrawler = new WebCrawler(new CachingDownloader(1.0), downloaders, extractors, perHost)) {
            webCrawler.download(url, depth);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


