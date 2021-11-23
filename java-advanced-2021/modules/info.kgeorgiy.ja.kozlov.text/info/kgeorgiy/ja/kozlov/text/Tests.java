package info.kgeorgiy.ja.kozlov.text;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.runner.JUnitCore;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.Result;
import java.rmi.Remote;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

// :NOTE: no tests on statistics presentation
@DisplayName("Test Text")
public class Tests {

    private final String DEFAULT_INPUT_FILE = "./input.txt";
    private final String DEFAULT_OUTPUT_FILE = "output.txt";

    private final Locale DEFAULT_RUSSIAN_LOCALE = Locale.forLanguageTag("ru");

    private static final Set<Locale> russianLocales = new HashSet<>() {
        {
            add(Locale.forLanguageTag("ru-ua"));
            add(Locale.forLanguageTag("ru-ru"));
            add(Locale.forLanguageTag("ru"));
        }
    };

    private static final Set<Locale> englishLocales = new HashSet<>() {
        {
            add(Locale.US);
            add(Locale.UK);
            add(Locale.CANADA);
            add(Locale.ENGLISH);
        }
    };

    // ENGLISH TESTS

    @Test
    public void numbersEnTest() {
        for (Locale locale : englishLocales) {
            // :NOTE: TextStatistics shouldn't have output file as a construction parameter
            TextStatistics textStatistics = new TextStatistics("3 4,0 5. \n English is 43,43 good! \n Ok", locale);
            ResultStatistics result = textStatistics.execute(DEFAULT_OUTPUT_FILE, Locale.ENGLISH);
            Assertions.assertEquals(4, result.getNumberStatisticsCollector().getCount());
            Assertions.assertEquals(13.8575, result.getNumberStatisticsCollector().getAverageValue());
            Assertions.assertEquals(43.43, result.getNumberStatisticsCollector().getMaxValue());
            Assertions.assertEquals(3L, result.getNumberStatisticsCollector().getMinValue());
        }
    }

    @Test
    public void moneyEnTest() {
        String text = "10,80 ₽  £32.3 20. Привет. Hello, friend. $10.2 $100.20 34 43. £1.3 ::JJ fsjfs 65 £100 fsssdd свылоаы::: \"";
        TextStatistics textStatistics = new TextStatistics(text, Locale.UK);
        ResultStatistics result = textStatistics.execute(DEFAULT_OUTPUT_FILE, Locale.UK);
        Assertions.assertEquals(3L, result.getMoneyStatisticsCollector().getCount());
        Assertions.assertEquals(100L, result.getMoneyStatisticsCollector().getMaxValue());
        Assertions.assertEquals("£100.00", result.getMoney(result.getMoneyStatisticsCollector().getMaxValue(), 2));
        Assertions.assertEquals(1.3, result.getMoneyStatisticsCollector().getMinValue());
        Assertions.assertEquals("£1.30", result.getMoney(result.getMoneyStatisticsCollector().getMinValue(), 2));
        Assertions.assertEquals("44.533", result.getNumber(result.getMoneyStatisticsCollector().getAverageValue(), 3));
        Assertions.assertEquals("£44.53", result.getMoney(result.getMoneyStatisticsCollector().getAverageValue(), 2));

        for (Locale locale : Set.of(Locale.US, Locale.CANADA)) {
            textStatistics = new TextStatistics(text, locale);
            result = textStatistics.execute(DEFAULT_OUTPUT_FILE, locale);
            Assertions.assertEquals(2L, result.getMoneyStatisticsCollector().getCount());
            Assertions.assertEquals(100.2, result.getMoneyStatisticsCollector().getMaxValue());
            Assertions.assertEquals("$100.20", result.getMoney(result.getMoneyStatisticsCollector().getMaxValue(), 2));
            Assertions.assertEquals(10.2, result.getMoneyStatisticsCollector().getMinValue());
            Assertions.assertEquals("$10.20", result.getMoney(result.getMoneyStatisticsCollector().getMinValue(), 2));
            Assertions.assertEquals("55.2", result.getNumber(result.getMoneyStatisticsCollector().getAverageValue(), 1));
            Assertions.assertEquals("$55.20", result.getMoney(result.getMoneyStatisticsCollector().getAverageValue(), 1));
        }
    }

