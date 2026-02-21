package com.blackrock.challenge.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static long toEpoch(String date) {
        return LocalDateTime.parse(date, FORMATTER)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

        public static LocalDateTime parse(String date) {
            return LocalDateTime.parse(date, FORMATTER);
        }

        public static boolean isWithin(LocalDateTime date,
                                       LocalDateTime start,
                                       LocalDateTime end) {
            return !date.isBefore(start) && !date.isAfter(end);
        }
}