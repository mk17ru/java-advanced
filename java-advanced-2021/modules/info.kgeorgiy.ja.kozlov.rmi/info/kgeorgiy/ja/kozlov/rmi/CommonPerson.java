package info.kgeorgiy.ja.kozlov.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CommonPerson<A extends Account> implements Serializable, Person {

    protected Map<String, A> personAccounts = new ConcurrentHashMap<>();
    protected String lastName;
    protected String passportInfo;
    protected String firstName;


    public CommonPerson(String passportInfo, String firstName, String lastName) {
        this.passportInfo = passportInfo;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public abstract A createAccount(String id) throws RemoteException;

    public String getLastName() {
        return lastName;
    }

    public String getPassportInfo() {
        return passportInfo;
    }

    public String getFirstName() {
        return firstName;
    }

    @Override
    public Map<String, Account> getPersonAccounts() {
        return Collections.unmodifiableMap(personAccounts);
    }

    public A getPersonAccount(String sudId) {
        return personAccounts.get(sudId);
    }


}
