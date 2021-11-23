package info.kgeorgiy.ja.kozlov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WebCrawler implements AdvancedCrawler {
    private static final String DOWNLOADERS_NAME = "Downloader Service";
    private static final String EXTRACTORS_NAME = "Extractors Service";
    private static final Predicate<String> DEFAULT_SET = s -> true;

    private final ExecutorService loaderPoll;
    private final ExecutorService extractorsService;
    private final Downloader downloader;
    private final int pagesPerHost;

    // :NOTE: ConcurrentHashMap
    private final ConcurrentHashMap<String, ExecutionQueue> queues;

    @Override
    public void close() {
        shutDownPools();
        waiting(loaderPoll, DOWNLOADERS_NAME);
        waiting(extractorsService, EXTRACTORS_NAME);
    }

    private void shutDownPools() {
        extractorsService.shutdown();
        loaderPoll.shutdown();
    }

    private static void waiting(final ExecutorService currentService, final String serviceName) {
        currentService.shutdown();
        try {
            final boolean f = currentService.awaitTermination(108, TimeUnit.SECONDS);
            if (!f) {
                currentService.shutdownNow();
                if (!currentService.awaitTermination(108, TimeUnit.MILLISECONDS)) {
                    System.err.println(String.join(" ", serviceName, "don't terminated!"));
                }
            }
        } catch (final InterruptedException exception) {
            currentService.shutdownNow();
            System.err.printf("%s %s%n", serviceName, exception.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Result download(final String url, final int searchDepth) {
        return new CrawlerDownloader(DEFAULT_SET).download(url, searchDepth);
    }

    @Override
    public Result download(final String url, int depth, final List<String> hosts) {
        return new CrawlerDownloader(hosts::contains).download(url, depth);
    }

    private static void printErrors(final Result result) {
        System.out.println("Errors: ");
        for (final Map.Entry<String, IOException> cur : result.getErrors().entrySet()) {
            System.out.println(String.join(" ", "Error for",
                    cur.getKey(), "-", cur.getValue().getMessage()));
        }
    }

    private static int getNumber(final int num, int defaultArg, final String[] args) {
        if (args.length > num && num > 0 && args[num] != null) {
            return Integer.parseInt(args[num]);
        }
        return defaultArg;
    }

    private static void printDownloaded(final Result result) {
        System.out.println("Downloaded: ");
        for (final String cur : result.getDownloaded()) {
            System.out.println(cur);
        }
    }



    public static void main(final String[] args) {
        checkArguments(args);
        if (args.length <= 0 || args.length >= 6) {
            System.err.println("Arguments for web crawler should be between 1 and 5: ");
            return;
        }
        try (final Crawler webCrawler = new WebCrawler(new CachingDownloader(),
                getNumber(2, 1, args),
                getNumber(3, 8, args),
                getNumber(4, 8, args)
        )) {
            final int depth = getNumber(1, 1, args);
            final Result result = webCrawler.download(args[0], depth);
            printDownloaded(result);
            printErrors(result);
        } catch (final IOException exception) {
            System.err.println("Downloading error" + exception.getMessage());
        }
    }

    private static void checkArguments(final String[] arguments) {
        Objects.requireNonNull(arguments);
        for (int j = 0; j < arguments.length; ++j) {
            if (arguments[j] == null) {
                System.err.println(String.join(" ", "Argument", Integer.toString(j) , "can't be null"));
            }
        }

    }

    /**
     * Constructor
     * @param downloader позволяет скачивать страницы и извлекать из них ссылки;
     * @param maxDownloader максимальное число одновременно загружаемых страниц;
     * @param maxExtractors максимальное число страниц, из которых одновременно извлекаются ссылки;
     * @param pagesPerHost максимальное число страниц, одновременно загружаемых c одного хоста. Для опредения хоста следует использовать метод getHost класса URLUtils из тестов.
     */
    public WebCrawler(final Downloader downloader, final int maxDownloader, final int maxExtractors, final int pagesPerHost) {
        this.queues = new ConcurrentHashMap<String, ExecutionQueue>();
        this.extractorsService = Executors.newFixedThreadPool(maxExtractors);
        this.downloader = downloader;
        this.loaderPoll = Executors.newFixedThreadPool(maxDownloader);
        this.pagesPerHost = pagesPerHost;
    }

    private class CrawlerDownloader {
        final Phaser phaser = new Phaser();
        final Map<String, IOException> exceptions = new ConcurrentHashMap<>();
        final Set<String> alreadyDownloaded = Collections.newSetFromMap(new ConcurrentHashMap<>());
        private final Predicate<String> hostPredicate;

        public CrawlerDownloader(Predicate<String> hostPredicate) {
            this.hostPredicate = hostPredicate;
        }

        public CrawlerDownloader() {
            this.hostPredicate = DEFAULT_SET;
        }

        private void jobDownload(final int currentDepth, final String url) {
            if (currentDepth <= 0) {
                return;
            }
            phaser.register();
            AtomicReference<Set<String>> newVertices = new AtomicReference<>(Set.of(url));
            IntStream.range(0, currentDepth).forEach( phase -> {
                final Set<String> currentVertices = Collections.newSetFromMap(new ConcurrentHashMap<>());
                for (final String curUrl : newVertices.get()) {
                    oneDownloads(currentDepth, phase, currentVertices, curUrl);
                }
                newVertices.set(currentVertices);
                phaser.arriveAndAwaitAdvance();
            });
        }

        private void oneDownloads(
                final int currentDepth,
                final int phase, final Set<String> currentVertex, final String curUrl
        ) {
            try {
                final String host = URLUtils.getHost(curUrl);
                if (!hostPredicate.test(host)) {
                    return;
                }
                if (alreadyDownloaded.add(curUrl)) {
                    phaser.register();
                    final ExecutionQueue hostDownloader =
                            queues.computeIfAbsent(host, h -> new ExecutionQueue(loaderPoll, pagesPerHost));
                    Callable<Void> callable = () -> {
                        try {
                            fillNewPhaseAndExtractLinks(phaser, curUrl, currentDepth - phase, currentVertex);
                        } catch (final IOException exception) {
                            exceptions.put(curUrl, exception);
                        } finally {
                            phaser.arriveAndDeregister();
                            hostDownloader.remove();
                        }
                        return null;
                    };
                    hostDownloader.offer(callable);
                }
            } catch (final MalformedURLException exception) {
                exceptions.put(curUrl, exception);
            }
        }

        public Result download(final String url, final int searchDepth) {
            jobDownload(searchDepth, url);
            final List<String> result = alreadyDownloaded.stream()
                    .filter(Predicate.not(exceptions::containsKey))
                    .collect(Collectors.toList());
            return new Result(result, exceptions);
        }

        private void fillNewPhaseAndExtractLinks(
                final Phaser phaser,
                final String url,
                final int currentDepth,
                final Set<String> nextVertex
        ) throws IOException {
            final Document curDocument = downloader.download(url);
            if (currentDepth >= 2) {
                phaser.register();
                extractorsService.submit(() -> {
                    try {
                        nextVertex.addAll(curDocument.extractLinks());
                    } catch (final IOException exception) {
                        //exceptions.put(url, exception);
                    } finally {
                        phaser.arriveAndDeregister();
                    }
                });
            }
        }
    }

}
