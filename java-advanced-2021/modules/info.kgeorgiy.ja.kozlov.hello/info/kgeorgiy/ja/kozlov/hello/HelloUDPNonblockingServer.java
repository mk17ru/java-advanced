package info.kgeorgiy.ja.kozlov.hello;


import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import info.kgeorgiy.java.advanced.hello.HelloServer;


public class HelloUDPNonblockingServer implements HelloServer {

    private ExecutorService service;

    private Selector selector;
    private static final String SERVER_SELECTOR_NAME = "Server Selector";
    private static final String SERVER_CHANNEL_NAME = "Server Channel";
    private DatagramChannel datagramChannel;
    private Deque<ByteBuffer> buffers;
    private final Deque<DatagramPacket> writeReady;

    public HelloUDPNonblockingServer() {
        this.writeReady = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void close() {
        CommonMethods.close(datagramChannel, SERVER_CHANNEL_NAME);
        CommonMethods.close(selector, SERVER_SELECTOR_NAME);
        CommonMethods.terminated(service, CommonMethods.SERVER_SERVICE_NAME);
    }

    @Override
    public void start(int currentPort, int threadsNumber) {
        try {
            datagramChannel = DatagramChannel.open();
            selector = Selector.open();
            datagramChannel.configureBlocking(false);
            datagramChannel.register(selector, SelectionKey.OP_READ);
            datagramChannel.bind(new InetSocketAddress(currentPort));
//            InetSocketAddress inetSocketAddress = new InetSocketAddress(currentPort);
        } catch (IOException exception) {
            close();
            System.err.println("Start Error" + exception.getMessage());
            return;
        }
        service = Executors.newSingleThreadExecutor();
        int bufferSize;
        try {
            bufferSize = datagramChannel.socket().getReceiveBufferSize();
        } catch (SocketException exception) {
            System.err.println("Error with buffer size" + exception.getMessage());
            return;
        }
        buffers = Stream.generate(() -> ByteBuffer.allocate(bufferSize)).limit(threadsNumber).collect(Collectors.toCollection(ConcurrentLinkedDeque::new));

        service.submit(() -> {
            while (true) {
                try {
                    selector.select();
                    for (final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext();) {
                        final SelectionKey selectionKey = iterator.next();
                        try {
                            if (selectionKey.isValid() && selectionKey.isWritable()) {
                                boolean isWrite = write(selectionKey, bufferSize);
                                if (isWrite) {
                                    selectionKey.interestOps(SelectionKey.OP_READ);
                                }
                            } else if (selectionKey.isValid() && selectionKey.isReadable()) {
                                read(selectionKey);
                            }
                        } finally {
                            iterator.remove();
                        }
                    }
                } catch (IOException exception) {
                    System.err.println(exception.getMessage());
                    close();
                    return;
                }
            }

        });
    }

    // TODO need to remove first two rows
    private void read(final SelectionKey selectionKey) {
        DatagramChannel datagramChannel = (DatagramChannel) selectionKey.channel();
        int readByteMask = SelectionKey.OP_READ;
        if (buffers.isEmpty()) {
            selectionKey.interestOpsAnd(~readByteMask);
            return;
        }
        ByteBuffer buffer = buffers.remove();
        buffer.clear();
        SocketAddress socketAddress;
        try {
            if (datagramChannel.isOpen()) {
                socketAddress = datagramChannel.receive(buffer);
            } else {
                return;
            }
           buffer.flip();
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("Error with receive " + exception.getMessage());
            return;
        }
        String response = CommonMethods.HELLO_STRING + StandardCharsets.UTF_8.decode(buffer).toString();
        buffer.clear();
        buffer.put(response.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        selectionKey.interestOps(SelectionKey.OP_WRITE);
        writeReady.add(new DatagramPacket(response.getBytes(StandardCharsets.UTF_8), response.length(), socketAddress));
        buffers.add(buffer);
        /// TODO why?????
        selector.wakeup();
    }

    private boolean write(SelectionKey selectionKey, int bufferSize) {
        DatagramChannel datagramChannel = (DatagramChannel) selectionKey.channel();
        DatagramPacket datagramPacket = writeReady.remove();
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
            byteBuffer.clear();
            byteBuffer.put(datagramPacket.getData());
            byteBuffer.flip();
            synchronized (HelloUDPNonblockingServer.this) {
                datagramChannel.send(byteBuffer, datagramPacket.getSocketAddress());
                selectionKey.interestOps(SelectionKey.OP_WRITE);
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("Can't send request" + exception.getMessage());
            return false;
        }

    }

}
