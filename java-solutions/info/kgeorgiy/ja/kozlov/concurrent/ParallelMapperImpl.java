package info.kgeorgiy.ja.kozlov.concurrent;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import info.kgeorgiy.java.advanced.mapper.*;
import java.util.function.Function;
import java.nio.file.attribute.BasicFileAttributes;

public class ParallelMapperImpl implements ParallelMapper {

    private final List<Thread> executions;
    private ExecutionQueue<Runnable> queue;

    /**
     * Stops all threads. All unfinished mappings leave in undefined.
     */
    @Override
    public void close() {
        for (Thread execution : executions) {
            execution.interrupt();
        }
        for (Thread execution : executions) {
            stopThread(execution);
        }
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performs in parallel.
     *
     * @param mapFunction mapper
     * @param arguments   args
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <F, S> List<S> map(Function<? super F, ? extends S> mapFunction, final List<? extends F> arguments)
            throws InterruptedException {
        final ExceptionWrapper<RuntimeException> exceptionWrapper = new ExceptionWrapper<>(null);
        ResultExecutions<S> result = new ResultExecutions<S>(arguments.size());
        for (int cur = 0; cur < arguments.size(); ++cur) {
            final int finalCur = cur;
            Runnable runnable = () -> {
                try {
                    result.put(mapFunction.apply(arguments.get(finalCur)), finalCur);
                } catch (final RuntimeException exc) {
                    if (exceptionWrapper.getException() != null) {
                        exceptionWrapper.addException(exc);
                    } else {
                        exceptionWrapper.setException(exc);
                    }
                }
            };
            queue.offer(runnable);
        }
        checkErrors(exceptionWrapper);
        return result.getExecutions();
    }


    private static class ExceptionWrapper<E extends RuntimeException> {
        private E exception;

        public ExceptionWrapper(E exception) {
            this.exception = exception;
        }

        public ExceptionWrapper() {
            this.exception = null;
        }

        public E getException() {
            return exception;
        }

        public void setException(final E exception) {
            this.exception = exception;
        }

        public void addException(E exception) {
            this.exception.addSuppressed(exception);
        }

        @Override
        public String toString() {
            return "ExceptionWrapper{" +
                    "exception=" + exception +
                    '}';
        }

    }


    private void checkErrors(ExceptionWrapper<RuntimeException> exceptionWrapper) {
        if (exceptionWrapper.getException() != null) {
            throw exceptionWrapper.getException();
        }
    }

    private void stopThread(Thread execution) {
        while (true) {
            try {
                execution.join();
                break;
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Constructor
     *
     * @param numberOfThreads threads number
     */
    public ParallelMapperImpl(final int numberOfThreads) {
        if (numberOfThreads < 1) {
            throw new IllegalArgumentException("Threads number of threads should be > 0!");
        }
        this.queue = new ExecutionQueue<>();
        Function<Integer, Thread> runnable = (cur) -> new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    Runnable run = queue.remove();
                    run.run();
                }
            } catch (final InterruptedException ignored) {

            } finally {
                Thread.currentThread().interrupt();
            }
        }
        );
        this.executions = ThreadUtils.threadsCollecting(numberOfThreads, runnable);
    }

    private static class ResultExecutions<S> {
        private int numberOfWorks;
        private final List<S> result;

        public ResultExecutions(int number) {
            result = new ArrayList<>(Collections.nCopies(number, null));
            this.numberOfWorks = number;
        }

        public synchronized List<S> getExecutions() throws InterruptedException {
            //--numberOfWorks;
            while (numberOfWorks > 0) {
                wait();
            }
            return result;
        }

        public ResultExecutions() {
            result = new ArrayList<>();
            this.numberOfWorks = 0;
        }

        public synchronized void put(final S work, final int index) {
            result.set(index, work);
            --numberOfWorks;
            if (numberOfWorks == 0) {
                notify();
            }
        }

        public synchronized int size() {
            return result.size();
        }

    }

}
