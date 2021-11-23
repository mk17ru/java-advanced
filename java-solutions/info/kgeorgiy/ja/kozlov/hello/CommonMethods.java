package info.kgeorgiy.ja.kozlov.hello;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import info.kgeorgiy.java.advanced.hello.HelloServer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.nio.channels.*;
import info.kgeorgiy.java.advanced.hello.HelloClient;
import java.util.function.*;
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
            final boolean f = currentService.awaitTermination(1024, TimeUnit.MILLISECONDS);
            if (!f) {
                currentService.shutdownNow();
                if (!currentService.awaitTermination(1024, TimeUnit.MILLISECONDS)) {
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

    public static void sendRequest(String requestString, final DatagramSocket socket, final DatagramPacket request,
                                   final SocketAddress inetSocketAddress) throws IOException {
        request.setData(requestString.getBytes(StandardCharsets.UTF_8));
        request.setLength(requestString.length());
        socket.send(request);
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
        String prefixRequest = args[2];
        client.run(currentHost, currentPort, prefixRequest, threadsNumber, numberRequestOnPort);
    }

    public static void iterate(final Selector selector, final Function<SelectionKey, Boolean> write,
                               Consumer<SelectionKey> read) {
        for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext();) {
            final SelectionKey selectionKey = iterator.next();

            try {
                if (!selectionKey.isValid()) {
                    continue;
                }
                if (selectionKey.isWritable()) {
                    write.apply(selectionKey);
                } else if (selectionKey.isReadable()) {
                    read.accept(selectionKey);
                }
            } finally {
                iterator.remove();
            }
        }
    }

    public static void trySelect(final Selector selector) {
        try {
//            final int number = ;
            if (selector.select(128) == 0) {
                for (SelectionKey key : selector.keys()) {
//                    if (!key.isWritable()) {
                    key.interestOps(SelectionKey.OP_WRITE);
//                    }
                }
            }
        } catch (IOException exception) {
            System.err.println("Selector error" + exception.getMessage());
        }
    }

    public static String getPattern(final int curRequest, final int currentThread) {
        return String.join("",
                String.format("[^0-9]*%d[^0-9]*%d[^0-9]*", currentThread, curRequest));
    }

    public static DatagramChannel createAndConfigureNewDatagramChannel(Selector selector, DataInterface data,
                                                                       int selectionKey, final InetSocketAddress inetSocket) {

        DatagramChannel datagramChannel;
        try {
            datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);
            datagramChannel.bind(inetSocket);
            data.initByDatagramChannel(datagramChannel);
            datagramChannel.register(selector, selectionKey, data);
            return datagramChannel;
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("Start Error" + exception.getMessage());
            //close();
            return null;
        }
    }


    public static void bufferOperations(final String data, ByteBuffer byteBuffer) {
        bufferOperations(byteBuffer, data.getBytes(StandardCharsets.UTF_8));
    }

    public static void bufferOperations(ByteBuffer byteBuffer, final byte[] data) {
        byteBuffer.clear();
        byteBuffer.put(data);
        byteBuffer.flip();
    }


    public static boolean commonReceive(SocketAddress socketAddress, DatagramChannel datagramChannel, ByteBuffer buffer) {
        buffer.clear();
        try {
            socketAddress = datagramChannel.receive(buffer);
            if (socketAddress == null) {
                return false;
            }
            buffer.flip();
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("Error with receive " + exception.getMessage());
            return false;
        }
        return true;
    }

    public static void bufferPrepareFlip(final ByteBuffer byteBuffer) {
        byteBuffer.clear();
        byteBuffer.flip();
    }

    public static<T> DatagramChannel getDatagramChannel(SelectionKey selectionKey, Collection<T> collection) {
        if (collection.isEmpty()) {
            selectionKey.interestOps(SelectionKey.OP_READ);
            return null;
        }
        return (DatagramChannel) selectionKey.channel();
    }
}