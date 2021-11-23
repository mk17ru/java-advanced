package info.kgeorgiy.ja.kozlov.hello;


import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import info.kgeorgiy.java.advanced.hello.HelloServer;


public class HelloUDPNonblockingServer implements HelloServer {

    private ExecutorService service;
    private ExecutorService threads;

    private Selector selector;
    private static final String SERVER_SELECTOR_NAME = "Server Selector";
    private static final String SERVER_CHANNEL_NAME = "Server Channel";
    private DatagramChannel datagramChannel;
    private Set<ByteBuffer> buffers;
    private final Deque<DatagramPacket> writeReady;

    public HelloUDPNonblockingServer() {
        this.writeReady = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void close() {
        closeAll();
        terminateAll();
    }

    private void terminateAll() {
        CommonMethods.terminated(threads, CommonMethods.SERVER_SERVICE_NAME);
        CommonMethods.terminated(service, CommonMethods.SERVER_SERVICE_NAME);
    }

    @Override
    public void start(int currentPort, int threadsNumber) {
        threads = Executors.newFixedThreadPool(threadsNumber);
        service = Executors.newSingleThreadExecutor();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(currentPort);
        int bufferSize;
        try {
            selector = Selector.open();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        try {
            datagramChannel = CommonMethods.createAndConfigureNewDatagramChannel(selector, new ServerDataAttribute(threadsNumber, datagramChannel),
                    SelectionKey.OP_READ, new InetSocketAddress(currentPort));
            bufferSize = datagramChannel.socket().getReceiveBufferSize();
        } catch (SocketException exception) {
            System.err.println("Error with buffer size" + exception.getMessage());
            close();
            return;
        }
        buffers = Stream.generate(() -> ByteBuffer.allocate(bufferSize)).limit(threadsNumber)
                .collect(Collectors.toCollection(() -> Collections.newSetFromMap(new ConcurrentHashMap<>())));

        service.submit(() -> {
            while (selector.isOpen() && !Thread.currentThread().isInterrupted()) {
                try {
                    final int number = selector.select();
//                    if (number == 0) {
//                        for (SelectionKey key : selector.keys()) {
//                            if (!key.isReadable()) {
//                                key.interestOps(SelectionKey.OP_READ);
//                            }
//                        }
//                    }
                    CommonMethods.iterate(selector, this::write, this::read);
                } catch (IOException exception) {
                    exception.printStackTrace();
                    System.err.println(exception.getMessage());
                    close();
                    return;
                }
            }

        });
    }

    private void closeAll() {
        CommonMethods.close(datagramChannel, SERVER_CHANNEL_NAME);
        CommonMethods.close(selector, SERVER_SELECTOR_NAME);
    }

    private boolean read(final SelectionKey selectionKey) {
        boolean isRead = selectionKey.isReadable();
        DatagramChannel datagramChannel = CommonMethods.getDatagramChannel(selectionKey, buffers);
        if (isRead) {
            selectionKey.interestOpsAnd(~SelectionKey.OP_READ);
        }
        if (datagramChannel == null) {
            return false;
        }
        ByteBuffer buffer = buffers.stream().findAny().get();
        buffers.remove(buffer);
        buffer.clear();
        SocketAddress socketAddress;
        try {
            socketAddress = datagramChannel.receive(buffer);
            if (socketAddress == null) {
                return false;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("Error with receive " + exception.getMessage());
            return true;
        } finally {
            buffer.flip();
        }
        threads.submit(() -> {
            String response = CommonMethods.HELLO_STRING + StandardCharsets.UTF_8.decode(buffer).toString();
            buffer.clear();
//            buffer.put(response.getBytes(StandardCharsets.UTF_8));
            buffer.flip();
            writeReady.add(new DatagramPacket(response.getBytes(StandardCharsets.UTF_8), response.length(), socketAddress));
            buffers.add(buffer);
            selectionKey.interestOpsOr(SelectionKey.OP_WRITE);
            selector.wakeup();
        });
        return true;
    }

    private boolean write(SelectionKey selectionKey) {
        DatagramChannel datagramChannel = CommonMethods.getDatagramChannel(selectionKey, writeReady);
        if (datagramChannel == null) {
            return false;
        }
        DatagramPacket datagramPacket = writeReady.poll();
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(datagramChannel.socket().getReceiveBufferSize());
            CommonMethods.bufferOperations(byteBuffer, datagramPacket.getData());
            final int getBytes = datagramChannel.send(byteBuffer, datagramPacket.getSocketAddress());
            selectionKey.interestOpsOr(SelectionKey.OP_WRITE);
            selectionKey.interestOpsOr(SelectionKey.OP_READ);
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("Can't send request" + exception.getMessage());
            return false;
        }

    }


}