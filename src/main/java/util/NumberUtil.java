package main.java.util;

/**
 * Created by alfonce on 01/07/2017.
 */
public class NumberUtil {
    public static double stringToDouble(String value) {
        if (value == null || value.isEmpty()) {
            return -1;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static int stringToInt(String value) {
        if (value == null || value.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    public static double getNearestWholeNumber(double val) {
        return Math.round(val);
    }
}
