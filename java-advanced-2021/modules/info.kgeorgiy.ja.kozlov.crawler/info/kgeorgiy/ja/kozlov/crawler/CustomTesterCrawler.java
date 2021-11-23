package info.kgeorgiy.ja.kozlov.crawler;

import info.kgeorgiy.java.advanced.crawler.Tester;

public class CustomTesterCrawler extends Tester {

    public static void main(String[] args) {
        new CustomTesterCrawler()
                .add("Custom", CustomTestCrawler.class)
                .run(args);
    }
}