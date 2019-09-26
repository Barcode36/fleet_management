package main.java.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.text.NumberFormat;
import java.text.ParseException;

public class CurrencyUtil {
    public static StringProperty getStringProperty(double amount) {
        return new SimpleStringProperty(formatCurrency(amount));
    }

    public static String formatCurrency(double amount) {
        return NumberFormat.getInstance().format(amount);
    }

    public static double parseCurrency(String currency) {
        if (currency != null) {
            try {
                return NumberFormat.getInstance().parse(currency).doubleValue();
            } catch (ParseException ignored) {
            }
        }
        return -1;
    }
}
