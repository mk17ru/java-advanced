package info.kgeorgiy.ja.kozlov.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Person extends Remote {

    String getPassportInfo() throws RemoteException;

    String getLastName() throws RemoteException;

    String getFirstName() throws RemoteException;

    Map<String, Account> getPersonAccounts() throws RemoteException;

    Account getPersonAccount(String id) throws RemoteException;
}
