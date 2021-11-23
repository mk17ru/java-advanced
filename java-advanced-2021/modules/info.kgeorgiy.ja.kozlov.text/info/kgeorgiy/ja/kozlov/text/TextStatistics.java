package info.kgeorgiy.ja.kozlov.text;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class TextStatistics {

    private String text;
    private Locale textLocale;
    private Collator collator;
    private final Comparator<String> STRING_COMPARATOR = (String x, String y) -> collator
            .compare(x, y);
    private final Comparator<String> COMPARATOR_LENGTH =
            Comparator.comparingInt((String x) -> x.replaceAll(System.lineSeparator(), "").length());
    public enum Types {
        Numbers,
        Sentences,
        Words,
        Common,
        Money,
        Date
    }

    private void checkArguments(String[] args) {
        Objects.requireNonNull(args);
        for (String arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException("Arguments can't be null");
            }
        }
        return;
    }

    public ResultStatistics execute(String fileReport, Locale outputLocale) {
        ResultStatistics resultStatistics = new ResultStatistics(fileReport, outputLocale);

        stringStatistics(Types.Sentences, resultStatistics.getSentenceStatisticsCollector(),
                BreakIterator.getSentenceInstance(textLocale));

        stringStatistics(Types.Words, resultStatistics.getWordStatisticsCollector(),
                BreakIterator.getWordInstance(textLocale));

        iterate(resultStatistics.getNumberStatisticsCollector(), BreakIterator.getWordInstance(textLocale),
                this::numberParsing, this::evaluateNumber);

        iterate(resultStatistics.getNumberStatisticsCollector(), BreakIterator.getWordInstance(textLocale),
                this::numberParsing, this::evaluateNumber);

        iterate(resultStatistics.getMoneyStatisticsCollector(),
                BreakIterator.getWordInstance(textLocale), this::moneyParsing, this::evaluateNumber);

        iterate(resultStatistics.getDateStatisticsCollector(),
                BreakIterator.getWordInstance(textLocale), this::dateParsing, this::evaluateDate);

        return resultStatistics;
    }

    private<T> void iterate(StatisticsCollector<T> numberStatisticsCollector, final BreakIterator breakIterator,
                            final Function<ParsePosition, T> getParsing, BiConsumer<List<T>, StatisticsCollector<T>> eval) {
        breakIterator.setText(text);
        List<T> list = new ArrayList<>();
        if (breakIterator.first() == BreakIterator.DONE) {
            return;
        }
        ParsePosition last = new ParsePosition(breakIterator.first());
        for (int iterator = breakIterator.first(); breakIterator.current() != BreakIterator.DONE; iterator = breakIterator.current()) {
            if (breakIterator.next() == BreakIterator.DONE) {
                break;
            }
            ParsePosition parsePosition = new ParsePosition(iterator);
            if (parsePosition.getIndex() < last.getIndex()) {
                continue;
            }
            T num = getParsing.apply(parsePosition);
            if (num != null) {
                list.add(num);
            }
            last = parsePosition;
        }
        eval.accept(list, numberStatisticsCollector);
    }


    private void stringStatistics(Types type, final StatisticsCollector<String> stringStatistics,
                                  final BreakIterator breakIterator) {
        breakIterator.setText(text);
        List<String> list = new ArrayList<>();
        if (breakIterator.first() == -1) {
            return;
        }
        for (int iterator = breakIterator.first(); breakIterator.current() != -1; iterator = breakIterator.current()) {
            if (breakIterator.next() == -1) {
                break;
            }
            String currentString = text.substring(iterator, breakIterator.current());
            currentString = currentString.trim().replaceAll(System.lineSeparator(), " ");
            if (currentString.isEmpty()) {
                continue;
            }
            if (type == Types.Words) {
                if (!checkWord(currentString)) {
                    continue;
                }
            }
            list.add(currentString);
        }
        evaluateString(list, stringStatistics);
    }

    private Number moneyParsing(ParsePosition parsePosition) {
        return NumberFormat.getCurrencyInstance(textLocale).parse(text, parsePosition);
    }

    private Number numberParsing (ParsePosition parsePosition) {
        return NumberFormat.getNumberInstance().parse(text, parsePosition);
    }

    private Date dateParsing(ParsePosition parsePosition) {
        Set<DateFormat> dates = new HashSet<>() {
            {
                add(DateFormat.getDateInstance(DateFormat.FULL, textLocale));
                add(DateFormat.getDateInstance(DateFormat.MEDIUM, textLocale));
                add(DateFormat.getDateInstance(DateFormat.LONG, textLocale));
                add(DateFormat.getDateInstance(DateFormat.SHORT, textLocale));
                add(DateFormat.getDateInstance(DateFormat.DEFAULT, textLocale));
            }
        };
        Date newDate = null;
        for (DateFormat d : dates) {
            newDate = d.parse(text, parsePosition);
            if (newDate != null) {
                break;
            }
        }
        return newDate;
    }

    private boolean checkWord(String currentString) {
        // FROM EXAMPLE
        return currentString.codePoints().anyMatch(Character::isLetter);
    }

    private void evaluateString(List<String> lists, final StatisticsCollector<String> stringStatistics) {
        commonStat(STRING_COMPARATOR, stringStatistics, lists);
        stringStat(stringStatistics, lists);
    }

    private void evaluateNumber(List<Number> list, StatisticsCollector<Number> statisticsCollector) {
        commonStat(Comparator.comparingDouble(Number::doubleValue), statisticsCollector, list);
        statisticsCollector.setAverageValue(list.stream().mapToDouble(Number::doubleValue).reduce(0, Double::sum)
                / statisticsCollector.getCount());
    }

    private void evaluateDate(List<Date> list, StatisticsCollector<Date> statisticsCollector) {
        commonStat(Comparator.comparingLong(Date::getTime), statisticsCollector, list);
        statisticsCollector.setAverageValue(list.stream().mapToLong(Date::getTime).reduce(0, Long::sum) * 1.0
                / statisticsCollector.getCount());
    }

    private Stream<Integer> listToLengthElement(List<String> list) {
        return list.stream().map(String::length);
    }

    private void stringStat(StatisticsCollector<String> statisticsCollector, List<String> list) {
        if (list.isEmpty()) {
            return;
        }
        statisticsCollector.setAverageValue(listToLengthElement(list).reduce(0, Integer::sum).doubleValue()
                / statisticsCollector.getCount());

        statisticsCollector.setMaxLengthValue(Collections.max(list, COMPARATOR_LENGTH));
        statisticsCollector.setMinLengthValue(Collections.min(list, COMPARATOR_LENGTH));
    }

    private <T> void commonStat(Comparator<T> comparator,
                                StatisticsCollector<T> statisticsCollector, List<T> lists) {
        statisticsCollector.setCount(lists.size());
        statisticsCollector.setMaxValue(lists.stream().max(comparator).orElse(null));
        statisticsCollector.setMinValue(lists.stream().min(comparator).orElse(null));
        TreeSet<T> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(lists);
        statisticsCollector.setCountUni(treeSet.size());
    }

    public TextStatistics(String text, Locale textLocale) {
        this.text = text;
        this.textLocale = textLocale;
        this.collator = Collator.getInstance(textLocale);
    }

    public static void main(String... args) {
        Objects.requireNonNull(args);
        if (args.length != 4) {
            System.err.println("Arguments size should be 4");
            return;
        }
        Locale outputLocale = createLocale(args[1]);
        Locale textLocale = createLocale(args[0]);
        String fileText = args[2];
        String currentText;
        try {
            currentText = Files.readString(Path.of(fileText));
        } catch (IOException exception) {
            // :NOTE: no localization
            System.err.println("Error with reading input file " + exception.getMessage());
            exception.printStackTrace();
            return;
        }
        String fileReport = args[3];
        if (!ResultStatistics.outputLanguages.contains(outputLocale)) {
            System.err.println("Can't use output locale " + outputLocale);
            return;
        }
        Path path;
        try {
            path = Path.of(fileReport);
        } catch (InvalidPathException exception) {
            System.err.println("Error with get path " + fileReport);
            exception.printStackTrace();
            return;
        }
        ResultStatistics result = new TextStatistics(currentText, textLocale).execute(fileText, outputLocale);
        System.out.println(result.getResult());
        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            bw.write(result.getResult());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // TODO IDK
    public static Locale createLocale(String locale) {
        return Locale.forLanguageTag(locale);
    }

    private static Path getPath(String path) {
        return Path.of(path);
    }
}
