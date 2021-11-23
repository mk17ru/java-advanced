package info.kgeorgiy.ja.kozlov.concurrent;


import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;
import java.util.function.Predicate;
import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

public class IterativeParallelism implements AdvancedIP {

    private final ParallelMapper parallelMapper;

    /**
     * Reduces values using monoid
     * @param threadsNumber nuber of threads
     * @param values values to reduce.
     * @param monoid monoid to use.
     *
     * @param <S> type
     * @throws InterruptedException exception
     */
    @Override
    public <S> S reduce(int threadsNumber, List<S> values, Monoid<S> monoid) throws InterruptedException {
        return reducer(Function.identity(), values, threadsNumber, monoid);
    }

    private <S> Function<Stream<S>, S> monoidApplyer(Monoid<S> currentMonoid) {
        return group -> group.reduce(currentMonoid.getIdentity(), currentMonoid.getOperator());
    }

    /**
     * Returns whether all of values satisfies predicate.
     * @param threadsNumber threads
     * @param values values to test.
     * @param predicate test predicate.
     * @param <T> type
     * @return whether any of values satisfies predicate.
     * @throws InterruptedException exception
     */
    @Override
    public <T> boolean all(final int threadsNumber, List<? extends T> values, final Predicate<? super T> predicate)
            throws InterruptedException {
        return execute(group -> group.allMatch(Boolean::booleanValue),
                group -> group.allMatch(predicate), values, threadsNumber);
    }

    /**
     * Join values to string.
     *
     * @param threadsNumber number of concurrent threads.
     * @param vals values to join.
     *
     * @return list of joined result of {@link #toString()} call on each value.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public String join(int threadsNumber, List<?> vals) throws InterruptedException {
        return execute(groups -> groups.collect(Collectors.joining()),
                groups -> groups.map(Object::toString).collect(Collectors.joining()), vals, threadsNumber);
    }

    private <T> List<T> collectGroupsToList(Stream<? extends List<T>> group) {
        return ThreadUtils.collectList(group.flatMap(Collection::stream));
    }

    /**
     * Maximum value
     * @param threadsNumber threads
     * @param values values to get maximum of.
     * @param comparator value comparator.
     * @param <T> typr
     * @throws InterruptedException exception
     */
    @Override
    public <T> T maximum(final int threadsNumber, List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return minimum(threadsNumber, values, Collections.reverseOrder(comparator));
    }

    private <S, F, E> S execute(final Function<? super Stream<S>, S> accum,
                                final Function<? super Stream<? extends F>, S> execute,
                                final List<? extends F> vals,
                                int threadsNumber) throws InterruptedException {
        if ((threadsNumber = Math.min(vals.size(), threadsNumber)) < 1) {
            throw new IllegalArgumentException("Threads number of threads should be > 0!");
        }
        final int remainder = vals.size() % threadsNumber;
        final int groupLength = vals.size() / threadsNumber;
        if (parallelMapper == null) {
            final List<S> result = new ArrayList<S>(Collections.nCopies(threadsNumber, null));
//        if (remainder != 0) {
//            groupLength++;
//        }
            int numberOfTask = 0;
            int currentGroup = 0;
            List<Thread> executions = createThreads(remainder, groupLength,
                    result, threadsNumber, execute, vals);
            exceptionChecker(workTryJoin(executions));
            return accum.apply(result.stream());
        } else {
            List<Stream<? extends F>> maps = generateMaps(threadsNumber, remainder, groupLength, vals);
            Stream<S> list = parallelMapper.map(execute, maps).stream();
            return accum.apply(list);
        }
    }

    private <F> List<Stream<? extends F>> generateMaps(final int threadsNumber, final int remainder,
                                                       final int groupLength, final java.util.List<? extends F> elements) {
        ArrayList<Stream<? extends F>> list = new ArrayList<>();
        return ThreadUtils.collectList(IntStream.range(0, threadsNumber).mapToObj((curGroup) ->
                getSubList(curGroup, groupLength, remainder, elements)
        ));
    }

    /**
     * Constructor
     * @param parallelMapper mapper
     */
    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    /**
     *
     */
    public IterativeParallelism() {
        this.parallelMapper = null;
    }

//    private <S, E> S executeWorks(final int numberOfTask, final int groupLength,
//                                  final List<E> vals, final Function<? super Stream<E>, S> execute) {
//
//        return ;
//    }

    //    private <S> void addThreads(final List<S> result, final int currentGroup, final List<Thread> works, S executeWork) {
    //
    //    }

    private<S, F> List<Thread> createThreads( int remainder, final int groupLength,
                                              final List<S> result, final int threadsNumber,
                                              final Function<? super Stream<? extends F>, S> execute,
                                              final List<? extends F> elements) {
        return ThreadUtils.threadsCollecting(threadsNumber, (curGroup) -> new Thread(() -> {
            result.set(curGroup,
                    execute.apply(getSubList(curGroup, groupLength, remainder, elements)));
        }));
    }

