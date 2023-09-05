package info.kgeorgiy.ja.kadochnikova.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class HelloUDPServer implements HelloServer {

    private volatile boolean run;
    private DatagramSocket serverSocket;
    private int numThreads;
    List<Thread> threadList = new ArrayList<>();

    @Override
    public void start(int port, int threads) {
        numThreads = threads;
        try {
            serverSocket = new DatagramSocket(port);
            run = true;
            for (int i = 0; i < numThreads; i++) {
                Thread thread = new Thread(() -> {
                    while (run) {
                        try {
                            byte[] buffer = new byte[1024];
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            serverSocket.receive(packet);
                            String request = new String(packet.getData(), 0, packet.getLength());
                            String response = "Hello, " + request;
                            serverSocket.send(new DatagramPacket(response.getBytes(), response.length(), packet.getAddress(), packet.getPort()));
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                    }
                });

                threadList.add(thread);
                thread.start();
            }

        } catch (SocketException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void close() {
        run = false;
        serverSocket.close();
        threadList.forEach(thread -> {
            thread.interrupt();
            while (true) {
                try {
                    thread.join();
                    break;
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
        });
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("Error with args");
            return;
        }
        int port = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);

        (new HelloUDPServer()).start(port, threads);
    }
}