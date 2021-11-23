package info.kgeorgiy.ja.kozlov.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LocalPerson extends CommonPerson<LocalAccount> implements Serializable {

    @Override
    public LocalAccount createAccount(String id) {
        personAccounts.putIfAbsent(id, new LocalAccount(id));
        return personAccounts.get(id);
    }

    public LocalAccount getPersonAccount(String id) {
        return this.personAccounts.get(id);
    }

    public LocalPerson(Person person) throws RemoteException {
        super(person.getPassportInfo(), person.getFirstName(), person.getLastName());
        for (Map.Entry<String, Account> accounts : person.getPersonAccounts().entrySet()) {
            personAccounts.put(accounts.getKey(), new LocalAccount(accounts.getValue().getId()));
        }
    }


}
