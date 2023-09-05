package info.kgeorgiy.ja.kadochnikova.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public class IterativeParallelism implements ScalarIP {
    private ParallelMapper mapper = null;
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    public IterativeParallelism() {}

    private <T, E> E streams(int threads, List<? extends T> values,
                             Function<List<? extends T>, ? extends E> thread,
                             Function<Stream<? extends E>, ? extends E> finished) throws InterruptedException {
        if (mapper == null) {
            if (threads > values.size()) {
                threads = values.size();
            }
            int size = (values.size() + threads - 1) / threads;
            int block = (values.size() + size - 1) / size;
            final List<List<? extends T>> keep = new ArrayList<>(Collections.nCopies(block, null));
            List<Thread> streamFirst = new ArrayList<>(threads);

            for (int i = 0, j = 0; j < block && i < values.size(); i += size, j++) {
                keep.set(j, values.subList(i, Integer.min(values.size(), i + size)));
            }
            List<E> res = new ArrayList<>(Collections.nCopies(keep.size(), null));

            for (int i = 0; i < block; i++) {
                final int ind = i;
                Thread worker = new Thread(() -> {
                    E result = thread.apply(keep.get(ind));
                    res.set(ind, result);
                });
                worker.start();
                streamFirst.add(worker);
            }

            for (Thread worker : streamFirst) {
                while (true) {
                    try {
                        worker.join();
                        break;
                    } catch (final InterruptedException e) {
                        worker.interrupt();
                    }
                }
            }
            return finished.apply(res.stream());
        } else {
            if (threads > values.size()) {
                threads = values.size();
            }
            int size = (values.size() + threads - 1) / threads;
            int block = (values.size() + size - 1) / size;

            final List<List<? extends T>> keep = new ArrayList<>(Collections.nCopies(block, null));

            for (int i = 0, j = 0; j < block && i < values.size(); i += size, j++) {
                keep.set(j, values.subList(i, Integer.min(values.size(), i + size)));
            }
            return finished.apply(mapper.map(thread, keep).stream());
        }
    }

    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return streams(threads, values, stream -> stream.stream().max(comparator).orElse(null), stream -> stream.max(comparator).orElse(null));
    }

    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return streams(threads, values, stream -> stream.stream().allMatch(predicate), stream -> stream.allMatch(Boolean::booleanValue));
    }

    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return streams(threads, values, stream -> (int) stream.stream().filter(predicate).count(), stream -> stream.mapToInt(Integer::intValue).sum());
    }
}
