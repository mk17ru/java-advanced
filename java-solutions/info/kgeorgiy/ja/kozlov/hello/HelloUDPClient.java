package info.kgeorgiy.ja.kozlov.hello;


import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
//import static info.kgeorgiy.ja.kozlov.hello.CommonMethods.*;
import java.util.function.IntFunction;
import info.kgeorgiy.java.advanced.hello.HelloClient;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {

    final private static int TIMEOUT_TIME = 128;
    final private static String CLIENT_SERVICE = "Client Service";

    @Override
    public void run(String currentHost, int currentPort, final String currentPrefix, int threads, int requests) {
        final SocketAddress inetSocketAddress;
        try {
            inetSocketAddress = new InetSocketAddress(InetAddress.getByName(currentHost), currentPort);
        } catch (UnknownHostException exception) {
            System.err.println("Unknown host!");
            exception.printStackTrace();
            return;
        }
        ExecutorService threadService = Executors.newFixedThreadPool(threads);
        IntFunction<Runnable> runnableFunction = (currentThread) ->
                () -> {
                    try (final DatagramSocket socket = new DatagramSocket()) {
                        socket.setSoTimeout(TIMEOUT_TIME);
                        DatagramPacket response;
                        try {
                            response = CommonMethods.createFixedPacket(socket.getReceiveBufferSize());
                        } catch (SocketException exception) {
                            System.err.println("Response packet error " + exception.getMessage());
                            return;
                        }
                        byte[] buffer = CommonMethods.DEFAULT_BUFFER;
                        DatagramPacket request = new DatagramPacket(new byte[0], 0, 0, inetSocketAddress);
                        IntStream.range(0, requests).forEach(i -> {
                            final int ind = i;
                            final String requestString = String.format("%s%s%s%d", currentPrefix, currentThread, "_", ind);
                            try {
                                String pattern = String.join("",
                                        String.format("[^0-9]*%d[^0-9]*%d[^0-9]*", currentThread, i));
                                CommonMethods.prepareRequest(requestString, socket, request, inetSocketAddress);
                                while (true) {
                                    socket.send(request);
                                    try {
                                        socket.receive(response);
                                        final String getResponse = CommonMethods.getPacketString("", response);
                                        if (getResponse.matches(pattern)) {
                                            System.out.printf("Request %s, response %s", requestString, getResponse);
                                            break;
                                        }
                                    } catch (IOException exception) {
                                        System.err.println("Error in packet" + exception.getMessage());
                                    }
                                }
                            } catch (IOException exception) {
                                System.err.println();
                                exception.printStackTrace();
                            }
                        });
                    } catch (SocketException exception) {
                        System.err.println("Socket exception" + exception.getMessage());
                        exception.printStackTrace();
                    }
                };
        CommonMethods.submitTasks(threads, runnableFunction, threadService);
        CommonMethods.terminated(threadService, CLIENT_SERVICE);
    }


    public static void main(String[] args) {
        HelloClient helloClient = new HelloUDPClient();
        CommonMethods.mainClient(helloClient, args);
    }


}
