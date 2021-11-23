package info.kgeorgiy.ja.kozlov.crawler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class ExecutionQueue {
    private final Deque<Callable<Void>> queue = new ArrayDeque<>();
    private int pagesPerHost;
    private int scheduled;
    private final ExecutorService loaderPoll;

    public ExecutionQueue(final ExecutorService loaderPoll, int pagesPerHost) {
        this.pagesPerHost = pagesPerHost;
        this.loaderPoll = loaderPoll;
    }

    public synchronized void remove() {
        Callable<Void> run;
        if ((run = queue.poll()) != null) {
            loaderPoll.submit(run);
            return;
        }
        scheduled--;
    }

    //
//    public synchronized void add(Runnable j) {
//        if (pagesPerHost <= scheduled) {
//            queue.add(j);
//        } else
    //    loaderPoll.submit(j);

//    }

    public synchronized void offer(final Callable<Void> job) {
        if (pagesPerHost <= scheduled) {
            queue.add(job);
            return;
        }
        loaderPoll.submit(job);
        scheduled++;
    }
}