    private<F> Stream<? extends F> getSubList(final int curGroup, int groupLength, final int remainder,
                                              final List<? extends F> elements) {
        final int begin = curGroup * groupLength + Math.min(remainder, curGroup);
        return elements.subList(begin,
                Math.min(begin + groupLength + (curGroup < remainder ? 1 : 0), elements.size()))
                .stream();
    }

    private <S> void checkValues(final List<? extends S> vals) {
        if (vals == null || vals.isEmpty()) {
            throw new IllegalArgumentException("Values shouldn't be empty!");
        }
    }

    private InterruptedException workTryJoin(List<Thread> works) throws InterruptedException {
        InterruptedException currentInterruptedException = null;
        for (int j = 0; j < works.size(); ++j) {
            try {
                works.get(j).join();
            } catch (InterruptedException exception) {
                currentInterruptedException = exception;
                currentInterruptedException.addSuppressed(worksInterrupt(works, j));
                exception.printStackTrace();
                System.err.println(currentInterruptedException.getMessage());
            }
        }
        return currentInterruptedException;
    }

    private void exceptionChecker(InterruptedException currentInterruptedException) throws InterruptedException {
        if (currentInterruptedException != null) {
            throw currentInterruptedException;
        }
    }

    /**
     * Filters values by predicate.
     *
     * @param threadsNumber number of concurrent threads.
     * @param values values to filter.
     * @param predicate filter predicate.
     *
     * @return list of values satisfying given predicated. Order of values is preserved.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> List<T> filter(int threadsNumber, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return execute(this::collectGroupsToList, group -> group.filter(predicate).collect(Collectors.toList()),
                values, threadsNumber);
    }


    /**
     * Maps and reduces values using monoid.
     * @param threadsNumber threads
     * @param values values to reduce.
     * @param lift mapping function.
     * @param monoid monoid to use.
     *
     * @return values reduced by provided monoid or {@link Monoid#getIdentity() identity} if not values specified.
     * @throws InterruptedException exception
     */
    @Override
    public <T, S> S mapReduce(int threadsNumber, List<T> values, Function<T, S> lift, Monoid<S> monoid)
            throws InterruptedException {
        return reducer(lift, values, threadsNumber, monoid);
    }


    /**
     * Reduces values using monoid.
     *
     * @param threadsNumber number of concurrent threads.
     * @param monoid monoid to use.
     * @param vals values to reduce.
     *
     * @return values reduced by provided monoid or {@link Monoid#getIdentity() identity} if not values specified.
     *
     */
    private <T, S> S reducer(final Function<T, S> lift,final List<T> vals,
                             final int threadsNumber,  final Monoid<S> monoid) throws InterruptedException {
        final Function<Stream<S>, S> monoidApplier = monoidApplyer(monoid);
        return execute(monoidApplier, group -> monoidApplier.apply(group.map(lift)), vals, threadsNumber);
    }

    /**
     * Maps values.
     *
     * @param threadsNumber number of concurrent threads.
     * @param values values to filter.
     * @param mapFunction mapper function.
     *
     * @return list of values mapped by given function.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <S, T> List<T> map(int threadsNumber, List<? extends S> values, Function<? super S, ? extends T> mapFunction)
            throws InterruptedException {
        return execute(this::collectGroupsToList,
                group -> ThreadUtils.collectList(group.map(mapFunction)), values, threadsNumber);
    }

    /**
     * minimum value.
     * @param threadsNumber threads
     * @param values values to get minimum of.
     * @param comparator value comparator.
     * @throws InterruptedException exception
     */
    @Override
    public <S> S minimum(final int threadsNumber, List<? extends S> values, final Comparator<? super S> comparator)
            throws InterruptedException {
        checkValues(values);
        Function<? super Stream<? extends S>, S> minStream = (groups) -> groups.min(comparator).orElse(null);
        return execute(minStream, minStream, values, threadsNumber);
    }

    private Exception worksInterrupt(final List<Thread> exercises, final int startIndex) throws InterruptedException {
        Exception fullExceptions = new Exception();
        for (int cur = startIndex; cur < exercises.size(); ++cur) {
            exercises.get(cur).interrupt();
        }
        IntStream.range(startIndex + 1, exercises.size()).forEach((cur) -> {
                    try {
                        exercises.get(cur).join();
                    } catch (InterruptedException exception) {
                        fullExceptions.addSuppressed(exception);
                    }
                }
        );
        return fullExceptions;
    }

    /**
     * Returns whether any of values satisfies predicate.
     * @param threadsNumber threads
     * @param values values to test.
     * @param predicate test predicate.
     * @param <T> type
     * @return whether any of values satisfies predicate.
     * @throws InterruptedException exception
     */
    @Override
    public <T> boolean any(final int threadsNumber, List<? extends T> values, final Predicate<? super T> predicate)
            throws InterruptedException {
        return !all(threadsNumber, values, predicate.negate());
    }


}

