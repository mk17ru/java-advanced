package info.kgeorgiy.ja.kozlov.rmi;

import java.io.Serializable;
import java.util.Map;

public abstract class CommonAccount implements Serializable, Account {

    private int amount;
    private final String id;


    public CommonAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    public CommonAccount(int amount, final String id) {
        this.amount = amount;
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    @Override
    public synchronized void setAmount(int amount) {
        System.out.println(String.join(" ", "Setting amount of money for account ", id, Integer.toString(amount)));
        this.amount = amount;
    }

    @Override
    public synchronized void addAmount(int amount) {
        System.out.println(String.join(" ", "Adding amount of money for account ", id, Integer.toString(amount)));
        this.amount += amount;
    }


}
