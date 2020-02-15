package me.richardcollins.market.storage;

import me.richardcollins.market.Settings;
import me.richardcollins.universal.Universal;
import me.richardcollins.universal.objects.MySQLInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQL {
    String user = "";
    String database = "";
    String password = "";
    String hostname = "";
    Connection connection = null;

    public MySQL() {
        MySQLInfo info = Universal.getMySQLInfo();

        this.hostname = info.getHost();
        this.database = Settings.database;
        this.user = info.getUsername();
        this.password = info.getPassword();
    }

    public Connection open() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.hostname + ":3306/" + this.database, this.user, this.password);
            return connection;
        } catch (SQLException e) {
            System.out.println("Could not connect to MySQL server! because: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found!");
        }
        return this.connection;
    }

    public boolean checkConnection() {
        if (this.connection != null) {
            return true;
        }
        return false;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void close() {
        try {
            connection.close();
        } catch (Exception e) {

        }
    }

    public Boolean tableExists(String table) {
        try {
            ResultSet tables = getConnection().getMetaData().getTables(null, null, table, null);
            return tables.next();
        } catch (SQLException e) {
            System.out.println("Failed to check if table '" + table + "' exists: " + e.getMessage());
            return false;
        }
    }

    public Boolean columnExists(String tabel, String column) {
        try {
            ResultSet columns = getConnection().getMetaData().getColumns(null, null, tabel, column);
            return columns.next();
        } catch (SQLException e) {
            System.out.println("Failed to check if column '" + column + "' exists: " + e.getMessage());
            return false;
        }
    }

    public ResultSet select(String query) {
        try {
            return getConnection().createStatement().executeQuery(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public void insert(String query) {
        try {
            getConnection().createStatement().executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void update(String query) {
        try {
            getConnection().createStatement().executeUpdate(query);
        } catch (SQLException ex) {
        }
    }

    public void delete(String query) {
        try {
            getConnection().createStatement().executeUpdate(query);
        } catch (SQLException ex) {
        }
    }

    public Boolean execute(String query) {
        try {
            getConnection().createStatement().execute(query);
            return true;
        } catch (SQLException ex) {
        }

        return false;
    }
}