    @Test
    public void wordEnTest() {
        for (Locale locale : englishLocales) {
            TextStatistics textStatistics = new TextStatistics("3 4.0 5. \n English is good okk ! \n Ok", locale);
            ResultStatistics result = textStatistics.execute(DEFAULT_OUTPUT_FILE, Locale.ENGLISH);
            StatisticsCollector<String> stat = result.getWordStatisticsCollector();
            Assertions.assertEquals(5L, stat.getCount());
            Assertions.assertEquals("okk", stat.getMaxValue());
            Assertions.assertEquals("English", stat.getMinValue());
            Assertions.assertEquals(3.6, stat.getAverageValue());
            Assertions.assertEquals(7, stat.getMaxLengthValue().length());
            Assertions.assertEquals(2, stat.getMinLengthValue().length());
        }
    }

    @Test
    public void sentenceEnTest() {
        for (Locale locale : englishLocales) {
            TextStatistics textStatistics = new TextStatistics("Gor 4 u. \n English is good okk ! \n Ok jfjds.     Fdsfsd. \n",
                    locale);
            ResultStatistics result = textStatistics.execute(DEFAULT_OUTPUT_FILE, Locale.ENGLISH);
            StatisticsCollector<String> stat = result.getSentenceStatisticsCollector();
            Assertions.assertEquals(4L, stat.getCount());
            Assertions.assertEquals("Ok jfjds.", stat.getMaxValue());
            Assertions.assertEquals("English is good okk !", stat.getMinValue());
            Assertions.assertEquals(11.25, stat.getAverageValue());
            Assertions.assertEquals("English is good okk !", stat.getMaxLengthValue());
            Assertions.assertEquals("Fdsfsd.", stat.getMinLengthValue());
        }
    }


    @Test
    public void dateEnTest() throws ParseException {
        TextStatistics textStatistics;
        ResultStatistics result;
        StatisticsCollector<Date> stat;
        for (Locale locale : Set.of(Locale.US, Locale.ENGLISH)) {
            textStatistics = new TextStatistics("10,80 ₽ 6/7/21 Привет \n , kjfksdj. kfjskdj! Monday, July 8, 2321 232 -2001 100,00 12/10/2001 Jun 7, 2021",
                    locale);
            result = textStatistics.execute(DEFAULT_OUTPUT_FILE, Locale.ENGLISH);
            stat = result.getDateStatisticsCollector();
            Assertions.assertEquals(4L, stat.getCount());
            Assertions.assertEquals(3L, stat.getCountUni());
            Assertions.assertEquals(new SimpleDateFormat("dd.MM.yyyy").parse("08.07.2321"), stat.getMaxValue());
            Assertions.assertEquals(new SimpleDateFormat("dd.MM.yyyy").parse("10.12.2001"), stat.getMinValue());
            Assertions.assertEquals(new SimpleDateFormat("dd.MM.yyyy").parse("31.07.2091"), new Date((long) stat.getAverageValue()));
        }

        for (Locale locale : englishLocales) {
            if (locale.equals(Locale.US) || locale.equals(Locale.ENGLISH)) {
                continue;
            }
            textStatistics = new TextStatistics("10,80 ₽ 07/06/2001 07/06/2021 06/07/21 Привет \n, ! Monday, 7 June 2021 hello! Hi! fksjdfs ksfksf \n Jun 7, 2021.",
                    Locale.UK);
            result = textStatistics.execute(DEFAULT_OUTPUT_FILE, Locale.ENGLISH);
            stat = result.getDateStatisticsCollector();
            Assertions.assertEquals(4L, stat.getCount());
            Assertions.assertEquals(3L, stat.getCountUni());
            Assertions.assertEquals(new SimpleDateFormat("dd.MM.yyyy").parse("06.07.2021"), stat.getMaxValue());
            Assertions.assertEquals(new SimpleDateFormat("dd.MM.yyyy").parse("07.06.2001"), stat.getMinValue());
            Assertions.assertEquals("13.06.2016", new SimpleDateFormat("dd.MM.yyyy").format(new Date((long) stat.getAverageValue())));
        }
    }

