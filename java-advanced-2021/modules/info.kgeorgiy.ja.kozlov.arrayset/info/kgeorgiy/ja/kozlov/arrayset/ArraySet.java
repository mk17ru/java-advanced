package info.kgeorgiy.ja.kozlov.arrayset;

import java.util.*;

public class ArraySet<S> extends AbstractSet<S> implements NavigableSet<S> {

    private final List<S> objs;

    private final Comparator<? super S> comparator;

    public ArraySet(final Collection<? extends S> collection, final Comparator<? super S> comparator) {
        Set<S> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        this.objs = new ArrayList<>(treeSet);
        this.comparator = comparator;
    }

    private ArraySet(final MyDescendingList<S> collection, final Comparator<? super S> comparator) {
        this.objs = collection;
        this.comparator = comparator;
    }

    public ArraySet(final SortedSet<S> collection, final Comparator<? super S> comparator) {
        this.objs = List.copyOf(collection);
        this.comparator = comparator;
    }

    public ArraySet(final SortedSet<S> collection) {
        this.objs = List.copyOf(collection);
        this.comparator = collection.comparator();
    }

    public ArraySet(final Collection<? extends S> collection) {
        this(collection, null);
    }


    public ArraySet(Comparator<? super S> comparator) {
        this(Collections.emptyList(), comparator);
    }

    public ArraySet() {
        this(Collections.emptyList());
    }

    private ArraySet(final List<S> collection, Comparator<? super S> comparator) {
        this.objs = Collections.unmodifiableList(collection);
        this.comparator = comparator;
    }

    private void verifyContent() {
        if (objs == null || objs.isEmpty()) {
            throw new NoSuchElementException();
        }
    }

    private S getObjectByIndex(final int index) {
        if (verifyIndex(index)) {
            return objs.get(index);
        } else {
            return null;
        }
    }

    private int findIndex(final S element) {
        return Collections.binarySearch(objs, element, comparator);
    }


    private int evalIndex(final int changeIfFind, final int changeIfNotFind, final S e) {
        int ind = findIndex(e);
        if (ind >= 0) {
            return ind + changeIfFind;
        } else {
            return -ind - 1 - changeIfNotFind;
        }
    }

    @Override
    public S higher(S e) {
        return getObjectByIndex(evalIndex(1, 0, e));
    }

    @Override
    public S lower(S e) {
        return getObjectByIndex(evalIndex(-1, 1, e));
    }

    @Override
    public Comparator<? super S> comparator() {
        return comparator;
    }

    @Override
    public int size() {
        return objs.size();
    }

    @Override
    public S floor(S element) {
        return getObjectByIndex(evalIndex(0, 1, element));
    }

    @Override
    public S ceiling(S e) {
        return getObjectByIndex(evalIndex(0, 0, e));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object object) {
        return Collections.binarySearch(objs, (S)object, comparator) >= 0;
    }

    @Override
    public Iterator<S> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<S> descendingSet() {
        return new ArraySet<>((new MyDescendingList<>(objs)).swapReverse(), Collections.reverseOrder(comparator));
    }

    private boolean verifyIndex(final int index) {
        return index < objs.size() && index >= 0;
    }

    private NavigableSet<S> newEmptySet() {
        return new ArraySet<>(Collections.emptyList(), comparator);
    }

    @Override
    public NavigableSet<S> subSet(S fromElement, boolean fromInclusive, S toElement, boolean toInclusive) {
        int leftIndex = evalIndex(fromInclusive ? 0 : 1, 0, fromElement);
        int rightIndex = evalIndex(toInclusive ? 0 : -1, 1, toElement);
        elementsOrder(fromElement, toElement);
        if (leftIndex <= rightIndex && verifyIndex(leftIndex) && verifyIndex(rightIndex)) {
            return new ArraySet<>(objs.subList(leftIndex, rightIndex + 1), comparator);
        }  else {
            return newEmptySet();
        }
    }

    @Override
    public NavigableSet<S> headSet(S toElement, boolean toInclusive) {
        int ind = findIndex(toElement);
        int rightIndex = (ind >= 0) ? ind + (!toInclusive ? -1 : 0) : -ind - 2;
        if (rightIndex < 0) {
            return newEmptySet();
        } else {
            return subSet(first(), true, toElement, toInclusive);
        }
    }

    @Override
    public NavigableSet<S> tailSet(S fromElement, boolean fromInclusive) {
        int ind = findIndex(fromElement);
        int leftIndex = (ind >= 0) ? ind + (!fromInclusive ? 1 : 0) : -ind - 1;
        //int leftIndex = evalIndex(fromInclusive ? 0 : 1, 0, fromElement);
        if (leftIndex >= size()) {
            return newEmptySet();
        } else {
            return subSet(fromElement, fromInclusive, last(), true);
        }
    }
    @SuppressWarnings("unchecked")
    private void elementsOrder(S el1, S el2) {
        if (comparator() != null) {
            if (comparator().compare(el1, el2) > 0) {
                throw new IllegalArgumentException();
            }
        } else {
            Comparable<S> element1, element2;
            if (el1 instanceof Comparable) {
                element1 = (Comparable<S>)el1;
            }
            if (el2 instanceof Comparable) {
                element2 = (Comparable<S>)el2;
            }

            if ((el1 instanceof Comparable) && (((Comparable<S>) el1).compareTo(el2) > 0)) {
                throw new IllegalArgumentException(el1 + " " + el2);
            }
        }
    }

    @Override
    public SortedSet<S> subSet(S fromElement, S toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public Iterator<S> iterator() {
        return Collections.unmodifiableList(objs).iterator();
    }

    @Override
    public S first() {
        verifyContent();
        return getObjectByIndex(0);
    }

    @Override
    public S last() {
        verifyContent();
        return getObjectByIndex(objs.size() - 1);
    }

    @Override
    public S pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public S pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<S> headSet(S toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<S> tailSet(S fromElement) {
        return tailSet(fromElement, true);
    }

}
