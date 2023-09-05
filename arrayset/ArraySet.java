package info.kgeorgiy.ja.kadochnikova.arrayset;

import java.util.*;
public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {
    private final List<E> list;
    private final Comparator<? super E> comparator;
    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    private ArraySet(List<E> list, Comparator<? super E> comparator) {
        this.list = list;
        this.comparator = comparator;
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        this.comparator = comparator;
        SortedSet<E> sortedSet = new TreeSet<>(comparator);
        sortedSet.addAll(collection);
        list = new ArrayList<>(sortedSet);
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.get(0);
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.get(size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked") // :NOTE: don't use SuppressWarnings
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (fromElement == null || toElement == null) {
            throw new NullPointerException("fromElement == null or toElement == null");
        }
        if (comparator != null) {
            if (comparator.compare(fromElement, toElement) > 0) {
                throw new IllegalArgumentException("fromElement > toElement");
            }
        }
        if (comparator == null) {
            if (((Comparable<? super E>) fromElement).compareTo(toElement) > 0) {
                throw new IllegalArgumentException("fromElement > toElement");
            }
        }
        int index1 = Collections.binarySearch(list, fromElement, comparator);
        int index2 = Collections.binarySearch(list, toElement, comparator);
        if (index1 < 0) {
            index1 = -(index1 + 1);
        }
        if (index2 < 0) {
            index2 = -(index2 + 1);
        }
        return new ArraySet<>(list.subList(index1, index2), comparator);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        int index = Collections.binarySearch(list, toElement, comparator);
        if (index < 0) {
            index = -(index + 1);
        }
        return new ArraySet<>(list.subList(0, index), comparator);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        int index = Collections.binarySearch(list, fromElement, comparator);
        if (index < 0) {
            index = -(index + 1);
        }
        return new ArraySet<>(list.subList(index, list.size()), comparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        int index = Collections.binarySearch(list, (E)o, comparator);
        return index < size() && index >= 0;
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }
}