    @Test
    public void numbersRuTest() {
        for (Locale locale : russianLocales) {
            TextStatistics textStatistics = new TextStatistics("3 4,0 5. \n Русский круто!  43,43  \n Ok",
                    locale);
            ResultStatistics result = textStatistics.execute(DEFAULT_OUTPUT_FILE, DEFAULT_RUSSIAN_LOCALE);
            Assertions.assertEquals(4, result.getNumberStatisticsCollector().getCount());
            Assertions.assertEquals(13.8575, result.getNumberStatisticsCollector().getAverageValue());
            Assertions.assertEquals(43.43, result.getNumberStatisticsCollector().getMaxValue());
            Assertions.assertEquals(3L, result.getNumberStatisticsCollector().getMinValue());
        }
    }

    @Test
    public void moneyRuTest() {
        String text = "10,80 ₽  £32.3 20. Привет. Hello, 100,20 ₴ friend. 0 ₴ YYdf 9,01 ₴ 10 ₽ ::JJ fsjfs 65 £100 11,11 ₴  fsssdd 1043,9 ₽свылоаы::: \"";
        TextStatistics textStatistics = new TextStatistics(text,
                Locale.forLanguageTag("ru-ru"));
        ResultStatistics result = textStatistics.execute(DEFAULT_OUTPUT_FILE, DEFAULT_RUSSIAN_LOCALE);
        Assertions.assertEquals(3L, result.getMoneyStatisticsCollector().getCount());
        Assertions.assertEquals(1043.9, result.getMoneyStatisticsCollector().getMaxValue());
        Assertions.assertEquals(10L, result.getMoneyStatisticsCollector().getMinValue());
        Assertions.assertEquals("354,9", result.getNumber(result.getMoneyStatisticsCollector().getAverageValue(), 1));

        textStatistics = new TextStatistics(text,
                Locale.forLanguageTag("ru-ua"));
        result = textStatistics.execute(DEFAULT_OUTPUT_FILE, DEFAULT_RUSSIAN_LOCALE);
        Assertions.assertEquals(4L, result.getMoneyStatisticsCollector().getCount());
        Assertions.assertEquals(100.2, result.getMoneyStatisticsCollector().getMaxValue());
        Assertions.assertEquals(0L, result.getMoneyStatisticsCollector().getMinValue());
        Assertions.assertEquals("30,1", result.getNumber(result.getMoneyStatisticsCollector().getAverageValue(), 1));

    }

    @Test
    public void wordRuTest() {
        for (Locale locale : russianLocales) {
            TextStatistics textStatistics = new TextStatistics(" Привет 3 4 hi. Я сказал на русском! 5. \n English ! Нет... \n Ok",
                    locale);
            ResultStatistics result = textStatistics.execute(DEFAULT_OUTPUT_FILE, DEFAULT_RUSSIAN_LOCALE);
            StatisticsCollector<String> stat = result.getWordStatisticsCollector();
            Assertions.assertEquals(9L, stat.getCount());
            Assertions.assertEquals("Я", stat.getMaxValue());
            Assertions.assertEquals("English", stat.getMinValue());
            Assertions.assertEquals(4.0, stat.getAverageValue());
            Assertions.assertEquals(7, stat.getMaxLengthValue().length());
            Assertions.assertEquals(1, stat.getMinLengthValue().length());
        }
    }

