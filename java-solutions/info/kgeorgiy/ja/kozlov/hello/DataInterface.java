package info.kgeorgiy.ja.kozlov.hello;

import java.net.SocketException;
import java.nio.channels.DatagramChannel;

public interface DataInterface {
    void initByDatagramChannel(DatagramChannel datagramChannel) throws SocketException;
}
