package info.kgeorgiy.ja.kozlov.concurrent;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ThreadUtils {

    public static <R> List<R> collectList(Stream<R> stream) {
        return stream.collect(Collectors.toList());
    }

    public static <T extends Thread> List<T> threadsCollecting(final int numberOfThreads, final Function<Integer, T> function) {
        return ThreadUtils.collectList(IntStream.range(0, numberOfThreads).mapToObj((cur) -> {
            T currentThread = function.apply(cur);
            currentThread.start();
            return currentThread;
        }));
    }
    //Future
    // MyThread extends Thread
}