    @Test
    public void sentenceRuTest() {
        for (Locale locale : russianLocales) {
            TextStatistics textStatistics = new TextStatistics("Gor 4 u. \n English is good okk ! \n Ok jfjds.  Fdsfsd :: fejf. \n",
                    locale);
            ResultStatistics result = textStatistics.execute(DEFAULT_OUTPUT_FILE, DEFAULT_RUSSIAN_LOCALE);
            StatisticsCollector<String> stat = result.getSentenceStatisticsCollector();
            Assertions.assertEquals(4L, stat.getCount());
            Assertions.assertEquals("Ok jfjds.", stat.getMaxValue());
            Assertions.assertEquals("English is good okk !", stat.getMinValue());
            Assertions.assertEquals(13.25, stat.getAverageValue());
            Assertions.assertEquals(21, stat.getMaxLengthValue().length());
            Assertions.assertEquals(8, stat.getMinLengthValue().length());
        }
    }


    @Test
    public void dateRuTest() throws ParseException {
        for (Locale locale : russianLocales) {
            TextStatistics textStatistics = new TextStatistics("Приветствие понедельник, 7 июня 2021 г. 10,80 ₽ 6/7/21 Привет , kjfksdj. kfjskdj! Monday, July 8, " +
                    "07.06.2021 2321 232 -2001 100,00 12/10/2001 Jun 7, 2021 7 июн. 2021 г. такс 7 июня 2021 г. а другая дата: \n  3 сентября 1991 г.",
                    locale);
            ResultStatistics result = textStatistics.execute(DEFAULT_OUTPUT_FILE, DEFAULT_RUSSIAN_LOCALE);
            StatisticsCollector<Date> stat = result.getDateStatisticsCollector();
            Assertions.assertEquals(5L, stat.getCount());
            Assertions.assertEquals(2L, stat.getCountUni());
            Assertions.assertEquals(new SimpleDateFormat("dd.MM.yyyy").parse("07.06.2021"), stat.getMaxValue());
            Assertions.assertEquals(new SimpleDateFormat("dd.MM.yyyy").parse("03.09.1991"), stat.getMinValue());
            Assertions.assertEquals(new SimpleDateFormat("dd.MM.yyyy").parse("25.06.2015"), new Date((long) stat.getAverageValue()));
        }
    }

    // International
    @Test
    public void chineseLang() {
        TextStatistics textStatistics = new TextStatistics("該文本是中文的特殊文本，出於說明目的，是辛勤工作的一部分。 這是第二句話。 這是第三句話。",
                Locale.CHINESE);
        ResultStatistics result = textStatistics.execute(DEFAULT_OUTPUT_FILE, Locale.ENGLISH);
        StatisticsCollector<String> stat = result.getSentenceStatisticsCollector();
        Assertions.assertEquals(3L, stat.getCount());
        Assertions.assertEquals(3L, stat.getCountUni());
        Assertions.assertEquals("這是第三句話。", stat.getMaxValue());
        Assertions.assertEquals("該文本是中文的特殊文本，出於說明目的，是辛勤工作的一部分。", stat.getMinValue());
        Assertions.assertEquals("該文本是中文的特殊文本，出於說明目的，是辛勤工作的一部分。", stat.getMaxLengthValue());
        Assertions.assertEquals("這是第二句話。", stat.getMinLengthValue());
        Assertions.assertEquals("14.333", result.getNumber(stat.getAverageValue(), 3));

        stat = result.getWordStatisticsCollector();
        Assertions.assertEquals(5L, stat.getCount());
        Assertions.assertEquals(5L, stat.getCountUni());
        Assertions.assertEquals("這是第三句話", stat.getMaxValue());
        Assertions.assertEquals("出於說明目的", stat.getMinValue());
        Assertions.assertEquals("該文本是中文的特殊文本", stat.getMaxLengthValue());
        Assertions.assertEquals("出於說明目的", stat.getMinLengthValue());
        Assertions.assertEquals("7.6", result.getNumber(stat.getAverageValue(), 1));
    }

