/*
 * Copyright (C) 2010-2013 StackFrame, LLC
 * This code is licensed under GPLv2.
 */
package com.stackframe.sarariman;

import static com.stackframe.sql.SQLUtilities.convert;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * All of the background jobs that run periodically.
 *
 * @author mcculley
 */
class CronJobs {

    private final ScheduledThreadPoolExecutor timer;

    private final Sarariman sarariman;

    private final Directory directory;

    private final EmailDispatcher emailDispatcher;

    CronJobs(ScheduledThreadPoolExecutor timer, Sarariman sarariman, Directory directory, EmailDispatcher emailDispatcher) {
        this.timer = timer;
        this.sarariman = sarariman;
        this.directory = directory;
        this.emailDispatcher = emailDispatcher;
    }

    // Useful time intervals in terms of milliseconds;
    private static final long ONE_SECOND = 1000;

    private static final long ONE_MINUTE = 60 * ONE_SECOND;

    private static final long ONE_HOUR = 60 * ONE_MINUTE;

    private static final long ONE_DAY = 24 * ONE_HOUR;

    private void scheduleWeeknightTask() {
        // The weeknight task runs once each night and filters out Saturday and Sunday itself.
        Calendar firstTime = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        firstTime.set(Calendar.HOUR_OF_DAY, 23);
        firstTime.set(Calendar.MINUTE, 0);
        firstTime.set(Calendar.SECOND, 0);
        if (firstTime.before(now)) {
            firstTime.add(Calendar.DATE, 1);
        }

        long period = ONE_DAY;
        long initialDelay = firstTime.getTime().getTime() - now.getTimeInMillis();
        timer.scheduleAtFixedRate(new WeeknightTask(sarariman, directory, emailDispatcher), initialDelay, period,
                                  TimeUnit.MILLISECONDS);
    }

    private void scheduleMorningTask() {
        // The morning task runs once each morning.
        Calendar firstTime = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        firstTime.set(Calendar.HOUR_OF_DAY, 8);
        firstTime.set(Calendar.MINUTE, 0);
        firstTime.set(Calendar.SECOND, 0);
        if (firstTime.before(now)) {
            firstTime.add(Calendar.DATE, 1);
        }

        long period = ONE_DAY;
        long initialDelay = firstTime.getTime().getTime() - now.getTimeInMillis();
        timer.scheduleAtFixedRate(new MorningTask(sarariman, directory, emailDispatcher), initialDelay, period,
                                  TimeUnit.MILLISECONDS);
    }

    private void scheduleDirectoryReload() {
        // Reload the directory once an hour.  The main use case is to discover new employees that were added after the application
        // started.
        timer.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                directory.reload();
                try {
                    sarariman.getDirectorySynchronizer().synchronize(directory, sarariman.getDataSource());
                } catch (Exception e) {
                    // FIXME: log
                    System.err.println("Trouble synchronizing directory with database: " + e);
                }
            }

        }, ONE_HOUR, ONE_HOUR, TimeUnit.MILLISECONDS);
    }

    private void schedulePaidTimeOffUpdate() {
        // Update paid time off once an hour. This should only need to happen once per day, but doing it more often ensures we
        // correctly update employees added in the middle of the day.
        timer.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    Calendar today = Calendar.getInstance();
                    java.util.Date todayDate = today.getTime();
                    PaidTimeOff.creditWeeklyPaidTimeOff(sarariman, convert(DateUtils.weekStart(todayDate)));
                    PaidTimeOff.creditHolidayPTO(sarariman);
                } catch (SQLException se) {
                    System.err.println("caught exception in PTO update:" + se);
                }
            }

        }, 0L, ONE_HOUR, TimeUnit.MILLISECONDS);
    }

    void start() {
        scheduleMorningTask();
        scheduleWeeknightTask();
        scheduleDirectoryReload();
        schedulePaidTimeOffUpdate();
    }

}
