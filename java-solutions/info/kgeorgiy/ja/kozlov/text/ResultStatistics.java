package info.kgeorgiy.ja.kozlov.text;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.BiFunction;

public class ResultStatistics {

    private final Locale locale;
    public final static Set<Locale> outputLanguages = Set.of(Locale.forLanguageTag("ru"),
            Locale.forLanguageTag("ru-ru"), Locale.forLanguageTag("ru-uk"), Locale.UK, Locale.ENGLISH, Locale.US);
    private final ResourceBundle bundle;
    private final ResourceBundle stringBundle;

    private final static String NO = "No";

    private final String TEMPLATE_TOTAL;

    private final String TEMPLATE_STRINGS;

    private final String TEMPLATE_NUMBER;


    String file;

    private StatisticsCollector<Number> numberStatisticsCollector;

    private StatisticsCollector<String> sentenceStatisticsCollector;

    private StatisticsCollector<String> wordStatisticsCollector;

    private StatisticsCollector<Date> dateStatisticsCollector;

    private StatisticsCollector<Number> moneyStatisticsCollector;

    {
        numberStatisticsCollector = new StatisticsCollector<Number>(null);
        wordStatisticsCollector = new StatisticsCollector<String>(null);
        sentenceStatisticsCollector = new StatisticsCollector<String>(null);
        dateStatisticsCollector = new StatisticsCollector<Date>(null);
        moneyStatisticsCollector = new StatisticsCollector<Number>(null);
    }


    public ResultStatistics(String file, Locale locale) {
        this.locale = locale;
        this.file = file;
        bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.kozlov.text.bundles.UsageResourceBundle", locale);
        stringBundle = ResourceBundle.getBundle("info.kgeorgiy.ja.kozlov.text.bundles.Template", locale);
        this.TEMPLATE_TOTAL = getTTemplate();
        this.TEMPLATE_STRINGS = getSTemplate();
        this.TEMPLATE_NUMBER = getNTemplate();
    }

    //private String getNumberWith

    public String getResult() {
//        StatisticsCollector<String> sentences = (StatisticsCollector<String>) stats.get(TextStatistics.Types.Sentences);
//        StatisticsCollector<String> words = (StatisticsCollector<String>) stats.get(TextStatistics.Types.Words);
//        StatisticsCollector<Number> numbers = (StatisticsCollector<Number>) stats.get(TextStatistics.Types.Numbers);
//        StatisticsCollector<Date> dates = (StatisticsCollector<Date>) stats.get(TextStatistics.Types.Date);
//        StatisticsCollector<Number> moneys = (StatisticsCollector<Number>) stats.get(TextStatistics.Types.Money);

        return String.join(System.lineSeparator(),

                String.format(TEMPLATE_TOTAL,
                bundle.getString("analyzeFile"),
                file,
                bundle.getString("fullStat"),
                bundle.getString("sentenceNumber"),
                sentenceStatisticsCollector.getCount(),
                bundle.getString("wordsNumber"),
                wordStatisticsCollector.getCount(),
                bundle.getString("numberNumber"),
                numberStatisticsCollector.getCount(),
                bundle.getString("sumNumber"),
                moneyStatisticsCollector.getCount(),
                bundle.getString("dateNumber"),
                dateStatisticsCollector.getCount()),

                getStringTemplate(sentenceStatisticsCollector, "sentence"),
                getStringTemplate(wordStatisticsCollector, "word"),
                getNumberTemplate(numberStatisticsCollector, "number", this::getNumber),
                getNumberTemplate(moneyStatisticsCollector, "sum", this::getMoney),
                getDateTemplate(dateStatisticsCollector, "date")
        );

    }

    private String getStringTemplate(final StatisticsCollector<String> sentences, String prefix) {
        return String.format(TEMPLATE_STRINGS,
                bundle.getString(prefix + "Stat"),
                bundle.getString(prefix + "Number"),
                sentences.getCount(),
                sentences.getCountUni(),
                bundle.getString("diff"),
                bundle.getString(prefix + "Min"),
                sentences.getMinValue(),
                bundle.getString(prefix + "Max"),
                sentences.getMaxValue(),
                bundle.getString(prefix + "MinLength"),
                sentences.getMinLengthValue().length(),
                sentences.getMinLengthValue(),
                bundle.getString(prefix + "MinLength"),
                sentences.getMaxLengthValue().length(),
                sentences.getMaxLengthValue(),
                bundle.getString(prefix + "Average"),
                getNumber(sentences.getAverageValue(), 3));
    }


    private String getNumberTemplate(final StatisticsCollector<Number> sentences, String prefix,
                                                                        BiFunction<Number, Integer, String> parse) {
        return String.format(TEMPLATE_NUMBER,
                bundle.getString(prefix + "Stat"),
                bundle.getString(prefix + "Number"),
                sentences.getCount(),
                sentences.getCountUni(),
                bundle.getString("diff"),
                bundle.getString(prefix + "Min"),
                parse.apply(sentences.getMinValue(), 3),
                bundle.getString(prefix + "Max"),
                parse.apply(sentences.getMaxValue().doubleValue(), 3),
                bundle.getString(prefix + "Average"),
                parse.apply(sentences.getAverageValue(), 3));
    }

    private String getDateTemplate(final StatisticsCollector<Date> sentences, String prefix) {
        int dateFormat = Integer.parseInt(stringBundle.getString("dateFormat"));
        return String.format(TEMPLATE_NUMBER,
                bundle.getString(prefix + "Stat"),
                bundle.getString(prefix + "Number"),
                sentences.getCount(),
                sentences.getCountUni(),
                bundle.getString("diff"),
                bundle.getString(prefix + "Min"),
                getDate(locale, dateFormat, sentences.getMinValue()),
                bundle.getString(prefix + "Max"),
                getDate(locale, dateFormat, sentences.getMaxValue()),
                bundle.getString(prefix + "Average"),
                getDate(locale, dateFormat, new Date((long) sentences.getAverageValue())));
    }

    private String getDate(final Locale locale, int dateFormat, Date str) {
        if (str != null) {
            return DateFormat.getDateInstance(dateFormat, locale).format(str);
        } else {
            return NO;
        }
    }

    public <T extends Number> String getNumber(Number number, int dec) {
        MessageFormat temp = new MessageFormat("{0, number,#." + "#".repeat(dec) + "}", locale);
        if (number != null) {
                return temp.format(new Number[]{number});
        } else {
            return NO;
        }
    }

    public <T extends Number> String getMoney(Number number, int dec) {
        MessageFormat temp = new MessageFormat(stringBundle.getString("moneyParsing"), locale);
        if (number != null) {
            return temp.format(new Number[]{number});
        } else {
            return NO;
        }
    }

    public StatisticsCollector<Number> getNumberStatisticsCollector() {
        return numberStatisticsCollector;
    }

    public StatisticsCollector<String> getSentenceStatisticsCollector() {
        return sentenceStatisticsCollector;
    }

    public StatisticsCollector<String> getWordStatisticsCollector() {
        return wordStatisticsCollector;
    }

    public StatisticsCollector<Date> getDateStatisticsCollector() {
        return dateStatisticsCollector;
    }

    public StatisticsCollector<Number> getMoneyStatisticsCollector() {
        return moneyStatisticsCollector;
    }

    private String getTTemplate() {
        return stringBundle.getString("totalTemplate");
    }

    private String getNTemplate() {
        return stringBundle.getString("numberTemplate");
    }

    private String getSTemplate() {
        return stringBundle.getString("stringTemplate");
    }

}
