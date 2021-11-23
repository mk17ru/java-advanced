package info.kgeorgiy.ja.kozlov.rmi;

import org.junit.*;
import org.junit.internal.TextListener;
import org.junit.jupiter.api.*;
import org.junit.runner.JUnitCore;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.Result;
import java.rmi.Remote;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;


@DisplayName("Test Bank")
public class BankTest {

    private RemoteBank testBank;

    private static int PORT = Registry.REGISTRY_PORT;

    private static Registry testRegistry;

    private static String BANK_NAME = String.join("", "//localhost:", Integer.toString(PORT), "/bank");

    @BeforeAll
    public static void beforeAllTest() {
        try {
            testRegistry = LocateRegistry.createRegistry(PORT);
        } catch (RemoteException exception) {
            System.err.println("Registry already in use " + exception.getMessage());
        }
    }

    @Before
    public void beforeEachTest() throws RemoteException, MalformedURLException {
        testBank = new RemoteBank(PORT);
        UnicastRemoteObject.exportObject(testBank, PORT);
        testRegistry.rebind(BANK_NAME, testBank);
    }

    static {
        try {
            testRegistry = LocateRegistry.createRegistry(PORT);
        } catch (RemoteException exception) {
            System.err.println("Error with registry " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private Person createDefaultPerson() throws RemoteException {
        Person person = testBank.createPerson("999", "IVANOV", "PETROV");
        Assertions.assertNotNull(person);
        return person;
    }


    private void checkPerson(final Person person, final String expectedPassport, final String expectedLastName, final String expectedFirstName) throws RemoteException {
        Assertions.assertEquals(expectedLastName, person.getLastName());
        Assertions.assertEquals(expectedPassport, person.getPassportInfo());
        Assertions.assertEquals(expectedFirstName, person.getFirstName());
    }

    private void checkAccount(final Account account, final String id, int value) throws RemoteException {
        Assertions.assertEquals(id, account.getId());
        Assertions.assertEquals(value, account.getAmount());
    }

    private Bank getTestBank() throws RemoteException {
        Bank bank;
        try {
            bank = (Bank) testRegistry.lookup(BANK_NAME);
        } catch (NotBoundException exception) {
            throw new RemoteException("No Bank");
        }
        return bank;
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        Result result = junit.run(BankTest.class);
        if (result.getFailureCount() > 0) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }


    @Test
    @DisplayName("Create and get Person")
    public void personCreateAndGet() throws RemoteException, NotBoundException, MalformedURLException {
        Person person = createDefaultPerson();
        checkPerson(person, "999", "IVANOV", "PETROV");
        checkPerson(testBank.getRemotePerson("999"), "999", "IVANOV", "PETROV");
    }

    @Test
    @DisplayName("One account create, get and amount changing")
    public void getAccountPerson() throws RemoteException {
        Person defaultPerson = testBank.createPerson("999", "K", "S");
        Assertions.assertNotNull(defaultPerson);
        final Account account = testBank.createAccount("999:2");
        checkAccount(account, "2", 0);
        account.setAmount(4324);
        checkAccount(account, "2", 4324);
        Person additionPerson = testBank.createPerson("998", "I", "O");
        checkAccount(account, "2", 4324);
        Person person = testBank.getLocalPerson("999");
        Assertions.assertNotNull(person);
        checkPerson(person, "999", "K", "S");
    }

    @Test
    @DisplayName("Account create, get and amount changing from bank")
    public void getAccountPersonFromBank() throws RemoteException {
        Person defaultPerson = testBank.createPerson("999", "K", "S");
        Assertions.assertNotNull(defaultPerson);
        final Account account = testBank.createAccount("999:2");
        final Account account1 = testBank.getPersonAccount("999:2");
        final Account account2 = testBank.createAccount("999:3");
        checkAccount(account, "2", 0);
        Assert.assertEquals(account, account1);
        Map<String, Account> map = testBank.getPersonAccounts("999");
        Assert.assertEquals(account, map.get("2"));
        Assert.assertEquals(account2, map.get("3"));
        System.out.println(Locale.getDefault());
    }


    @Test
    @DisplayName("Create Local Person")
    public void getLocalPerson() throws RemoteException {
        createDefaultPerson();
        testBank.createPerson("998", "IVANOV", "IVAN");
        checkPerson(testBank.getLocalPerson("998"), "998", "IVANOV", "IVAN");
        testBank.createAccount("998:0");
        testBank.createAccount("998:i");
        testBank.createAccount("998:2");
        Person localPerson = testBank.getLocalPerson("998");
        checkAccount(localPerson.getPersonAccount("i"), "i", 0);
        localPerson.getPersonAccount("i").setAmount(42342);
        checkAccount(localPerson.getPersonAccount("i"), "i", 42342);
        localPerson.getPersonAccount("0").setAmount(29);
        checkAccount(localPerson.getPersonAccount("0"), "0", 29);
        checkAccount(localPerson.getPersonAccount("i"), "i", 42342);
    }


    @Test
    @DisplayName("Change Account")
    public void changeAccountPerson() throws RemoteException {
        testBank.createPerson("999", "K", "S");
        final Account account = testBank.createAccount("999:0");
        checkAccount(account, "0", 0);
        account.setAmount(3);
        checkAccount(account, "0", 3);
    }

    private void commonParallel(Runnable run) {
        final ExecutorService service = Executors.newFixedThreadPool(10);
        IntStream.range(0, 10).forEach(i ->
        {
            service.submit(run);
        });
        terminated(service);
    }

    @Test
    public void parallelCreate() throws RemoteException {
        final Set<RemoteException> exceptions = Collections.newSetFromMap(new ConcurrentHashMap<>());
        Runnable run = () -> {
            final int ch = Math.abs(new Random().nextInt()) % 1000;
            try {
                Person person = testBank.createPerson(Integer.toString(ch), "K", "D");
                Assertions.assertNotNull(person);
                for (int j = 0; j < 10; ++j) {
                    Account account = testBank.createAccount(person.getPassportInfo() + ":" + j);
                    Assertions.assertNotNull(account);
                }
            } catch (RemoteException exception) {
                exceptions.add(exception);
            }
        };
        commonParallel(run);
        if (exceptions.isEmpty()) {
            return;
        } else {
            RemoteException exception = new RemoteException();
            for (RemoteException e : exceptions) {
                exception.addSuppressed(e);
            }
            throw exception;
        }
    }

    @Test
    public void parallelChange() throws RemoteException {
        final ExecutorService service = Executors.newFixedThreadPool(10);
        final Set<RemoteException> exceptions = Collections.newSetFromMap(new ConcurrentHashMap<>());
        Person person = testBank.createPerson("90", "K", "D");
        Account account = testBank.createAccount("90:o");
        Assertions.assertEquals(account.getAmount(), 0);
        IntStream.range(0, 1000).forEach(i ->
        {
            final int ind = i;
            Runnable run = () -> {
                try {
                    account.addAmount(10);
                } catch (RemoteException exception) {
                    exceptions.add(exception);
                }
            };
            service.submit(run);
        });
        terminated(service);
        if (exceptions.isEmpty()) {
            Assertions.assertEquals(account.getAmount(), 10000);
            return;
        } else {
            RemoteException exception = new RemoteException();
            for (RemoteException e : exceptions) {
                exception.addSuppressed(e);
            }
            throw exception;
        }
    }

    @Test
    @DisplayName("Error Person")
    public void ErrorPerson() throws RemoteException {
        checkPerson(testBank.createPerson("56", "K", "S"),
                "56", "K", "S");
        Assertions.assertNull(testBank.createAccount("56:"));
        Assertions.assertNull(testBank.createAccount(":"));
        Assertions.assertNull(testBank.createAccount(":56"));
        Assertions.assertNull(testBank.createAccount("56y:3"));
        Assertions.assertNull(testBank.createAccount("fsdfdsf"));
        Assertions.assertNull(testBank.createAccount("56::9"));
        Assertions.assertNull(testBank.createAccount(null));
        Account account = testBank.createAccount("56:1");
        checkAccount(account, "1", 0);
    }


    @Test
    @DisplayName("Error Account")
    public void ErrorAccount() throws RemoteException {
        Assertions.assertNull(testBank.createPerson("999", null, "S"));
        Assertions.assertNull(testBank.createPerson("999", "d", null));
        Assertions.assertNull(testBank.createPerson(null, "D", "S"));
        Assertions.assertNull(testBank.createPerson("o", "", "S"));
        Assertions.assertNull(testBank.createPerson("", "D", "S"));
        Assertions.assertNull(testBank.createPerson("OO", "D", ""));
        Assertions.assertNull(testBank.createPerson("o", "i", ""));
        Assertions.assertNull(testBank.createPerson(null, null, null));
        Assertions.assertNull(testBank.createPerson("999", null, null));
        Person person = createDefaultPerson();
        checkPerson(person, "999", "IVANOV", "PETROV");
    }

    @Test
    @DisplayName("Change Local Person")
    public void changeLocalPerson() throws RemoteException {
        testBank.createPerson("998", "IVANOV", "IVAN");
        checkPerson(testBank.getLocalPerson("998"), "998", "IVANOV", "IVAN");
        testBank.createAccount("998:0");
        testBank.createAccount("998:i");
        testBank.createAccount("998:2");
        checkAccount(testBank.getRemotePerson("998").getPersonAccount("i"), "i", 0);
        Person localPerson = testBank.getLocalPerson("998");
        localPerson.getPersonAccount("i").setAmount(42342);
        checkAccount(localPerson.getPersonAccount("i"), "i", 42342);
        checkAccount(testBank.getRemotePerson("998").getPersonAccount("i"), "i", 0);
    }

    @Test
    public void serverClient() throws RemoteException {
        Server.main();
        Client.main("1", "2", "3", "4", "5");
    }


    @Test
    public void personRepeatedChange() throws RemoteException {
        Person person = testBank.createPerson("9343", "O", "D");
        Assertions.assertEquals(person, testBank.createPerson("9343", "S", "A"));
        Assertions.assertEquals(person, testBank.getRemotePerson("9343"));
        checkPerson(testBank.getLocalPerson("9343"), "9343", "O", "D");
    }


    @Test
    public void accountRepeatedChange() throws RemoteException {
        RemotePerson person = testBank.createPerson("9343", "O", "D");
        Account account = person.createAccount("b");
        Assertions.assertEquals(account, testBank.createAccount("9343:b"));
        checkAccount(testBank.createAccount("9343:c"), "c", 0);
        checkPerson(testBank.getLocalPerson("9343"), "9343", "O", "D");
    }

    @After()
    public void afterTest() throws RemoteException {
        try {
            testRegistry.unbind(BANK_NAME);
        } catch (NotBoundException exception) {
            System.err.println("Error with unbind registry");
        }
    }


    public static void terminated(final ExecutorService currentService) {
        currentService.shutdown();
        try {
            final boolean f = currentService.awaitTermination(64, TimeUnit.SECONDS);
            if (!f) {
                currentService.shutdownNow();
                if (!currentService.awaitTermination(64, TimeUnit.SECONDS)) {
                    System.err.println(String.join(" ", "don't terminated!"));
                }
            }
        } catch (final InterruptedException exception) {
            currentService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}