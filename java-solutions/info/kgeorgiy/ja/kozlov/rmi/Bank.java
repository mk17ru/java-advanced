package info.kgeorgiy.ja.kozlov.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {

    Person createPerson(String passport, String lastName, String firstName) throws RemoteException;

    LocalPerson getLocalPerson(String passport) throws RemoteException;

    RemotePerson getRemotePerson(String passport) throws RemoteException;

    /**
     * Creates a new account with specified identifier if it is not already exists.
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

}