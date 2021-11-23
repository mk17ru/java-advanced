package info.kgeorgiy.ja.kozlov.rmi;

public class CommonMethods {
    public static String createAccountId(final String passport, final String subId) {
        return String.format("%s:%s", passport, subId);
    }
}