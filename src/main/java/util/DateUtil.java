package main.java.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

import java.sql.Date;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alfonce on 24/04/2017.
 */
public class DateUtil {
    private static final String DATE_PATTERN = "dd-MM-yyyy";
    private static final String SQL_DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "hh:mm a";
    private static final String DATE_TIME_PATTERN = "dd-MM-yyyy hh:mm a";

    //date formatter;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    //time formatter;

    //date time formatter

    //date formatter

    //returns a well formatted time string

    static Map<String, String> tokenizeTime(LocalTime localTime) {
        if (localTime == null) {
            return null;
        } else {
            Map<String, String> map = new HashMap<>();
            String[] tokens = formatTime(localTime).split(" ");
            if (tokens.length > 1) {
                map.put("Hour", tokens[0].split(":")[0]);
                map.put("Minute", tokens[0].split(":")[1]);
                map.put("Period", tokens[1]);
            }
            return map;
        }
    }


    static ObservableList<String> generateMinutes() {
        ObservableList<Integer> list = FXCollections.observableArrayList();
        return generateTimeHelper(60);
    }

    private static ObservableList<String> generateTimeHelper(int length) {
        ObservableList<String> list = FXCollections.observableArrayList();
        for (int i = -1; i < length - 1; i++) {
            String value = i + 1 + "";
            if (value.length() != 2) {
                value = "0" + value;
            }
            list.add(value);
        }
        return list;
    }

    static ObservableList<String> generateHours() {
        return generateTimeHelper(12);
    }

    //string converter for date picker
    public static StringConverter<LocalDate> getDatePickerConverter() {
        return new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate object) {
                return formatDate(object);
            }

            @Override
            public LocalDate fromString(String string) {
                return parseDate(string);
            }
        };
    }

    //returns a well formatted date time string
    public static String formatDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return DATE_TIME_FORMATTER.format(localDateTime);
    }

    //returns a well formatted date string
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return DATE_FORMATTER.format(date);
    }

    //get time string from localdatetime
    public static String getTimeFromLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        LocalTime localTime = localDateTime.toLocalTime();
        return formatTime(localTime);
    }
    //get date from local date time
    public static String getDateFromLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        LocalDate localDate = localDateTime.toLocalDate();
        return formatDate(localDate);
    }

    public static String formatTime(LocalTime localTime) {
        if (localTime == null) {
            return null;
        }
        return TIME_FORMATTER.format(localTime);
    }

    //converts a date string to local date
    public static LocalDate parseDate(String dateString) {
        try {
            return DATE_FORMATTER.parse(dateString, LocalDate::from);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        try {
            return DATE_TIME_FORMATTER.parse(dateTime, LocalDateTime::from);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static StringProperty dateStringProperty(LocalDate localDate) {
        if (localDate == null) {
            return null;
        } else {
            return new SimpleStringProperty(formatDateLong(localDate));
        }
    }

    public static LocalDate convertDateToLocalDate(java.util.Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // get a long string rep from local date
    public static String formatDateLong(LocalDate date) {
        if (date == null) {
            return null;
        }
        return DateFormat.getDateInstance().format(Date.valueOf(date));
    }


    static LocalTime parseTime(String time) {
        if (time == null) {
            return null;
        }
        return TIME_FORMATTER.parse(time, LocalTime::from);
    }

    public static int getNumDaysDiff(LocalDate start, LocalDate end) {
        return (int)ChronoUnit.DAYS.between(start, end);
    }
}
