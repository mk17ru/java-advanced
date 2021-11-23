package info.kgeorgiy.ja.kozlov.rmi;

import java.net.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;


public final class Server {

    private final static int DEFAULT_PORT = 8080;

    public Server() {}

    public static void main(final String... args) throws RemoteException {

        final Bank bank = new RemoteBank(DEFAULT_PORT);
        try {
            UnicastRemoteObject.exportObject(bank, DEFAULT_PORT);
            Registry registry = LocateRegistry.createRegistry(DEFAULT_PORT);
            registry.rebind("bankName", bank);
        } catch (final RemoteException exception) {
            System.out.println("Cannot export object: " + exception.getMessage());
            exception.printStackTrace();
        }
        System.out.println("Server has been started");
    }
}
