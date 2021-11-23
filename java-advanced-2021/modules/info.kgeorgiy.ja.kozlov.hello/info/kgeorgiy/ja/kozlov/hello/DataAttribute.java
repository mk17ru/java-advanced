package info.kgeorgiy.ja.kozlov.hello;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class DataAttribute {

    private final ByteBuffer byteBuffer;
    private int curRequest = 0;
    private int thread;
    private final String prefix;

    public DataAttribute(int thread, DatagramChannel datagramChannel, String prefix) throws SocketException {
        this.byteBuffer = ByteBuffer.allocate(datagramChannel.socket().getReceiveBufferSize());
        this.thread = thread;
        this.prefix = prefix;
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


    public String getPrefix() {
        return prefix;
    }
}
