package cn.biq.mn.base.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;

public final class CalendarUtil {

    public static Long[] getIn1Day() {
        Calendar start = Calendar.getInstance();
        start.add(Calendar.HOUR, -24);
        Calendar end = Calendar.getInstance();
        return new Long[]{ start.getTimeInMillis(), end.getTimeInMillis() };
    }

    public static long startOfDay(long milliseconds) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault());
        dateTime = dateTime.with(LocalTime.MIN);
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static long endOfDay(long milliseconds) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault());
        dateTime = dateTime.with(LocalTime.MAX);
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // https://stackoverflow.com/questions/24745217/checking-if-a-date-object-occurred-within-the-past-24-hours
    public static boolean inLastDay(Long timestamp) {
        if (timestamp == null) return false;
        return (System.currentTimeMillis() - timestamp) < (24 * 60 * 60 * 1000);
    }

}
