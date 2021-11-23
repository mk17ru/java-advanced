package info.kgeorgiy.ja.kozlov.hello;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.stream.IntStream;
import java.util.*;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import info.kgeorgiy.java.advanced.hello.HelloClient;
import java.nio.charset.StandardCharsets;

public class HelloUDPNonblockingClient implements HelloClient {

    private final static String SELECTOR_NAME = "Selector";
    private final static String CHANNEL_NAME = "Channel";
    private int threadsNumber;
    private int alreadyExecute = 0;

    public static void main(String[] args) {
        HelloClient helloClient = new HelloUDPNonblockingClient();
        CommonMethods.mainClient(helloClient, args);
    }


    @Override
    public void run(final String currentHost, int currentPort, String prefix, int threadsNumber, int requests) {
        final SocketAddress inetSocketAddress;

        try {
            inetSocketAddress = new InetSocketAddress(InetAddress.getByName(currentHost), currentPort);
        } catch (UnknownHostException exception) {
            System.err.println("Unknown host!");
            exception.printStackTrace();
            return;
        }
        this.alreadyExecute = 0;

        final List<DatagramChannel> channelList = new ArrayList<>();

        try (Selector selector = Selector.open()) {
            IntStream.range(0, threadsNumber).forEach(ind -> {
                DatagramChannel datagramChannel = CommonMethods.createAndConfigureNewDatagramChannel(selector,
                        new ClientDataAttribute(requests, ind, prefix, inetSocketAddress),
                        SelectionKey.OP_WRITE, null);
                if (datagramChannel == null) {
                    channelList.forEach(channel -> CommonMethods.close(channel, CHANNEL_NAME));
                    System.err.println("Error with create DatagramChannel");
                    CommonMethods.close(selector, SELECTOR_NAME);
                    return;
                }
                channelList.add(datagramChannel);
            });
            while (alreadyExecute < threadsNumber) {
                CommonMethods.trySelect(selector);
                CommonMethods.iterate(selector, this::write, this::read);
            }
            for (DatagramChannel datagramChannel : channelList) {
                CommonMethods.close(datagramChannel, "Channel");
            }
        } catch (IOException exception) {
            System.err.println("Error open selector!");
            exception.printStackTrace();
            return;
        }
    }

    private boolean write(final SelectionKey selectionKey) {
        final ClientDataAttribute dataAttribute = (ClientDataAttribute) selectionKey.attachment();
        DatagramChannel datagramChannel = (DatagramChannel) selectionKey.channel();
        ByteBuffer byteBuffer = dataAttribute.byteBuffer;
        byteBuffer.clear();
        int currentThread = dataAttribute.getThread();
        final int curRequest = dataAttribute.getCurRequest();
        final String currentPrefix = dataAttribute.getPrefix();
        SocketAddress inetSocketAddress = dataAttribute.getSocketAddress();
        CommonMethods.bufferOperations(String.format("%s%s%s%d", currentPrefix, currentThread, "_", curRequest), byteBuffer);
        //byteBuffer.flip();
        try {
            final int getBytes = datagramChannel.send(byteBuffer, inetSocketAddress);
            selectionKey.interestOps(SelectionKey.OP_READ);
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("Can't send request" + exception.getMessage());
            return false;
        }
    }

    private void read(SelectionKey selectionKey) {
        final ClientDataAttribute dataAttribute = (ClientDataAttribute) selectionKey.attachment();
        DatagramChannel datagramChannel = (DatagramChannel) selectionKey.channel();
        SocketAddress socketAddress = null;
        ByteBuffer byteBuffer = dataAttribute.byteBuffer;
        CommonMethods.commonReceive(socketAddress, datagramChannel, byteBuffer);
        if (String.join("", StandardCharsets.UTF_8.decode(byteBuffer))
                .matches(CommonMethods.getPattern(
                        dataAttribute.getCurRequest(), dataAttribute.getThread()))) {
            selectionKey.interestOps(SelectionKey.OP_WRITE);
            dataAttribute.nextRequest();
            if (dataAttribute.numberOfThreads == dataAttribute.getCurRequest()) {
                CommonMethods.close(datagramChannel, CHANNEL_NAME);
                alreadyExecute++;
            }
        }
    }

}