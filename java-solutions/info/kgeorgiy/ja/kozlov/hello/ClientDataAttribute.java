package info.kgeorgiy.ja.kozlov.hello;

import java.net.*;
import java.nio.*;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

public class ClientDataAttribute implements DataInterface {

    ByteBuffer byteBuffer;
    int curRequest = 0;
    int thread;
    final String prefix;
    SocketAddress socketAddress;
    final int numberOfThreads;

    public ClientDataAttribute(int numberOfThreads, int thread, String prefix, SocketAddress socketAddress) {
        this.thread = thread;
        this.socketAddress = socketAddress;
        this.prefix = prefix;
        this.numberOfThreads = numberOfThreads;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void nextRequest() {
        ++curRequest;
    }

    public int getCurRequest() {
        return curRequest;
    }

    public int getThread() {
        return thread;
    }

    @Override
    public void initByDatagramChannel(DatagramChannel datagramChannel) throws SocketException {
        this.byteBuffer = ByteBuffer.allocate(datagramChannel.socket().getReceiveBufferSize());
    }

    public String getPrefix() {
        return prefix;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public void setSocketAddress(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public void setCurRequest(int curRequest) {
        this.curRequest = curRequest;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

}