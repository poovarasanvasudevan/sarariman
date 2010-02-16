/*
 * Copyright (C) 2009 StackFrame, LLC
 * This code is licensed under GPLv2.
 */
package com.stackframe.sarariman;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author mcculley
 */
public class Project {

    private final long id;
    private final String name;
    private final String contract;
    private final String subcontract;
    private final long customer;
    private final BigDecimal funded;
    private final Sarariman sarariman;

    public static Map<Long, Project> getProjects(Sarariman sarariman) throws SQLException {
        Connection connection = sarariman.openConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM projects ORDER BY name");
        try {
            ResultSet resultSet = ps.executeQuery();
            try {
                Map<Long, Project> map = new LinkedHashMap<Long, Project>();
                while (resultSet.next()) {
                    long id = resultSet.getLong("id");
                    String name = resultSet.getString("name");
                    long customer = resultSet.getLong("customer");
                    String contract = resultSet.getString("contract_number");
                    String subcontract = resultSet.getString("subcontract_number");
                    BigDecimal funded = resultSet.getBigDecimal("funded");
                    map.put(id, new Project(sarariman, id, name, customer, contract, subcontract, funded));
                }
                return map;
            } finally {
                resultSet.close();
            }
        } finally {
            ps.close();
            connection.close();
        }
    }

    Project(Sarariman sarariman, long id, String name, long customer, String contract, String subcontract, BigDecimal funded) {
        this.sarariman = sarariman;
        this.id = id;
        this.name = name;
        this.customer = customer;
        this.contract = contract;
        this.subcontract = subcontract;
        this.funded = funded;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContract() {
        return contract;
    }

    public String getSubcontract() {
        return subcontract;
    }

    public long getCustomer() {
        return customer;
    }

    public BigDecimal getFunded() {
        return funded;
    }

    public Collection<Task> getTasks() throws SQLException {
        return Task.getTasks(sarariman, this);
    }

    public static Project create(Sarariman sarariman, String name, Long customer, String contract, String subcontract, BigDecimal funded) throws SQLException {
        Connection connection = sarariman.openConnection();
        PreparedStatement ps = connection.prepareStatement("INSERT INTO projects (name, customer, contract_number, subcontract_number, funded) VALUES(?, ?, ?, ?, ?)");
        try {
            ps.setString(1, name);
            ps.setLong(2, customer);
            ps.setString(3, contract);
            ps.setString(4, subcontract);
            ps.setBigDecimal(5, funded);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            try {
                rs.next();
                return new Project(sarariman, rs.getLong(1), name, customer, contract, subcontract, funded);
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
            connection.close();
        }
    }

    public void update(String name, Long customer, String contract, String subcontract, BigDecimal funded) throws SQLException {
        Connection connection = sarariman.openConnection();
        PreparedStatement ps = connection.prepareStatement("UPDATE projects SET name=?, customer=?, contract_number=?, subcontract_number=?, funded=? WHERE id=?");
        try {
            ps.setString(1, name);
            ps.setLong(2, customer);
            ps.setString(3, contract);
            ps.setString(4, subcontract);
            ps.setBigDecimal(5, funded);
            ps.setLong(6, id);
            ps.executeUpdate();
        } finally {
            ps.close();
            connection.close();
        }
    }

    public void delete() throws SQLException {
        Connection connection = sarariman.openConnection();
        PreparedStatement ps = connection.prepareStatement("DELETE FROM projects WHERE id=?");
        try {
            ps.setLong(1, id);
            ps.executeUpdate();
        } finally {
            ps.close();
            connection.close();
        }
    }

    @Override
    public String toString() {
        return "{id=" + id + ",name=" + name + ",customer=" + customer + ",contract=" + contract + ",subcontract=" + subcontract + ",funded=" + funded + "}";
    }

}
