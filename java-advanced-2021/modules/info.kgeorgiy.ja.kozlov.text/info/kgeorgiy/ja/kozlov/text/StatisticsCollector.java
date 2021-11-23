package info.kgeorgiy.ja.kozlov.text;

import java.util.*;

public class StatisticsCollector<E>  {
    private long count;
    private long countUni;
    private E maxValue;
    private E minValue;
    private double averageValue;

    private String maxLengthValue;
    private String minLengthValue;

    public StatisticsCollector(E def) {
        this.maxValue = def;
        this.minValue = def;
    }

    public String getMaxLengthValue() {
        return maxLengthValue;
    }

    public void setMaxLengthValue(String maxLengthValue) {
        this.maxLengthValue = maxLengthValue;
    }

    public String getMinLengthValue() {
        return minLengthValue;
    }

    public void setMinLengthValue(String minLengthValue) {
        this.minLengthValue = minLengthValue;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getCountUni() {
        return countUni;
    }

    public void setCountUni(long countUni) {
        this.countUni = countUni;
    }

    public E getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(E maxValue) {
        this.maxValue = maxValue;
    }

    public E getMinValue() {
        return minValue;
    }

    public void setMinValue(E minValue) {
        this.minValue = minValue;
    }

    public double getAverageValue() {
        return averageValue;
    }

    public void setAverageValue(double averageValue) {
        this.averageValue = averageValue;
    }
}
