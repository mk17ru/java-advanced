package info.kgeorgiy.ja.kozlov.rmi;

import java.rmi.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Client {

    private final static int DEFAULT_PORT = 8080;

    private Client() {}

    public static void checkArguments(String[] args) {
        Objects.requireNonNull(args);
        for (String argument : args) {
            if (argument == null) {
                throw new IllegalArgumentException("Argument can't be null");
            }
        }
    }

    public static void main(String... args) throws RemoteException {
        checkArguments(args);
        final Bank bank;
        final int changeBalance;
        if (args.length != 5) {
            System.err.println("Arguments for client should be <last name> <first name> <passport> <new balance> <subId>");
        }

        try {
            Registry registry = LocateRegistry.getRegistry(DEFAULT_PORT);
            bank = (Bank) registry.lookup("bankName");
        } catch (NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        }

        try {
            changeBalance = Integer.parseInt(args[3]);
        } catch (NumberFormatException exception) {
            System.err.println("Illegal new balance " + exception.getMessage());
            exception.printStackTrace();
            return;
        }
        Person person = bank.createPerson(args[2], args[1], args[0]);
        Account account = bank.createAccount(args[2] + ":" + args[4]);
        LocalPerson localPerson = bank.getLocalPerson(person.getPassportInfo());
        Account account1 = localPerson.createAccount(args[2] + ":" + "b");
        printPersonInfo(person, account);
        printPersonInfo(localPerson, account1);
        System.out.println("Apply balance changing");
        account.setAmount(account.getAmount() + changeBalance);
        System.out.println("New balance is " + account.getAmount());
    }

    private static void printPersonInfo(final Person person, Account account) throws RemoteException {
        System.out.printf("Information for person: %s %s%n", person.getFirstName(), person.getLastName());
        System.out.println("Passport information is " + person.getPassportInfo());
        System.out.printf("Account is %s%n", account.getId());
        System.out.printf("Money is %s%n", account.getAmount());
    }


}