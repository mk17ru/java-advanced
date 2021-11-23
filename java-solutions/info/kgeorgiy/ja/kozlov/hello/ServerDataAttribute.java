package info.kgeorgiy.ja.kozlov.hello;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ServerDataAttribute implements DataInterface {
    ByteBuffer byteBuffer;
    int curRequest = 0;
    int thread;
    final DatagramChannel datagramChannel;

    public ServerDataAttribute(int thread, DatagramChannel datagramChannel) throws SocketException {
        this.thread = thread;
        this.datagramChannel = datagramChannel;
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

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public void setCurRequest(int curRequest) {
        this.curRequest = curRequest;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

    public DatagramChannel getDatagramChannel() {
        return datagramChannel;
    }
}
