package main.java.util;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;

public class DateComparator implements Comparator< Map<String, String>> {
    @Override
    public int compare(Map<String, String> o1, Map<String, String> o2) {
        LocalDate first = DateUtil.parseDate(o1.get("date")), second = DateUtil.parseDate(o2.get("date"));
        if (first == null || second == null) {
            return -1;
        }
        if (first.isBefore(second)) {
            return -1;
        } else{
            return 0;
        }
    }
}
