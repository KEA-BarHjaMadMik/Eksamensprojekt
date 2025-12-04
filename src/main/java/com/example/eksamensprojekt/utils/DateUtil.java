package com.example.eksamensprojekt.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DateUtil {

    public static long businessDaysBetween(LocalDate startDate, LocalDate endDate) {
        long days = 0;
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            DayOfWeek dow = current.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                days++;
            }
            current = current.plusDays(1);
        }

        return days;
    }
}
