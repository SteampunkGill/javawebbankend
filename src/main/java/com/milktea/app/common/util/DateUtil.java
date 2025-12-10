// File: milktea-backend/src/main/java/com/milktea/app/common/util/DateUtil.java
package com.milktea.app.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {

    private static final DateTimeFormatter ISO_INSTANT_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private static final DateTimeFormatter ISO_LOCAL_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_TIME_FORMATTER_WITH_ZONE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");


    public static Instant now() {
        return Instant.now();
    }

    public static Date toDate(Instant instant) {
        return instant == null ? null : Date.from(instant);
    }

    public static Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }

    public static String formatInstant(Instant instant) {
        return instant == null ? null : ISO_INSTANT_FORMATTER.format(instant);
    }

    public static Instant parseInstant(String instantString) {
        return instantString == null ? null : Instant.parse(instantString);
    }

    public static String formatLocalDate(LocalDate localDate) {
        return localDate == null ? null : ISO_LOCAL_DATE_FORMATTER.format(localDate);
    }

    public static LocalDate parseLocalDate(String dateString) {
        return dateString == null ? null : LocalDate.parse(dateString, ISO_LOCAL_DATE_FORMATTER);
    }

    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime == null ? null : ISO_LOCAL_DATE_TIME_FORMATTER.format(localDateTime);
    }

    public static LocalDateTime parseLocalDateTime(String dateTimeString) {
        return dateTimeString == null ? null : LocalDateTime.parse(dateTimeString, ISO_LOCAL_DATE_TIME_FORMATTER);
    }

    /**
     * Converts Instant to a formatted String suitable for DTOs with timezone.
     * E.g., "2023-01-01T00:00:00Z" (ISO-8601 UTC)
     */
    public static String toIsoString(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC).format(ISO_INSTANT_FORMATTER);
    }

    /**
     * Converts a formatted String (e.g., "2023-01-01T00:00:00Z") to Instant.
     */
    public static Instant fromIsoString(String isoString) {
        return isoString == null ? null : Instant.parse(isoString);
    }
}