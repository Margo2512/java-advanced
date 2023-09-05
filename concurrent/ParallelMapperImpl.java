package info.kgeorgiy.ja.kadochnikova.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final Queue<Runnable> tasksQueue;
    private final List<Thread> threads;

    public ParallelMapperImpl(int count) {
        this.threads = new ArrayList<>(count);
        this.tasksQueue = new ArrayDeque<>(count);

        for (int i = 0; i < count; i++) {
            Thread thread = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Runnable task;
                        synchronized (tasksQueue) {
                            while (tasksQueue.isEmpty()) {
                                tasksQueue.wait();
                            }
                            task = tasksQueue.poll();
                            tasksQueue.notify();
                        }
                        task.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            thread.start();
            threads.add(thread);
        }
    }

    private static class Results<T> {
        private final List<T> list;
        private final Map<Thread, RuntimeException> errors = new HashMap<>();
        private int cnt = 0;

        public Results(int size) {
            list = new ArrayList<>(Collections.nCopies(size, null));
        }

        public synchronized void set(int idx, T value) {
            list.set(idx, value);
            cnt++;
            notify();
        }

        private synchronized void appendError(Thread thread, RuntimeException e) {
            cnt++;
            errors.put(thread, e);
        }

        public synchronized List<T> toList() throws InterruptedException {
            while (cnt < list.size()) {
                wait();
            }
            for (RuntimeException e : errors.values()) {
                throw e;
            }
            return list;
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f,
                              List<? extends T> args) throws InterruptedException {
        Results<R> results = new Results<>(args.size());

        synchronized (tasksQueue) {
            for (int i = 0; i < args.size(); i++) {
                int finalI = i;
                tasksQueue.add(() -> {
                    R result;
                    try {
                        result = f.apply(args.get(finalI));
                    } catch (RuntimeException e) {
                        results.appendError(Thread.currentThread(), e);
                        return;
                    }
                    synchronized (results) {
                        results.set(finalI, result);
                        results.notify();
                    }
                });
                tasksQueue.notify();
            }
        }

        return results.toList();
    }

    @Override
    public void close() {
        for (Thread t : threads) {
            t.interrupt();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (final InterruptedException ignored) {
            }
        }
    }
}
