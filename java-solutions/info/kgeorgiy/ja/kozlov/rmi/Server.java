package info.kgeorgiy.ja.kozlov.rmi;

import java.net.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.Scanner;


public class Server {

    private final static int DEFAULT_PORT = 8080;

    public Server() {

    }

    public static void main(String... args) throws RemoteException {

        final Bank bank = new RemoteBank(DEFAULT_PORT);
        long time = System.currentTimeMillis();
        runBank(bank);
        if (args.length > 0) {
            stopBank(args);
            System.exit(0);
        }
    }

    private static void runBank(Bank bank) {
        try {
            UnicastRemoteObject.exportObject(bank, DEFAULT_PORT);
            Registry registry = LocateRegistry.createRegistry(DEFAULT_PORT);
            registry.rebind("bankName", bank);
        } catch (RemoteException exception) {
            System.out.println("Cannot export object: " + exception.getMessage());
            exception.printStackTrace();
            return;
        }
    }

    private static void stopBank(String[] args)  {
        try {
            Thread.sleep(Integer.parseInt(args[0]));
        } catch (InterruptedException e) {
            // log error
        }
    }
}