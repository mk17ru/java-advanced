package info.kgeorgiy.ja.kozlov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public class HelloUDPNonblockingClient implements HelloClient {

    private final static String SELECTOR_NAME = "Selector";
    private final static String CHANNEL_NAME = "Channel";
    private int threadsNumber;
    private int requests;
    private int alreadyExecute = 0;

    public static void main(String[] args) {
        HelloClient helloClient = new HelloUDPNonblockingClient();
        CommonMethods.mainClient(helloClient, args);
    }


    @Override
    public void run(String currentHost, int currentPort, String prefix, int threadsNumber, int requests) {
        final SocketAddress inetSocketAddress;
        try {
            inetSocketAddress = new InetSocketAddress(InetAddress.getByName(currentHost), currentPort);
        } catch (UnknownHostException exception) {
            System.err.println("Unknown host!");
            exception.printStackTrace();
            return;
        }
        final List<DatagramChannel> channelList = new ArrayList<>();
        Selector selector;
        try {
            selector = Selector.open();
        } catch (IOException exception) {
            System.err.println("Error open selector!");
            exception.printStackTrace();
            return;
        }
        IntStream.range(0, threadsNumber).forEach((i) -> {
            try {
                DatagramChannel datagramChannel = DatagramChannel.open();
                datagramChannel.configureBlocking(false);
                datagramChannel.bind(null);
                datagramChannel.register(selector, SelectionKey.OP_WRITE, new DataAttribute(i, datagramChannel, prefix));
                channelList.add(datagramChannel);
            } catch (IOException exception) {
                channelList.forEach(channel -> CommonMethods.close(channel, CHANNEL_NAME));
                System.err.println(exception.getMessage());
                CommonMethods.close(selector, SELECTOR_NAME);
                return;
            }
        });
        this.alreadyExecute = 0;
        this.requests = requests;
        this.threadsNumber = threadsNumber;
        while(alreadyExecute < threadsNumber) {
            try {
                selector.select(300);
                if (selector.selectedKeys().isEmpty()) {
                    selector.keys().forEach(i -> {
                        i.interestOps(SelectionKey.OP_WRITE);
                    });
                }
                for (final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext();) {
                    final SelectionKey selectionKey = iterator.next();
                    try {
                        if (selectionKey.isValid() && selectionKey.isWritable()) {
                            boolean isWrite = write(selectionKey, prefix, inetSocketAddress);
                            if (isWrite) {
                                selectionKey.interestOps(SelectionKey.OP_READ);
                            }
                        } else if (selectionKey.isValid() && selectionKey.isReadable()) {
                            read(channelList, selectionKey);
                        }
                    } finally {
                        iterator.remove();
                    }
                }
            } catch (IOException exception) {
                System.err.println("Selector error" + exception.getMessage());
            }
        }
    }

    private boolean write(final SelectionKey selectionKey, String currentPrefix, SocketAddress inetSocketAddress) {
        final DataAttribute dataAttribute = (DataAttribute) selectionKey.attachment();
        DatagramChannel datagramChannel = (DatagramChannel) selectionKey.channel();
        ByteBuffer byteBuffer = dataAttribute.getByteBuffer();
        byteBuffer.clear();
        int currentThread = dataAttribute.getThread();
        final int curRequest =  dataAttribute.getCurRequest();
        final String requestString = String.format("%s%s%s%d", currentPrefix, currentThread, "_", curRequest);
        byteBuffer.put(requestString.getBytes(StandardCharsets.UTF_8));
        byteBuffer.flip();
        try {
            final int getBytes = datagramChannel.send(byteBuffer, inetSocketAddress);
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("Can't send request" + exception.getMessage());
            return false;
        }
    }

    // TODO need to remove first two rows
    private void read(List<DatagramChannel> channelList, final SelectionKey selectionKey) {
        final DataAttribute dataAttribute = (DataAttribute) selectionKey.attachment();
        DatagramChannel datagramChannel = (DatagramChannel) selectionKey.channel();

        int currentThread = dataAttribute.getThread();
        final int curRequest = dataAttribute.getCurRequest();
        SocketAddress socketAddress;
        ByteBuffer byteBuffer = dataAttribute.getByteBuffer();
        byteBuffer.clear();
        try {
            socketAddress = datagramChannel.receive(byteBuffer);
            byteBuffer.flip();
        } catch (IOException exception) {
            System.err.println("Receive error" + exception.getMessage());
            return;
        }
        String pattern = String.join("",
                String.format("[^0-9]*%d[^0-9]*%d[^0-9]*", currentThread, curRequest));
        String response = StandardCharsets.UTF_8.decode(byteBuffer).toString();
        if (response.matches(pattern)) {
            channelList.remove(datagramChannel);
            selectionKey.interestOps(SelectionKey.OP_WRITE);
            dataAttribute.nextRequest();
            if (requests == dataAttribute.getCurRequest()) {
                CommonMethods.close(datagramChannel, CHANNEL_NAME);
                alreadyExecute++;
            }
        }
    }

}
