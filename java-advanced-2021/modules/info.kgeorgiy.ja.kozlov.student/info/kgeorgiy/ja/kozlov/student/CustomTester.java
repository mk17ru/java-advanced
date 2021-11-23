package info.kgeorgiy.ja.kozlov.student;

import info.kgeorgiy.java.advanced.student.StudentQueryTest;
import info.kgeorgiy.java.advanced.student.Tester;

public class CustomTester extends Tester {

    public static void main(String[] args) {
        new CustomTester()
                .add("Custom", CustomTest.class)
                .add("StudentQueryTest", StudentQueryTest.class)
                .run(args);
    }
}