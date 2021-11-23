package info.kgeorgiy.ja.kozlov.hello;

import java.util.function.Supplier;
import java.io.IOException;
import java.net.*;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import info.kgeorgiy.java.advanced.hello.HelloServer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class HelloUDPServer implements HelloServer {

    private static final String SERVER_SERVICE_NAME = "Server Service";
    private static final String SERVER_SOCKET_NAME = "Server Socket";
    public final static String HELLO_STRING = "Hello, ";
    private ExecutorService service;
    private DatagramSocket socket;

    @Override
    public void start(int currentPort, int currentThreads) {
        try {
            socket = new DatagramSocket(currentPort);
            service = Executors.newFixedThreadPool(currentThreads);
            IntFunction<Runnable> runnable = (currentThread) -> () -> {
                byte[] buffer = CommonMethods.DEFAULT_BUFFER;
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                DatagramPacket request;
                try {
                    request = CommonMethods.createFixedPacket(socket.getReceiveBufferSize());
                } catch (SocketException exception) {
                    System.err.println("Request packet error " + exception.getMessage());
                    return;
                }
                while(!socket.isClosed()) {
                    try {
                        socket.receive(request);
                        if (!socket.isClosed()) {
                            String requestString =  CommonMethods.getPacketString(HELLO_STRING, request);
                            CommonMethods.prepareRequest(requestString, socket, response, request.getSocketAddress());
                            response.setSocketAddress(request.getSocketAddress());
                            try {
                                socket.send(response);
                            } catch (IOException exception) {
                                System.err.println("Can't send request " + exception.getMessage());
                            }
                        }
                    } catch (IOException exception) {
                        System.err.println("Can't receive");
                    }
                }
            };
            CommonMethods.submitTasks(currentThreads, runnable, service);
        } catch (SocketException exception) {
            System.err.println("Error datagram socket server " + exception.getMessage());
        }
    }

    @Override
    public void close() {
        CommonMethods.close(socket, SERVER_SOCKET_NAME);
        CommonMethods.terminated(service, SERVER_SERVICE_NAME);
    }

    public static void main(String[] args) {
        HelloServer server = new HelloUDPServer();
        CommonMethods.mainServer(server, args);
    }

}