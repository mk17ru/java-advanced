package info.kgeorgiy.ja.kozlov.concurrent;


import info.kgeorgiy.java.advanced.concurrent.Tester;
public class CustomTesterConcur extends Tester {

    public static void main(String[] args) {
        new CustomTesterConcur()
                .add("Custom", CustomTestConcur.class)
                .run(args);
    }
}