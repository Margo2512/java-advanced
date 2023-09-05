package info.kgeorgiy.ja.kadochnikova.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HelloUDPNonblockingClient implements HelloClient {
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(300);
            SocketAddress serverAddress = new InetSocketAddress(host, port);

            for (int i = 1; i <= threads; i++) {
                int threadNum = i;
                String finalPrefix = prefix;

                for (int j = 1; j <= requests; j++) {
                    String request = finalPrefix + threadNum + "_" + j;
                    byte[] requestData = request.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, serverAddress);
                    DatagramPacket responsePacket = new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());

                    while (true) {
                        try {
                            socket.send(requestPacket);
                            socket.receive(responsePacket);
                            String response = new String(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength(), StandardCharsets.UTF_8);
                            if (response.startsWith("Hello, " + request)) {
                                break;
                            }
                        } catch (SocketTimeoutException e) {
                            System.err.println(e.getMessage());
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Error with args");
            return;
        }
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String prefix = args[2];
            int threads = Integer.parseInt(args[3]);
            int requests = Integer.parseInt(args[4]);

            (new HelloUDPClient()).run(host, port, prefix, threads, requests);
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
        }
    }
}
