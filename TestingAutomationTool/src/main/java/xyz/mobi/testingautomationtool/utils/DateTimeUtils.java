package xyz.mobi.testingautomationtool.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    public static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private DateTimeUtils() {}

    public static ZonedDateTime toZonedDateTime(Instant instant, String timezoneId) {
        if (instant == null) return null;
        ZoneId zone = resolveZone(timezoneId);
        return instant.atZone(zone);
    }

    public static String format(Instant instant, String timezoneId) {
        if (instant == null) return null;
        return toZonedDateTime(instant, timezoneId).format(FORMATTER);
    }

    public static ZoneId resolveZone(String timezoneId) {
        if (timezoneId == null || timezoneId.trim().isEmpty()) {
            return DEFAULT_ZONE;
        }
        try {
            return ZoneId.of(timezoneId.trim());
        } catch (Exception e) {
            return DEFAULT_ZONE;
        }
    }
}
