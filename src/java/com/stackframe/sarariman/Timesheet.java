/*
 * Copyright (C) 2009 StackFrame, LLC
 * This code is licensed under GPLv2.
 */
package com.stackframe.sarariman;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mcculley
 */
public class Timesheet {

    // FIXME: These hard coded task numbers should come from a config file.
    private static final int holidayTask = 4;
    private static final int PTOTask = 5;
    private final Sarariman sarariman;
    private final int employeeNumber;
    private final Date week;
    private final Logger logger = Logger.getLogger(getClass().getName());

    public Timesheet(Sarariman sarariman, int employeeNumber, Date week) {
        this.sarariman = sarariman;
        this.employeeNumber = employeeNumber;
        this.week = week;
    }

    public static Timesheet lookup(Sarariman sarariman, int employeeNumber, java.util.Date week) {
        return new Timesheet(sarariman, employeeNumber, new Date(week.getTime()));
    }

    public double getRegularHours() throws SQLException {
        Connection connection = sarariman.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT SUM(hours.duration) AS total " +
                "FROM hours " +
                "WHERE employee=? AND hours.date >= ? AND hours.date < DATE_ADD(?, INTERVAL 7 DAY) AND hours.task != ? AND hours.task != ?");
        try {
            ps.setInt(1, employeeNumber);
            ps.setDate(2, week);
            ps.setDate(3, week);
            ps.setInt(4, holidayTask);
            ps.setInt(5, PTOTask);
            ResultSet resultSet = ps.executeQuery();
            try {
                if (!resultSet.first()) {
                    return 0;
                } else {
                    String total = resultSet.getString("total");
                    return total == null ? 0 : Double.parseDouble(total);
                }
            } finally {
                resultSet.close();
            }
        } finally {
            ps.close();
        }
    }

    public double getTotalHours() throws SQLException {
        Connection connection = sarariman.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT SUM(hours.duration) AS total " +
                "FROM hours " +
                "WHERE employee=? AND hours.date >= ? AND hours.date < DATE_ADD(?, INTERVAL 7 DAY)");
        try {
            ps.setInt(1, employeeNumber);
            ps.setDate(2, week);
            ps.setDate(3, week);
            ResultSet resultSet = ps.executeQuery();
            try {
                if (!resultSet.first()) {
                    return 0;
                } else {
                    String total = resultSet.getString("total");
                    return total == null ? 0 : Double.parseDouble(total);
                }
            } finally {
                resultSet.close();
            }
        } finally {
            ps.close();
        }
    }

    private double getHours(int task) throws SQLException {
        Connection connection = sarariman.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT SUM(hours.duration) AS total " +
                "FROM hours " +
                "WHERE employee=? AND hours.date >= ? AND hours.date < DATE_ADD(?, INTERVAL 7 DAY) AND hours.task = ?");
        try {
            ps.setInt(1, employeeNumber);
            ps.setDate(2, week);
            ps.setDate(3, week);
            ps.setInt(4, task);
            ResultSet resultSet = ps.executeQuery();
            try {
                if (!resultSet.first()) {
                    return 0;
                } else {
                    String total = resultSet.getString("total");
                    return total == null ? 0 : Double.parseDouble(total);
                }
            } finally {
                resultSet.close();
            }
        } finally {
            ps.close();
        }
    }

    public double getHours(Date day) throws SQLException {
        Connection connection = sarariman.getConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT SUM(duration) AS total FROM hours WHERE employee=? AND date=?");
        try {
            ps.setInt(1, employeeNumber);
            ps.setDate(2, day);
            ResultSet resultSet = ps.executeQuery();
            try {
                if (!resultSet.first()) {
                    return 0;
                } else {
                    String total = resultSet.getString("total");
                    return total == null ? 0 : Double.parseDouble(total);
                }
            } finally {
                resultSet.close();
            }
        } finally {
            ps.close();
        }
    }

    public double getPTOHours() throws SQLException {
        return getHours(PTOTask);
    }

    public double getHolidayHours() throws SQLException {
        return getHours(holidayTask);
    }

    public boolean isSubmitted() throws SQLException {
        Connection connection = sarariman.getConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM timecards WHERE date = ? AND employee = ?");
        try {
            ps.setDate(1, week);
            ps.setInt(2, employeeNumber);
            ResultSet resultSet = ps.executeQuery();
            try {
                return resultSet.first();
            } finally {
                resultSet.close();
            }
        } finally {
            ps.close();
        }
    }

    public boolean isApproved() throws SQLException {
        Connection connection = sarariman.getConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM timecards WHERE date = ? AND employee = ?");
        try {
            ps.setDate(1, week);
            ps.setInt(2, employeeNumber);
            ResultSet resultSet = ps.executeQuery();
            try {
                if (!resultSet.first()) {
                    return false;
                } else {
                    return resultSet.getBoolean("approved");
                }
            } finally {
                resultSet.close();
            }
        } finally {
            ps.close();
        }
    }

    public boolean approve() {
        try {
            Connection connection = sarariman.getConnection();
            PreparedStatement ps = connection.prepareStatement("UPDATE timecards SET approved=true WHERE date=? AND employee=?");
            try {
                ps.setDate(1, week);
                ps.setInt(2, employeeNumber);
                int rowCount = ps.executeUpdate();
                if (rowCount != 1) {
                    logger.severe("update for week=" + week + " and employee=" + employeeNumber + " did not modify a row");
                    return false;
                } else {
                    Employee employee = sarariman.getDirectory().getByNumber().get(employeeNumber);
                    sarariman.getEmailDispatcher().send(employee.getEmail(), null, "timesheet approved",
                            "Timesheet approved for " + employee.getFullName() + " for week of " + week + ".");
                    return true;
                }
            } finally {
                ps.close();
            }
        } catch (SQLException se) {
            logger.log(Level.SEVERE, "caught exception approving timesheet", se);
            return false;
        }
    }

    public static boolean approve(Timesheet timesheet) {
        return timesheet.approve();
    }

    public boolean reject() {
        try {
            Connection connection = sarariman.getConnection();
            PreparedStatement ps = connection.prepareStatement("DELETE FROM timecards WHERE date=? AND employee=?");
            try {
                ps.setDate(1, week);
                ps.setInt(2, employeeNumber);
                int rowCount = ps.executeUpdate();
                if (rowCount != 1) {
                    logger.severe("reject for week=" + week + " and employee=" + employeeNumber + " did not modify a row");
                    return false;
                } else {
                    Employee employee = sarariman.getDirectory().getByNumber().get(employeeNumber);
                    sarariman.getEmailDispatcher().send(employee.getEmail(), null, "timesheet rejected",
                            "Timesheet rejected for " + employee.getFullName() + " for week of " + week + ".");
                    return true;
                }
            } finally {
                ps.close();
            }
        } catch (SQLException se) {
            logger.log(Level.SEVERE, "caught exception rejecting timesheet", se);
            return false;
        }
    }

    public static boolean reject(Timesheet timesheet) {
        return timesheet.reject();
    }

    public boolean submit() {
        try {
            Connection connection = sarariman.getConnection();
            PreparedStatement ps = connection.prepareStatement("INSERT INTO timecards (employee, date, approved) values(?, ?, false)");
            try {
                ps.setInt(1, employeeNumber);
                ps.setDate(2, week);
                int rowCount = ps.executeUpdate();
                if (rowCount != 1) {
                    logger.severe("submit for week=" + week + " and employee=" + employeeNumber + " did not modify a row");
                    return false;
                } else {
                    Employee employee = sarariman.getDirectory().getByNumber().get(employeeNumber);
                    sarariman.getEmailDispatcher().send(EmailDispatcher.addresses(sarariman.getApprovers()), null,
                            "timesheet submitted",
                            "Timesheet submitted for " + employee.getFullName() + " for week of " + week + ".");
                    return true;
                }
            } finally {
                ps.close();
            }
        } catch (SQLException se) {
            logger.log(Level.SEVERE, "caught exception submitting timesheet", se);
            return false;
        }
    }

    public static boolean submit(Timesheet timesheet) {
        return timesheet.submit();
    }

}
