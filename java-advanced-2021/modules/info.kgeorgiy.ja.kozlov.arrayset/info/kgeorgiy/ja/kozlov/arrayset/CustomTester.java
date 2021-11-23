package info.kgeorgiy.ja.kozlov.arrayset;

import info.kgeorgiy.java.advanced.arrayset.NavigableSetTest;
import info.kgeorgiy.java.advanced.arrayset.Tester;

public class CustomTester extends Tester {

    public static void main(String[] args) {
        new CustomTester()
                .add("Custom", CustomTest.class)
                .add("NavigableSet", NavigableSetTest.class)
                .run(args);
    }
}