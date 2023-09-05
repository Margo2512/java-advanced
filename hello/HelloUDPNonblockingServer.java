package info.kgeorgiy.ja.kadochnikova.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;


public class HelloUDPNonblockingServer implements HelloServer {
    private Selector selector;
    private DatagramChannel channel;
    private Queue<SocketAddress> clientAddresses;
    private List<String> responses;
    protected ExecutorService pool;

    private Queue<ToWrite> toWriteQueue = new ConcurrentLinkedQueue<>();

    private static class ToWrite {
        public SocketAddress address;
        public String message;

        public ToWrite(SocketAddress address, String message) {
            this.address = address;
            this.message = message;
        }
    }

    protected static final long THREAD_TERMINATION_SECONDS = Long.MAX_VALUE;

    protected String helloResponse(final String s) {
        return "Hello, ".concat(s);
    }

    @Override
    public void start(final int port, final int threads) {
        try {
            selector = Selector.open();
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(port));
            channel.register(selector, SelectionKey.OP_READ);
            pool = Executors.newFixedThreadPool(threads);
            clientAddresses = new ConcurrentLinkedQueue<>();
            responses = new ArrayList<>();

            Executors.newSingleThreadExecutor().submit(() -> {
                while (channel.isOpen()) {
                    try {
                        selector.select();
                        var key = selector.selectedKeys().iterator().next();
                        if (key.isValid()) {
                            System.out.println("ASD");
                            if (key.isWritable() && !toWriteQueue.isEmpty()) {
                                System.out.println("12");
                                var nxt = toWriteQueue.poll();
                                channel.send(ByteBuffer.wrap(nxt.message.getBytes()), nxt.address);
                            } else if (key.isReadable()) {
                                System.out.println("34");
                                ByteBuffer buffer = ByteBuffer.allocate(1024);
                                channel.receive(buffer);
                                System.out.println("56");
                                String message = StandardCharsets.UTF_8.decode(buffer).toString();
                                System.out.println(message);
                                pool.submit(() -> {
                                    try {
                                    toWriteQueue.add(new ToWrite(
                                            channel.getRemoteAddress(),
                                            "Hello, " + message
                                    ));
                                    } catch (Exception e) {

                                    }
                                });
                            }
                        }
                    } catch (final IOException e) {
                        System.err.printf("Selector I/O exception: %s%n", e.getLocalizedMessage());
                        close();
                    } catch (final ClosedSelectorException e) {
                        System.err.printf("Selector closed: %s%n", e.getLocalizedMessage());
                    }
                }
            });
        } catch (final IOException e) {
            System.err.printf("Server setup error: %s%n On port: %d%n", e.getLocalizedMessage(), port);
        }
    }

    @Override
    public void close() {
        try {
            channel.close();
            selector.close();
            pool.shutdown();
            if (!pool.awaitTermination(THREAD_TERMINATION_SECONDS, TimeUnit.SECONDS)) {
                System.err.println("Timeout for thread pool");
            }
        } catch (final IOException | InterruptedException e) {
            System.err.printf("Resources close exception: %s%n", e.getLocalizedMessage());
        }
    }


    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("Error with args");
            return;
        }

        try {
            int port = Integer.parseInt(args[0]);
            int threads = Integer.parseInt(args[1]);

            (new HelloUDPNonblockingServer()).start(port, threads);
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
        }
    }
}
