package com.stackframe.sarariman;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author mcculley
 */
public class DateUtils {

    private DateUtils() {
    }

    public static Date now() {
        return new Date();
    }

    public static Date weekStart(Date date) {
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(date);

        // Roll back to previous Saturday if not already on a Saturday.
        int day = startDate.get(Calendar.DAY_OF_WEEK);
        if (day != Calendar.SATURDAY) {
            startDate.add(Calendar.DATE, -day);
        }

        return startDate.getTime();
    }

    public static Date nextWeek(Date date) {
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(date);
        startDate.add(Calendar.DATE, 7);
        return startDate.getTime();
    }

    public static Date prevWeek(Date date) {
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(date);
        startDate.add(Calendar.DATE, -7);
        return startDate.getTime();
    }

}