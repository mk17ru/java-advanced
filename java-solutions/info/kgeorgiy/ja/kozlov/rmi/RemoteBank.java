package info.kgeorgiy.ja.kozlov.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, RemoteAccount> accounts;
    private final ConcurrentMap<String, RemotePerson> persons;

    public RemoteBank(final int port) throws RemoteException {
        this.port = port;
        persons = new ConcurrentHashMap<>();
        accounts = new ConcurrentHashMap<>();
    }

    public RemoteAccount getPersonAccount(final String accountId) {
        System.out.printf("Getting account %s%n", accountId);
        return accounts.get(accountId);
    }


    public Map<String, Account> getPersonAccounts(final String passport) throws RemoteException {
        System.out.printf("Getting account %s%n", passport);
        Person person = persons.get(passport);
        if (person == null) {
            System.err.println("No person for passport: " + passport);
            return null;
        } else {
            return person.getPersonAccounts();
        }
    }

    private boolean checkPerson(String passport, String lastName, String firstName) {
        if (passport == null || lastName == null || firstName == null || passport.length() == 0
                || lastName.length() == 0 || firstName.length() == 0) {
            System.err.println("Person data can't be empty or null!");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public RemotePerson createPerson(String passport, String lastName, String firstName) throws RemoteException {
        if (!checkPerson(passport, lastName, firstName)) {
            return null;
        }
        if (!persons.containsKey(passport)) {
            System.out.printf("Create Person %s %s %s%n", passport, lastName, firstName);
            final RemotePerson person = new RemotePerson(this, passport, firstName, lastName);
            persons.put(passport, person);
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            System.out.printf("Person with %s already exist%n", passport);
            return getRemotePerson(passport);
        }
    }

    @Override
    public LocalPerson getLocalPerson(String passport) throws RemoteException {
        System.out.println("Get Person " + passport);
        return new LocalPerson(persons.get(passport));
    }

    @Override
    public RemotePerson getRemotePerson(String passport) throws RemoteException {
        System.out.println("Get Person " + passport);
        return persons.get(passport);
    }


    Pair<String, String> parseSubId(String subId) {
        final String[] data = subId.split(":");
        if (data.length != 2) {
            System.err.println("Incorrect subId");
            return null;
        }
        return new Pair<>(data[0], data[1]);
    }


    @Override
    public Account createAccount(final String subId) throws RemoteException {
        if (subId == null) {
            System.err.println("SubId can't be bull");
            return null;
        }
        Pair<String, String> data = parseSubId(subId);
        if (data == null) {
            return null;
        }
        String passport = data.first;
        String id = data.second;
        RemoteException exception = new RemoteException("Error with export account");
        RemotePerson person = getRemotePerson(passport);
        if (person == null) {
            System.err.printf("No person with passport %s%n", passport);
            return null;
        }
        if (!accounts.containsKey(subId)) {
            System.out.println("Creating account " + subId);
            // :NOTE: data race
            RemoteAccount account = person.createAccountImpl(id);
            accounts.put(subId, account);
            try {
                UnicastRemoteObject.exportObject(account, port);
            } catch (RemoteException e) {
                exception.addSuppressed(e);
                throw exception;
            }
        } else {
            System.out.printf("Account %s already exist%n", subId);
        }
        return accounts.get(subId);

    }

}
