package info.kgeorgiy.ja.kozlov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.HelloServer;
import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class CommonMethods {

    public static final String SERVER_SERVICE_NAME = "Server Service";
    public final static String HELLO_STRING = "Hello, ";

    final public static byte[] DEFAULT_BUFFER = new byte[0];

    public static void checkArguments(String[] args) {
        Objects.requireNonNull(args);
        for (String argument : args) {
            if (argument == null) {
                throw new IllegalArgumentException("Argument can't be null!");
            }
        }
    }


    public static void terminated(final ExecutorService currentService, final String serviceName) {
        currentService.shutdown();
        try {
            final boolean f = currentService.awaitTermination(64, TimeUnit.SECONDS);
            if (!f) {
                currentService.shutdownNow();
                if (!currentService.awaitTermination(64, TimeUnit.SECONDS)) {
                    System.err.println(String.join(" ", serviceName, "don't terminated!"));
                }
            }
        } catch (final InterruptedException exception) {
            currentService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void mainServer(HelloServer server, final String[] args) {
        CommonMethods.checkArguments(args);
        int threadsNumber;
        int currentPort;
        try {
            threadsNumber = Integer.parseInt(args[1]);
            currentPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            System.err.println(exception.getMessage());
            return;
        }
        server.start(currentPort, threadsNumber);
    }

    public static void prepareRequest(String requestString, final DatagramSocket socket, final DatagramPacket request,
                                   final SocketAddress inetSocketAddress) throws IOException {
        request.setData(requestString.getBytes(StandardCharsets.UTF_8));
        request.setLength(requestString.length());
    }

    public static <R extends Closeable> void close(R element, String name) {
        try {
            element.close();
        } catch (IOException exception) {
            System.err.printf("Can't close %s", name);
        }
    }

    public static String getPacketString(String prefix, DatagramPacket packet) {
        return String.join("", prefix, new String(packet.getData(), packet.getOffset(),
                packet.getLength(), StandardCharsets.UTF_8));
    }

    public static void submitTasks(int threads, final IntFunction<Runnable> runnable, final ExecutorService threadService) {
        IntStream.range(0, threads).forEach(currentThread -> {
            threadService.submit(runnable.apply(currentThread));
        });
    }

    public static DatagramPacket createFixedPacket(int size) {
        return new DatagramPacket(new byte[size], size);
    }

    public static void mainClient(final HelloClient client, final String[] args) {
        CommonMethods.checkArguments(args);
        if (args.length != 5) {
            throw new IllegalArgumentException("Arguments can't be null!");
        }        int threadsNumber;
        int currentPort;
        int numberRequestOnPort;
        try {
            threadsNumber = Integer.parseInt(args[3]);
            currentPort = Integer.parseInt(args[1]);
            numberRequestOnPort = Integer.parseInt(args[4]);
        } catch (NumberFormatException exception) {
            System.err.println(exception.getMessage());
            return;
        }
        final String currentHost = args[0];
        final String prefixRequest = args[2];
        client.run(currentHost, currentPort, prefixRequest, threadsNumber, numberRequestOnPort);
    }

    public synchronized <E> E getBufferInput(final Deque<E> queue, int selectionKeyData, SelectionKey selectionKey) {
        E buffer = queue.removeFirst();
        if (queue.isEmpty()) {
            selectionKey.interestOps(~selectionKeyData);
        }
        return buffer;
    }

}
