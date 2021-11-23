package info.kgeorgiy.ja.kozlov.implementor;


import info.kgeorgiy.java.advanced.implementor.Tester;

public class CustomTesterImpl extends Tester {

    public static void main(String[] args) {
        new CustomTesterImpl()
                .add("Custom", CustomTestImpl.class)
                .run(args);
    }
}