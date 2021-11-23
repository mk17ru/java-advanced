package info.kgeorgiy.ja.kozlov.rmi;

import java.rmi.RemoteException;
import java.util.Map;

public class RemotePerson extends CommonPerson<RemoteAccount> {

    private RemoteBank bank;

    public RemotePerson(RemoteBank bank, final String passportInfo, final String firstName, final String lastName)
            throws RemoteException {
        super(passportInfo, firstName, lastName);
        this.bank = bank;
    }

    @Override
    public RemoteAccount createAccount(String sub) throws RemoteException {
        bank.createAccount(CommonMethods.createAccountId(passportInfo, sub));
        return personAccounts.get(sub);
    }

    public RemoteAccount createAccountImpl(String sub) throws RemoteException {
        personAccounts.putIfAbsent(sub, new RemoteAccount(sub));
        return personAccounts.get(sub);
    }


}
