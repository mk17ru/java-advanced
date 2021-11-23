package info.kgeorgiy.ja.kozlov.concurrent;

import java.util.*;

public class ExecutionQueue<R extends Runnable> {
    private final Deque<R> queue;

    public synchronized R remove() throws InterruptedException {
        while(queue.isEmpty()) {
            wait();
        }
        return queue.poll();
    }

    public synchronized void offer(final R element) {
        queue.add(element);
        notify();
    }

    public ExecutionQueue() {
        queue = new ArrayDeque<>();
    }

    public synchronized int size() {
        return queue.size();
    }

    @Override
    public synchronized String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (R runnable : queue) {
            stringBuilder.append(runnable.toString());
        }
        return stringBuilder.toString();
    }
}