    // International
    @Test
    public void BanglaLang() {
        TextStatistics textStatistics = new TextStatistics("এই পাঠ্যটি বাংলায় একটি বিশেষ পাঠ, বিক্ষোভমূলক উদ্দেশ্যে যা কঠোর পরিশ্রমের অংশ হিসাবে অর্পণ করা হয়েছে। এটি দ্বিতীয় বাক্য। এবং এটি তৃতীয় বাক্য।",
                Locale.CHINESE);
        ResultStatistics result = textStatistics.execute(DEFAULT_OUTPUT_FILE, Locale.ENGLISH);
        StatisticsCollector<String> stat = result.getSentenceStatisticsCollector();
        Assertions.assertEquals(3L, stat.getCount());
        Assertions.assertEquals(3L, stat.getCountUni());
        Assertions.assertEquals("এবং এটি তৃতীয় বাক্য।", stat.getMaxValue());
        Assertions.assertEquals("এই পাঠ্যটি বাংলায় একটি বিশেষ পাঠ, বিক্ষোভমূলক উদ্দেশ্যে যা কঠোর পরিশ্রমের অংশ হিসাবে অর্পণ করা হয়েছে।", stat.getMinValue());
        Assertions.assertEquals("এই পাঠ্যটি বাংলায় একটি বিশেষ পাঠ, বিক্ষোভমূলক উদ্দেশ্যে যা কঠোর পরিশ্রমের অংশ হিসাবে অর্পণ করা হয়েছে।", stat.getMaxLengthValue());
        Assertions.assertEquals("এটি দ্বিতীয় বাক্য।", stat.getMinLengthValue());
        Assertions.assertEquals("47.667", result.getNumber(stat.getAverageValue(), 3));

        stat = result.getWordStatisticsCollector();
        Assertions.assertEquals(23L, stat.getCount());
        Assertions.assertEquals(21L, stat.getCountUni());
        Assertions.assertEquals("হিসাবে", stat.getMaxValue());
        Assertions.assertEquals("অংশ", stat.getMinValue());
        Assertions.assertEquals("বিক্ষোভমূলক", stat.getMaxLengthValue());
        Assertions.assertEquals("এই", stat.getMinLengthValue());
        Assertions.assertEquals("5.3", result.getNumber(stat.getAverageValue(), 1));
    }

    @Test
    public void outputExampleTest() {
        String in = "./goldenCollection/inputexample.txt";
        String out = "./goldenCollection/outputexample.txt";
        String golden = "./goldenCollection/goldenexample.txt";

        TextStatistics.main("ru-ru", "ru-ru",  in, out);
        Assertions.assertTrue(checkFiles(golden, out));
    }

    @Test
    public void outputTest() {
        String in = "./goldenCollection/inputexampleEn.txt";
        String out = "./goldenCollection/outputexampleEn.txt";
        String golden = "./goldenCollection/goldenexampleEn.txt";

        TextStatistics.main("en-US", "en-US",  in, out);
        Assertions.assertTrue(checkFiles(golden, out));
    }

    @Test
    public void outputZhTest() {
        String in = "./goldenCollection/inputexampleCh.txt";
        String out = "./goldenCollection/outputexampleCh.txt";
        String golden = "./goldenCollection/goldenexampleCh.txt";

        TextStatistics.main("zh", "zh",  in, out);
        Assertions.assertTrue(checkFiles(golden, out));
    }

    private boolean checkFiles(String p1, String p2) {
        Path path2;
        Path path1;
        try {
            path1 = Path.of(p1);
            path2 = Path.of(p2);
        } catch (InvalidPathException exception) {
            System.err.println("Error with get path ");
            exception.printStackTrace();
            return false;
        }
        try (BufferedReader bw = Files.newBufferedReader(path1, StandardCharsets.UTF_8)) {
            String str;
            try (BufferedReader bw2 = Files.newBufferedReader(path2, StandardCharsets.UTF_8)) {
                while((str = bw.readLine()) != null) {
                    String str2 = bw2.readLine();
                    if (!str.strip().equals(str2.strip())) {
                        return false;
                    }

                 }
            } catch (IOException exception) {
                exception.printStackTrace();
                return false;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

}
